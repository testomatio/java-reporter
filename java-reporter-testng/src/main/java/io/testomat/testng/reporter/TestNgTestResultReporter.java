package io.testomat.testng.reporter;

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
import org.testng.ISuite;
import org.testng.ITestResult;
import org.testng.annotations.Test;

public class TestNgTestResultReporter {
    private static final String DISABLED_MESSAGE = "Test disabled via @Test(enabled = false)";

    private final TestNgTestResultConstructor resultConstructor;
    private final TestNgMetaDataExtractor metaDataExtractor;
    private final GlobalRunManager runManager;
    private final Set<String> processedTests;

    public TestNgTestResultReporter() {
        this.resultConstructor = new TestNgTestResultConstructor();
        this.metaDataExtractor = new TestNgMetaDataExtractor();
        this.runManager = GlobalRunManager.getInstance();
        this.processedTests = new HashSet<>();
    }

    /**
     * Constructor for testing
     */
    public TestNgTestResultReporter(TestNgTestResultConstructor resultConstructor,
                                    TestNgMetaDataExtractor metaDataExtractor,
                                    GlobalRunManager runManager) {
        this.resultConstructor = resultConstructor;
        this.metaDataExtractor = metaDataExtractor;
        this.runManager = runManager;
        this.processedTests = new HashSet<>();
    }

    /**
     * Reports test result for regular TestNG test execution.
     */
    public void reportTestResult(ITestResult result, String status) {
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
     * Reports disabled tests from TestNG suite.
     */
    public void reportTestResult(ISuite suite) {
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

    /**
     * Reports disabled test method with SKIPPED status.
     */
    private void reportDisabledTest(Method method, Class<?> testClass) {
        TestNgTestWrapper wrapper = TestNgTestWrapper.forDisabledTest(method, testClass);
        TestMetadata metadata = metaDataExtractor.extractTestMetadata(wrapper);
        reportTestResult(metadata, SKIPPED, DISABLED_MESSAGE, null);
    }
}
