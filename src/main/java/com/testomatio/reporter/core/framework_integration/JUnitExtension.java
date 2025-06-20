package com.testomatio.reporter.core.framework_integration;

import com.testomatio.reporter.model.TestMetadata;
import java.lang.reflect.Method;
import java.util.Optional;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.opentest4j.TestAbortedException;

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
public class JUnitExtension extends BaseTestReporter implements BeforeEachCallback,
        BeforeAllCallback,
        AfterAllCallback,
        TestWatcher {

    @Override
    public void beforeAll(ExtensionContext context) {
        String className = context.getTestClass().map(Class::getSimpleName).orElse("Unknown");
        handleSuiteStart(className);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        String className = context.getTestClass().map(Class::getSimpleName).orElse("Unknown");
        handleSuiteFinish(className);
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
        processTestResult(context, SKIPPED, reasonText);
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        LOGGER.fine("Test passed successfully: " + context.getDisplayName());
        processTestResult(context, PASSED, null);
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        LOGGER.fine(String.format("Test aborted: %s - Cause: %s",
                context.getDisplayName(), cause.getMessage()));
        processTestResult(context, SKIPPED, cause.getMessage());
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        LOGGER.fine(String.format("Test failed: %s - Cause: %s",
                context.getDisplayName(), cause.getMessage()));
        processTestResult(context, FAILED, null);
    }

    private void processTestResult(ExtensionContext context, String status, String customMessage) {
        Optional<Method> testMethodOptional = context.getTestMethod();
        if (testMethodOptional.isEmpty()) {
            LOGGER.warning("No test method found in context, cannot report test result");
            return;
        }

        Method testMethod = testMethodOptional.get();
        TestMetadata metadata = metadataExtractor.extractFromJUnit(testMethod, context);

        if (customMessage != null) {
            reportTest(context.getDisplayName(), metadata, status, customMessage);
        } else {
            Optional<Throwable> exception = context.getExecutionException();
            Throwable throwable = exception.filter(t -> !(t instanceof TestAbortedException)).orElse(null);
            reportTest(context.getDisplayName(), metadata, status, throwable);
        }
    }
}