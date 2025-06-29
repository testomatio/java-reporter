package com.testomatio.reporter.core.frameworkintegration;

import static com.testomatio.reporter.constants.CommonConstants.FAILED;
import static com.testomatio.reporter.constants.CommonConstants.PASSED;
import static com.testomatio.reporter.constants.CommonConstants.SKIPPED;

import com.testomatio.reporter.core.constructor.ResultConstructor;
import com.testomatio.reporter.core.constructor.TestNgTestResultConstructor;
import com.testomatio.reporter.core.constructor.TestResultWrapper;
import com.testomatio.reporter.core.extractor.MetaDataExtractor;
import com.testomatio.reporter.core.extractor.TestNgMetaDataExtractor;
import com.testomatio.reporter.core.extractor.wrapper.TestNgTestWrapper;
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

/**
 * TestNG listener for Testomat.io integration.
 * Reports TestNG test execution results to Testomat.io platform.
 * Supports custom annotations (@Title, @TestId) and handles disabled tests.
 */
public class TestNgListener extends AbstractTestFrameworkListener
        implements ISuiteListener, ITestListener, IInvokedMethodListener {

    private static final String DISABLED_MESSAGE = "Test disabled via @Test(enabled = false)";
    private final MetaDataExtractor<TestNgTestWrapper> metaDataExtractor =
            new TestNgMetaDataExtractor();
    private final Set<String> processedTests = new HashSet<>();

    /**
     * Creates new TestNG listener.
     */
    public TestNgListener() {
        super();
    }

    @Override
    protected ResultConstructor createResultConstructor() {
        return new TestNgTestResultConstructor();
    }

    @Override
    protected void addFrameworkSpecificData(
            TestResultWrapper.Builder builder, Object frameworkSpecificData) {
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
        handleTestNgResult(result, PASSED);
    }

    /**
     * Called when test fails. Reports test as failed.
     *
     * @param result TestNG test result
     */
    @Override
    public void onTestFailure(ITestResult result) {
        handleTestNgResult(result, FAILED);
    }

    /**
     * Called when test is skipped. Reports test as skipped.
     *
     * @param result TestNG test result
     */
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
     * @param method    disabled test method
     * @param testClass class containing the disabled test
     */
    private void reportDisabledTest(Method method, Class<?> testClass) {
        TestNgTestWrapper wrapper = TestNgTestWrapper.forDisabledTest(method, testClass);
        TestMetadata metadata = metaDataExtractor.extractTestMetadata(wrapper);
        logMetadataCreation(metadata);
        reportTestResult(metadata, SKIPPED, DISABLED_MESSAGE, null);

        LOGGER.fine(String.format("Disabled test reported: %s - %s",
                metadata.getTitle(), DISABLED_MESSAGE));
    }
}
