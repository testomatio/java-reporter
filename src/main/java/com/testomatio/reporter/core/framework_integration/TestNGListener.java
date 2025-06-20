package com.testomatio.reporter.core.framework_integration;

import com.testomatio.reporter.model.TestMetadata;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.annotations.Test;

import static com.testomatio.reporter.constants.CommonConstants.FAILED;
import static com.testomatio.reporter.constants.CommonConstants.PASSED;
import static com.testomatio.reporter.constants.CommonConstants.SKIPPED;

/**
 * TestNG listener implementation that integrates test execution with Testomat.io reporting system.
 * Implements ISuiteListener, ITestListener, and IInvokedMethodListener to handle suite-level,
 * test-level events, and disabled tests.
 * Supports custom annotations (@Title, @TestId) for enhanced test metadata.
 */
public class TestNGListener extends BaseTestReporter implements ISuiteListener, ITestListener, IInvokedMethodListener {
    private final Set<String> processedTests = new HashSet<>();

    @Override
    public void onStart(ISuite suite) {
        handleSuiteStart(suite.getName());
        checkAndReportDisabledTests(suite);
    }

    @Override
    public void onFinish(ISuite suite) {
        handleSuiteFinish(suite.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        processTestResult(result, PASSED);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        processTestResult(result, FAILED);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        processTestResult(result, SKIPPED);
    }

    private void checkAndReportDisabledTests(ISuite suite) {
        if (!runManager.isActive()) {
            return;
        }

        suite.getXmlSuite().getTests().forEach(xmlTest -> {
            xmlTest.getXmlClasses().forEach(xmlClass -> {
                try {
                    Class<?> testClass = Class.forName(xmlClass.getName());
                    Method[] methods = testClass.getDeclaredMethods();

                    for (Method method : methods) {
                        Test testAnnotation = method.getAnnotation(Test.class);
                        if (testAnnotation != null && !testAnnotation.enabled()) {
                            String methodKey = xmlClass.getName() + "." + method.getName();

                            if (!processedTests.contains(methodKey)) {
                                processedTests.add(methodKey);
                                reportDisabledTest(method, testClass);
                                LOGGER.finer("Reported disabled test: " + methodKey);
                            }
                        }
                    }
                } catch (ClassNotFoundException e) {
                    LOGGER.severe(String.format(
                            "Could not load test class for: %s \n %s \n %s",
                            xmlClass.getName(), e.getCause(), e.getMessage()));
                }
            });
        });
    }

    private void reportDisabledTest(Method method, Class<?> testClass) {
        TestMetadata metadata = metadataExtractor.extractFromTestNGDisabled(method, testClass);
        String reason = "Test disabled via @Test(enabled = false)";
        reportTest(method.getName(), metadata, SKIPPED, reason);
        
        LOGGER.fine(String.format("Disabled test reported: %s - %s", metadata.getTitle(), reason));
    }

    private void processTestResult(ITestResult result, String status) {
        if (!runManager.isActive()) {
            return;
        }

        String methodKey = result.getTestClass().getName() + "." + result.getMethod().getMethodName();
        if (!processedTests.contains(methodKey)) {
            processedTests.add(methodKey);

            TestMetadata metadata = metadataExtractor.extractFromTestNG(result);
            reportTest(methodKey, metadata, status, result.getThrowable());

            LOGGER.finer(String.format("Test result reported: %s - %s", metadata.getTitle(), status));
        }
    }
}