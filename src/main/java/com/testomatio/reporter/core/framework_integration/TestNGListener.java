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
     * All public methods here are related to the tests lifecycle by their names
     */
    @Override
    public void onStart(ISuite suite) {
        runManager.onSuiteStart();
        LOGGER.debug("TestNG suite started: {}", suite.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        reportTestResult(result, "passed");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        reportTestResult(result, "failed");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        reportTestResult(result, "skipped");
    }

    private void reportTestResult(ITestResult result, String status) {
        if (!runManager.isActive()) {
            return;
        }

        try {
            TestMetadata metadata = extractTestMetadata(result);
            TestResult testResult = createTestResult(metadata, status, result);
            runManager.reportTest(testResult);
            LOGGER.debug("Reported TestNG test: {} [{}]", testResult.getTitle(), status);
        } catch (Exception e) {
            LOGGER.error("Failed to report TestNG test result", e);
        }
    }

    private TestMetadata extractTestMetadata(ITestResult result) {
        Method method = result.getMethod().getConstructorOrMethod().getMethod();
        String title = getTestTitle(method, result);
        String testId = getTestId(method);
        String suiteTitle = result.getTestClass().getName();
        String file = suiteTitle + ".java";

        return new TestMetadata(title, testId, suiteTitle, file);
    }

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

    private String getTestTitle(Method method, ITestResult result) {
        Title titleAnnotation = method.getAnnotation(Title.class);
        return titleAnnotation != null ? titleAnnotation.value() : result.getName();
    }

    private String getTestId(Method method) {
        TestId testIdAnnotation = method.getAnnotation(TestId.class);
        return testIdAnnotation != null ? testIdAnnotation.value() : null;
    }

    private String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }
}