package com.core.framework_integration;

import com.annotation.TestId;
import com.annotation.Title;
import com.core.GlobalTestRunManager;
import com.model.TestMetadata;
import com.model.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.ISuiteListener;
import org.testng.ISuite;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;

import static com.constants.CommonConstants.FAILED;
import static com.constants.CommonConstants.PASSED;
import static com.constants.CommonConstants.SKIPPED;

public class TestNGListener implements ISuiteListener, ITestListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestNGListener.class);
    private final GlobalTestRunManager runManager = GlobalTestRunManager.getInstance();

    // Suite lifecycle
    @Override
    public void onStart(ISuite suite) {
        runManager.incrementSuiteCounter();
        LOGGER.debug("Started suite: {}", suite.getName());
    }
    
    @Override
    public void onFinish(ISuite suite) {
        runManager.decrementSuiteCounter();
        LOGGER.debug("Finished suite: {}", suite.getName());
    }

    // Test lifecycle
    @Override
    public void onTestSuccess(ITestResult result) {
        reportTestResult(result, PASSED);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        reportTestResult(result, FAILED);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        reportTestResult(result, SKIPPED);
    }
    
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