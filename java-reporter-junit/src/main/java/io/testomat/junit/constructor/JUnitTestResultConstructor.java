package io.testomat.junit.constructor;

import io.testomat.core.model.ExceptionDetails;
import io.testomat.core.model.TestResult;
import io.testomat.junit.model.TestResultWrapper;
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

    public TestResult constructTestRunResult(TestResultWrapper wrapper) {
        validateWrapper(wrapper);

        String message;
        String stack;

        if (wrapper.getMessage() != null) {
            log.debug("Creating JUnit test result with custom message: {} - {}",
                    wrapper.getTestMetadata().getTitle(), wrapper.getMessage());
            message = wrapper.getMessage();
            stack = extractStackTrace(wrapper);
        } else {
            log.debug("Creating JUnit test result with exception details for: {}",
                    wrapper.getTestMetadata().getTitle());
            ExceptionDetails details = extractExceptionDetails(wrapper);
            message = details.getMessage();
            stack = details.getStack();
        }

        return createTestResult(wrapper, message, stack);
    }

    /**
     * Validates that wrapper and its metadata are not null.
     */
    private void validateWrapper(TestResultWrapper wrapper) {
        if (wrapper == null) {
            throw new IllegalArgumentException("TestRunResultWrapper cannot be null");
        }
        if (wrapper.getTestMetadata() == null) {
            throw new IllegalArgumentException("TestMetadata cannot be null");
        }
    }

    /**
     * Creates TestResult with provided message and stack trace.
     */
    private TestResult createTestResult(TestResultWrapper wrapper, String message, String stack) {
        var metadata = wrapper.getTestMetadata();
        return TestResult.builder()
                .withTitle(metadata.getTitle())
                .withTestId(metadata.getTestId())
                .withSuiteTitle(metadata.getSuiteTitle())
                .withFile(metadata.getFile())
                .withStatus(wrapper.getStatus())
                .withMessage(message)
                .withStack(stack)
                .build();
    }

    /**
     * Extracts exception details from JUnit extension context.
     */
    private ExceptionDetails extractExceptionDetails(TestResultWrapper wrapper) {
        return Optional.ofNullable(wrapper.getJunitExtensionContext())
                .flatMap(ExtensionContext::getExecutionException)
                .filter(this::isReportableException)
                .map(this::createExceptionDetails)
                .orElse(ExceptionDetails.empty());
    }

    /**
     * Extracts stack trace from JUnit extension context.
     */
    private String extractStackTrace(TestResultWrapper wrapper) {
        return Optional.ofNullable(wrapper.getJunitExtensionContext())
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
