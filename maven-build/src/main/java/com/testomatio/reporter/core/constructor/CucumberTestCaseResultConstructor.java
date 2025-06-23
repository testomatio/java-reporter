package com.testomatio.reporter.core.constructor;

import com.testomatio.reporter.model.TestCaseResult;
import java.util.Optional;

public class CucumberTestCaseResultConstructor extends AbstractTestCaseResultConstructor {

    @Override
    protected boolean hasCustomMessage(TestCaseResultWrapper holder) {
        return false;
    }

    @Override
    protected String getCustomMessage(TestCaseResultWrapper holder) {
        return null;
    }

    @Override
    protected TestCaseResult createWithCustomMessage(TestCaseResultWrapper holder) {
        return buildTestResult(holder)
                .withMessage(null)
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
        return "Cucumber";
    }

    private ExceptionDetails extractExceptionDetails(TestCaseResultWrapper holder) {
        return Optional.ofNullable(holder.getCucumberTestCaseFinished())
                .map(event -> event.getResult().getError())
                .filter(this::isReportableException)
                .map(this::createExceptionDetails)
                .orElse(ExceptionDetails.empty());
    }
}