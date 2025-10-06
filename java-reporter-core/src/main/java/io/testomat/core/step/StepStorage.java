package io.testomat.core.step;

import java.util.ArrayList;
import java.util.List;

/**
 * Thread-local storage for test steps during test execution.
 * Ensures thread safety when multiple tests run concurrently.
 */
public class StepStorage {
    private static final ThreadLocal<List<TestStep>> STEPS = ThreadLocal.withInitial(ArrayList::new);

    /**
     * Adds a step to the current thread's step list.
     *
     * @param step the step to store
     */
    public static void addStep(TestStep step) {
        STEPS.get().add(step);
    }

    /**
     * Retrieves all steps for the current thread.
     *
     * @return list of steps
     */
    public static List<TestStep> getSteps() {
        return new ArrayList<>(STEPS.get());
    }

    /**
     * Clears all steps for the current thread.
     * Should be called after reporting test results.
     */
    public static void clear() {
        STEPS.get().clear();
    }
}
