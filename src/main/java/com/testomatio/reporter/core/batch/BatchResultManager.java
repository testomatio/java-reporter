package com.testomatio.reporter.core.batch;

import static com.testomatio.reporter.constants.PropertyNameConstants.BATCH_FLUSH_INTERVAL_PROPERTY_NAME;
import static com.testomatio.reporter.constants.PropertyNameConstants.BATCH_SIZE_PROPERTY_NAME;
import static com.testomatio.reporter.logger.LoggerConfig.getLogger;

import com.testomatio.reporter.client.ApiInterface;
import com.testomatio.reporter.model.TestResult;
import com.testomatio.reporter.propertyconfig.impl.PropertyProviderFactoryImpl;
import com.testomatio.reporter.propertyconfig.interf.PropertyProvider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Manages batch processing of test results for efficient API reporting.
 * Collects test results in batches and periodically flushes them to Testomat.io.
 * Provides automatic retry mechanism and graceful shutdown handling.
 */
public class BatchResultManager {

    private static final Logger LOGGER = getLogger(BatchResultManager.class);
    private static final int DEFAULT_BATCH_SIZE = 10;
    private static final int DEFAULT_FLUSH_INTERVAL_SECONDS = 5;
    private static final int MAX_RETRY_ATTEMPTS = 3;

    private final List<TestResult> pendingResults = new ArrayList<>();
    private final List<TestResult> failedResults = new ArrayList<>();
    private final int batchSize;
    private final ApiInterface apiClient;
    private final String runUid;
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean isActive = new AtomicBoolean(true);

    /**
     * Creates batch result manager with configurable settings.
     * Initializes batch size and flush interval from properties with fallback to defaults.
     * Starts background scheduler for automatic periodic flushing.
     *
     * @param apiClient API client for reporting test results
     * @param runUid    unique identifier of the test run
     * @throws NumberFormatException if batch size or flush interval properties are invalid
     */
    public BatchResultManager(ApiInterface apiClient, String runUid) {
        this.apiClient = apiClient;
        this.runUid = runUid;

        PropertyProvider propertyProvider =
                PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();
        this.batchSize = Integer.parseInt(
                propertyProvider.getProperty(BATCH_SIZE_PROPERTY_NAME) != null
                        ? propertyProvider.getProperty(BATCH_SIZE_PROPERTY_NAME)
                        : String.valueOf(DEFAULT_BATCH_SIZE)
        );
        int flushInterval = Integer.parseInt(
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

        LOGGER.finer(String.format(
                "BatchResultManager initialized: batchSize= %d, flushInterval= %d sec",
                batchSize, flushInterval));
    }

    /**
     * Adds test result to pending batch for reporting.
     * Automatically flushes batch when size limit is reached.
     * Thread-safe operation for concurrent test execution.
     *
     * @param result test result to add to batch
     */
    public synchronized void addResult(TestResult result) {
        if (!isActive.get()) {
            LOGGER.warning("BatchResultManager is not active, skipping result: "
                    + result.getTitle());
            return;
        }

        pendingResults.add(result);
        LOGGER.finer(String.format("Added test result: %s (pending: %d)",
                result.getTitle(), pendingResults.size()));

        if (pendingResults.size() >= batchSize) {
            flushPendingResults();
        }
    }

    /**
     * Immediately flushes all pending test results to API.
     * Thread-safe operation that can be called concurrently.
     */
    public synchronized void flushPendingResults() {
        if (pendingResults.isEmpty()) {
            return;
        }
        List<TestResult> toSend = new ArrayList<>(pendingResults);
        pendingResults.clear();
        sendBatch(toSend, 1);
    }

    /**
     * Sends batch of test results with retry mechanism.
     * Uses exponential backoff for retries and moves failed results to failed list.
     *
     * @param results list of test results to send
     * @param attempt current attempt number (1-based)
     */
    private void sendBatch(List<TestResult> results, int attempt) {
        try {
            if (results.size() == 1) {
                apiClient.reportTest(runUid, results.get(0));
                LOGGER.finer("Reported single test: " + results.get(0).getTitle());
            } else {
                apiClient.reportTests(runUid, results);
                LOGGER.finer("Reported batch of %d tests" + results.size());
            }
        } catch (IOException e) {
            LOGGER.severe(String.format("Failed to report batch (attempt %d/%d): %s",
                    attempt, MAX_RETRY_ATTEMPTS, e.getMessage()));

            if (attempt < MAX_RETRY_ATTEMPTS) {
                scheduler.schedule(() -> sendBatch(results, attempt + 1),
                        (long) Math.pow(2, attempt), TimeUnit.SECONDS);
            } else {
                synchronized (this) {
                    failedResults.addAll(results);
                }
                LOGGER.severe(String.format("Failed to report %d tests after %d attempts",
                        results.size(), MAX_RETRY_ATTEMPTS));
            }
        }
    }

    /**
     * Gracefully shuts down batch result manager.
     * Flushes remaining results, stops scheduler, and reports any failures.
     * Should be called when test execution completes.
     */
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
            LOGGER.warning(String.format("BatchResultManager shutdown with %d failed results",
                    failedResults.size()));
        }

        LOGGER.fine("BatchResultManager shutdown completed");
    }
}
