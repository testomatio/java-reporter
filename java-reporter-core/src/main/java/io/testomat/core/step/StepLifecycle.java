package io.testomat.core.step;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Manages the lifecycle of test steps including nesting, tracking current step
 * and accessing the last finished step. Uses ThreadLocal storage to support
 * parallel test execution.
 */
public class StepLifecycle {

    private static final ThreadLocal<Deque<TestStep>> CURRENT_STEPS =
        ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<TestStep> LAST_FINISHED =
        new ThreadLocal<>();

    /**
     * Starts a new test step and registers it as a child of the current step
     * if one exists.
     *
     * @param step step to start
     */
    public static void start(TestStep step) {
        Deque<TestStep> stack = CURRENT_STEPS.get();
        TestStep parent = stack.peek();

        if (parent != null) {
            parent.getSubsteps().add(step);
        } else {
            StepStorage.addStep(step);
        }
        stack.push(step);
    }

    /**
     * Finishes the current step and stores it as the last finished step.
     */
    public static void finish() {
        Deque<TestStep> stack = CURRENT_STEPS.get();
        if (!stack.isEmpty()) {
            LAST_FINISHED.set(stack.pop());
        }
        if(stack.isEmpty()) {
            CURRENT_STEPS.remove();
        }
    }

    /**
     * Returns the currently active step.
     *
     * @return current step or null if none exists
     */
    public static TestStep current(){
        return CURRENT_STEPS.get().peek();
    }

    /**
     * Returns the last finished step.
     *
     * @return last finished step or null
     */
    public static TestStep lastFinished() {
        TestStep lastStep = LAST_FINISHED.get();
        if (lastStep != null) {
            LAST_FINISHED.remove();
        }
        return lastStep;
    }

    /**
     * Clears lifecycle state for the current thread.
     */
    public static void reset(){
        CURRENT_STEPS.remove();
        LAST_FINISHED.remove();
    }
}