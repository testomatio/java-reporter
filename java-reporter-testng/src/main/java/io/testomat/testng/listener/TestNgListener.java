package io.testomat.testng.listener;

import static io.testomat.core.constants.CommonConstants.FAILED;
import static io.testomat.core.constants.CommonConstants.PASSED;
import static io.testomat.core.constants.CommonConstants.SKIPPED;

import io.testomat.core.exception.ReportTestResultException;
import io.testomat.core.exception.TestClassNotFoundException;
import io.testomat.core.model.TestMetadata;
import io.testomat.core.model.TestResult;
import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.testng.constructor.ResultConstructor;
import io.testomat.testng.constructor.TestNgTestResultConstructor;
import io.testomat.testng.constructor.TestResultWrapper;
import io.testomat.testng.extractor.MetaDataExtractor;
import io.testomat.testng.extractor.TestNgMetaDataExtractor;
import io.testomat.testng.extractor.TestNgTestWrapper;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(TestNgListener.class);
    private static final String DISABLED_MESSAGE = "Test disabled via @Test(enabled = false)";

    private final ResultConstructor resultConstructor = new TestNgTestResultConstructor();
    private final GlobalRunManager runManager = GlobalRunManager.getInstance();
    private final Set<String> processedTests = new HashSet<>();
    private final MetaDataExtractor<TestNgTestWrapper> metaDataExtractor =
            new TestNgMetaDataExtractor();

    @Override
    public void onStart(ISuite suite) {
        handleSuiteStarted(suite.getName());
        checkAndReportDisabledTests(suite);
    }

    @Override
    public void onFinish(ISuite suite) {
        handleSuiteFinished(suite.getName());
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
     *
     * @param result TestNG test result
     * @param status test status (PASSED, FAILED, SKIPPED)
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
        reportTestResult(metadata, status, result);
    }

    /**
     * Identifies and reports disabled tests marked with {@code @Test(enabled = false)}.
     *
     * @param suite TestNG test suite to scan for disabled tests
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
     *
     * @param method    disabled test method
     * @param testClass class containing the disabled test
     */
    private void reportDisabledTest(Method method, Class<?> testClass) {
        TestNgTestWrapper wrapper = TestNgTestWrapper.forDisabledTest(method, testClass);
        TestMetadata metadata = metaDataExtractor.extractTestMetadata(wrapper);
        reportTestResult(metadata, SKIPPED, DISABLED_MESSAGE, null);
    }

    /**
     * Handles test suite start by incrementing suite counter.
     *
     * @param suiteName name of the starting test suite
     */
    private void handleSuiteStarted(String suiteName) {
        runManager.incrementSuiteCounter();
    }

    /**
     * Handles test suite completion by decrementing suite counter.
     *
     * @param suiteName name of the finished test suite
     */
    private void handleSuiteFinished(String suiteName) {
        runManager.decrementSuiteCounter();
    }

    /**
     * Reports test result to Testomat.io.
     *
     * @param metadata              test metadata containing title, ID, and suite info
     * @param status                test execution status (PASSED, FAILED, SKIPPED)
     * @param frameworkSpecificData framework-specific test data
     */
    private void reportTestResult(TestMetadata metadata,
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
    private void reportTestResult(TestMetadata metadata,
                                  String status,
                                  String message,
                                  Object frameworkSpecificData) {
        if (!runManager.isActive()) {
            return;
        }

        try {
            TestResult result = createTestResult(metadata, status, message, frameworkSpecificData);
            logAndReportResult(result);
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
    private TestResult createTestResult(TestMetadata metadata, String status,
                                        String message, Object frameworkSpecificData) {
        TestResultWrapper holder = buildTestRunResultWrapper(metadata,
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
    private TestResultWrapper buildTestRunResultWrapper(TestMetadata metadata, String status,
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
     * Logs and reports test result to run manager.
     *
     * @param result test case result to report
     */
    private void logAndReportResult(TestResult result) {
        runManager.reportTest(result);
    }

    private void addFrameworkSpecificData(
            TestResultWrapper.Builder builder, Object frameworkSpecificData) {
        if (frameworkSpecificData instanceof ITestResult) {
            builder.withTestResult((ITestResult) frameworkSpecificData);
        }
    }
}
