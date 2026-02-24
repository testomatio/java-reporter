package io.testomat.karate.hooks;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.intuit.karate.Suite;
import com.intuit.karate.core.Scenario;
import com.intuit.karate.core.ScenarioRuntime;
import io.testomat.core.exception.ReportTestResultException;
import io.testomat.core.model.TestResult;
import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.karate.constructor.KarateTestResultConstructor;
import io.testomat.karate.exception.KarateHookException;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class KarateHookTest {

    KarateTestResultConstructor constructor = mock(KarateTestResultConstructor.class);
    FacadeFunctionsHandler functionsHandler = mock(FacadeFunctionsHandler.class);
    GlobalRunManager runManager = mock(GlobalRunManager.class);

    ScenarioRuntime sr = mock(ScenarioRuntime.class);
    TestResult testResult = mock(TestResult.class);

    KarateHook hook;

    @BeforeEach
    void setUp() {
        hook = new KarateHook(constructor, functionsHandler, runManager);
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

