package com.testomatio.reporter.core.framework_integration;

import com.testomatio.reporter.annotation.TestId;
import com.testomatio.reporter.annotation.Title;
import com.testomatio.reporter.core.GlobalTestRunManager;
import com.testomatio.reporter.model.TestMetadata;
import com.testomatio.reporter.model.TestResult;
import java.io.PrintWriter;
import java.io.StringWriter;
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
 *
 * Implements JUnit 5 extension callbacks to handle test class lifecycle and individual test results.
 * Supports custom annotations (@Title, @TestId) for enhanced test metadata.
 *
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
            TestMetadata metadata = getTestMetadata(testMethod, context);
            String status = determineTestStatus(context);
            TestResult result = createTestResult(metadata, status, context);

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

    /**
     * Extracts test metadata from a JUnit test method and execution context.
     * Retrieves test title, test ID, suite information, and file path for reporting.
     * Supports custom @Title and @TestId annotations for enhanced metadata.
     *
     * @param testMethod the test method being executed
     * @param context the JUnit extension context containing test execution details
     * @return TestMetadata object containing structured test information
     */
    private TestMetadata getTestMetadata(Method testMethod, ExtensionContext context) {
        String title = getTestTitle(testMethod, context);
        String suiteTitle = context.getTestClass().map(Class::getSimpleName).orElse("Unknown");
        String file = suiteTitle + ".java";
        String testId = getTestId(testMethod);

        LOGGER.debug("Extracted test metadata - Title: {}, ID: {}, Suite: {}, File: {}",
                title, testId, suiteTitle, file);

        return new TestMetadata(title, testId, suiteTitle, file);
    }

    /**
     * Determines the test execution status based on the extension context.
     * Maps JUnit execution results to Testomat.io status values.
     *
     * @param context the JUnit extension context containing execution details
     * @return test status string (PASSED, FAILED, or SKIPPED)
     */
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

    /**
     * Creates a TestResult object from test metadata and execution details.
     * Includes error information (message and stack trace) for failed tests.
     * Excludes error details for skipped tests (TestAbortedException).
     *
     * @param metadata the extracted test metadata
     * @param status the determined test execution status
     * @param context the JUnit extension context containing execution details
     * @return TestResult object ready for API reporting
     */
    private TestResult createTestResult(TestMetadata metadata, String status, ExtensionContext context) {
        String message = null;
        String stack = null;

        Optional<Throwable> exception = context.getExecutionException();
        if (exception.isPresent() && !(exception.get() instanceof TestAbortedException)) {
            Throwable t = exception.get();
            message = t.getMessage();
            stack = getStackTrace(t);
            LOGGER.debug("Including error details for failed test: {}", metadata.getTitle());
        }

        return new TestResult(metadata.getTitle(), metadata.getTestId(), metadata.getSuiteTitle(),
                metadata.getFile(), status, message, stack);
    }

    /**
     * Retrieves the test title from method annotations or JUnit context.
     * Prioritizes custom @Title annotation over the default JUnit display name.
     *
     * @param testMethod the test method to check for annotations
     * @param context the JUnit extension context containing the default display name
     * @return the test title to use for reporting
     */
    private String getTestTitle(Method testMethod, ExtensionContext context) {
        Title titleAnnotation = testMethod.getAnnotation(Title.class);
        String title = titleAnnotation != null ? titleAnnotation.value() : context.getDisplayName();
        LOGGER.debug("Using test title: {} (from {})", title,
                titleAnnotation != null ? "@Title annotation" : "JUnit display name");
        return title;
    }

    /**
     * Retrieves the test ID from method annotations.
     * Returns the value of @TestId annotation if present, null otherwise.
     *
     * @param testMethod the test method to check for @TestId annotation
     * @return the test ID if specified, null if not annotated
     */
    private String getTestId(Method testMethod) {
        TestId testIdAnnotation = testMethod.getAnnotation(TestId.class);
        String testId = testIdAnnotation != null ? testIdAnnotation.value() : null;
        if (testId != null) {
            LOGGER.debug("Found @TestId annotation: {}", testId);
        }
        return testId;
    }

    /**
     * Converts a throwable's stack trace to a string representation.
     * Used for capturing detailed error information for failed tests.
     *
     * @param t the throwable to extract stack trace from
     * @return string representation of the complete stack trace
     */
    private String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        String stackTrace = sw.toString();
        LOGGER.debug("Extracted stack trace for exception: {} (length: {} chars)",
                t.getClass().getSimpleName(), stackTrace.length());
        return stackTrace;
    }
}