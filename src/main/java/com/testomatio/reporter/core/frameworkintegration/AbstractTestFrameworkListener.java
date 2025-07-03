package com.testomatio.reporter.core.frameworkintegration;

import static com.testomatio.reporter.constants.CommonConstants.FAILED;
import static com.testomatio.reporter.constants.CommonConstants.PASSED;
import static com.testomatio.reporter.constants.CommonConstants.SKIPPED;

import com.testomatio.reporter.core.GlobalRunManager;
import com.testomatio.reporter.core.constructor.ResultConstructor;
import com.testomatio.reporter.core.constructor.TestResultWrapper;
import com.testomatio.reporter.exception.ReportTestResultException;
import com.testomatio.reporter.model.TestMetadata;
import com.testomatio.reporter.model.TestResult;

/**
 * Base class for test framework integrations with Testomat.io.
 * Provides common functionality for JUnit, TestNG, and Cucumber listeners.
 */
public abstract class AbstractTestFrameworkListener {

    protected final GlobalRunManager runManager = GlobalRunManager.getInstance();
    protected final ResultConstructor resultConstructor;

    protected AbstractTestFrameworkListener() {
        this.resultConstructor = createResultConstructor();
    }

    /**
     * Creates framework-specific result constructor.
     *
     * @return result constructor for the specific test framework
     */
    protected abstract ResultConstructor createResultConstructor();

    /**
     * Handles test suite start by incrementing suite counter.
     *
     * @param suiteName name of the starting test suite
     */
    protected void handleSuiteStarted(String suiteName) {
        runManager.incrementSuiteCounter();
    }

    /**
     * Handles test suite completion by decrementing suite counter.
     *
     * @param suiteName name of the finished test suite
     */
    protected void handleSuiteFinished(String suiteName) {
        runManager.decrementSuiteCounter();
    }

    /**
     * Reports test result to Testomat.io.
     *
     * @param metadata              test metadata containing title, ID, and suite info
     * @param status                test execution status (PASSED, FAILED, SKIPPED)
     * @param frameworkSpecificData framework-specific test data
     */
    protected void reportTestResult(TestMetadata metadata,
                                    String status,
                                    Object frameworkSpecificData) {
        reportTestResult(metadata, status, null, frameworkSpecificData);
    }

    /**
     * Reports test result to Testomat.io with custom message.
     *
     * @param metadata              test metadata containing title, ID, and suite info
     * @param status                test execution status (PASSED, FAILED, SKIPPED)
     * @param message               optional custom message describing the result
     * @param frameworkSpecificData framework-specific test data
     */
    protected void reportTestResult(TestMetadata metadata,
                                    String status,
                                    String message,
                                    Object frameworkSpecificData) {
        if (!runManager.isActive()) {
            return;
        }

        try {
            TestResult result = createTestResult(metadata, status, message, frameworkSpecificData);
            logAndReportResult(result, status, message);
        } catch (Exception e) {
            String testName = metadata != null ? metadata.getTitle() : "Unknown Test";
            throw new ReportTestResultException("Failed to report test result for: " + testName, e);
        }
    }

    /**
     * Creates test case result using framework-specific constructor.
     *
     * @param metadata              test metadata
     * @param status                test status
     * @param message               optional message
     * @param frameworkSpecificData framework-specific data
     * @return constructed test case result
     */
    protected TestResult createTestResult(TestMetadata metadata, String status,
                                          String message, Object frameworkSpecificData) {
        TestResultWrapper holder = buildTestRunResultHolder(metadata,
                status,
                message,
                frameworkSpecificData);
        return resultConstructor.constructTestRunResult(holder);
    }

    /**
     * Builds test result wrapper with framework-specific data.
     *
     * @param metadata              test metadata
     * @param status                test status
     * @param message               optional message
     * @param frameworkSpecificData framework-specific data
     * @return configured test case result wrapper
     */
    protected TestResultWrapper buildTestRunResultHolder(TestMetadata metadata, String status,
                                                         String message,
                                                         Object frameworkSpecificData) {
        TestResultWrapper.Builder builder = TestResultWrapper.builder()
                .withTestMetadata(metadata)
                .withStatus(status);

        if (message != null) {
            builder.withMessage(message);
        }

        addFrameworkSpecificData(builder, frameworkSpecificData);
        return builder.build();
    }

    /**
     * Adds framework-specific data to result wrapper builder.
     *
     * @param builder               result wrapper builder
     * @param frameworkSpecificData framework-specific data to add
     */
    protected abstract void addFrameworkSpecificData(TestResultWrapper.Builder builder,
                                                     Object frameworkSpecificData);

    /**
     * Logs and reports test result to run manager.
     *
     * @param result  test case result to report
     * @param status  test status for logging
     * @param message optional message for logging
     */
    protected void logAndReportResult(TestResult result, String status, String message) {
        logTestReporting(result, status, message);
        runManager.reportTest(result);
    }

    /**
     * Logs test reporting details before submission.
     *
     * @param result  test case result
     * @param status  test status
     * @param message optional message
     */
    protected void logTestReporting(TestResult result, String status, String message) {
        if (result.getTestId() != null) {
            String logMessage = String.format(
                    "Reporting test with TestId: %s | Test: %s | Status: %s",
                    result.getTestId(), result.getTitle(), status);
            if (message != null) {
                logMessage += " | Message: " + message;
            }
        } else {
            String logMessage = "Reporting test without TestId: "
                    + result.getTitle()
                    + " - "
                    + status;
            if (message != null) {
                logMessage += " | Message: " + message;
            }
        }
    }

    /**
     * Normalizes framework-specific status to standard format.
     *
     * @param frameworkStatus framework-specific status object
     * @return normalized status (PASSED, FAILED, or SKIPPED)
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
