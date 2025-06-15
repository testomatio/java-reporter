package com.core.framework_integration;

import com.annotation.TestId;
import com.annotation.Title;
import com.core.GlobalTestRunManager;
import com.model.TestMetadata;
import com.model.TestResult;
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

import static com.constants.CommonConstants.FAILED;
import static com.constants.CommonConstants.PASSED;
import static com.constants.CommonConstants.SKIPPED;

public class JUnitExtension implements BeforeAllCallback, AfterEachCallback, AfterAllCallback {
    private static final Logger LOGGER = LoggerFactory.getLogger(JUnitExtension.class);
    private final GlobalTestRunManager runManager = GlobalTestRunManager.getInstance();

    @Override
    public void beforeAll(ExtensionContext context) {
        runManager.incrementSuiteCounter();
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
    }

    @Override
    public void afterAll(ExtensionContext context) {
        runManager.decrementSuiteCounter();
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
        return new TestResult(metadata.getTitle(), metadata.getTestId(), metadata.getSuiteTitle(),
                metadata.getFile(), status, message, stack);
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
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }
}