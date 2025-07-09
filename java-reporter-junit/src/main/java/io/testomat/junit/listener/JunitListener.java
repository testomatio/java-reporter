package io.testomat.junit.listener;

import static io.testomat.core.constants.CommonConstants.FAILED;
import static io.testomat.core.constants.CommonConstants.PASSED;
import static io.testomat.core.constants.CommonConstants.SKIPPED;

import io.testomat.core.exception.ReportTestResultException;
import io.testomat.core.model.TestMetadata;
import io.testomat.core.model.TestResult;
import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.junit.constructor.JUnitTestResultConstructor;
import io.testomat.junit.extractor.JunitMetaDataExtractor;
import io.testomat.junit.model.TestResultWrapper;
import java.util.Optional;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

/**
 * JUnit 5 extension for Testomat.io integration.
 * Reports JUnit test execution results to Testomat.io platform.
 * Usage: Add {@code @ExtendWith(JUnitExtension.class)} to test classes.
 */
public class JunitListener implements BeforeEachCallback, BeforeAllCallback,
        AfterAllCallback, TestWatcher {

    private final GlobalRunManager runManager = GlobalRunManager.getInstance();
    private final JUnitTestResultConstructor resultConstructor = new JUnitTestResultConstructor();
    private final JunitMetaDataExtractor metaDataExtractor = new JunitMetaDataExtractor();

    @Override
    public void beforeAll(ExtensionContext context) {
        handleSuiteStarted();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        handleSuiteFinished();
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        String reasonText = reason.orElse("Test disabled");
        handleTestResult(context, SKIPPED, reasonText);
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        handleTestResult(context, PASSED, null);
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        handleTestResult(context, SKIPPED, cause.getMessage());
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        handleTestResult(context, FAILED, cause.getMessage());
    }

    /**
     * Handles test result from JUnit extension callbacks.
     *
     * @param context JUnit extension context
     * @param status  test status (PASSED, FAILED, SKIPPED)
     * @param message optional message describing the result
     */
    private void handleTestResult(ExtensionContext context, String status, String message) {
        TestMetadata metadata = metaDataExtractor.extractTestMetadata(context);
        reportTestResult(metadata, status, message, context);
    }

    /**
     * Handles test suite start by incrementing suite counter.
     */
    private void handleSuiteStarted() {
        runManager.incrementSuiteCounter();
    }

    /**
     * Handles test suite completion by decrementing suite counter.
     */
    private void handleSuiteFinished() {
        runManager.decrementSuiteCounter();
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
            TestResult result = createTestResult(
                    metadata, status, message, frameworkSpecificData);
            reportResult(result);
        } catch (Exception e) {
            String testName = metadata != null ? metadata.getTitle() : "Unknown Test";
            throw new ReportTestResultException("Failed to report test result for: "
                    + testName, e);
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
        TestResultWrapper holder = buildTestRunResultHolder(metadata,
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
    private TestResultWrapper buildTestRunResultHolder(TestMetadata metadata, String status,
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
     * Adds JUnit-specific data to result wrapper builder.
     *
     * @param builder               result wrapper builder
     * @param frameworkSpecificData framework-specific data to add
     */
    private void addFrameworkSpecificData(TestResultWrapper.Builder builder,
                                          Object frameworkSpecificData) {
        if (frameworkSpecificData instanceof ExtensionContext) {
            builder.withJunitExtensionContext((ExtensionContext) frameworkSpecificData);
        }
    }

    /**
     * Logs and reports test result to run manager.
     *
     * @param result test case result to report
     */
    private void reportResult(TestResult result) {
        runManager.reportTest(result);
    }
}
