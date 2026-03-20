package io.testomat.karate.extractor;

import com.intuit.karate.core.Feature;
import com.intuit.karate.core.Scenario;
import com.intuit.karate.core.ScenarioResult;
import com.intuit.karate.core.ScenarioRuntime;
import com.intuit.karate.core.StepResult;
import com.intuit.karate.core.Tag;
import com.intuit.karate.core.Tags;
import com.intuit.karate.resource.Resource;
import io.testomat.core.model.ExceptionDetails;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TestDataExtractorTest {

    private final TestDataExtractor extractor = new TestDataExtractor();

    @Test
    void extractExceptionDetailsWhenFailed() {
        ScenarioRuntime sr = mockScenarioRuntime();

        ScenarioResult result = mock(ScenarioResult.class);
        when(result.isFailed()).thenReturn(true);
        when(result.getError()).thenReturn(new RuntimeException("exception"));

        injectFinalField(sr, "result", result);

        ExceptionDetails details = extractor.extractExceptionDetails(sr);

        assertThat(details.getMessage()).isEqualTo("exception");
        assertThat(details.getStack()).contains("RuntimeException");
    }

    @Test
    void extractExceptionDetailsWhenPassed() {
        ScenarioRuntime sr = mockScenarioRuntime();

        ScenarioResult result = mock(ScenarioResult.class);
        when(result.isFailed()).thenReturn(false);

        injectFinalField(sr, "result", result);

        ExceptionDetails details = extractor.extractExceptionDetails(sr);

        assertThat(details)
            .usingRecursiveComparison()
            .isEqualTo(ExceptionDetails.empty());
    }

    @Test
    void extractTestIdNotFound() {
        ScenarioRuntime sr = mockScenarioRuntimeWithTags("smoke", "regression");

        assertThat(extractor.extractTestId(sr)).isNull();
    }

    @Test
    void extractTitleFromTag() {
        ScenarioRuntime sr = mockScenarioRuntimeWithTags("@title:Karate_Test");

        String title = extractor.extractTitle(sr);

        assertThat(title).isEqualTo("Karate Test");
    }

    @Test
    void extractTitleFallbackToScenarioName() {
        ScenarioRuntime sr = mockScenarioRuntimeWithTags();

        when(sr.scenario.getName()).thenReturn("Scenario Name");

        String title = extractor.extractTitle(sr);

        assertThat(title).isEqualTo("Scenario Name");
    }

    @Test
    void extractFileNameSuccess() {
        ScenarioRuntime sr = mockScenarioRuntime();

        String file = extractor.extractFileName(sr);

        assertThat(file).isEqualTo("features/KarateTest.feature");
    }

    @Test
    void extractFileNameFailureReturnsNull() {
        ScenarioRuntime sr = mock(ScenarioRuntime.class);

        assertThat(extractor.extractFileName(sr)).isNull();
    }

    @Test
    void getNormalizedStatusPassed() {
        ScenarioRuntime sr = mockScenarioRuntime();

        ScenarioResult result = mock(ScenarioResult.class);
        when(result.isFailed()).thenReturn(false);
        when(result.getStepResults()).thenReturn(List.of(mock(StepResult.class)));

        injectFinalField(sr, "result", result);

        assertThat(extractor.getNormalizedStatus(sr)).isEqualTo("passed");
    }

    @Test
    void getNormalizedStatusSkipped() {
        ScenarioRuntime sr = mockScenarioRuntime();

        ScenarioResult result = mock(ScenarioResult.class);
        when(result.isFailed()).thenReturn(false);
        when(result.getStepResults()).thenReturn(List.of());

        injectFinalField(sr, "result", result);

        assertThat(extractor.getNormalizedStatus(sr)).isEqualTo("skipped");
    }

    @Test
    void getNormalizedStatusFailed() {
        ScenarioRuntime sr = mockScenarioRuntime();

        ScenarioResult result = mock(ScenarioResult.class);
        when(result.isFailed()).thenReturn(true);

        injectFinalField(sr, "result", result);

        assertThat(extractor.getNormalizedStatus(sr)).isEqualTo("failed");
    }

    @Test
    void getRidGeneratesStableUuid() {
        ScenarioRuntime sr = mockScenarioRuntime();

        when(sr.scenario.getLine()).thenReturn(4);
        when(sr.scenario.getExampleIndex()).thenReturn(-1);

        String rid1 = extractor.getRid(sr);
        String rid2 = extractor.getRid(sr);

        assertThat(rid1)
            .isNotNull()
            .isEqualTo(rid2);
    }

    @Test
    void getRidSameWithoutExampleIndex() {
        ScenarioRuntime sr1 = mockScenarioRuntime();
        ScenarioRuntime sr2 = mockScenarioRuntime();

        when(sr1.scenario.getLine()).thenReturn(5);
        when(sr2.scenario.getLine()).thenReturn(5);

        when(sr1.scenario.getExampleIndex()).thenReturn(-1);
        when(sr2.scenario.getExampleIndex()).thenReturn(-1);

        String rid1 = extractor.getRid(sr1);
        String rid2 = extractor.getRid(sr2);

        assertThat(rid1).isEqualTo(rid2);
    }

    @Test
    void getRidDifferentLinesProduceDifferentRid() {
        ScenarioRuntime sr1 = mockScenarioRuntime();
        ScenarioRuntime sr2 = mockScenarioRuntime();

        when(sr1.scenario.getLine()).thenReturn(1);
        when(sr2.scenario.getLine()).thenReturn(2);

        when(sr1.scenario.getExampleIndex()).thenReturn(-1);
        when(sr2.scenario.getExampleIndex()).thenReturn(-1);

        String rid1 = extractor.getRid(sr1);
        String rid2 = extractor.getRid(sr2);

        assertThat(rid1).isNotEqualTo(rid2);
    }

    @Test
    void getRidIncludesExampleIndex() {
        ScenarioRuntime sr1 = mockScenarioRuntime();
        ScenarioRuntime sr2 = mockScenarioRuntime();

        when(sr1.scenario.getLine()).thenReturn(10);
        when(sr2.scenario.getLine()).thenReturn(10);

        when(sr1.scenario.getExampleIndex()).thenReturn(0);
        when(sr2.scenario.getExampleIndex()).thenReturn(1);

        String rid1 = extractor.getRid(sr1);
        String rid2 = extractor.getRid(sr2);

        assertThat(rid1).isNotEqualTo(rid2);
    }

    @Test
    void getRidDifferentFeatureFilesProduceDifferentRid() {
        ScenarioRuntime sr1 = mockScenarioRuntime();
        ScenarioRuntime sr2 = mockScenarioRuntime();

        when(sr1.scenario.getLine()).thenReturn(1);
        when(sr2.scenario.getLine()).thenReturn(1);

        when(sr1.scenario.getExampleIndex()).thenReturn(-1);
        when(sr2.scenario.getExampleIndex()).thenReturn(-1);

        when(sr2.scenario.getFeature()
            .getResource()
            .getRelativePath())
            .thenReturn("features/Other.feature");

        String rid1 = extractor.getRid(sr1);
        String rid2 = extractor.getRid(sr2);

        assertThat(rid1).isNotEqualTo(rid2);
    }

    @Test
    void getRidMatchesExpectedUuid() {
        ScenarioRuntime sr = mockScenarioRuntime();

        when(sr.scenario.getLine()).thenReturn(4);
        when(sr.scenario.getExampleIndex()).thenReturn(-1);

        String expected =
            UUID.nameUUIDFromBytes(
                "features/KarateTest.feature:4"
                    .getBytes(StandardCharsets.UTF_8)
            ).toString();

        String rid = extractor.getRid(sr);

        assertThat(rid).isEqualTo(expected);
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

        injectFinalField(sr, "scenario", scenario);
        return sr;
    }

    private ScenarioRuntime mockScenarioRuntimeWithTags(String... tags) {
        ScenarioRuntime sr = mockScenarioRuntime();
        Collection<Tag> listTags =
            Arrays.stream(tags)
                .map(t -> new Tag(1, t))
                .collect(Collectors.toCollection(ArrayList::new));
        injectFinalField(sr, "tags", new Tags(listTags));
        return sr;
    }

    private static void injectFinalField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
