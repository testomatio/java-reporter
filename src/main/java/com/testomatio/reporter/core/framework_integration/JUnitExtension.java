package com.testomatio.reporter.core.framework_integration;

import com.testomatio.reporter.core.constructor.JUnitTestCaseResultConstructor;
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
 * JUnit 5 extension that integrates test execution with Testomat.io reporting system.
 * Extends AbstractTestFrameworkListener to leverage common reporting functionality.
 * Implements JUnit 5 extension callbacks to handle test class lifecycle and individual test results.
 * To use this extension, add @ExtendWith(JUnitExtension.class) to your test classes.
 */
public class JUnitExtension extends AbstractTestFrameworkListener
        implements BeforeEachCallback, BeforeAllCallback, AfterAllCallback, TestWatcher {

    private final MetaDataExtractor<JUnitTestWrapper> jUnitMetaDataExtractor = new JUnitMetaDataExtractor();

    public JUnitExtension() {
        super();
    }

    @Override
    protected ResultConstructor createResultConstructor() {
        return new JUnitTestCaseResultConstructor();
    }

    @Override
    protected void addFrameworkSpecificData(TestCaseResultWrapper.Builder builder, Object frameworkSpecificData) {
        if (frameworkSpecificData instanceof ExtensionContext) {
            builder.withJUnitExtensionContext((ExtensionContext) frameworkSpecificData);
        }
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        String className = context.getTestClass().map(Class::getSimpleName).orElse("Unknown");
        handleSuiteStarted(className);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        String className = context.getTestClass().map(Class::getSimpleName).orElse("Unknown");
        handleSuiteFinished(className);
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        LOGGER.finer("Starting test run: " + extensionContext.getDisplayName());
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        String reasonText = reason.orElse("Test disabled");
        LOGGER.fine(String.format("Test disabled: %s - Reason: %s",
                context.getDisplayName(), reasonText));
        handleTestResult(context, SKIPPED, reasonText);
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        LOGGER.fine("Test passed successfully: " + context.getDisplayName());
        handleTestResult(context, PASSED, null);
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        LOGGER.fine(String.format("Test aborted: %s - Cause: %s",
                context.getDisplayName(), cause.getMessage()));
        handleTestResult(context, SKIPPED, cause.getMessage());
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        LOGGER.fine(String.format("Test failed: %s - Cause: %s",
                context.getDisplayName(), cause.getMessage()));
        handleTestResult(context, FAILED, cause.getMessage());
    }

    /**
     * Common method for handling test results from JUnit extension callbacks.
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