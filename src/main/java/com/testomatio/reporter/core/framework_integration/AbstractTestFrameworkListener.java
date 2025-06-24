package com.testomatio.reporter.core.framework_integration;

import com.testomatio.reporter.core.GlobalRunManager;
import com.testomatio.reporter.core.constructor.ResultConstructor;
import com.testomatio.reporter.core.constructor.TestCaseResultWrapper;
import com.testomatio.reporter.exception.ReportTestResultException;
import com.testomatio.reporter.model.TestMetadata;
import com.testomatio.reporter.model.TestCaseResult;
import java.util.logging.Logger;

import static com.testomatio.reporter.constants.CommonConstants.FAILED;
import static com.testomatio.reporter.constants.CommonConstants.PASSED;
import static com.testomatio.reporter.constants.CommonConstants.SKIPPED;
import static com.testomatio.reporter.logger.LoggerUtils.getLogger;

/**
 * Abstract base class for test framework integrations.
 * Provides common functionality for reporting test results to Testomat.io
 * across different testing frameworks (JUnit, TestNG, Cucumber).
 */
public abstract class AbstractTestFrameworkListener {

    protected final Logger LOGGER = getLogger(this.getClass());
    protected final GlobalRunManager runManager = GlobalRunManager.getInstance();
    protected final ResultConstructor resultConstructor;

    protected AbstractTestFrameworkListener() {
        this.resultConstructor = createResultConstructor();
        LOGGER.fine(getClass().getSimpleName() + " initialized");
    }

    /**
     * Factory method to create framework-specific result constructor.
     * Must be implemented by each concrete framework listener.
     */
    protected abstract ResultConstructor createResultConstructor();

    /**
     * Increments the suite counter and logs the start of a test suite.
     */
    protected void handleSuiteStarted(String suiteName) {
        LOGGER.fine("Starting test suite: " + suiteName);
        runManager.incrementSuiteCounter();
        LOGGER.finer("Active suite count incremented for: " + suiteName);
    }

    /**
     * Decrements the suite counter and logs the completion of a test suite.
     */
    protected void handleSuiteFinished(String suiteName) {
        LOGGER.fine("Finishing test suite: " + suiteName);
        runManager.decrementSuiteCounter();
        LOGGER.finer("Active suite count decremented for: " + suiteName);
    }

    /**
     * Common method for reporting test results.
     * Handles creation of TestRunResult and delegates to runManager.
     */
    protected void reportTestResult(TestMetadata metadata, String status, Object frameworkSpecificData) {
        reportTestResult(metadata, status, null, frameworkSpecificData);
    }

    /**
     * Common method for reporting test results with custom message.
     */
    protected void reportTestResult(TestMetadata metadata, String status, String message, Object frameworkSpecificData) {
        if (!runManager.isActive()) {
            LOGGER.fine("Test run manager is not active, skipping test result reporting");
            return;
        }

        try {
            TestCaseResult result = createTestResult(metadata, status, message, frameworkSpecificData);
            logAndReportResult(result, status, message);
        } catch (Exception e) {
            String testName = metadata != null ? metadata.getTitle() : "Unknown Test";
            LOGGER.severe("Failed to report test result for: " + testName);
            throw new ReportTestResultException("Failed to report test result for: " + testName, e);
        }
    }

    /**
     * Creates a TestRunResult using the framework-specific constructor.
     */
    protected TestCaseResult createTestResult(TestMetadata metadata, String status,
                                              String message, Object frameworkSpecificData) {
        TestCaseResultWrapper holder = buildTestRunResultHolder(metadata, status, message, frameworkSpecificData);
        return resultConstructor.constructTestRunResult(holder);
    }

    /**
     * Builds TestRunResultHolder with framework-specific data.
     * Can be overridden by subclasses if needed.
     */
    protected TestCaseResultWrapper buildTestRunResultHolder(TestMetadata metadata, String status,
                                                             String message, Object frameworkSpecificData) {
        TestCaseResultWrapper.Builder builder = TestCaseResultWrapper.builder()
                .withTestMetadata(metadata)
                .withStatus(status);

        if (message != null) {
            builder.withMessage(message);
        }

        addFrameworkSpecificData(builder, frameworkSpecificData);
        return builder.build();
    }

    /**
     * Adds framework-specific data to the TestRunResultHolder builder.
     * Must be implemented by each concrete framework listener.
     */
    protected abstract void addFrameworkSpecificData(TestCaseResultWrapper.Builder builder, Object frameworkSpecificData);

    /**
     * Logs and reports the test result to the run manager.
     */
    protected void logAndReportResult(TestCaseResult result, String status, String message) {
        logTestReporting(result, status, message);
        runManager.reportTest(result);
        logTestReported(result);
        LOGGER.finer("Test result reported successfully: " + result.getTitle());
    }

    /**
     * Logs test metadata creation for debugging purposes.
     */
    protected void logMetadataCreation(TestMetadata metadata) {
        LOGGER.finer("Created TestMetadata: Title=" + metadata.getTitle() +
                ", TestId=" + metadata.getTestId() +
                ", Suite=" + metadata.getSuiteTitle() +
                ", File=" + metadata.getFile());

        if (metadata.getTestId() != null) {
            LOGGER.fine("TestMetadata contains TestId: " + metadata.getTestId() +
                    " which will be sent as test_id field");
        }
    }

    /**
     * Logs test reporting details.
     */
    protected void logTestReporting(TestCaseResult result, String status, String message) {
        if (result.getTestId() != null) {
            String logMessage = String.format("Reporting test with TestId: %s | Test: %s | Status: %s",
                    result.getTestId(), result.getTitle(), status);
            if (message != null) {
                logMessage += " | Message: " + message;
            }
            LOGGER.info(logMessage);
        } else {
            String logMessage = "Reporting test without TestId: " + result.getTitle() + " - " + status;
            if (message != null) {
                logMessage += " | Message: " + message;
            }
            LOGGER.fine(logMessage);
        }
    }

    protected void logTestReported(TestCaseResult result) {
        if (result.getTestId() != null) {
            LOGGER.fine("✓ TestId " + result.getTestId() + " successfully sent to Testomat.io as test_id field");
        }
    }

    /**
     * Determines test status from framework-specific status.
     * Can be overridden by subclasses for framework-specific logic.
     */
    protected String normalizeStatus(Object frameworkStatus) {
        if (frameworkStatus == null) {
            return FAILED;
        }

        String statusStr = frameworkStatus.toString().toUpperCase();
        switch (statusStr) {
            case "PASSED":
            case "SUCCESS":
            case "SUCCESSFUL":
                return PASSED;
            case "SKIPPED":
            case "PENDING":
            case "UNDEFINED":
            case "AMBIGUOUS":
            case "DISABLED":
            case "ABORTED":
                return SKIPPED;
            default:
                return FAILED;
        }
    }
}