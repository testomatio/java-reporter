package io.testomat.core.batch;

import static io.testomat.core.constants.PropertyValuesConstants.DEFAULT_BATCH_SIZE;
import static io.testomat.core.constants.PropertyValuesConstants.DEFAULT_FLUSH_INTERVAL_SECONDS;

import io.testomat.core.client.ApiInterface;
import io.testomat.core.model.TestResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages batch processing of test results for efficient API reporting.
 * Collects test results in batches and periodically flushes them to Testomat.io.
 * Provides automatic retry mechanism and graceful shutdown handling.
 * 
 * <p>Thread-safe for concurrent test execution with configurable batch size and flush intervals.
 */
public class BatchResultManager {

    private static final Logger log = LoggerFactory.getLogger(BatchResultManager.class);
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int TERMINATION_AWAIT_TIMEOUT_SECONDS = 10;

    private final List<TestResult> pendingResults = new ArrayList<>();
    private final List<TestResult> failedResults = new ArrayList<>();
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
     */
    public BatchResultManager(ApiInterface apiClient, String runUid) {
        this.apiClient = apiClient;
        this.runUid = runUid;

        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "TestomatBatchFlush");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(this::flushPendingResults,
                DEFAULT_FLUSH_INTERVAL_SECONDS,
                DEFAULT_FLUSH_INTERVAL_SECONDS,
                TimeUnit.SECONDS);

        log.debug("BatchResultManager initialized: batchSize= {}, flushInterval= {} sec",
                DEFAULT_BATCH_SIZE, DEFAULT_FLUSH_INTERVAL_SECONDS);
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
            log.warn("BatchResultManager is not active, skipping result: {}", result.getTitle());
            return;
        }

        pendingResults.add(result);
        log.debug("Added test result: {} (pending: {})", result.getTitle(), pendingResults.size());

        if (pendingResults.size() >= DEFAULT_BATCH_SIZE) {
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
                log.debug("Reported single test: {}", results.get(0).getTitle());
            } else {
                apiClient.reportTests(runUid, results);
                log.debug("Reported batch of {} tests", results.size());
            }
        } catch (IOException e) {
            log.error("Failed to report batch (attempt {}/{}): {}", attempt,
                    MAX_RETRY_ATTEMPTS, e.getMessage());

            if (attempt < MAX_RETRY_ATTEMPTS) {
                scheduler.schedule(() -> sendBatch(results, attempt + 1),
                        (long) Math.pow(2, attempt), TimeUnit.SECONDS);
            } else {
                synchronized (this) {
                    failedResults.addAll(results);
                }
                log.error("Failed to report {} tests after {} attempts", results.size(),
                        MAX_RETRY_ATTEMPTS);
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
            if (!scheduler.awaitTermination(TERMINATION_AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        if (!failedResults.isEmpty()) {
            log.warn("BatchResultManager shutdown with {} failed results", failedResults.size());
        }

        log.debug("BatchResultManager shutdown completed");
    }
}
