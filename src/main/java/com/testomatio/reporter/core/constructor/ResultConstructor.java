package com.testomatio.reporter.core.constructor;

import com.testomatio.reporter.model.TestCaseResult;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.opentest4j.TestAbortedException;

public interface ResultConstructor {


    TestCaseResult constructTestRunResult(TestCaseResultWrapper testCaseResultWrapper);

    default String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

    default String extractStackTrace(TestCaseResultWrapper holder) {
        return Optional.ofNullable(holder.getJUnitExtensionContext())
                .flatMap(ExtensionContext::getExecutionException)
                .filter(this::isReportableException)
                .map(this::getStackTrace)
                .orElse(null);
    }

    default boolean isReportableException(Throwable throwable) {
        return !(throwable instanceof TestAbortedException);
    }
}