package io.testomat.testng.constructor;

import io.testomat.core.model.ExceptionDetails;
import io.testomat.core.model.TestResult;
import io.testomat.core.step.StepLifecycle;
import io.testomat.core.step.StepStorage;
import io.testomat.core.step.TestStep;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;
import org.testng.ITestResult;

/**
 * Constructs test case results from TestNG test results.
 * Supports custom messages/reasons and extracts exception details from test results.
 * Enhanced to support parameterized tests with example data and RID.
 */
public class TestNgTestResultConstructor {

    /**
     * Constructs test case result from wrapper containing framework-specific data.
     *
     * @param wrapper wrapper containing test metadata and framework data
     * @return constructed test case result
     */
    public TestResult constructTestRunResult(TestResultWrapper wrapper) {
        validateWrapper(wrapper);

        String message;
        String stack;

        if (wrapper.getReason() != null || wrapper.getMessage() != null) {
            message = Optional.ofNullable(wrapper.getReason()).orElse(wrapper.getMessage());
            stack = null;
        } else {
            ExceptionDetails details = extractExceptionDetails(wrapper);
            message = details.getMessage();
            stack = details.getStack();
        }

        return buildTestResult(wrapper, message, stack);
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
     * Builds test result with provided message and stack trace.
     * Enhanced to include parameterized test data (example and RID) and test steps.
     */
    private TestResult buildTestResult(TestResultWrapper wrapper, String message, String stack) {
        var metadata = wrapper.getTestMetadata();

        // Collect steps from ThreadLocal storage
        List<TestStep> steps = StepStorage.getSteps();

        TestResult.Builder builder = TestResult.builder()
                .withTitle(metadata.getTitle())
                .withTestId(metadata.getTestId())
                .withSuiteTitle(metadata.getSuiteTitle())
                .withFile(metadata.getFile())
                .withStatus(wrapper.getStatus())
                .withMessage(message)
                .withStack(stack)
                .withExample(wrapper.getExample())
                .withRid(wrapper.getRid());

        if (!steps.isEmpty()) {
            builder.withSteps(steps);
        }

        // Clear steps after collecting them
        StepStorage.clear();
        StepLifecycle.reset();

        return builder.build();
    }

    /**
     * Extracts exception details from TestNG test result.
     */
    private ExceptionDetails extractExceptionDetails(TestResultWrapper wrapper) {
        return Optional.ofNullable(wrapper.getTestResult())
                .map(ITestResult::getThrowable)
                .map(this::createExceptionDetails)
                .orElse(ExceptionDetails.empty());
    }

    /**
     * Creates exception details with message and stack trace.
     */
    private ExceptionDetails createExceptionDetails(Throwable throwable) {
        String message = throwable.getMessage();
        String stack = getStackTrace(throwable);
        return new ExceptionDetails(message, stack);
    }

    /**
     * Converts throwable to stack trace string.
     */
    public String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}
