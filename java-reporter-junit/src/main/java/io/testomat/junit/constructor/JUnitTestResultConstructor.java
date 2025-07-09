package io.testomat.junit.constructor;

import io.testomat.core.model.TestResult;
import io.testomat.junit.model.ExceptionDetails;
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

    public TestResult constructTestRunResult(TestResultWrapper holder) {
        validateHolder(holder);

        boolean hasCustomMessage = hasCustomMessage(holder);
        logTestResultCreation(holder, hasCustomMessage);

        return hasCustomMessage
                ? createWithCustomMessage(holder)
                : createWithExceptionDetails(holder);
    }

    /**
     * Validates that wrapper and its metadata are not null.
     *
     * @param holder wrapper to validate
     * @throws IllegalArgumentException if wrapper or metadata is null
     */
    private void validateHolder(TestResultWrapper holder) {
        if (holder == null) {
            throw new IllegalArgumentException("TestRunResultWrapper cannot be null");
        }
        if (holder.getTestMetadata() == null) {
            throw new IllegalArgumentException("TestMetadata cannot be null");
        }
    }

    /**
     * Logs test result creation details for debugging.
     */
    private void logTestResultCreation(TestResultWrapper holder, boolean hasCustomMessage) {
        var testTitle = holder.getTestMetadata().getTitle();
        if (hasCustomMessage) {
            var message = getCustomMessage(holder);
            log.debug("Creating JUnit test result with custom message: {} - {}",
                    testTitle, message);
        } else {
            log.debug("Creating JUnit test result with exception details for: {}",
                    testTitle);
        }
    }

    /**
     * Builds test result with basic metadata and status.
     *
     * @param holder wrapper containing test metadata
     * @return test result builder with basic fields populated
     */
    private TestResult.Builder buildTestResult(TestResultWrapper holder) {
        var metadata = holder.getTestMetadata();
        return TestResult.builder()
                .withTitle(metadata.getTitle())
                .withTestId(metadata.getTestId())
                .withSuiteTitle(metadata.getSuiteTitle())
                .withFile(metadata.getFile())
                .withStatus(holder.getStatus());
    }

    /**
     * Creates exception details with message and stack trace.
     *
     * @param throwable exception to extract details from
     * @return exception details object
     */
    private ExceptionDetails createExceptionDetails(Throwable throwable) {
        var message = throwable.getMessage();
        var stack = getStackTrace(throwable);
        log.debug("Including error details for failed test");
        return new ExceptionDetails(message, stack);
    }

    /**
     * Checks if wrapper contains custom message for the test result.
     */
    private boolean hasCustomMessage(TestResultWrapper holder) {
        return holder.getMessage() != null;
    }

    /**
     * Extracts custom message from wrapper.
     */
    private String getCustomMessage(TestResultWrapper holder) {
        return holder.getMessage();
    }

    /**
     * Creates test result using custom message from wrapper.
     */
    private TestResult createWithCustomMessage(TestResultWrapper holder) {
        var stack = extractStackTrace(holder);

        return buildTestResult(holder)
                .withMessage(holder.getMessage())
                .withStack(stack)
                .build();
    }

    /**
     * Creates test result using exception details from framework-specific data.
     */
    private TestResult createWithExceptionDetails(TestResultWrapper holder) {
        var exceptionDetails = extractExceptionDetails(holder);

        return buildTestResult(holder)
                .withMessage(exceptionDetails.getMessage())
                .withStack(exceptionDetails.getStack())
                .build();
    }

    /**
     * Extracts exception details from JUnit extension context.
     */
    private ExceptionDetails extractExceptionDetails(TestResultWrapper holder) {
        return Optional.ofNullable(holder.getJunitExtensionContext())
                .flatMap(ExtensionContext::getExecutionException)
                .filter(this::isReportableException)
                .map(this::createExceptionDetails)
                .orElse(ExceptionDetails.empty());
    }

    /**
     * Converts throwable to stack trace string.
     *
     * @param t throwable to convert
     * @return stack trace as string
     */
    private String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Extracts stack trace from JUnit extension context.
     *
     * @param holder wrapper containing JUnit extension context
     * @return stack trace string or null if no reportable exception
     */
    private String extractStackTrace(TestResultWrapper holder) {
        return Optional.ofNullable(holder.getJunitExtensionContext())
                .flatMap(ExtensionContext::getExecutionException)
                .filter(this::isReportableException)
                .map(this::getStackTrace)
                .orElse(null);
    }

    /**
     * Checks if exception should be reported (excludes test aborted exceptions).
     *
     * @param throwable exception to check
     * @return true if exception should be reported
     */
    private boolean isReportableException(Throwable throwable) {
        return !(throwable instanceof TestAbortedException);
    }
}
