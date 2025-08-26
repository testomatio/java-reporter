package io.testomat.cucumber.constructor;

import io.testomat.core.model.ExceptionDetails;
import io.testomat.core.model.TestResult;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Constructs test case results from Cucumber test case finished events.
 * Extracts exception details from Cucumber result errors when available.
 */
public class CucumberTestResultConstructor {
    private static final Logger log = LoggerFactory.getLogger(CucumberTestResultConstructor.class);

    public TestResult constructTestRunResult(TestResultWrapper holder) {
        validateHolder(holder);

        log.debug("Creating Cucumber test result with exception details for: {}",
                holder.getTestMetadata().getTitle());

        ExceptionDetails exceptionDetails = extractExceptionDetails(holder);

        return TestResult.builder()
                .withTitle(holder.getTestMetadata().getTitle())
                .withTestId(holder.getTestMetadata().getTestId())
                .withSuiteTitle(holder.getTestMetadata().getSuiteTitle())
                .withFile(holder.getTestMetadata().getFile())
                .withStatus(holder.getStatus())
                .withMessage(exceptionDetails.getMessage())
                .withStack(exceptionDetails.getStack())
                .withRid(holder.getCucumberTestCaseFinished().getTestCase().getId().toString())
                .withExample(holder.getExample())
                .build();
    }

    private void validateHolder(TestResultWrapper holder) {
        if (holder == null) {
            throw new IllegalArgumentException("TestRunResultWrapper cannot be null");
        }
        if (holder.getTestMetadata() == null) {
            throw new IllegalArgumentException("TestMetadata cannot be null");
        }
    }

    private ExceptionDetails extractExceptionDetails(TestResultWrapper holder) {
        return Optional.ofNullable(holder.getCucumberTestCaseFinished())
                .map(event -> event.getResult().getError())
                .map(this::createExceptionDetails)
                .orElse(ExceptionDetails.empty());
    }

    private ExceptionDetails createExceptionDetails(Throwable throwable) {
        String message = throwable.getMessage();
        String stack = getStackTrace(throwable);
        log.debug("Including error details for failed test");
        return new ExceptionDetails(message, stack);
    }

    private String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }
}
