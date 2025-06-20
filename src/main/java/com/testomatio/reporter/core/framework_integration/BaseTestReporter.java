package com.testomatio.reporter.core.framework_integration;

import com.testomatio.reporter.core.GlobalRunManager;
import com.testomatio.reporter.core.builder.TestResultBuilder;
import com.testomatio.reporter.core.extractor.TestMetadataExtractor;
import com.testomatio.reporter.exception.ReportTestResultException;
import com.testomatio.reporter.model.TestMetadata;
import com.testomatio.reporter.model.TestRunResult;
import java.util.logging.Logger;

import static com.testomatio.reporter.logger.LoggerUtils.getLogger;

/**
 * Base class for test framework integrations providing common reporting functionality.
 * Handles suite lifecycle management and test result reporting to avoid code duplication
 * across different framework listeners.
 */
public abstract class BaseTestReporter {
    protected final Logger LOGGER = getLogger(this.getClass());
    protected final GlobalRunManager runManager = GlobalRunManager.getInstance();
    protected final TestMetadataExtractor metadataExtractor = new TestMetadataExtractor();
    protected final TestResultBuilder resultBuilder = new TestResultBuilder();

    /**
     * Called when a test suite starts execution.
     * Increments the suite counter and logs the start.
     */
    protected void handleSuiteStart(String suiteName) {
        LOGGER.fine("Starting test suite: " + suiteName);
        runManager.incrementSuiteCounter();
        LOGGER.finer("Active suite count incremented for: " + suiteName);
    }

    /**
     * Called when a test suite finishes execution.
     * Decrements the suite counter and logs the finish.
     */
    protected void handleSuiteFinish(String suiteName) {
        LOGGER.fine("Finishing test suite: " + suiteName);
        runManager.decrementSuiteCounter();
        LOGGER.finer("Active suite count decremented for: " + suiteName);
    }

    /**
     * Reports a test result if the run manager is active.
     * 
     * @param testName descriptive name for logging
     * @param metadata test metadata
     * @param status test execution status
     * @param throwable optional exception that occurred during test execution
     * @param customMessage optional custom message (for disabled/aborted tests)
     */
    protected void reportTest(String testName, TestMetadata metadata, String status, 
                            Throwable throwable, String customMessage) {
        if (!runManager.isActive()) {
            LOGGER.fine("Test run manager is not active, skipping test result reporting");
            return;
        }

        try {
            TestRunResult result = createTestResult(metadata, status, throwable, customMessage);
            logTestReporting(result, status);
            runManager.reportTest(result);
            logTestReported(result);
        } catch (Exception e) {
            LOGGER.severe("Failed to report test result for: " + testName);
            throw new ReportTestResultException("Failed to report test result for: " + testName, e);
        }
    }

    /**
     * Convenience method for reporting tests with throwable.
     */
    protected void reportTest(String testName, TestMetadata metadata, String status, Throwable throwable) {
        reportTest(testName, metadata, status, throwable, null);
    }

    /**
     * Convenience method for reporting tests with custom message.
     */
    protected void reportTest(String testName, TestMetadata metadata, String status, String customMessage) {
        reportTest(testName, metadata, status, null, customMessage);
    }

    /**
     * Creates a test result based on the provided parameters.
     */
    private TestRunResult createTestResult(TestMetadata metadata, String status, 
                                         Throwable throwable, String customMessage) {
        if (customMessage != null) {
            return resultBuilder.buildWithMessage(metadata, status, customMessage);
        } else {
            return resultBuilder.buildWithThrowable(metadata, status, throwable);
        }
    }

    private void logTestReporting(TestRunResult result, String status) {
        if (result.getTestId() != null) {
            LOGGER.info("Reporting test with TestId: " + result.getTestId() +
                    " | Test: " + result.getTitle() + " | Status: " + status);
        } else {
            LOGGER.fine("Reporting test without TestId: " + result.getTitle() + " - " + status);
        }
    }

    private void logTestReported(TestRunResult result) {
        if (result.getTestId() != null) {
            LOGGER.info("✓ TestId " + result.getTestId() + " successfully sent to Testomat.io as test_id field");
        }
    }
}