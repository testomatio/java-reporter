package com.testomatio.reporter.core.constructor;

import com.testomatio.reporter.logger.LoggerUtils;
import com.testomatio.reporter.model.TestResult;
import java.util.logging.Logger;

/**
 * Base constructor for framework-specific test case results.
 * Uses template method pattern to handle custom messages vs exception details.
 */
public abstract class AbstractTestResultConstructor implements ResultConstructor {
    private static final Logger LOGGER = LoggerUtils.getLogger(AbstractTestResultConstructor.class);

    @Override
    public final TestResult constructTestRunResult(TestResultWrapper holder) {
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
    protected final void validateHolder(TestResultWrapper holder) {
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
    protected final void logTestResultCreation(TestResultWrapper holder,
                                               boolean hasCustomMessage) {
        var testTitle = holder.getTestMetadata().getTitle();
        if (hasCustomMessage) {
            var message = getCustomMessage(holder);
            LOGGER.finer("Creating " + getFrameworkName()
                    + " test result with custom message: "
                    + testTitle + " - " + message);
        } else {
            LOGGER.finer("Creating " + getFrameworkName()
                    + " test result with exception details for: "
                    + testTitle);
        }
    }

    /**
     * Builds test result with basic metadata and status.
     *
     * @param holder wrapper containing test metadata
     * @return test result builder with basic fields populated
     */
    protected final TestResult.Builder buildTestResult(TestResultWrapper holder) {
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
    protected final ExceptionDetails createExceptionDetails(Throwable throwable) {
        var message = throwable.getMessage();
        var stack = getStackTrace(throwable);
        LOGGER.finer("Including error details for failed test");
        return new ExceptionDetails(message, stack);
    }

    /**
     * Checks if wrapper contains custom message for the test result.
     */
    protected abstract boolean hasCustomMessage(TestResultWrapper holder);

    /**
     * Extracts custom message from wrapper.
     */
    protected abstract String getCustomMessage(TestResultWrapper holder);

    /**
     * Creates test result using custom message from wrapper.
     */
    protected abstract TestResult createWithCustomMessage(TestResultWrapper holder);

    /**
     * Creates test result using exception details from framework-specific data.
     */
    protected abstract TestResult createWithExceptionDetails(TestResultWrapper holder);

    /**
     * Returns name of the test framework for logging purposes.
     */
    protected abstract String getFrameworkName();
}
