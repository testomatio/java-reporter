package com.testomatio.reporter.core.constructor;

import com.testomatio.reporter.model.TestResult;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Constructs test case results from JUnit 5 extension contexts.
 * Supports custom messages and extracts exception details from execution context.
 */
public class JUnitTestResultConstructor extends AbstractTestResultConstructor {

    @Override
    protected boolean hasCustomMessage(TestResultWrapper holder) {
        return holder.getMessage() != null;
    }

    @Override
    protected String getCustomMessage(TestResultWrapper holder) {
        return holder.getMessage();
    }

    @Override
    protected TestResult createWithCustomMessage(TestResultWrapper holder) {
        var stack = extractStackTrace(holder);

        return buildTestResult(holder)
                .withMessage(holder.getMessage())
                .withStack(stack)
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
        return "JUnit";
    }

    /**
     * Extracts exception details from JUnit extension context.
     */
    private ExceptionDetails extractExceptionDetails(TestResultWrapper holder) {
        return Optional.ofNullable(holder.getJunitExtensionContext())
                .flatMap(ExtensionContext::getExecutionException)
                .filter(this::isReportableException)
                .map(this::createExceptionDetails)
                .orElse(ExceptionDetails.empty());
    }
}
