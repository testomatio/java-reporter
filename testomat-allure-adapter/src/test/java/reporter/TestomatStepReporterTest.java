package reporter;

import io.qameta.allure.model.Status;
import io.qameta.allure.model.StatusDetails;
import io.qameta.allure.model.StepResult;
import io.testomat.core.step.StepLifecycle;
import io.testomat.core.step.StepStatus;
import io.testomat.core.step.TestStep;
import io.testomat.reporter.TestomatStepReporter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.MockedStatic;
import java.util.Objects;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class TestomatStepReporterTest {

    private final TestomatStepReporter reporter = new TestomatStepReporter();

    @Test
    void shouldStartStep() {
        StepResult result = new StepResult();

        try (MockedStatic<StepLifecycle> lifecycle = mockStatic(StepLifecycle.class)) {
            reporter.afterStepStart(result);

            lifecycle.verify(() ->
                StepLifecycle.start(argThat(Objects::nonNull))
            );
        }
    }

    @Test
    void shouldHandlePassedStep() {
        StepResult result = createStep(Status.PASSED, 10, 20);
        TestStep step = new TestStep();

        try (MockedStatic<StepLifecycle> lifecycle = mockLifecycle(step)) {
            reporter.afterStepStop(result);

            assertEquals("user", step.getCategory());
            assertEquals("step", step.getStepTitle());
            assertEquals(10, step.getDuration());
            assertEquals(StepStatus.passed, step.getStatus());

            lifecycle.verify(StepLifecycle::finish);
        }
    }

    @ParameterizedTest
    @EnumSource(value = Status.class, names = {"FAILED", "BROKEN"})
    void shouldHandleFailedAndBrokenSteps(Status status) {
        StepResult result = createStep(status, 0, 50)
            .setStatusDetails(new StatusDetails()
                .setMessage("error")
                .setTrace("stack"));

        TestStep step = new TestStep();

        try (MockedStatic<StepLifecycle> lifecycle = mockLifecycle(step)) {
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
    void shouldHandleUnknownStatus() {
        StepResult result = createStep(Status.SKIPPED, 0, 1);
        TestStep step = new TestStep();

        try (MockedStatic<StepLifecycle> lifecycle = mockLifecycle(step)) {
            reporter.afterStepStop(result);

            assertEquals("user", step.getCategory());
            assertEquals("step", step.getStepTitle());
            assertEquals(1, step.getDuration());
            assertEquals(StepStatus.none, step.getStatus());

            lifecycle.verify(StepLifecycle::finish);
        }
    }

    @Test
    void shouldHandleNullTimestampsGracefully() {
        StepResult result = new StepResult()
            .setName("step")
            .setStatus(Status.PASSED);

        TestStep step = new TestStep();

        try (MockedStatic<StepLifecycle> lifecycle = mockLifecycle(step)) {
            reporter.afterStepStop(result);

            assertEquals("user", step.getCategory());
            assertEquals("step", step.getStepTitle());
            assertEquals(StepStatus.passed, step.getStatus());

            lifecycle.verify(StepLifecycle::finish);
        }
    }

    private StepResult createStep(Status status, long start, long stop) {
        return new StepResult()
            .setName("step")
            .setStart(start)
            .setStop(stop)
            .setStatus(status);
    }

    private MockedStatic<StepLifecycle> mockLifecycle(TestStep step) {
        MockedStatic<StepLifecycle> lifecycle = mockStatic(StepLifecycle.class);
        lifecycle.when(StepLifecycle::current).thenReturn(step);
        return lifecycle;
    }
}