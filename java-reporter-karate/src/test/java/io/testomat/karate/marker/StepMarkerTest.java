package io.testomat.karate.marker;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.intuit.karate.core.ScenarioEngine;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class StepMarkerTest {

    private static final String LOG_NEXT_STEP = "log_next_step";
    private static final String LOG_NEXT_STEP_TITLE = "log_next_step_title";
    private static final String MY_STEP = "My step";
    private static final String TITLE = "title";

    @Test
    void shouldMarkNextStep() {
        ScenarioEngine engine = mock(ScenarioEngine.class);

        try (MockedStatic<ScenarioEngine> mocked = mockStatic(ScenarioEngine.class)) {
            mocked.when(ScenarioEngine::get).thenReturn(engine);

            StepMarker.mark();

            verify(engine).setVariable(LOG_NEXT_STEP, true);
            verifyNoMoreInteractions(engine);
        }
    }

    @Test
    void shouldMarkNextStepWithTitle() {
        ScenarioEngine engine = mock(ScenarioEngine.class);

        try (MockedStatic<ScenarioEngine> mocked = mockStatic(ScenarioEngine.class)) {
            mocked.when(ScenarioEngine::get).thenReturn(engine);

            StepMarker.mark(MY_STEP);

            verify(engine).setVariable(LOG_NEXT_STEP, true);
            verify(engine).setVariable(LOG_NEXT_STEP_TITLE, MY_STEP);
            verifyNoMoreInteractions(engine);
        }
    }

    @Test
    void shouldDoNothingIfEngineIsNull() {
        try (MockedStatic<ScenarioEngine> mocked = mockStatic(ScenarioEngine.class)) {
            mocked.when(ScenarioEngine::get).thenReturn(null);

            StepMarker.mark();
            StepMarker.mark(TITLE);

            mocked.verify(ScenarioEngine::get, times(2));
        }
    }
}
