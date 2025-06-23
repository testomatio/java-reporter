package com.testomatio.reporter.core.constructor;

import com.testomatio.reporter.logger.LoggerUtils;
import com.testomatio.reporter.model.TestCaseResult;
import java.util.logging.Logger;

public abstract class AbstractTestCaseResultConstructor implements ResultConstructor {
    private static final Logger LOGGER = LoggerUtils.getLogger(AbstractTestCaseResultConstructor.class);

    @Override
    public final TestCaseResult constructTestRunResult(TestCaseResultWrapper holder) {
        validateHolder(holder);

        boolean hasCustomMessage = hasCustomMessage(holder);
        logTestResultCreation(holder, hasCustomMessage);

        return hasCustomMessage
                ? createWithCustomMessage(holder)
                : createWithExceptionDetails(holder);
    }

    protected final void validateHolder(TestCaseResultWrapper holder) {
        if (holder == null) {
            throw new IllegalArgumentException("TestRunResultWrapper cannot be null");
        }
        if (holder.getTestMetadata() == null) {
            throw new IllegalArgumentException("TestMetadata cannot be null");
        }
    }

    protected final void logTestResultCreation(TestCaseResultWrapper holder, boolean hasCustomMessage) {
        var testTitle = holder.getTestMetadata().getTitle();
        if (hasCustomMessage) {
            var message = getCustomMessage(holder);
            LOGGER.finer("Creating " + getFrameworkName() + " test result with custom message: "
                    + testTitle + " - " + message);
        } else {
            LOGGER.finer("Creating " + getFrameworkName() + " test result with exception details for: "
                    + testTitle);
        }
    }

    protected final TestCaseResult.Builder buildTestResult(TestCaseResultWrapper holder) {
        var metadata = holder.getTestMetadata();
        return TestCaseResult.builder()
                .withTitle(metadata.getTitle())
                .withTestId(metadata.getTestId())
                .withSuiteTitle(metadata.getSuiteTitle())
                .withFile(metadata.getFile())
                .withStatus(holder.getStatus());
    }

    protected final ExceptionDetails createExceptionDetails(Throwable throwable) {
        var message = throwable.getMessage();
        var stack = getStackTrace(throwable);
        LOGGER.finer("Including error details for failed test");
        return new ExceptionDetails(message, stack);
    }

    protected abstract boolean hasCustomMessage(TestCaseResultWrapper holder);
    protected abstract String getCustomMessage(TestCaseResultWrapper holder);
    protected abstract TestCaseResult createWithCustomMessage(TestCaseResultWrapper holder);
    protected abstract TestCaseResult createWithExceptionDetails(TestCaseResultWrapper holder);
    protected abstract String getFrameworkName();
}