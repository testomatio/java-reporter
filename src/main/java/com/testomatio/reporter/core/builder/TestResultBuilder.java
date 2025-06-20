package com.testomatio.reporter.core.builder;

import com.testomatio.reporter.model.TestMetadata;
import com.testomatio.reporter.model.TestRunResult;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

import static com.testomatio.reporter.logger.LoggerUtils.getLogger;

/**
 * Service class responsible for building TestRunResult objects.
 * Replaces static utility methods with instance-based approach for better testability
 * and dependency injection support.
 */
public class TestResultBuilder {
    private static final Logger LOGGER = getLogger(TestResultBuilder.class);

    /**
     * Builds a test result with throwable information.
     */
    public TestRunResult buildWithThrowable(TestMetadata metadata, String status, Throwable throwable) {
        String message = null;
        String stack = null;

        if (throwable != null) {
            message = throwable.getMessage();
            stack = getStackTrace(throwable);
            LOGGER.finer("Including error details for test: " + metadata.getTitle());
        }

        return new TestRunResult(
                metadata.getTitle(),
                metadata.getTestId(),
                metadata.getSuiteTitle(),
                metadata.getFile(),
                status,
                message,
                stack
        );
    }

    /**
     * Builds a test result with custom message.
     */
    public TestRunResult buildWithMessage(TestMetadata metadata, String status, String message) {
        LOGGER.finer(String.format("Creating test result with custom message: %s - %s",
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

    private String getStackTrace(Throwable throwable) {
        if (throwable == null) {
            return null;
        }

        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            return sw.toString();
        } catch (Exception e) {
            return "Could not get stack trace: " + throwable.getClass().getName();
        }
    }
}