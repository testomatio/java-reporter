package com.testomatio.reporter.core.constructor;

import com.testomatio.reporter.model.TestCaseResult;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtensionContext;

public class JUnitTestCaseResultConstructor extends AbstractTestCaseResultConstructor {

    @Override
    protected boolean hasCustomMessage(TestCaseResultWrapper holder) {
        return holder.getMessage() != null;
    }

    @Override
    protected String getCustomMessage(TestCaseResultWrapper holder) {
        return holder.getMessage();
    }

    @Override
    protected TestCaseResult createWithCustomMessage(TestCaseResultWrapper holder) {
        var stack = extractStackTrace(holder);

        return buildTestResult(holder)
                .withMessage(holder.getMessage())
                .withStack(stack)
                .build();
    }

    @Override
    protected TestCaseResult createWithExceptionDetails(TestCaseResultWrapper holder) {
        var exceptionDetails = extractExceptionDetails(holder);

        return buildTestResult(holder)
                .withMessage(exceptionDetails.getMessage())
                .withStack(exceptionDetails.getStack())
                .build();
    }

    @Override
    protected String getFrameworkName() {
        return "JUnit";
    }

    private ExceptionDetails extractExceptionDetails(TestCaseResultWrapper holder) {
        return Optional.ofNullable(holder.getJUnitExtensionContext())
                .flatMap(ExtensionContext::getExecutionException)
                .filter(this::isReportableException)
                .map(this::createExceptionDetails)
                .orElse(ExceptionDetails.empty());
    }
}