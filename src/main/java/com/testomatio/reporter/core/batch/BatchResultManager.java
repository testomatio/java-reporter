package com.testomatio.reporter.core.batch;

import com.testomatio.reporter.client.ApiInterface;
import com.testomatio.reporter.model.TestRunResult;
import com.testomatio.reporter.property_config.impl.PropertyProviderFactoryImpl;
import com.testomatio.reporter.property_config.interf.PropertyProvider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.testomatio.reporter.constants.PropertyNameConstants.BATCH_FLUSH_INTERVAL_PROPERTY_NAME;
import static com.testomatio.reporter.constants.PropertyNameConstants.BATCH_SIZE_PROPERTY_NAME;

/**
 * Manages batch processing and reporting of test results to the Testomat.io API.
 * This class collects test results in batches and periodically flushes them to improve
 * performance and reduce API calls during test execution.

 * The manager automatically starts a background thread for periodic flushing
 * and handles both single test and batch reporting scenarios.
 */
public class BatchResultManager {

    //TODO: Refactor class for SRP

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchResultManager.class);
    private static final int DEFAULT_BATCH_SIZE = 10;
    private static final int DEFAULT_FLUSH_INTERVAL_SECONDS = 5;
    private static final int MAX_RETRY_ATTEMPTS = 3;

    private final List<TestRunResult> pendingResults = new ArrayList<>();
    private final List<TestRunResult> failedResults = new ArrayList<>();
    private final int batchSize;
    private final int flushInterval;
    private final ApiInterface apiClient;
    private final String runUid;
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean isActive = new AtomicBoolean(true);

    /**
     * Constructs a new BatchResultManager with configurable batch settings.
     * Initializes the batch size and flush interval from properties, falling back to defaults.
     * Starts a background scheduler for automatic periodic flushing.
     *
     * @param apiClient the API client interface for reporting test results
     * @param runUid the unique identifier of the test run to report results to
     * @throws NumberFormatException if batch size or flush interval properties are not valid integers
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

    /**
     * Adds a test result to the pending batch for reporting.
     * If the batch size is reached, automatically triggers a flush operation.
     * Thread-safe operation that can be called concurrently from multiple test threads.
     *
     * @param result the test result to add to the batch
     * @throws IllegalStateException if the manager is not active (shutdown has been called)
     */
    public synchronized void addResult(TestRunResult result) {
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

    /**
     * Immediately flushes all pending test results to the API.
     * Creates a copy of pending results, clears the pending list, and sends the batch
     * for processing. This method is called automatically when batch size is reached
     * or during scheduled flushes.
     *
     * Thread-safe operation that handles concurrent access to pending results.
     */
    public synchronized void flushPendingResults() {
        if (pendingResults.isEmpty()) {
            return;
        }
        List<TestRunResult> toSend = new ArrayList<>(pendingResults);
        pendingResults.clear();
        sendBatch(toSend, 1);
    }

    /**
     * Sends a batch of test results to the API with retry mechanism.
     * Attempts to report the results, and if it fails, schedules a retry with exponential backoff.
     * After maximum retry attempts, moves failed results to the failed results list.
     *
     * Uses different API methods for single results vs. batch results for optimization.
     *
     * @param results the list of test results to send
     * @param attempt the current attempt number (1-based)
     */
    private void sendBatch(List<TestRunResult> results, int attempt) {
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

    /**
     * Gracefully shuts down the batch result manager.
     * Stops accepting new results, flushes any remaining pending results,
     * and shuts down the background scheduler thread.
     *
     * This method should be called when test execution is complete to ensure
     * all pending results are reported before the application terminates.
     *
     * The shutdown process:
     * 1. Sets the manager as inactive to reject new results
     * 2. Flushes any remaining pending results
     * 3. Shuts down the scheduler with a 10-second timeout
     * 4. Reports any failed results that couldn't be sent
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
            LOGGER.warn("BatchResultManager shutdown with {} failed results", failedResults.size());
        }

        LOGGER.info("BatchResultManager shutdown completed");
    }
}