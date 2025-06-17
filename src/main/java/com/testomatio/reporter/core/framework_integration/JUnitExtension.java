package com.testomatio.reporter.core.framework_integration;

import com.testomatio.reporter.core.GlobalRunManager;
import com.testomatio.reporter.core.util.TestRunMetaDataExtractorUtil;
import com.testomatio.reporter.core.util.TestRunResultConstructorUtil;
import com.testomatio.reporter.model.TestMetadata;
import com.testomatio.reporter.model.TestRunResult;
import java.lang.reflect.Method;
import java.util.Optional;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.testomatio.reporter.constants.CommonConstants.FAILED;
import static com.testomatio.reporter.constants.CommonConstants.PASSED;
import static com.testomatio.reporter.constants.CommonConstants.SKIPPED;

/**
 * Core class for integration with JUnit.
 * This extension automatically captures test results and forwards them to the GlobalTestRunManager
 * for batch reporting to the Testomat.io API.
 * Implements JUnit 5 extension callbacks to handle test class lifecycle and individual test results.
 * To use this extension, add @ExtendWith(JUnitExtension.class) to your test classes.
 */
public class JUnitExtension implements BeforeAllCallback, AfterAllCallback, TestWatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(JUnitExtension.class);
    private final GlobalRunManager runManager = GlobalRunManager.getInstance();

    /**
     * Called before all tests in a test class are executed.
     * Increments the active suite counter in the global test run manager to track
     * how many test classes are currently running.
     *
     * @param context the current extension context containing test class information
     */
    @Override
    public void beforeAll(ExtensionContext context) {
        String className = context.getTestClass().map(Class::getSimpleName).orElse("Unknown");
        LOGGER.debug("Starting test class: {}", className);
        runManager.incrementSuiteCounter();
        LOGGER.debug("Active suite count incremented for class: {}", className);
    }

    /**
     * Called after all tests in a test class have been executed.
     * @param context the current extension context containing test class information
     */
    @Override
    public void afterAll(ExtensionContext context) {
        String className = context.getTestClass().map(Class::getSimpleName).orElse("Unknown");
        LOGGER.debug("Finishing test class: {}", className);
        runManager.decrementSuiteCounter();
        LOGGER.debug("Active suite count decremented for class: {}", className);
    }

    /**
     * @param context the current extension context containing test method information
     * @param reason  the optional reason why the test was disabled
     */
    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        String reasonText = reason.orElse("Test disabled");
        LOGGER.debug("Test disabled: {} - Reason: {}", context.getDisplayName(), reasonText);
        reportTestResult(context, SKIPPED, reasonText);
    }

    /**
     * @param context the current extension context containing test method information
     */
    @Override
    public void testSuccessful(ExtensionContext context) {
        LOGGER.debug("Test passed successfully: {}", context.getDisplayName());
        reportTestResult(context, PASSED, null);
    }

    /**
     * @param context the current extension context containing test method information
     * @param cause   the throwable that caused the test to be aborted
     */
    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        LOGGER.debug("Test aborted: {} - Cause: {}", context.getDisplayName(), cause.getMessage());
        reportTestResult(context, SKIPPED, cause.getMessage());
    }

    /**
     * @param context the current extension context containing test method information
     * @param cause   the throwable that caused the test to fail
     */
    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        LOGGER.debug("Test failed: {} - Cause: {}", context.getDisplayName(), cause.getMessage());
        reportTestResult(context, FAILED, null);
    }

    /**
     * Common method for reporting test results to the global test run manager.
     * Validates preconditions and delegates actual processing to helper methods.
     *
     * @param context the current extension context containing test method execution details
     * @param status  the test execution status (PASSED, FAILED, SKIPPED)
     * @param message optional custom message (null for normal execution, specific message for disabled/aborted tests)
     */
    private void reportTestResult(ExtensionContext context, String status, String message) {
        if (!runManager.isActive()) {
            LOGGER.debug("Test run manager is not active, skipping test result reporting");
            return;
        }

        Optional<Method> testMethodOptional = context.getTestMethod();
        if (testMethodOptional.isEmpty()) {
            LOGGER.warn("No test method found in context, cannot report test result");
            return;
        }

        try {
            Method testMethod = testMethodOptional.get();
            TestMetadata metadata = TestRunMetaDataExtractorUtil.extractTestMetadata(testMethod, context);
            TestRunResult result = createTestResult(metadata, status, message, context);
            logAndReportResult(result, status, message);
        } catch (Exception e) {
            LOGGER.error("Failed to report test result for context: {}", context.getDisplayName(), e);
        }
    }

    /**
     * Creates a TestRunResult object using the appropriate constructor based on message presence.
     *
     * @param metadata the extracted test metadata
     * @param status   the test execution status
     * @param message  optional custom message (null for normal execution)
     * @param context  the extension context (used for normal execution)
     * @return TestRunResult object ready for reporting
     */
    private TestRunResult createTestResult(TestMetadata metadata, String status, String message, ExtensionContext context) {
        return message != null
                ? TestRunResultConstructorUtil.createJUnitTestResultWithMessage(metadata, status, message)
                : TestRunResultConstructorUtil.createJUnitTestResult(metadata, status, context);
    }

    /**
     * Logs and reports the test result to the run manager.
     *
     * @param result  the test result to report
     * @param status  the test execution status (for logging)
     * @param message optional custom message (for logging)
     */
    private void logAndReportResult(TestRunResult result, String status, String message) {
        if (message != null) {
            LOGGER.debug("Reporting test result with message: {} - {} - {} ({})",
                    result.getTitle(), status, message, result.getTestId());
        } else {
            LOGGER.debug("Reporting test result: {} - {} ({})",
                    result.getTitle(), status, result.getTestId());
        }

        runManager.reportTest(result);
        LOGGER.debug("Test result reported successfully: {}", result.getTitle());
    }
}