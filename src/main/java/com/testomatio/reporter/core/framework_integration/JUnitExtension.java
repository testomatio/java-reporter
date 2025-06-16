package com.testomatio.reporter.core.framework_integration;

import com.testomatio.reporter.core.GlobalTestRunManager;
import com.testomatio.reporter.core.util.TestResultConstructorUtil;
import com.testomatio.reporter.core.util.TestMetaDataExtractorUtil;
import com.testomatio.reporter.model.TestMetadata;
import com.testomatio.reporter.model.TestResult;
import java.lang.reflect.Method;
import java.util.Optional;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.opentest4j.TestAbortedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.testomatio.reporter.constants.CommonConstants.FAILED;
import static com.testomatio.reporter.constants.CommonConstants.PASSED;
import static com.testomatio.reporter.constants.CommonConstants.SKIPPED;

/**
 * JUnit 5 Extension that integrates test execution with Testomat.io reporting system.
 * This extension automatically captures test results and forwards them to the GlobalTestRunManager
 * for batch reporting to the Testomat.io API.
 * <p>
 * Implements JUnit 5 extension callbacks to handle test class lifecycle and individual test results.
 * Supports custom annotations (@Title, @TestId) for enhanced test metadata.
 * <p>
 * To use this extension, add @ExtendWith(JUnitExtension.class) to your test classes.
 */
public class JUnitExtension implements BeforeAllCallback, AfterEachCallback, AfterAllCallback {
    private static final Logger LOGGER = LoggerFactory.getLogger(JUnitExtension.class);
    private final GlobalTestRunManager runManager = GlobalTestRunManager.getInstance();

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
     * Called after each individual test method execution.
     * Extracts test metadata, determines test status, and reports the result
     * to the global test run manager for batch processing.
     *
     * @param context the current extension context containing test method execution details
     */
    @Override
    public void afterEach(ExtensionContext context) {
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
            TestMetadata metadata = TestMetaDataExtractorUtil.extractTestMetadata(testMethod, context);
            String status = determineTestStatus(context);
            TestResult result = TestResultConstructorUtil.createJUnitTestResult(metadata, status, context);

            LOGGER.debug("Reporting test result: {} - {} ({})",
                    metadata.getTitle(), status, metadata.getTestId());
            runManager.reportTest(result);
            LOGGER.debug("Test result reported successfully: {}", metadata.getTitle());
        } catch (Exception e) {
            LOGGER.error("Failed to report test result for context: {}", context.getDisplayName(), e);
        }
    }

    /**
     * Called after all tests in a test class have been executed.
     * Decrements the active suite counter in the global test run manager.
     * When all test classes are finished, this triggers the test run finalization.
     *
     * @param context the current extension context containing test class information
     */
    @Override
    public void afterAll(ExtensionContext context) {
        String className = context.getTestClass().map(Class::getSimpleName).orElse("Unknown");
        LOGGER.debug("Finishing test class: {}", className);
        runManager.decrementSuiteCounter();
        LOGGER.debug("Active suite count decremented for class: {}", className);
    }

    private String determineTestStatus(ExtensionContext context) {
        Optional<Throwable> exception = context.getExecutionException();
        if (exception.isPresent()) {
            Throwable t = exception.get();
            String status = t instanceof TestAbortedException ? SKIPPED : FAILED;
            LOGGER.debug("Test failed with status: {} - Exception: {}", status, t.getClass().getSimpleName());
            return status;
        }
        LOGGER.debug("Test passed successfully");
        return PASSED;
    }
}