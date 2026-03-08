package io.testomat.karate.adapter;

import com.intuit.karate.core.ScenarioEngine;
import com.intuit.karate.core.Variable;

public class CustomKarateEngineAdapter implements KarateEngineAdapter {

    @Override
    public Object getVariable(String name) {
        ScenarioEngine engine = ScenarioEngine.get();
        Variable variable = engine.vars.get(name);
        return variable != null ? variable.getValue() : null;
    }

    @Override
    public void setVariable(String name, Object value) {
        ScenarioEngine.get().setVariable(name, value);
    }
}
