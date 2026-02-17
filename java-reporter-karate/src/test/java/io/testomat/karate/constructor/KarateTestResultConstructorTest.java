package io.testomat.karate.constructor;

import com.intuit.karate.core.Feature;
import com.intuit.karate.core.Scenario;
import com.intuit.karate.core.ScenarioRuntime;
import com.intuit.karate.resource.Resource;
import io.testomat.core.model.ExceptionDetails;
import io.testomat.core.model.TestResult;
import io.testomat.core.step.StepStorage;
import io.testomat.core.step.TestStep;
import io.testomat.karate.extractor.TestDataExtractor;
import java.lang.reflect.Field;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class KarateTestResultConstructorTest {

    private final TestDataExtractor extractor = mock(TestDataExtractor.class);
    private final KarateTestResultConstructor constructor =
        new KarateTestResultConstructor(extractor);

    @AfterEach
    void cleanup() {
        StepStorage.clear();
    }

    @Test
    void shouldBuildResultWithSteps() {
        String uuid = UUID.randomUUID().toString();

        ScenarioRuntime sr = mockScenarioRuntime();

        when(extractor.extractExceptionDetails(sr))
            .thenReturn(ExceptionDetails.empty());
        when(extractor.getNormalizedStatus(sr))
            .thenReturn("passed");
        when(extractor.extractTestId(sr))
            .thenReturn("@T12345678");
        when(extractor.extractFileName(sr))
            .thenReturn("KarateTest.feature");
        when(extractor.extractTitle(sr))
            .thenReturn("karate test");
        when(extractor.getRid(sr))
            .thenReturn(uuid);

        TestStep step = new TestStep();
        StepStorage.addStep(step);

        TestResult result = constructor.constructTestRunResult(sr);

        assertThat(result.getStatus()).isEqualTo("passed");
        assertThat(result.getTestId()).isEqualTo("@T12345678");
        assertThat(result.getFile()).isEqualTo("KarateTest.feature");
        assertThat(result.getTitle()).isEqualTo("karate test");
        assertThat(result.getRid()).isEqualTo(uuid);
        assertThat(result.getSteps()).containsExactly(step);
    }

    @Test
    void shouldBuildResultWithoutSteps() {
        ScenarioRuntime sr = mockScenarioRuntime();

        when(extractor.extractExceptionDetails(sr))
            .thenReturn(ExceptionDetails.empty());
        when(extractor.getNormalizedStatus(sr))
            .thenReturn("passed");

        TestResult result = constructor.constructTestRunResult(sr);

        assertThat(result.getSteps()).isNull();
    }

    @Test
    void shouldIncludeExceptionDetailsWhenFailed() {
        ScenarioRuntime sr = mockScenarioRuntime();

        ExceptionDetails details =
            new ExceptionDetails("exception", "stacktrace");

        when(extractor.extractExceptionDetails(sr))
            .thenReturn(details);
        when(extractor.getNormalizedStatus(sr))
            .thenReturn("failed");

        TestResult result = constructor.constructTestRunResult(sr);

        assertThat(result.getStatus()).isEqualTo("failed");
        assertThat(result.getMessage()).isEqualTo("exception");
        assertThat(result.getStack()).isEqualTo("stacktrace");
    }

    @Test
    void shouldCallExtractorMethods() {
        ScenarioRuntime sr = mockScenarioRuntime();

        when(extractor.extractExceptionDetails(sr))
            .thenReturn(ExceptionDetails.empty());
        when(extractor.getNormalizedStatus(sr))
            .thenReturn("passed");

        constructor.constructTestRunResult(sr);

        verify(extractor).extractExceptionDetails(sr);
        verify(extractor).extractAttachments(sr);
        verify(extractor).getNormalizedStatus(sr);
        verify(extractor).extractTestId(sr);
        verify(extractor).extractFileName(sr);
        verify(extractor).extractTitle(sr);
        verify(extractor).getRid(sr);
    }

    @Test
    void shouldAlwaysClearStepStorage() {
        ScenarioRuntime sr = mockScenarioRuntime();

        when(extractor.extractExceptionDetails(sr))
            .thenReturn(ExceptionDetails.empty());
        when(extractor.getNormalizedStatus(sr))
            .thenReturn("passed");
        when(extractor.extractFileName(sr))
            .thenReturn(null);

        StepStorage.addStep(new TestStep());

        constructor.constructTestRunResult(sr);

        assertThat(StepStorage.getSteps()).isEmpty();
    }

    @Test
    void shouldHandleNullFileName() {
        ScenarioRuntime sr = mockScenarioRuntime();

        when(extractor.extractExceptionDetails(sr))
            .thenReturn(ExceptionDetails.empty());
        when(extractor.getNormalizedStatus(sr))
            .thenReturn("passed");
        when(extractor.extractFileName(sr))
            .thenReturn(null);

        TestResult result = constructor.constructTestRunResult(sr);

        assertThat(result.getFile()).isNull();
    }

    private ScenarioRuntime mockScenarioRuntime() {
        ScenarioRuntime sr = mock(ScenarioRuntime.class);

        Scenario scenario = mock(Scenario.class);
        Feature feature = mock(Feature.class);
        Resource resource = mock(Resource.class);

        when(feature.getResource())
            .thenReturn(resource);
        when(feature.getResource().getRelativePath())
            .thenReturn("features/KarateTest.feature");
        when(scenario.getFeature()).thenReturn(feature);

        try {
            Field field = ScenarioRuntime.class.getDeclaredField("scenario");
            field.setAccessible(true);
            field.set(sr, scenario);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return sr;
    }

}
