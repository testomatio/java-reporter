package io.testomat.cucumber.constructor;

import io.cucumber.plugin.event.TestCaseFinished;
import io.testomat.core.model.ExceptionDetails;
import io.testomat.core.model.TestResult;
import io.testomat.cucumber.extractor.TestDataExtractor;

/**
 * Constructs test case results from Cucumber test case finished events.
 * Extracts exception details from Cucumber result errors when available.
 */
public class CucumberTestResultConstructor {

    private final TestDataExtractor testDataExtractor;

    public CucumberTestResultConstructor() {
        this.testDataExtractor = new TestDataExtractor();
    }

    public CucumberTestResultConstructor(TestDataExtractor testDataExtractor) {
        this.testDataExtractor = testDataExtractor;
    }

    public TestResult constructTestRunResult(TestCaseFinished event) {

        ExceptionDetails exceptionDetails = testDataExtractor.extractExceptionDetails(event);

        return TestResult.builder()
                .withStatus(testDataExtractor.getNormalizedStatus(event))
                .withSuiteTitle(event.getTestCase().getUri().toString())
                .withExample(testDataExtractor.createExample(event))
                .withTestId(testDataExtractor.extractTestId(event))
                .withFile(testDataExtractor.extractFileName(event))
                .withTitle(testDataExtractor.extractTitle(event))
                .withRid(event.getTestCase().getId().toString())
                .withMessage(exceptionDetails.getMessage())
                .withStack(exceptionDetails.getStack())
                .build();
    }
}
