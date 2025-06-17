package com.testomatio.reporter.core.util;

import com.testomatio.reporter.model.TestMetadata;
import com.testomatio.reporter.model.TestRunResult;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.opentest4j.TestAbortedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;

public class TestRunResultConstructorUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestRunResultConstructorUtil.class);

    /**
     * Creates a TestResult object from test metadata and execution details.
     * Includes error information (message and stack trace) for failed tests.
     * Excludes error details for skipped tests (TestAbortedException).
     *
     * @param metadata the extracted test metadata
     * @param status   the determined test execution status
     * @param context  the JUnit extension context containing execution details
     * @return TestResult object ready for API reporting
     */
    public static TestRunResult createJUnitTestResult(TestMetadata metadata, String status, ExtensionContext context) {
        String message = null;
        String stack = null;

        Optional<Throwable> exception = context.getExecutionException();
        if (exception.isPresent() && !(exception.get() instanceof TestAbortedException)) {
            Throwable t = exception.get();
            message = t.getMessage();
            stack = getStackTrace(t);
            LOGGER.debug("Including error details for failed test: {}", metadata.getTitle());
        }

        return new TestRunResult(metadata.getTitle(), metadata.getTestId(), metadata.getSuiteTitle(),
                metadata.getFile(), status, message, stack);
    }

    public static TestRunResult createTestNGTestResult(TestMetadata metadata, String status, ITestResult result) {
        String message = null;
        String stack = null;

        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            message = throwable.getMessage();
            stack = getStackTrace(throwable);
        }

        return new TestRunResult(
                metadata.getTitle(),
                metadata.getTestId(),
                metadata.getSuiteTitle(),
                metadata.getFile(),
                status, message, stack
        );
    }

    private static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }
}
