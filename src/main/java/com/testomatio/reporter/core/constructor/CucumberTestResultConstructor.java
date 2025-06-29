package com.testomatio.reporter.core.constructor;

import com.testomatio.reporter.model.TestResult;
import java.util.Optional;

/**
 * Constructs test case results from Cucumber test case finished events.
 * Extracts exception details from Cucumber result errors when available.
 */
public class CucumberTestResultConstructor extends AbstractTestResultConstructor {

    @Override
    protected boolean hasCustomMessage(TestResultWrapper holder) {
        return false;
    }

    @Override
    protected String getCustomMessage(TestResultWrapper holder) {
        return null;
    }

    @Override
    protected TestResult createWithCustomMessage(TestResultWrapper holder) {
        return buildTestResult(holder)
                .withMessage(null)
                .withStack(null)
                .build();
    }

    @Override
    protected TestResult createWithExceptionDetails(TestResultWrapper holder) {
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

    /**
     * Extracts exception details from Cucumber test case finished event.
     */
    private ExceptionDetails extractExceptionDetails(TestResultWrapper holder) {
        return Optional.ofNullable(holder.getCucumberTestCaseFinished())
                .map(event -> event.getResult().getError())
                .filter(this::isReportableException)
                .map(this::createExceptionDetails)
                .orElse(ExceptionDetails.empty());
    }
}
