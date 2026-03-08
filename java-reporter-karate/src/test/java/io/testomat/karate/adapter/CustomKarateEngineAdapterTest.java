package io.testomat.karate.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.intuit.karate.core.ScenarioEngine;
import com.intuit.karate.core.Variable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class CustomKarateEngineAdapterTest {

    CustomKarateEngineAdapter adapter = new CustomKarateEngineAdapter();

    @Test
    void shouldReturnVariableValue() throws Exception {
        ScenarioEngine engine = mock(ScenarioEngine.class);
        Variable variable = mock(Variable.class);

        when(variable.getValue()).thenReturn("value");

        Map<String, Variable> vars = new HashMap<>();
        vars.put("test", variable);

        Field varsField = ScenarioEngine.class.getDeclaredField("vars");
        varsField.setAccessible(true);
        varsField.set(engine, vars);

        try (MockedStatic<ScenarioEngine> mocked = mockStatic(ScenarioEngine.class)) {
            mocked.when(ScenarioEngine::get).thenReturn(engine);
            Object result = adapter.getVariable("test");
            assertEquals("value", result);
        }
    }

    @Test
    void shouldReturnNullWhenVariableMissing() throws Exception {
        ScenarioEngine engine = mock(ScenarioEngine.class);

        Map<String, Variable> vars = new HashMap<>();

        Field varsField = ScenarioEngine.class.getDeclaredField("vars");
        varsField.setAccessible(true);
        varsField.set(engine, vars);

        try (MockedStatic<ScenarioEngine> mocked = mockStatic(ScenarioEngine.class)) {
            mocked.when(ScenarioEngine::get).thenReturn(engine);
            Object result = adapter.getVariable("missing");
            assertNull(result);
        }
    }

    @Test
    void shouldSetVariable() {
        ScenarioEngine engine = mock(ScenarioEngine.class);

        try (MockedStatic<ScenarioEngine> mocked = mockStatic(ScenarioEngine.class)) {
            mocked.when(ScenarioEngine::get).thenReturn(engine);
            adapter.setVariable("key", "value");
            verify(engine).setVariable("key", "value");
        }
    }
}
