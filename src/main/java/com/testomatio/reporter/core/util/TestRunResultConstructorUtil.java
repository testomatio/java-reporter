package com.testomatio.reporter.core.util;

import com.testomatio.reporter.model.TestMetadata;
import com.testomatio.reporter.model.TestRunResult;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.logging.Logger;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.opentest4j.TestAbortedException;
import org.testng.ITestResult;

import static com.testomatio.reporter.logger.LoggerUtils.getLogger;

public class TestRunResultConstructorUtil {
    private static final Logger LOGGER = getLogger(TestRunResultConstructorUtil.class);

    /**
     * Creates a TestResult object from test metadata and execution details.
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
            LOGGER.finer("Including error details for failed test: " + metadata.getTitle());
        }

        return new TestRunResult(metadata.getTitle(), metadata.getTestId(), metadata.getSuiteTitle(),
                metadata.getFile(), status, message, stack);
    }

    /**
     * Creates a TestResult object for JUnit tests with custom message (e.g., disabled tests).
     *
     * @param metadata the extracted test metadata
     * @param status   the test execution status
     * @param message  the custom message (e.g., disabled reason, abort reason)
     * @return TestResult object ready for API reporting
     */
    public static TestRunResult createJUnitTestResultWithMessage(TestMetadata metadata, String status, String message) {
        LOGGER.finer(String.format("Creating JUnit test result with custom message: %s - %s",
                metadata.getTitle(), message));

        return new TestRunResult(
                metadata.getTitle(),
                metadata.getTestId(),
                metadata.getSuiteTitle(),
                metadata.getFile(),
                status,
                message,
                null
        );
    }

    /**
     * Creates a TestResult object from TestNG test result.
     *
     * @param metadata the extracted test metadata
     * @param status   the test execution status
     * @param result   the TestNG test result containing execution details
     * @return TestResult object ready for API reporting
     */
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

    /**
     * Creates a TestResult object for TestNG disabled tests.
     *
     * @param metadata the extracted test metadata
     * @param status   the test execution status (should be SKIPPED)
     * @param reason   the reason why the test was disabled
     * @return TestResult object ready for API reporting
     */
    public static TestRunResult createTestNGDisabledTestResult(TestMetadata metadata, String status, String reason) {
        LOGGER.finer(String.format("Creating TestNG disabled test result: %s - %s", metadata.getTitle(), reason));

        return new TestRunResult(
                metadata.getTitle(),
                metadata.getTestId(),
                metadata.getSuiteTitle(),
                metadata.getFile(),
                status,
                reason,
                null
        );
    }

    private static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }
}
