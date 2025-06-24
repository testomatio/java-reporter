package com.testomatio.reporter.core.framework_integration;

import com.testomatio.reporter.core.constructor.ResultConstructor;
import com.testomatio.reporter.core.constructor.TestCaseResultWrapper;
import com.testomatio.reporter.core.constructor.TestNGTestResultConstructor;
import com.testomatio.reporter.core.extractor.MetaDataExtractor;
import com.testomatio.reporter.core.extractor.TestNGMetaDataExtractor;
import com.testomatio.reporter.core.extractor.wrapper.TestNGTestWrapper;
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
 * TestNG listener for Testomat.io integration.
 * Reports TestNG test execution results to Testomat.io platform.
 * Supports custom annotations (@Title, @TestId) and handles disabled tests.
 */
public class TestNGListener extends AbstractTestFrameworkListener
        implements ISuiteListener, ITestListener, IInvokedMethodListener {

    private static final String DISABLED_MESSAGE = "Test disabled via @Test(enabled = false)";
    private final MetaDataExtractor<TestNGTestWrapper> metaDataExtractor = new TestNGMetaDataExtractor();
    private final Set<String> processedTests = new HashSet<>();

    /**
     * Creates new TestNG listener.
     */
    public TestNGListener() {
        super();
    }

    @Override
    protected ResultConstructor createResultConstructor() {
        return new TestNGTestResultConstructor();
    }

    @Override
    protected void addFrameworkSpecificData(TestCaseResultWrapper.Builder builder, Object frameworkSpecificData) {
        if (frameworkSpecificData instanceof ITestResult) {
            builder.withTestResult((ITestResult) frameworkSpecificData);
        }
    }

    /**
     * Called when test suite starts. Initializes suite tracking and reports disabled tests.
     *
     * @param suite TestNG test suite
     */
    @Override
    public void onStart(ISuite suite) {
        handleSuiteStarted(suite.getName());
        checkAndReportDisabledTests(suite);
    }

    /**
     * Called when test suite finishes. Completes suite tracking.
     *
     * @param suite TestNG test suite
     */
    @Override
    public void onFinish(ISuite suite) {
        handleSuiteFinished(suite.getName());
    }

    /**
     * Called when test passes successfully. Reports test as passed.
     *
     * @param result TestNG test result
     */
    @Override
    public void onTestSuccess(ITestResult result) {
        handleTestNGResult(result, PASSED);
    }

    /**
     * Called when test fails. Reports test as failed.
     *
     * @param result TestNG test result
     */
    @Override
    public void onTestFailure(ITestResult result) {
        handleTestNGResult(result, FAILED);
    }

    /**
     * Called when test is skipped. Reports test as skipped.
     *
     * @param result TestNG test result
     */
    @Override
    public void onTestSkipped(ITestResult result) {
        handleTestNGResult(result, SKIPPED);
    }

    /**
     * Handles TestNG test results and prevents duplicate reporting.
     *
     * @param result TestNG test result
     * @param status test status (PASSED, FAILED, SKIPPED)
     */
    private void handleTestNGResult(ITestResult result, String status) {
        String methodKey = result.getTestClass().getName() + "." + result.getMethod().getMethodName();
        if (processedTests.contains(methodKey)) {
            return;
        }

        processedTests.add(methodKey);
        TestNGTestWrapper wrapper = TestNGTestWrapper.forRegularTest(result);
        TestMetadata metadata = metaDataExtractor.extractTestMetadata(wrapper);
        logMetadataCreation(metadata);
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
                                LOGGER.finer("Reported disabled test: " + methodKey);
                            }
                        }
                    }
                } catch (ClassNotFoundException e) {
                    LOGGER.severe(String.format("Could not load test class for: %s \n %s \n %s",
                            xmlClass.getName(), e.getCause(), e.getMessage()));
                }
            });
        });
    }

    /**
     * Reports disabled test method with SKIPPED status.
     *
     * @param method disabled test method
     * @param testClass class containing the disabled test
     */
    private void reportDisabledTest(Method method, Class<?> testClass) {
        TestNGTestWrapper wrapper = TestNGTestWrapper.forDisabledTest(method, testClass);
        TestMetadata metadata = metaDataExtractor.extractTestMetadata(wrapper);
        logMetadataCreation(metadata);
        reportTestResult(metadata, SKIPPED, DISABLED_MESSAGE, null);

        LOGGER.fine(String.format("Disabled test reported: %s - %s",
                metadata.getTitle(), DISABLED_MESSAGE));
    }
}