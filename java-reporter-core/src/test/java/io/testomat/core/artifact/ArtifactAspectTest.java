package io.testomat.core.artifact;

import io.testomat.core.facade.Testomatio;
import io.testomat.core.facade.methods.artifact.ArtifactAspect;
import io.testomat.core.step.StepLifecycle;
import io.testomat.core.step.TestStep;
import java.io.File;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.*;

class ArtifactAspectTest {

    private final ArtifactAspect aspect = new ArtifactAspect();

    @Test
    void shouldSendStringArtifact() {
        try (MockedStatic<StepLifecycle> lifecycle = mockStatic(StepLifecycle.class);
            MockedStatic<Testomatio> testomatio = mockStatic(Testomatio.class)) {

            lifecycle.when(StepLifecycle::current).thenReturn(null);
            lifecycle.when(StepLifecycle::lastFinished).thenReturn(null);

            aspect.afterArtifact("file.txt");

            testomatio.verify(() -> Testomatio.artifact("file.txt"));
        }
    }

    @Test
    void shouldSendPathArtifact() {
        try (MockedStatic<StepLifecycle> lifecycle = mockStatic(StepLifecycle.class);
            MockedStatic<Testomatio> testomatio = mockStatic(Testomatio.class)) {

            lifecycle.when(StepLifecycle::current).thenReturn(null);
            lifecycle.when(StepLifecycle::lastFinished).thenReturn(null);

            Path path = Path.of("file.txt");

            aspect.afterArtifact(path);

            testomatio.verify(() -> Testomatio.artifact(path.toString()));
        }
    }

    @Test
    void shouldSendFileArtifact() {
        try (MockedStatic<StepLifecycle> lifecycle = mockStatic(StepLifecycle.class);
            MockedStatic<Testomatio> testomatio = mockStatic(Testomatio.class)) {

            lifecycle.when(StepLifecycle::current).thenReturn(null);
            lifecycle.when(StepLifecycle::lastFinished).thenReturn(null);

            File file = new File("file.txt");

            aspect.afterArtifact(file);

            testomatio.verify(() -> Testomatio.artifact(file.getAbsolutePath()));
        }
    }

    @Test
    void shouldSendStepArtifactWhenCurrentStepExists() {
        try (MockedStatic<StepLifecycle> lifecycle = mockStatic(StepLifecycle.class);
            MockedStatic<Testomatio> testomatio = mockStatic(Testomatio.class)) {

            TestStep step = mock(TestStep.class);

            when(step.getId()).thenReturn(UUID.randomUUID());

            lifecycle.when(StepLifecycle::current).thenReturn(step);

            aspect.afterArtifact("file.txt");

            testomatio.verify(() -> Testomatio.stepArtifact("file.txt"));
        }
    }

    @Test
    void shouldSendStepArtifactWhenLastFinishedStepExists() {
        try (MockedStatic<StepLifecycle> lifecycle = mockStatic(StepLifecycle.class);
            MockedStatic<Testomatio> testomatio = mockStatic(Testomatio.class)) {

            TestStep step = mock(TestStep.class);

            when(step.getId()).thenReturn(UUID.randomUUID());

            lifecycle.when(StepLifecycle::current).thenReturn(null);
            lifecycle.when(StepLifecycle::lastFinished).thenReturn(step);

            aspect.afterArtifact("file.txt");

            testomatio.verify(() -> Testomatio.stepArtifact("file.txt"));
        }
    }

    @Test
    void shouldSendArtifactWhenStepHasNullId() {
        try (MockedStatic<StepLifecycle> lifecycle = mockStatic(StepLifecycle.class);
            MockedStatic<Testomatio> testomatio = mockStatic(Testomatio.class)) {

            TestStep step = mock(TestStep.class);

            when(step.getId()).thenReturn(null);

            lifecycle.when(StepLifecycle::current).thenReturn(step);

            aspect.afterArtifact("file.txt");

            testomatio.verify(() -> Testomatio.artifact("file.txt"));
        }
    }

    @Test
    void shouldIgnoreUnsupportedType() {
        try (MockedStatic<StepLifecycle> lifecycle = mockStatic(StepLifecycle.class);
            MockedStatic<Testomatio> testomatio = mockStatic(Testomatio.class)) {

            aspect.afterArtifact(new Object());

            testomatio.verifyNoInteractions();
        }
    }

    @Test
    void shouldIgnoreNull() {
        try (MockedStatic<StepLifecycle> lifecycle = mockStatic(StepLifecycle.class);
            MockedStatic<Testomatio> testomatio = mockStatic(Testomatio.class)) {

            aspect.afterArtifact(null);

            testomatio.verifyNoInteractions();
        }
    }
}