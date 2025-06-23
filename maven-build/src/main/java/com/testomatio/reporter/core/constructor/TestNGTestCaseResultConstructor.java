package com.testomatio.reporter.core.constructor;

import com.testomatio.reporter.model.TestCaseResult;
import java.util.Optional;
import org.testng.ITestResult;

public class TestNGTestCaseResultConstructor extends AbstractTestCaseResultConstructor {

    @Override
    protected boolean hasCustomMessage(TestCaseResultWrapper holder) {
        return holder.getReason() != null || holder.getMessage() != null;
    }

    @Override
    protected String getCustomMessage(TestCaseResultWrapper holder) {
        return Optional.ofNullable(holder.getReason()).orElse(holder.getMessage());
    }

    @Override
    protected TestCaseResult createWithCustomMessage(TestCaseResultWrapper holder) {
        var message = getCustomMessage(holder);

        return buildTestResult(holder)
                .withMessage(message)
                .withStack(null)
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
        return "TestNG";
    }

    private ExceptionDetails extractExceptionDetails(TestCaseResultWrapper holder) {
        return Optional.ofNullable(holder.getTestResult())
                .map(ITestResult::getThrowable)
                .filter(this::isReportableException)
                .map(this::createExceptionDetails)
                .orElse(ExceptionDetails.empty());
    }
}