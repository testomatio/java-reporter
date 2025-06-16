package com.testomatio.reporter.core.framework_integration;

import com.testomatio.reporter.annotation.TestId;
import com.testomatio.reporter.annotation.Title;
import com.testomatio.reporter.core.GlobalTestRunManager;
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
     * All public methods here are related to the tests lifecycle by their names
     */
    @Override
    public void beforeAll(ExtensionContext context) {
        runManager.onSuiteStart();
        LOGGER.debug("JUnit test class started: {}", context.getDisplayName());
    }

    @Override
    public void afterEach(ExtensionContext context) {
        if (!runManager.isActive()) {
            return;
        }

        Optional<Method> testMethodOptional = context.getTestMethod();
        if (testMethodOptional.isEmpty()) {
            return;
        }

        Method testMethod = testMethodOptional.get();
        TestMetadata metadata = getTestMetadata(testMethod, context);
        String status = determineTestStatus(context);
        TestResult result = createTestResult(metadata, status, context);

        runManager.reportTest(result);
        LOGGER.debug("Reported JUnit test: {} [{}]", result.getTitle(), status);
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {

    }

    private TestMetadata getTestMetadata(Method testMethod, ExtensionContext context) {
        String title = getTestTitle(testMethod, context);
        String suiteTitle = context.getTestClass().map(Class::getSimpleName).orElse("Unknown");
        String file = suiteTitle + ".java";
        String testId = getTestId(testMethod);
        return new TestMetadata(title, testId, suiteTitle, file);
    }

    private String determineTestStatus(ExtensionContext context) {
        Optional<Throwable> exception = context.getExecutionException();
        if (exception.isPresent()) {
            Throwable t = exception.get();
            return t instanceof TestAbortedException ? SKIPPED : FAILED;
        }
        return PASSED;
    }

    private TestResult createTestResult(TestMetadata metadata, String status, ExtensionContext context) {
        String message = null;
        String stack = null;
        Optional<Throwable> exception = context.getExecutionException();
        if (exception.isPresent() && !(exception.get() instanceof TestAbortedException)) {
            Throwable t = exception.get();
            message = t.getMessage();
            stack = getStackTrace(t);
        }
        return new TestResult(metadata.getTitle(), metadata.getTestId(),
                metadata.getSuiteTitle(), metadata.getFile(), status, message, stack);
    }

    private String getTestTitle(Method testMethod, ExtensionContext context) {
        Title titleAnnotation = testMethod.getAnnotation(Title.class);
        return titleAnnotation != null ? titleAnnotation.value() : context.getDisplayName();
    }

    private String getTestId(Method testMethod) {
        TestId testIdAnnotation = testMethod.getAnnotation(TestId.class);
        return testIdAnnotation != null ? testIdAnnotation.value() : null;
    }

    private String getStackTrace(Throwable t) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }
}