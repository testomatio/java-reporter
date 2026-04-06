package reporter;

import io.qameta.allure.model.Status;
import io.qameta.allure.model.StatusDetails;
import io.qameta.allure.model.StepResult;
import io.testomat.core.step.StepLifecycle;
import io.testomat.core.step.StepStatus;
import io.testomat.core.step.TestStep;
import io.testomat.reporter.TestomatStepReporter;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TestomatStepReporterTest {

    TestomatStepReporter reporter = new TestomatStepReporter();

    @Test
    void shouldStartStep() {
        StepResult result = new StepResult();

        try (MockedStatic<StepLifecycle> lifecycle = mockStatic(StepLifecycle.class)) {
            reporter.afterStepStart(result);

            lifecycle.verify(() -> StepLifecycle.start(any(TestStep.class)));
        }
    }

    @Test
    void shouldHandlePassedStep() {
        StepResult result =
            new StepResult()
                .setName("step")
                .setStart(10L)
                .setStop(20L)
                .setStatus(Status.PASSED);

        TestStep step = mock(TestStep.class);

        try (MockedStatic<StepLifecycle> lifecycle = mockStatic(StepLifecycle.class)) {
            lifecycle.when(StepLifecycle::current).thenReturn(step);

            reporter.afterStepStop(result);

            verify(step).setCategory("user");
            verify(step).setStepTitle("step");
            verify(step).setDuration(10);
            verify(step).setStatus(StepStatus.passed);

            lifecycle.verify(StepLifecycle::finish);
        }
    }

    @Test
    void shouldHandleFailedStep() {
        StepResult result =
            new StepResult()
                .setName("step")
                .setStart(0L)
                .setStop(50L)
                .setStatus(Status.FAILED)
                .setStatusDetails(new StatusDetails()
                    .setMessage("error")
                    .setTrace("stack"));

        TestStep step = new TestStep();

        try (MockedStatic<StepLifecycle> lifecycle = mockStatic(StepLifecycle.class)) {
            lifecycle.when(StepLifecycle::current).thenReturn(step);
            reporter.afterStepStop(result);

            assertEquals("user", step.getCategory());
            assertEquals("step", step.getStepTitle());
            assertEquals(50, step.getDuration());
            assertEquals(StepStatus.failed, step.getStatus());
            assertEquals("error", step.getError());
            assertEquals("stack", step.getLog());

            lifecycle.verify(StepLifecycle::finish);
        }
    }

    @Test
    void shouldHandleBrokenStep() {
        StepResult result =
            new StepResult()
                .setName("step")
                .setStart(0L)
                .setStop(1L)
                .setStatus(Status.BROKEN)
                .setStatusDetails(
                    new StatusDetails()
                        .setMessage("error")
                        .setTrace("stack")
                );

        TestStep step = new TestStep();

        try (MockedStatic<StepLifecycle> lifecycle = mockStatic(StepLifecycle.class)) {
            lifecycle.when(StepLifecycle::current).thenReturn(step);
            reporter.afterStepStop(result);

            assertEquals("user", step.getCategory());
            assertEquals("step", step.getStepTitle());
            assertEquals(1, step.getDuration());
            assertEquals(StepStatus.failed, step.getStatus());
            assertEquals("error", step.getError());
            assertEquals("stack", step.getLog());

            lifecycle.verify(StepLifecycle::finish);
        }
    }

    @Test
    void shouldHandleUnknownStatus() {
        StepResult result =
            new StepResult()
                .setName("step")
                .setStart(0L)
                .setStop(1L)
                .setStatus(Status.SKIPPED);

        TestStep step = new TestStep();

        try (MockedStatic<StepLifecycle> lifecycle = mockStatic(StepLifecycle.class)) {
            lifecycle.when(StepLifecycle::current).thenReturn(step);
            reporter.afterStepStop(result);

            assertEquals("user", step.getCategory());
            assertEquals("step", step.getStepTitle());
            assertEquals(1, step.getDuration());
            assertEquals(StepStatus.none, step.getStatus());

            lifecycle.verify(StepLifecycle::finish);
        }
    }

}
