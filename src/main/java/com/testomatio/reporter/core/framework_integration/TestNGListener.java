package com.testomatio.reporter.core.framework_integration;

import com.testomatio.reporter.annotation.TestId;
import com.testomatio.reporter.annotation.Title;
import com.testomatio.reporter.core.GlobalTestRunManager;
import com.testomatio.reporter.model.TestMetadata;
import com.testomatio.reporter.model.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.ISuiteListener;
import org.testng.ISuite;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;

import static com.testomatio.reporter.constants.CommonConstants.FAILED;
import static com.testomatio.reporter.constants.CommonConstants.PASSED;
import static com.testomatio.reporter.constants.CommonConstants.SKIPPED;

/**
 * TestNG listener implementation that integrates with Testomat.io reporting system.
 * This listener captures test execution events and forwards them to the GlobalTestRunManager
 * for batch reporting to the Testomat.io API.
 *
 * Implements both ISuiteListener and ITestListener to handle suite-level and test-level events.
 * Supports custom annotations (@Title, @TestId) for enhanced test metadata.
 */
public class TestNGListener implements ISuiteListener, ITestListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestNGListener.class);
    private final GlobalTestRunManager runManager = GlobalTestRunManager.getInstance();

    /**
     * Called when a test suite starts execution.
     * Increments the active suite counter in the global test run manager.
     *
     * @param suite the TestNG suite that is starting
     */
    @Override
    public void onStart(ISuite suite) {
        runManager.incrementSuiteCounter();
        LOGGER.debug("Started suite: {}", suite.getName());
    }

    /**
     * Called when a test suite finishes execution.
     * Decrements the active suite counter in the global test run manager.
     * When all suites are finished, this triggers the test run finalization.
     *
     * @param suite the TestNG suite that has finished
     */
    @Override
    public void onFinish(ISuite suite) {
        runManager.decrementSuiteCounter();
        LOGGER.debug("Finished suite: {}", suite.getName());
    }

    /**
     * Called when a test method succeeds.
     * Reports the test result with PASSED status to the test run manager.
     *
     * @param result the TestNG test result containing test execution details
     */
    @Override
    public void onTestSuccess(ITestResult result) {
        reportTestResult(result, PASSED);
    }

    /**
     * Called when a test method fails.
     * Reports the test result with FAILED status to the test run manager.
     *
     * @param result the TestNG test result containing test execution details and failure information
     */
    @Override
    public void onTestFailure(ITestResult result) {
        reportTestResult(result, FAILED);
    }

    /**
     * Called when a test method is skipped.
     * Reports the test result with SKIPPED status to the test run manager.
     *
     * @param result the TestNG test result containing test execution details
     */
    @Override
    public void onTestSkipped(ITestResult result) {
        reportTestResult(result, SKIPPED);
    }

    /**
     * Processes a test result and reports it to the global test run manager.
     * Extracts test metadata, creates a TestResult object, and forwards it for reporting.
     * Includes error handling to prevent test execution failures from affecting reporting.
     *
     * @param result the TestNG test result to process
     * @param status the test execution status (PASSED, FAILED, or SKIPPED)
     */
    private void reportTestResult(ITestResult result, String status) {
        if (!runManager.isActive()) {
            return;
        }

        try {
            TestMetadata metadata = extractTestMetadata(result);
            TestResult testResult = createTestResult(metadata, status, result);
            runManager.reportTest(testResult);
        } catch (Exception e) {
            LOGGER.error("Failed to report test result", e);
        }
    }

    /**
     * Extracts test metadata from a TestNG test result.
     * Retrieves test title, test ID, suite information, and file path for reporting.
     * Supports custom @Title and @TestId annotations for enhanced metadata.
     *
     * @param result the TestNG test result to extract metadata from
     * @return TestMetadata object containing structured test information
     */
    private TestMetadata extractTestMetadata(ITestResult result) {
        Method method = result.getMethod().getConstructorOrMethod().getMethod();
        String title = getTestTitle(method, result);
        String testId = getTestId(method);
        String suiteTitle = result.getTestClass().getName();
        String file = suiteTitle + ".java";

        return new TestMetadata(title, testId, suiteTitle, file);
    }

    /**
     * Creates a TestResult object from test metadata and execution details.
     * Includes error information (message and stack trace) for failed tests.
     *
     * @param metadata the extracted test metadata
     * @param status the test execution status
     * @param result the TestNG test result containing execution details
     * @return TestResult object ready for API reporting
     */
    private TestResult createTestResult(TestMetadata metadata, String status, ITestResult result) {
        String message = null;
        String stack = null;

        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            message = throwable.getMessage();
            stack = getStackTrace(throwable);
        }

        return new TestResult(
                metadata.getTitle(),
                metadata.getTestId(),
                metadata.getSuiteTitle(),
                metadata.getFile(),
                status,
                message,
                stack
        );
    }

    /**
     * Retrieves the test title from method annotations or TestNG result.
     * Prioritizes custom @Title annotation over the default TestNG test name.
     *
     * @param method the test method to check for annotations
     * @param result the TestNG test result containing the default test name
     * @return the test title to use for reporting
     */
    private String getTestTitle(Method method, ITestResult result) {
        Title titleAnnotation = method.getAnnotation(Title.class);
        return titleAnnotation != null ? titleAnnotation.value() : result.getName();
    }

    /**
     * Retrieves the test ID from method annotations.
     * Returns the value of @TestId annotation if present, null otherwise.
     *
     * @param method the test method to check for @TestId annotation
     * @return the test ID if specified, null if not annotated
     */
    private String getTestId(Method method) {
        TestId testIdAnnotation = method.getAnnotation(TestId.class);
        return testIdAnnotation != null ? testIdAnnotation.value() : null;
    }

    /**
     * Converts a throwable's stack trace to a string representation.
     * Used for capturing detailed error information for failed tests.
     *
     * @param t the throwable to extract stack trace from
     * @return string representation of the complete stack trace
     */
    private String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }
}