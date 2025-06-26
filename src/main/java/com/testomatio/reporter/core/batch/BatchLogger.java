package com.testomatio.reporter.core.batch;

import com.testomatio.reporter.model.TestResult;
import java.util.List;
import java.util.logging.Logger;

import static com.testomatio.reporter.constants.PropertyValuesConstants.DEFAULT_BATCH_SIZE;
import static com.testomatio.reporter.constants.PropertyValuesConstants.DEFAULT_FLUSH_INTERVAL_SECONDS;
import static com.testomatio.reporter.core.batch.BatchResultManager.MAX_RETRY_ATTEMPTS;
import static com.testomatio.reporter.logger.LoggerConfig.getLogger;

public class BatchLogger {
    private static final Logger LOGGER = getLogger(BatchResultManager.class);

    public void logBatchManagerInitializationSuccess(int flushInterval, int batchSize) {
        LOGGER.finer(String.format("BatchResultManager initialized: batchSize= %d, flushInterval= %d sec",
                batchSize, flushInterval));
    }

    public void logInactiveBatchResultManagerAndSkip(TestResult result) {
        LOGGER.warning("BatchResultManager is not active, skipping result: " + result.getTitle());
    }

    public void logAddedResultAndPendingCount(TestResult result, int pendingResultsSize) {
        LOGGER.finer(String.format("Added test result: %s (pending: %d)",
                result.getTitle(), pendingResultsSize));
    }

    public void logFinalAttemptFailure(List<TestResult> results) {
        LOGGER.severe(String.format("Failed to report %d tests after %d attempts",
                results.size(), MAX_RETRY_ATTEMPTS));
    }

    public void logReportBatchSize(List<TestResult> results) {
        LOGGER.finer("Reported batch of %d tests" + results.size());
    }

    public void logSingleTestReport(TestResult result) {
        LOGGER.finer("Reported single test: " + result.getTitle());

    }

    public void logRetryFailure(Throwable e, int attempt) {
        LOGGER.severe(String.format("Failed to report batch (attempt %d/%d): %s",
                attempt, MAX_RETRY_ATTEMPTS, e.getMessage()));
    }

    public void notifyUserMinFlushIntervalIfNeeded(Integer userFlushIntervalSeconds) {
        if (userFlushIntervalSeconds == null || userFlushIntervalSeconds < DEFAULT_FLUSH_INTERVAL_SECONDS) {
            LOGGER.info(
                    String.format("Batch flush interval provided is less than min batch flush size: %d, default is used",
                            DEFAULT_FLUSH_INTERVAL_SECONDS)
            );

        }
    }

    public void notifyUserMinBatchSizeIfNeeded(Integer userBatchSize) {
        if (userBatchSize == null || userBatchSize < DEFAULT_BATCH_SIZE) {
            LOGGER.info(String.format("Batch size provided is less than min batch size: %d, default is used",
                    DEFAULT_BATCH_SIZE)
            );

        }
    }

    public void logWarnShutdownWithNoFails(int failedResultsSize) {
        LOGGER.warning(String.format("BatchResultManager shutdown with %d failed results", failedResultsSize));
    }

    public void logShutdownCompleted() {
        LOGGER.fine("BatchResultManager shutdown completed");
    }
}
