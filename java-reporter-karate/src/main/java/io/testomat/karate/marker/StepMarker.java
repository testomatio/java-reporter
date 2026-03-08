package io.testomat.karate.marker;

import com.intuit.karate.core.ScenarioEngine;

public final class StepMarker {

    private static final String LOG_NEXT_STEP = "log_next_step";
    private static final String LOG_NEXT_STEP_TITLE = "log_next_step_title";

    private StepMarker() {

    }

    public static void mark() {
        ScenarioEngine engine = ScenarioEngine.get();
        if (engine != null) {
            engine.setVariable(LOG_NEXT_STEP, true);
        }
    }

    public static void mark(String title) {
        ScenarioEngine engine = ScenarioEngine.get();
        if (engine != null) {
            engine.setVariable(LOG_NEXT_STEP, true);
            engine.setVariable(LOG_NEXT_STEP_TITLE, title);
        }
    }
}
