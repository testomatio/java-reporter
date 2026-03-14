package io.testomat.karate.constructor;

import com.intuit.karate.core.ScenarioRuntime;
import io.testomat.core.model.ExceptionDetails;
import io.testomat.core.model.TestResult;
import io.testomat.core.step.StepStorage;
import io.testomat.core.step.TestStep;
import io.testomat.karate.extractor.TestDataExtractor;
import java.util.List;

/**
 * Constructs test results from Karate test case finished events.
 * Extracts exception details from Karate execution errors when available.
 */
public class KarateTestResultConstructor {

    private final TestDataExtractor testDataExtractor;

    /**
     * Creates a new instance using a default {@link TestDataExtractor} implementation.
     */
    public KarateTestResultConstructor() {
        this.testDataExtractor = new TestDataExtractor();
    }

    /**
     * Creates a new instance using the specified {@link TestDataExtractor}.
     *
     * @param testDataExtractor the test data extractor to use
     */
    public KarateTestResultConstructor(TestDataExtractor testDataExtractor) {
        this.testDataExtractor = testDataExtractor;
    }

    /**
     * Constructs a test result from a finished Karate test case.
     * Extracts test metadata, execution status, error details,
     * and test steps from the provided {@link ScenarioRuntime}.
     *
     * @param sr the {@link ScenarioRuntime} representing the finished Karate test case
     * @return the constructed test result
     */
    public TestResult constructTestRunResult(ScenarioRuntime sr) {

        ExceptionDetails exceptionDetails = testDataExtractor.extractExceptionDetails(sr);

        testDataExtractor.extractAttachments(sr);

        // Collect steps from ThreadLocal storage
        List<TestStep> steps = StepStorage.getSteps();

        TestResult.Builder builder = TestResult.builder()
                .withStatus(testDataExtractor.getNormalizedStatus(sr))
                .withSuiteTitle(sr.scenario.getFeature().getResource().getRelativePath())
                .withTestId(testDataExtractor.extractTestId(sr))
                .withFile(testDataExtractor.extractFileName(sr))
                .withTitle(testDataExtractor.extractTitle(sr))
                .withRid(testDataExtractor.getRid(sr))
                .withMessage(exceptionDetails.getMessage())
                .withStack(exceptionDetails.getStack())
                .withOverwrite(false);

        if (!steps.isEmpty()) {
            builder.withSteps(steps);
        }

        // Clear steps after collecting them
        StepStorage.clear();

        return builder.build();
    }
}
