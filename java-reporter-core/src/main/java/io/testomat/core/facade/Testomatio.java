package io.testomat.core.facade;

import io.testomat.core.facade.methods.artifact.manager.ArtifactManager;
import io.testomat.core.facade.methods.label.LabelStorage;
import io.testomat.core.facade.methods.logmethod.LogStorage;
import io.testomat.core.facade.methods.meta.MetaStorage;
import io.testomat.core.step.StepLifecycle;
import io.testomat.core.step.StepStatus;
import io.testomat.core.step.StepTimer;
import io.testomat.core.step.TestStep;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

/**
 * Main public API facade for Testomat.io integration.
 * Provides simple static methods for test artifact management and reporting.
 */
public class Testomatio {
    /**
     * Registers artifact files or directories to be uploaded for the current test.
     *
     * @param directories paths to files or directories containing test artifacts
     */
    public static void artifact(String... directories) {
        ServiceRegistryUtil.getService(ArtifactManager.class).storeDirectories(directories);
    }

    /**
     * Attaches artifact directories to the current or last finished test step.
     *
     * @param directories artifact directories to attach (ignored if null or empty)
     */
    public static void stepArtifact(String... directories) {
        TestStep testStep = StepLifecycle.current();

        if (testStep == null) {
            testStep = StepLifecycle.lastFinished();
        }
        if (testStep == null || testStep.getId() == null) {
            return;
        }

        ServiceRegistryUtil.getService(ArtifactManager.class)
                .storeStepDirectories(testStep.getId(), directories);
    }

    /**
     * Executes a named test step and tracks its status, duration and errors.
     *
     * @param stepName step display name
     * @param action   code to execute inside the step
     */
    public static void step(String stepName, Runnable action) {
        TestStep step = new TestStep();
        step.setCategory("user");
        step.setStepTitle(stepName);

        long durationMillis;
        StepLifecycle.start(step);

        try {
            StepTimer.start(step.getId().toString());
            action.run();
            step.setStatus(StepStatus.passed);
        } catch (Throwable t) {
            step.setStatus(StepStatus.failed);
            step.setLog(getStackTrace(t));
            step.setError(
                    t.getMessage() == null || t.getMessage().isBlank()
                    ? t.getClass().getSimpleName()
                    : t.getClass().getSimpleName() + ": " + t.getMessage()
            );
            throwUnchecked(t);
        } finally {
            durationMillis = StepTimer.stop(step.getId().toString());
            step.setDuration(durationMillis);
            StepLifecycle.finish();
        }
    }

    static void step(String stepName, String passedLog, Runnable action) {

    }

    public static void meta(String key, String value) {
        MetaStorage.TEMP_META_STORAGE.get().put(key, value);
    }

    public static void meta(Map<String, String> metaMap) {
        MetaStorage.TEMP_META_STORAGE.get().putAll(metaMap);
    }

    public static void log(String log) {
        if (log == null || log.trim().isEmpty()) {
            return;
        }
        LogStorage.TEMP_LOG_STORAGE.get().add(log);
    }

    public static void label(String label) {
        LabelStorage.TEMP_LABEL_STORAGE.get().add(Map.of("label", label));
    }

    public static void label(String labelName, String labelValue) {
        LabelStorage.TEMP_LABEL_STORAGE.get().add(Map.of("label", labelName + ":" + labelValue));
    }

    public static void label(String labelName, List<String> labelValues) {
        if (labelValues != null && !labelValues.isEmpty()) {
            for (String labelValue : labelValues) {
                label(labelName, labelValue);
            }
        }
    }

    private static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private static void throwUnchecked(Throwable t) {
        if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        }
        if (t instanceof Error) {
            throw (Error) t;
        }
        throw new RuntimeException(t);
    }
}
