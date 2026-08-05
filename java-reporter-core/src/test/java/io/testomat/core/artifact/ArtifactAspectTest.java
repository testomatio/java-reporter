package io.testomat.core.artifact;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.testomat.core.facade.methods.artifact.ArtifactAspect;
import io.testomat.core.facade.methods.artifact.TempArtifactDirectoriesStorage;
import io.testomat.core.facade.methods.artifact.manager.ArtifactManager;
import io.testomat.core.step.StepLifecycle;
import io.testomat.core.step.TestStep;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.*;

class ArtifactAspectTest {

    private static final String FILE_NAME = "test-artifact.txt";
    private final ArtifactAspect aspect = new ArtifactAspect();
    private File tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("test-artifact-", ".txt");
        tempFile.deleteOnExit();
        TempArtifactDirectoriesStorage.DIRECTORIES.get().clear();
    }

    @AfterEach
    void tearDown() {
        TempArtifactDirectoriesStorage.DIRECTORIES.remove();
    }

    @Test
    void shouldSendStringArtifact() {
        try (MockedStatic<StepLifecycle> lifecycle = mockStatic(StepLifecycle.class)) {

            lifecycle.when(StepLifecycle::current).thenReturn(null);
            lifecycle.when(StepLifecycle::lastFinished).thenReturn(null);

            aspect.afterArtifact(tempFile.getAbsolutePath());

            List<String> dirs = TempArtifactDirectoriesStorage.DIRECTORIES.get();
            assertTrue(dirs.contains(tempFile.getAbsolutePath()));
        }
    }

    @Test
    void shouldSendPathArtifact() {
        try (MockedStatic<StepLifecycle> lifecycle = mockStatic(StepLifecycle.class)) {

            lifecycle.when(StepLifecycle::current).thenReturn(null);
            lifecycle.when(StepLifecycle::lastFinished).thenReturn(null);

            aspect.afterArtifact(tempFile.toPath());

            List<String> dirs = TempArtifactDirectoriesStorage.DIRECTORIES.get();
            assertTrue(dirs.contains(tempFile.getAbsolutePath()));
        }
    }

    @Test
    void shouldSendFileArtifact() {
        try (MockedStatic<StepLifecycle> lifecycle = mockStatic(StepLifecycle.class)) {

            lifecycle.when(StepLifecycle::current).thenReturn(null);
            lifecycle.when(StepLifecycle::lastFinished).thenReturn(null);

            aspect.afterArtifact(tempFile);

            List<String> dirs = TempArtifactDirectoriesStorage.DIRECTORIES.get();
            assertTrue(dirs.contains(tempFile.getAbsolutePath()));
        }
    }

    @Test
    void shouldSendStepArtifactWhenCurrentStepExists() {
        try (MockedStatic<StepLifecycle> lifecycle = mockStatic(StepLifecycle.class)) {

            TestStep step = mock(TestStep.class);
            when(step.getId()).thenReturn(UUID.randomUUID());
            lifecycle.when(StepLifecycle::current).thenReturn(step);

            aspect.afterArtifact(tempFile.getAbsolutePath());

            List<String> dirs = TempArtifactDirectoriesStorage.STEP_DATA
                .get(Thread.currentThread().getId())
                .get(step.getId())
                .getDirectories();
            assertTrue(dirs.contains(tempFile.getAbsolutePath()));
        }
    }

    @Test
    void shouldSendStepArtifactWhenLastFinishedStepExists() {
        try (MockedStatic<StepLifecycle> lifecycle = mockStatic(StepLifecycle.class)) {

            TestStep step = mock(TestStep.class);
            when(step.getId()).thenReturn(UUID.randomUUID());
            lifecycle.when(StepLifecycle::current).thenReturn(null);
            lifecycle.when(StepLifecycle::lastFinished).thenReturn(step);

            aspect.afterArtifact(tempFile.getAbsolutePath());

            List<String> dirs = TempArtifactDirectoriesStorage.STEP_DATA
                .get(Thread.currentThread().getId())
                .get(step.getId())
                .getDirectories();
            assertTrue(dirs.contains(tempFile.getAbsolutePath()));
        }
    }

    @Test
    void shouldSendArtifactWhenStepHasNullId() {
        try (MockedStatic<StepLifecycle> lifecycle = mockStatic(StepLifecycle.class)) {

            TestStep step = mock(TestStep.class);
            when(step.getId()).thenReturn(null);
            lifecycle.when(StepLifecycle::current).thenReturn(step);

            aspect.afterArtifact(tempFile.getAbsolutePath());

            List<String> dirs = TempArtifactDirectoriesStorage.DIRECTORIES.get();
            assertTrue(dirs.contains(tempFile.getAbsolutePath()));
        }
    }

    @Test
    void shouldIgnoreUnsupportedType() {
        aspect.afterArtifact(new Object());

        assertTrue(TempArtifactDirectoriesStorage.DIRECTORIES.get().isEmpty());
    }

    @Test
    void shouldIgnoreNull() {
        aspect.afterArtifact(null);

        assertTrue(TempArtifactDirectoriesStorage.DIRECTORIES.get().isEmpty());
    }
}