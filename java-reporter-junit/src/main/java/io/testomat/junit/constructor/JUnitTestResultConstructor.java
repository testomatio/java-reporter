package io.testomat.junit.constructor;

import io.testomat.core.model.ExceptionDetails;
import io.testomat.core.model.TestMetadata;
import io.testomat.core.model.TestResult;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.opentest4j.TestAbortedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Constructs test case results from JUnit 5 extension contexts.
 * Supports custom messages and extracts exception details from execution context.
 */
public class JUnitTestResultConstructor {
    private static final Logger log = LoggerFactory.getLogger(JUnitTestResultConstructor.class);

    public TestResult constructTestRunResult(TestMetadata metadata,
                                             String message,
                                             String status,
                                             ExtensionContext context) {
        String stack;

        if (message != null) {
            log.debug("Creating JUnit test result with custom message: {} - {}",
                    metadata.getTitle(), message);
            stack = extractStackTrace(context);
        } else {
            log.debug("Creating JUnit test result with exception details for: {}",
                    metadata.getTitle());
            ExceptionDetails details = extractExceptionDetails(context);
            message = details.getMessage();
            stack = details.getStack();
        }

        return createTestResult(metadata, message, status, stack);
    }

    /**
     * Creates TestResult with provided message and stack trace.
     */
    private TestResult createTestResult(TestMetadata metadata,
                                        String message,
                                        String status,
                                        String stack) {
        return TestResult.builder()
                .withSuiteTitle(metadata.getSuiteTitle())
                .withTestId(metadata.getTestId())
                .withTitle(metadata.getTitle())
                .withFile(metadata.getFile())
                .withMessage(message)
                .withStatus(status)
                .withStack(stack)
                .build();
    }

    /**
     * Extracts exception details from JUnit extension context.
     */
    private ExceptionDetails extractExceptionDetails(ExtensionContext context) {
        return Optional.ofNullable(context)
                .flatMap(ExtensionContext::getExecutionException)
                .filter(this::isReportableException)
                .map(this::createExceptionDetails)
                .orElse(ExceptionDetails.empty());
    }

    /**
     * Extracts stack trace from JUnit extension context.
     */
    private String extractStackTrace(ExtensionContext context) {
        return Optional.ofNullable(context)
                .flatMap(ExtensionContext::getExecutionException)
                .filter(this::isReportableException)
                .map(this::getStackTrace)
                .orElse(null);
    }

    /**
     * Creates exception details with message and stack trace.
     */
    private ExceptionDetails createExceptionDetails(Throwable throwable) {
        String message = throwable.getMessage();
        String stack = getStackTrace(throwable);
        log.debug("Including error details for failed test");
        return new ExceptionDetails(message, stack);
    }

    /**
     * Converts throwable to stack trace string.
     */
    private String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Checks if exception should be reported (excludes test aborted exceptions).
     */
    private boolean isReportableException(Throwable throwable) {
        return !(throwable instanceof TestAbortedException);
    }
}
