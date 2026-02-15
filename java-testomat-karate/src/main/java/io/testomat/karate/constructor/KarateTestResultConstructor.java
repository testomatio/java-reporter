package io.testomat.karate.constructor;

import com.intuit.karate.core.ScenarioRuntime;
import io.testomat.core.model.ExceptionDetails;
import io.testomat.core.model.TestResult;
import io.testomat.core.step.StepStorage;
import io.testomat.core.step.TestStep;
import io.testomat.karate.extractor.TestDataExtractor;
import java.util.List;

/**
 * Constructs test case results from Karate test case finished events. Extracts exception details from Karate result
 * errors when available.
 */
public class KarateTestResultConstructor {

    private final TestDataExtractor testDataExtractor;

    /**
     * Creates a new instance with default test data extractor.
     */
    public KarateTestResultConstructor() {
        this.testDataExtractor = new TestDataExtractor();
    }

    /**
     * Creates a new instance with the specified test data extractor.
     *
     * @param testDataExtractor the test data extractor to use
     */
    public KarateTestResultConstructor(TestDataExtractor testDataExtractor) {
        this.testDataExtractor = testDataExtractor;
    }

    /**
     * Constructs a test result from a Karate test case finished event. Extracts test metadata, status, error details,
     * and test steps from the event.
     *
     * @param sr the Karate test case finished ScenarioRuntime
     * @return the constructed test result
     */
    public TestResult constructTestRunResult(ScenarioRuntime sr) {

        ExceptionDetails exceptionDetails = testDataExtractor.extractExceptionDetails(sr);

        String fileName = testDataExtractor.extractFileName(sr);
        System.out.println("CucumberTestResultConstructor: extractFileName returned: " + fileName);

        testDataExtractor.extractAttachments(sr);

        // Collect steps from ThreadLocal storage
        List<TestStep> steps = StepStorage.getSteps();

        TestResult.Builder builder = TestResult.builder()
            .withStatus(testDataExtractor.getNormalizedStatus(sr))
            .withSuiteTitle(sr.scenario.getFeature().getResource().getRelativePath())
            .withTestId(testDataExtractor.extractTestId(sr))
            .withFile(fileName)
            .withTitle(testDataExtractor.extractTitle(sr))
            .withRid(testDataExtractor.getRid(sr))
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
