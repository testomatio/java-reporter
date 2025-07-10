package io.testomat.testng.listener;

import static io.testomat.core.constants.CommonConstants.FAILED;
import static io.testomat.core.constants.CommonConstants.PASSED;
import static io.testomat.core.constants.CommonConstants.SKIPPED;

import io.testomat.core.exception.ReportTestResultException;
import io.testomat.core.exception.TestClassNotFoundException;
import io.testomat.core.model.TestMetadata;
import io.testomat.core.model.TestResult;
import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.testng.constructor.TestNgTestResultConstructor;
import io.testomat.testng.constructor.TestResultWrapper;
import io.testomat.testng.extractor.TestNgMetaDataExtractor;
import io.testomat.testng.extractor.TestNgTestWrapper;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.annotations.Test;

/**
 * TestNG listener for Testomat.io integration.
 * Reports TestNG test execution results to Testomat.io platform.
 * Supports custom annotations (@Title, @TestId) and handles disabled tests.
 */
public class TestNgListener implements ISuiteListener, ITestListener, IInvokedMethodListener {
    private static final String DISABLED_MESSAGE = "Test disabled via @Test(enabled = false)";

    private final TestNgTestResultConstructor resultConstructor = new TestNgTestResultConstructor();
    private final GlobalRunManager runManager = GlobalRunManager.getInstance();
    private final Set<String> processedTests = new HashSet<>();
    private final TestNgMetaDataExtractor metaDataExtractor = new TestNgMetaDataExtractor();

    @Override
    public void onStart(ISuite suite) {
        runManager.incrementSuiteCounter();
        checkAndReportDisabledTests(suite);
    }

    @Override
    public void onFinish(ISuite suite) {
        runManager.decrementSuiteCounter();
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        handleTestNgResult(result, PASSED);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        handleTestNgResult(result, FAILED);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        handleTestNgResult(result, SKIPPED);
    }

    /**
     * Handles TestNG test results and prevents duplicate reporting.
     */
    private void handleTestNgResult(ITestResult result, String status) {
        String methodKey = result.getTestClass().getName()
                + "."
                + result.getMethod().getMethodName();
        if (processedTests.contains(methodKey)) {
            return;
        }

        processedTests.add(methodKey);
        TestNgTestWrapper wrapper = TestNgTestWrapper.forRegularTest(result);
        TestMetadata metadata = metaDataExtractor.extractTestMetadata(wrapper);
        reportTestResult(metadata, status, null, result);
    }

    /**
     * Identifies and reports disabled tests marked with {@code @Test(enabled = false)}.
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
                            }
                        }
                    }
                } catch (ClassNotFoundException e) {
                    throw new TestClassNotFoundException("Failed to load test class: "
                            + xmlClass.getName(), e);
                }
            });
        });
    }

    /**
     * Reports disabled test method with SKIPPED status.
     */
    private void reportDisabledTest(Method method, Class<?> testClass) {
        TestNgTestWrapper wrapper = TestNgTestWrapper.forDisabledTest(method, testClass);
        TestMetadata metadata = metaDataExtractor.extractTestMetadata(wrapper);
        reportTestResult(metadata, SKIPPED, DISABLED_MESSAGE, null);
    }

    /**
     * Reports test result to Testomat.io with optional custom message.
     */
    private void reportTestResult(TestMetadata metadata, String status,
                                  String message, Object frameworkSpecificData) {
        if (!runManager.isActive()) {
            return;
        }

        try {
            TestResultWrapper.Builder builder = TestResultWrapper.builder()
                    .withTestMetadata(metadata)
                    .withStatus(status);

            if (message != null) {
                builder.withMessage(message);
            }

            if (frameworkSpecificData instanceof ITestResult) {
                builder.withTestResult((ITestResult) frameworkSpecificData);
            }

            TestResultWrapper wrapper = builder.build();
            TestResult result = resultConstructor.constructTestRunResult(wrapper);
            runManager.reportTest(result);

        } catch (Exception e) {
            String testName = metadata != null ? metadata.getTitle() : "Unknown Test";
            throw new ReportTestResultException("Failed to report test result for: " + testName, e);
        }
    }
}
