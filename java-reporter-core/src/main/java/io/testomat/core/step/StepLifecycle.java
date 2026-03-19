package io.testomat.core.step;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

public class StepLifecycle {

    private static final ThreadLocal<Deque<UUID>> CURRENT_STEPS =
        ThreadLocal.withInitial(ArrayDeque::new);

    public static void start(UUID stepId) {
        CURRENT_STEPS.get().push(stepId);
    }

    public static void finish() {
        CURRENT_STEPS.get().pop();
    }

    public static UUID current() {
        return CURRENT_STEPS.get().peek();
    }
}