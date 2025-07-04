package com.testomatio.reporter.core.constructor;

import com.testomatio.reporter.model.TestResult;
import java.util.Optional;
import org.testng.ITestResult;

/**
 * Constructs test case results from TestNG test results.
 * Supports custom messages/reasons and extracts exception details from test results.
 */
public class TestNgTestResultConstructor extends AbstractTestResultConstructor {

    @Override
    protected boolean hasCustomMessage(TestResultWrapper holder) {
        return holder.getReason() != null || holder.getMessage() != null;
    }

    @Override
    protected String getCustomMessage(TestResultWrapper holder) {
        return Optional.ofNullable(holder.getReason()).orElse(holder.getMessage());
    }

    @Override
    protected TestResult createWithCustomMessage(TestResultWrapper holder) {
        var message = getCustomMessage(holder);

        return buildTestResult(holder)
                .withMessage(message)
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
        return "TestNG";
    }

    /**
     * Extracts exception details from TestNG test result.
     */
    private ExceptionDetails extractExceptionDetails(TestResultWrapper holder) {
        return Optional.ofNullable(holder.getTestResult())
                .map(ITestResult::getThrowable)
                .filter(this::isReportableException)
                .map(this::createExceptionDetails)
                .orElse(ExceptionDetails.empty());
    }
}
