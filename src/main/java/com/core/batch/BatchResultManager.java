package com.core.batch;

import com.client.interf.ApiInterface;
import com.model.TestResult;
import com.property_config.impl.PropertyProviderFactoryImpl;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.constants.CommonConstants.BATCH_FLUSH_INTERVAL_PROPERTY_NAME;
import static com.constants.CommonConstants.BATCH_SIZE_PROPERTY_NAME;

public class BatchResultManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(BatchResultManager.class);
    private static final int DEFAULT_BATCH_SIZE = 10;
    private static final int DEFAULT_FLUSH_INTERVAL_SECONDS = 5;
    private static final int MAX_RETRY_ATTEMPTS = 3;

    private final List<TestResult> pendingResults = new ArrayList<>();
    private final List<TestResult> failedResults = new ArrayList<>();
    private final int batchSize;
    private final int flushInterval;
    private final ApiInterface apiClient;
    private final String runUid;
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean isActive = new AtomicBoolean(true);

    public BatchResultManager(ApiInterface apiClient, String runUid) {
        this.apiClient = apiClient;
        this.runUid = runUid;

        var propertyProvider = PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();
        this.batchSize = Integer.parseInt(
                propertyProvider.getProperty(BATCH_SIZE_PROPERTY_NAME) != null
                        ? propertyProvider.getProperty(BATCH_SIZE_PROPERTY_NAME)
                        : String.valueOf(DEFAULT_BATCH_SIZE)
        );
        this.flushInterval = Integer.parseInt(
                propertyProvider.getProperty(BATCH_FLUSH_INTERVAL_PROPERTY_NAME) != null
                        ? propertyProvider.getProperty(BATCH_FLUSH_INTERVAL_PROPERTY_NAME)
                        : String.valueOf(DEFAULT_FLUSH_INTERVAL_SECONDS)
        );

        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "TestomatBatchFlush");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(this::flushPendingResults,
                flushInterval, flushInterval, TimeUnit.SECONDS);

        LOGGER.info("BatchResultManager initialized: batchSize={}, flushInterval={}s",
                batchSize, flushInterval);
    }

    public synchronized void addResult(TestResult result) {
        if (!isActive.get()) {
            LOGGER.warn("BatchResultManager is not active, skipping result: {}", result.getTitle());
            return;
        }

        pendingResults.add(result);
        LOGGER.debug("Added test result: {} (pending: {})", result.getTitle(), pendingResults.size());

        if (pendingResults.size() >= batchSize) {
            flushPendingResults();
        }
    }

    public synchronized void flushPendingResults() {
        if (pendingResults.isEmpty()) {
            return;
        }

        List<TestResult> toSend = new ArrayList<>(pendingResults);
        pendingResults.clear();

        sendBatch(toSend, 1);
    }

    private void sendBatch(List<TestResult> results, int attempt) {
        try {
            if (results.size() == 1) {
                apiClient.reportTest(runUid, results.get(0));
                LOGGER.debug("Reported single test: {}", results.get(0).getTitle());
            } else {
                apiClient.reportTests(runUid, results);
                LOGGER.debug("Reported batch of {} tests", results.size());
            }
        } catch (IOException e) {
            LOGGER.error("Failed to report batch (attempt {}/{}): {}",
                    attempt, MAX_RETRY_ATTEMPTS, e.getMessage());

            if (attempt < MAX_RETRY_ATTEMPTS) {
                scheduler.schedule(() -> sendBatch(results, attempt + 1),
                        (long) Math.pow(2, attempt), TimeUnit.SECONDS);
            } else {
                synchronized (this) {
                    failedResults.addAll(results);
                }
                LOGGER.error("Failed to report {} tests after {} attempts",
                        results.size(), MAX_RETRY_ATTEMPTS);
            }
        }
    }

    public synchronized void shutdown() {
        isActive.set(false);
        flushPendingResults();

        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        if (!failedResults.isEmpty()) {
            LOGGER.warn("BatchResultManager shutdown with {} failed results", failedResults.size());
        }

        LOGGER.info("BatchResultManager shutdown completed");
    }

    public synchronized int getPendingCount() {
        return pendingResults.size();
    }

    public synchronized int getFailedCount() {
        return failedResults.size();
    }
}