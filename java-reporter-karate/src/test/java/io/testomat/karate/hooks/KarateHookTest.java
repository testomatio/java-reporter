package io.testomat.karate.hooks;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.intuit.karate.Suite;
import com.intuit.karate.core.Result;
import com.intuit.karate.core.Scenario;
import com.intuit.karate.core.ScenarioEngine;
import com.intuit.karate.core.ScenarioRuntime;
import com.intuit.karate.core.Step;
import com.intuit.karate.core.StepResult;
import com.intuit.karate.core.Tags;
import io.testomat.core.exception.ReportTestResultException;
import io.testomat.core.model.TestResult;
import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.core.step.StepLifecycle;
import io.testomat.core.step.StepStatus;
import io.testomat.core.step.StepTimer;
import io.testomat.core.step.TestStep;
import io.testomat.karate.adapter.CustomKarateEngineAdapter;
import io.testomat.karate.adapter.KarateEngineAdapter;
import io.testomat.karate.constructor.KarateTestResultConstructor;
import io.testomat.karate.exception.KarateHookException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class KarateHookTest {

    KarateTestResultConstructor constructor = mock(KarateTestResultConstructor.class);
    FacadeFunctionsHandler functionsHandler = mock(FacadeFunctionsHandler.class);
    GlobalRunManager runManager = mock(GlobalRunManager.class);
    CustomKarateEngineAdapter engine = mock(CustomKarateEngineAdapter.class);

    ScenarioRuntime sr = mock(ScenarioRuntime.class);
    TestResult testResult = mock(TestResult.class);

    KarateHook hook;

    @BeforeEach
    void setUp() {
        hook = new KarateHook(constructor, functionsHandler, runManager, engine);
    }

    @AfterEach
    void cleanup() {
        StepLifecycle.reset();
    }

    @Test
    void shouldIncrementSuiteCounter() {
        hook.beforeSuite(mock(Suite.class));

        verify(runManager).incrementSuiteCounter();
    }

    @Test
    void shouldDecrementSuiteCounter() {
        hook.afterSuite(mock(Suite.class));

        verify(runManager).decrementSuiteCounter();
    }

    @Test
    void shouldDoNothingIfRunIsNotActive() {
        when(runManager.isActive()).thenReturn(false);

        hook.afterScenario(sr);

        verifyNoInteractions(constructor, functionsHandler);
    }

    @Test
    void shouldReportResultAndCallAfterEach() {
        when(runManager.isActive()).thenReturn(true);
        when(constructor.constructTestRunResult(sr)).thenReturn(testResult);

        hook.afterScenario(sr);

        verify(runManager).reportTest(testResult);
        verify(functionsHandler).handleFacadeFunctions(sr);
    }

    @Test
    void shouldThrowIfScenarioRuntimeIsNull() {
        when(runManager.isActive()).thenReturn(true);

        assertThatThrownBy(() -> hook.afterScenario(null))
            .isInstanceOf(KarateHookException.class)
            .hasMessageContaining("scenario runtime is null");
    }

    @Test
    void shouldWrapExceptionWhenReportFails() {
        when(runManager.isActive()).thenReturn(true);
        when(constructor.constructTestRunResult(sr))
            .thenThrow(new RuntimeException("exception"));

        Scenario scenario = mock(Scenario.class);
        when(scenario.getName()).thenReturn("Karate Test");

        injectFinalField(sr, "scenario", scenario);

        assertThatThrownBy(() -> hook.afterScenario(sr))
            .isInstanceOf(ReportTestResultException.class)
            .hasMessageContaining("Karate Test");

        verify(functionsHandler).handleFacadeFunctions(sr);
    }

    @Test
    void shouldStartTimerWhenLogNextStepIsTrue() {
        Step step = mock(Step.class);
        when(step.getPrefix()).thenReturn("Given");

        ScenarioRuntime sr = mock(ScenarioRuntime.class);

        Tags tags = mock(Tags.class);
        when(tags.getTags()).thenReturn(Collections.emptyList());
        injectFinalField(sr, "tags", tags);

        KarateEngineAdapter engine = mock(KarateEngineAdapter.class);
        when(engine.getVariable("log_next_step")).thenReturn(true);

        injectFinalField(hook, "engine", engine);

        try (MockedStatic<StepTimer> timer = mockStatic(StepTimer.class)) {
            boolean result = hook.beforeStep(step, sr);

            assertTrue(result);

            verify(engine).setVariable("log_next_step", false);
            timer.verify(() -> StepTimer.start(any()), times(1));
        }
    }

    @Test
    void shouldStartTimerWhenLogStepsTagPresent() {
        Step step = mock(Step.class);
        when(step.getPrefix()).thenReturn("When");

        ScenarioRuntime sr = mock(ScenarioRuntime.class);

        Tags tags = mock(Tags.class);
        when(tags.getTags()).thenReturn(List.of("LogSteps"));
        injectFinalField(sr, "tags", tags);

        KarateEngineAdapter engine = mock(KarateEngineAdapter.class);
        when(engine.getVariable("log_next_step")).thenReturn(false);

        injectFinalField(hook, "engine", engine);

        try (MockedStatic<StepTimer> timer = mockStatic(StepTimer.class)) {

            hook.beforeStep(step, sr);

            verify(engine).setVariable("log_next_step", false);
            timer.verify(() -> StepTimer.start(any()), times(1));
        }
    }

    @Test
    void shouldNotStartTimerWhenStepIsNotDslAndNoFlags() {
        Step step = mock(Step.class);
        ScenarioRuntime sr = mock(ScenarioRuntime.class);

        when(step.getPrefix()).thenReturn("*");

        ScenarioEngine engine = mock(ScenarioEngine.class);

        try (MockedStatic<ScenarioEngine> mocked = mockStatic(ScenarioEngine.class)) {
            mocked.when(ScenarioEngine::get).thenReturn(engine);

            boolean result = hook.beforeStep(step, sr);

            assertTrue(result);
            verify(engine, never()).setVariable(eq("log_next_step"), any());
        }
    }

    @Test
    void shouldSkipStepIfTimerNotStarted() {
        StepResult result = mock(StepResult.class);
        Step step = mock(Step.class);
        ScenarioRuntime sr = mock(ScenarioRuntime.class);

        when(result.getStep()).thenReturn(step);

        try (MockedStatic<StepTimer> timer = mockStatic(StepTimer.class)) {
            timer.when(() -> StepTimer.stop(any())).thenReturn(-1L);

            hook.afterStep(result, sr);

            timer.verify(() -> StepTimer.stop(any()));
        }
    }

    @Test
    void shouldStoreStepWhenTimerStopped() {
        StepResult result = mock(StepResult.class);
        Step step = mock(Step.class);
        ScenarioRuntime sr = mock(ScenarioRuntime.class);
        Result karateResult = mock(Result.class);

        when(result.getStep()).thenReturn(step);
        when(result.getResult()).thenReturn(karateResult);
        when(karateResult.isFailed()).thenReturn(false);
        when(karateResult.getStatus()).thenReturn("passed");
        when(step.getText()).thenReturn("Some step");

        try (MockedStatic<StepTimer> timer = mockStatic(StepTimer.class)) {
            TestStep testStep = new TestStep();
            StepLifecycle.start(testStep);
            timer.when(() -> StepTimer.stop(Mockito.anyString()))
                .thenReturn(100L);
            hook.afterStep(result, sr);

            TestStep finished = StepLifecycle.lastFinished();

            assertEquals("Some step", finished.getStepTitle());
            assertEquals(100L, finished.getDuration());
            assertEquals("user", finished.getCategory());
            assertEquals(StepStatus.passed, finished.getStatus());   // ← добавь
        }
    }

    @Test
    void shouldUseCustomStepTitle() {
        StepResult result = mock(StepResult.class);
        Step step = mock(Step.class);
        ScenarioRuntime sr = mock(ScenarioRuntime.class);
        Result karateResult = mock(Result.class);

        when(result.getStep()).thenReturn(step);
        when(result.getResult()).thenReturn(karateResult);
        when(karateResult.isFailed()).thenReturn(false);
        when(karateResult.getStatus()).thenReturn("passed");
        when(step.getText()).thenReturn("Original");

        KarateEngineAdapter engine = mock(KarateEngineAdapter.class);

        when(engine.getVariable("log_next_step_title"))
            .thenReturn("Custom title");

        try (MockedStatic<StepTimer> timer = mockStatic(StepTimer.class)) {
            TestStep testStep = new TestStep();
            StepLifecycle.start(testStep);
            timer.when(() -> StepTimer.stop(Mockito.anyString()))
                .thenReturn(50L);

            KarateHook hook = new KarateHook(
                constructor,
                functionsHandler,
                runManager,
                engine
            );

            hook.afterStep(result, sr);

            TestStep finished = StepLifecycle.lastFinished();

            assertEquals("Custom title", finished.getStepTitle());
            assertEquals(50L, finished.getDuration());
            assertEquals("user", finished.getCategory());
            assertEquals(StepStatus.passed, finished.getStatus());

            verify(engine).setVariable("log_next_step_title", null);
        }
    }

    static void injectFinalField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
