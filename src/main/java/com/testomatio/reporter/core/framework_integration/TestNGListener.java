package com.testomatio.reporter.core.framework_integration;

import com.testomatio.reporter.core.GlobalRunManager;
import com.testomatio.reporter.core.util.TestRunMetaDataExtractorUtil;
import com.testomatio.reporter.core.util.TestRunResultConstructorUtil;
import com.testomatio.reporter.exception.ReportTestResultException;
import com.testomatio.reporter.model.TestMetadata;
import com.testomatio.reporter.model.TestRunResult;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.annotations.Test;

import static com.testomatio.reporter.constants.CommonConstants.FAILED;
import static com.testomatio.reporter.constants.CommonConstants.PASSED;
import static com.testomatio.reporter.constants.CommonConstants.SKIPPED;
import static com.testomatio.reporter.logger.LoggerConfig.getLogger;

/**
 * TestNG listener implementation that integrates test execution with Testomat.io reporting system.
 * Implements ISuiteListener, ITestListener, and IInvokedMethodListener to handle suite-level,
 * test-level events, and disabled tests.
 * Supports custom annotations (@Title, @TestId) for enhanced test metadata.
 */
public class TestNGListener implements ISuiteListener, ITestListener, IInvokedMethodListener {
    private final Logger LOGGER = getLogger(this.getClass());
    private final GlobalRunManager runManager = GlobalRunManager.getInstance();

    private final Set<String> processedTests = new HashSet<>();

    @Override
    public void onStart(ISuite suite) {
        runManager.incrementSuiteCounter();
        LOGGER.fine("Started suite: " + suite.getName());

        checkAndReportDisabledTests(suite);
    }

    @Override
    public void onFinish(ISuite suite) {
        runManager.decrementSuiteCounter();
        LOGGER.fine("Finished suite: " + suite.getName());
    }

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

    /**
     * Checks for disabled tests in the suite and reports them with SKIPPED status.
     * This method iterates through all test methods in the suite and identifies
     * those marked with @Test(enabled = false).
     *
     * @param suite the TestNG suite to check for disabled tests
     */
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

    /**
     * Reports a disabled test method with SKIPPED status.
     * Uses TestRunResultConstructorUtil to create the test result object.
     *
     * @param method    the disabled test method
     * @param testClass the class containing the test method
     */
    private void reportDisabledTest(Method method, Class<?> testClass) {
        try {
            TestMetadata metadata = TestRunMetaDataExtractorUtil.extractTestMetadataForDisabledTest(method, testClass);
            TestRunResult testRunResult = TestRunResultConstructorUtil.createTestNGDisabledTestResult(
                    metadata,
                    SKIPPED,
                    "Test disabled via @Test(enabled = false)");

            runManager.reportTest(testRunResult);
            LOGGER.fine(String.format("Disabled test reported: %s - %s",
                    metadata.getTitle(),
                    "Test disabled via @Test(enabled = false)"));
        } catch (Exception e) {
            LOGGER.severe("Failed to report disabled test: " + method.getName());
            throw new ReportTestResultException("Failed to report disabled test: " + method.getName(), e);
        }
    }

    /**
     * Reports a test result to the global test run manager.
     * Uses TestRunResultConstructorUtil to create the test result object.
     *
     * @param result the TestNG test result
     * @param status the test execution status
     */
    private void reportTestResult(ITestResult result, String status) {
        if (!runManager.isActive()) {
            return;
        }

        try {
            String methodKey = result.getTestClass().getName() + "." + result.getMethod().getMethodName();
            if (!processedTests.contains(methodKey)) {
                processedTests.add(methodKey);

                TestMetadata metadata = TestRunMetaDataExtractorUtil.extractTestNGTestMetadata(result);
                TestRunResult testRunResult = TestRunResultConstructorUtil.createTestNGTestResult(metadata, status, result);
                runManager.reportTest(testRunResult);

                LOGGER.finer(String.format("Test result reported: %s - %s", metadata.getTitle(), status));
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to report test result" + e.getCause());
        }
    }
}