package com.testomatio.reporter.core.constructor;

import com.testomatio.reporter.model.TestResult;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.opentest4j.TestAbortedException;

/**
 * Constructs test case results from framework-specific data.
 */
public interface ResultConstructor {

    /**
     * Constructs test case result from wrapper containing framework-specific data.
     *
     * @param testCaseResultWrapper wrapper containing test metadata and framework data
     * @return constructed test case result
     */
    TestResult constructTestRunResult(TestCaseResultWrapper testCaseResultWrapper);

    /**
     * Converts throwable to stack trace string.
     *
     * @param t throwable to convert
     * @return stack trace as string
     */
    default String getStackTrace(Throwable t) {
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
    default String extractStackTrace(TestCaseResultWrapper holder) {
        return Optional.ofNullable(holder.getJUnitExtensionContext())
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
    default boolean isReportableException(Throwable throwable) {
        return !(throwable instanceof TestAbortedException);
    }
}