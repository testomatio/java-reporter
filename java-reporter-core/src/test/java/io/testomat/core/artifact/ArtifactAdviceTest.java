package io.testomat.core.artifact;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.testomat.core.facade.methods.artifact.ArtifactAdvice;
import io.testomat.core.facade.ServiceRegistryUtil;
import io.testomat.core.facade.methods.artifact.manager.ArtifactManager;
import io.testomat.core.step.StepLifecycle;
import io.testomat.core.step.TestStep;
import java.io.File;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class ArtifactAdviceTest {

    @Test
    void shouldSendStringArtifact() {
        try (MockedStatic<StepLifecycle> lifecycle = mockStatic(StepLifecycle.class);
             MockedStatic<ServiceRegistryUtil> services = mockStatic(ServiceRegistryUtil.class)) {
            ArtifactManager artifactManager = mock(ArtifactManager.class);
            services.when(() -> ServiceRegistryUtil.getService(ArtifactManager.class))
                    .thenReturn(artifactManager);
            lifecycle.when(StepLifecycle::current).thenReturn(null);
            lifecycle.when(StepLifecycle::lastFinished).thenReturn(null);

            ArtifactAdvice.exit("file.txt", null);

            verify(artifactManager).storeDirectories("file.txt");
        }
    }

    @Test
    void shouldSendPathArtifact() {
        try (MockedStatic<StepLifecycle> lifecycle = mockStatic(StepLifecycle.class);
             MockedStatic<ServiceRegistryUtil> services = mockStatic(ServiceRegistryUtil.class)) {
            ArtifactManager artifactManager = mock(ArtifactManager.class);
            services.when(() -> ServiceRegistryUtil.getService(ArtifactManager.class))
                    .thenReturn(artifactManager);
            lifecycle.when(StepLifecycle::current).thenReturn(null);
            lifecycle.when(StepLifecycle::lastFinished).thenReturn(null);

            ArtifactAdvice.exit(Path.of("file.txt"), null);

            verify(artifactManager).storeDirectories("file.txt");
        }
    }

    @Test
    void shouldSendFileArtifact() {
        try (MockedStatic<StepLifecycle> lifecycle = mockStatic(StepLifecycle.class);
             MockedStatic<ServiceRegistryUtil> services = mockStatic(ServiceRegistryUtil.class)) {
            ArtifactManager artifactManager = mock(ArtifactManager.class);
            services.when(() -> ServiceRegistryUtil.getService(ArtifactManager.class))
                    .thenReturn(artifactManager);
            lifecycle.when(StepLifecycle::current).thenReturn(null);
            lifecycle.when(StepLifecycle::lastFinished).thenReturn(null);

            ArtifactAdvice.exit(new File("file.txt"), null);

            verify(artifactManager).storeDirectories(new File("file.txt").getAbsolutePath());
        }
    }

    @Test
    void shouldSendStepArtifactWhenCurrentStepExists() {
        try (MockedStatic<StepLifecycle> lifecycle = mockStatic(StepLifecycle.class);
             MockedStatic<ServiceRegistryUtil> services = mockStatic(ServiceRegistryUtil.class)) {
            ArtifactManager artifactManager = mock(ArtifactManager.class);
            UUID stepId = UUID.randomUUID();
            TestStep step = mock(TestStep.class);
            services.when(() -> ServiceRegistryUtil.getService(ArtifactManager.class))
                    .thenReturn(artifactManager);
            when(step.getId()).thenReturn(stepId);
            lifecycle.when(StepLifecycle::current).thenReturn(step);

            ArtifactAdvice.exit("file.txt", null);

            verify(artifactManager).storeStepDirectories(stepId, "file.txt");
        }
    }

    @Test
    void shouldSendStepArtifactWhenLastFinishedStepExists() {
        try (MockedStatic<StepLifecycle> lifecycle = mockStatic(StepLifecycle.class);
             MockedStatic<ServiceRegistryUtil> services = mockStatic(ServiceRegistryUtil.class)) {
            ArtifactManager artifactManager = mock(ArtifactManager.class);
            UUID stepId = UUID.randomUUID();
            TestStep step = mock(TestStep.class);
            services.when(() -> ServiceRegistryUtil.getService(ArtifactManager.class))
                    .thenReturn(artifactManager);
            when(step.getId()).thenReturn(stepId);
            lifecycle.when(StepLifecycle::current).thenReturn(null);
            lifecycle.when(StepLifecycle::lastFinished).thenReturn(step);

            ArtifactAdvice.exit("file.txt", null);

            verify(artifactManager).storeStepDirectories(stepId, "file.txt");
        }
    }

    @Test
    void shouldSendArtifactWhenStepHasNullId() {
        try (MockedStatic<StepLifecycle> lifecycle = mockStatic(StepLifecycle.class);
             MockedStatic<ServiceRegistryUtil> services = mockStatic(ServiceRegistryUtil.class)) {
            ArtifactManager artifactManager = mock(ArtifactManager.class);
            TestStep step = mock(TestStep.class);
            services.when(() -> ServiceRegistryUtil.getService(ArtifactManager.class))
                    .thenReturn(artifactManager);
            when(step.getId()).thenReturn(null);
            lifecycle.when(StepLifecycle::current).thenReturn(step);

            ArtifactAdvice.exit("file.txt", null);

            verify(artifactManager).storeDirectories("file.txt");
        }
    }

    @Test
    void shouldIgnoreUnsupportedType() {
        ArtifactAdvice.exit(new Object(), null);
    }

    @Test
    void shouldIgnoreNull() {
        ArtifactAdvice.exit(null, null);
    }
}
