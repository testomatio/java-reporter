package com.testomatio.reporter.core.framework_integration;

import com.testomatio.reporter.core.constructor.JUnitTestResultConstructor;
import com.testomatio.reporter.core.constructor.ResultConstructor;
import com.testomatio.reporter.core.constructor.TestCaseResultWrapper;
import com.testomatio.reporter.core.extractor.JUnitMetaDataExtractor;
import com.testomatio.reporter.core.extractor.wrapper.JUnitTestWrapper;
import com.testomatio.reporter.core.extractor.MetaDataExtractor;
import com.testomatio.reporter.model.TestMetadata;
import java.lang.reflect.Method;
import java.util.Optional;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import static com.testomatio.reporter.constants.CommonConstants.FAILED;
import static com.testomatio.reporter.constants.CommonConstants.PASSED;
import static com.testomatio.reporter.constants.CommonConstants.SKIPPED;

/**
 * JUnit 5 extension for Testomat.io integration.
 * Reports JUnit test execution results to Testomat.io platform.
 * Usage: Add {@code @ExtendWith(JUnitExtension.class)} to test classes.
 */
public class JUnitExtension extends AbstractTestFrameworkListener
        implements BeforeEachCallback, BeforeAllCallback, AfterAllCallback, TestWatcher {

    private final MetaDataExtractor<JUnitTestWrapper> jUnitMetaDataExtractor = new JUnitMetaDataExtractor();

    /**
     * Creates new JUnit extension.
     */
    public JUnitExtension() {
        super();
    }

    @Override
    protected ResultConstructor createResultConstructor() {
        return new JUnitTestResultConstructor();
    }

    @Override
    protected void addFrameworkSpecificData(TestCaseResultWrapper.Builder builder, Object frameworkSpecificData) {
        if (frameworkSpecificData instanceof ExtensionContext) {
            builder.withJUnitExtensionContext((ExtensionContext) frameworkSpecificData);
        }
    }

    /**
     * Called before all tests in a class. Starts test suite tracking.
     *
     * @param context JUnit extension context
     */
    @Override
    public void beforeAll(ExtensionContext context) {
        String className = context.getTestClass().map(Class::getSimpleName).orElse("Unknown");
        handleSuiteStarted(className);
    }

    /**
     * Called after all tests in a class. Finishes test suite tracking.
     *
     * @param context JUnit extension context
     */
    @Override
    public void afterAll(ExtensionContext context) {
        String className = context.getTestClass().map(Class::getSimpleName).orElse("Unknown");
        handleSuiteFinished(className);
    }

    /**
     * Called before each test method execution.
     *
     * @param extensionContext JUnit extension context
     */
    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        LOGGER.finer("Starting test run: " + extensionContext.getDisplayName());
    }

    /**
     * Called when a test is disabled. Reports test as skipped.
     *
     * @param context JUnit extension context
     * @param reason optional reason for disabling the test
     */
    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        String reasonText = reason.orElse("Test disabled");
        LOGGER.fine(String.format("Test disabled: %s - Reason: %s",
                context.getDisplayName(), reasonText));
        handleTestResult(context, SKIPPED, reasonText);
    }

    /**
     * Called when a test completes successfully. Reports test as passed.
     *
     * @param context JUnit extension context
     */
    @Override
    public void testSuccessful(ExtensionContext context) {
        LOGGER.fine("Test passed successfully: " + context.getDisplayName());
        handleTestResult(context, PASSED, null);
    }

    /**
     * Called when a test is aborted. Reports test as skipped.
     *
     * @param context JUnit extension context
     * @param cause exception that caused the abort
     */
    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        LOGGER.fine(String.format("Test aborted: %s - Cause: %s",
                context.getDisplayName(), cause.getMessage()));
        handleTestResult(context, SKIPPED, cause.getMessage());
    }

    /**
     * Called when a test fails. Reports test as failed.
     *
     * @param context JUnit extension context
     * @param cause exception that caused the failure
     */
    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        LOGGER.fine(String.format("Test failed: %s - Cause: %s",
                context.getDisplayName(), cause.getMessage()));
        handleTestResult(context, FAILED, cause.getMessage());
    }

    /**
     * Handles test result from JUnit extension callbacks.
     *
     * @param context JUnit extension context
     * @param status test status (PASSED, FAILED, SKIPPED)
     * @param message optional message describing the result
     */
    private void handleTestResult(ExtensionContext context, String status, String message) {
        Optional<Method> testMethodOptional = context.getTestMethod();
        if (testMethodOptional.isEmpty()) {
            LOGGER.warning("No test method found in context, cannot report test result");
            return;
        }

        Method testMethod = testMethodOptional.get();
        JUnitTestWrapper testWrapper = new JUnitTestWrapper(testMethod, context);
        TestMetadata metadata = jUnitMetaDataExtractor.extractTestMetadata(testWrapper);

        logMetadataCreation(metadata);
        reportTestResult(metadata, status, message, context);
    }
}