package io.testomat.cucumber.constructor;

import io.cucumber.plugin.event.TestCaseFinished;
import io.testomat.core.model.ExceptionDetails;
import io.testomat.core.model.TestResult;
import io.testomat.core.step.StepStorage;
import io.testomat.core.step.TestStep;
import io.testomat.cucumber.extractor.TestDataExtractor;
import java.util.List;

/**
 * Constructs test case results from Cucumber test case finished events.
 * Extracts exception details from Cucumber result errors when available.
 */
public class CucumberTestResultConstructor {

    private final TestDataExtractor testDataExtractor;

    /**
     * Creates a new instance with default test data extractor.
     */
    public CucumberTestResultConstructor() {
        this.testDataExtractor = new TestDataExtractor();
    }

    /**
     * Creates a new instance with the specified test data extractor.
     *
     * @param testDataExtractor the test data extractor to use
     */
    public CucumberTestResultConstructor(TestDataExtractor testDataExtractor) {
        this.testDataExtractor = testDataExtractor;
    }

    /**
     * Constructs a test result from a Cucumber test case finished event.
     * Extracts test metadata, status, error details, and test steps from the event.
     *
     * @param event the Cucumber test case finished event
     * @return the constructed test result
     */
    public TestResult constructTestRunResult(TestCaseFinished event) {

        ExceptionDetails exceptionDetails = testDataExtractor.extractExceptionDetails(event);

        String fileName = testDataExtractor.extractFileName(event);
        System.out.println("CucumberTestResultConstructor: extractFileName returned: " + fileName);

        // Collect steps from ThreadLocal storage
        List<TestStep> steps = StepStorage.getSteps();

        TestResult.Builder builder = TestResult.builder()
                .withStatus(testDataExtractor.getNormalizedStatus(event))
                .withSuiteTitle(event.getTestCase().getUri().toString())
                .withExample(testDataExtractor.createExample(event))
                .withTestId(testDataExtractor.extractTestId(event))
                .withFile(fileName)
                .withTitle(testDataExtractor.extractTitle(event))
                .withRid(event.getTestCase().getId().toString())
                .withMessage(exceptionDetails.getMessage())
                .withStack(exceptionDetails.getStack());

        if (!steps.isEmpty()) {
            builder.withSteps(steps);
        }

        // Clear steps after collecting them
        StepStorage.clear();

        return builder.build();
    }
}
