package io.testomat.core.step;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StepLifecycleTest {

    @AfterEach
    void cleanup(){
        StepStorage.clear();
        StepLifecycle.reset();
    }

    @Test
    void shouldStartStep() {
        TestStep step = new TestStep();
        StepLifecycle.start(step);
        TestStep current = StepLifecycle.current();

        assertNotNull(current);
        assertEquals(step, current);
    }

    @Test
    void shouldHandleNestedSteps() {
        TestStep parent = new TestStep();
        TestStep child = new TestStep();

        StepLifecycle.start(parent);
        StepLifecycle.start(child);

        assertEquals(child, StepLifecycle.current());
        assertEquals(1, parent.getSubsteps().size());
        assertEquals(child, parent.getSubsteps().get(0));
    }

    @Test
    void shouldFinishStep() {
        TestStep step = new TestStep();

        StepLifecycle.start(step);
        StepLifecycle.finish();

        TestStep last = StepLifecycle.lastFinished();

        assertEquals(step,last);
        assertNull(StepLifecycle.current());
    }

    @Test
    void shouldResetState() {
        TestStep step = new TestStep();

        StepLifecycle.start(step);
        StepLifecycle.reset();

        assertNull(StepLifecycle.current());
        assertNull(StepLifecycle.lastFinished());
    }

    @Test
    void shouldSupportMultipleFinishCalls() {
        TestStep parent = new TestStep();
        TestStep child = new TestStep();

        StepLifecycle.start(parent);
        StepLifecycle.start(child);

        StepLifecycle.finish();

        assertEquals(child,StepLifecycle.lastFinished());
        assertEquals(parent,StepLifecycle.current());

        StepLifecycle.finish();

        assertEquals(parent,StepLifecycle.lastFinished());
        assertNull(StepLifecycle.current());
    }
}
