package io.testomat.testng.constructor;

import io.testomat.core.model.TestResult;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Constructs test case results from framework-specific data.
 */
public interface ResultConstructor {

    /**
     * Constructs test case result from wrapper containing framework-specific data.
     *
     * @param testResultWrapper wrapper containing test metadata and framework data
     * @return constructed test case result
     */
    TestResult constructTestRunResult(TestResultWrapper testResultWrapper);

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
}
