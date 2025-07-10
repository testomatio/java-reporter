package io.testomat.testng.constructor;

import io.testomat.core.model.ExceptionDetails;
import io.testomat.core.model.TestResult;
import java.util.Optional;
import org.testng.ITestResult;

/**
 * Constructs test case results from TestNG test results.
 * Supports custom messages/reasons and extracts exception details from test results.
 */
public class TestNgTestResultConstructor
        implements ResultConstructor {

    @Override
    public final TestResult constructTestRunResult(TestResultWrapper holder) {
        validateHolder(holder);

        return hasCustomMessage(holder)
                ? createWithCustomMessage(holder)
                : createWithExceptionDetails(holder);
    }

    private boolean hasCustomMessage(TestResultWrapper holder) {
        return holder.getReason() != null
                || holder.getMessage() != null;
    }

    private String getCustomMessage(TestResultWrapper holder) {
        return Optional.ofNullable(holder.getReason()).orElse(holder.getMessage());
    }

    private TestResult createWithCustomMessage(TestResultWrapper holder) {
        var message = getCustomMessage(holder);

        return buildTestResult(holder)
                .withMessage(message)
                .withStack(null)
                .build();
    }

    private TestResult createWithExceptionDetails(TestResultWrapper holder) {
        var exceptionDetails = extractExceptionDetails(holder);

        return buildTestResult(holder)
                .withMessage(exceptionDetails.getMessage())
                .withStack(exceptionDetails.getStack())
                .build();
    }

    /**
     * Extracts exception details from TestNG test result.
     */
    private ExceptionDetails extractExceptionDetails(TestResultWrapper holder) {
        return Optional.ofNullable(holder.getTestResult())
                .map(ITestResult::getThrowable)
                .map(this::createExceptionDetails)
                .orElse(ExceptionDetails.empty());
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

    private ExceptionDetails createExceptionDetails(Throwable throwable) {
        var message = throwable.getMessage();
        var stack = getStackTrace(throwable);
        return new ExceptionDetails(message, stack);
    }

}
