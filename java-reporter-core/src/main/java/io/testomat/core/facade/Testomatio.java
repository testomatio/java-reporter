package io.testomat.core.facade;

import io.testomat.core.facade.methods.artifact.manager.ArtifactManager;
import io.testomat.core.facade.methods.label.LabelStorage;
import io.testomat.core.facade.methods.logmethod.LogStorage;
import io.testomat.core.facade.methods.meta.MetaStorage;
import io.testomat.core.step.StepLifecycle;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    public static void stepArtifact(String... directories) {
        UUID stepId = StepLifecycle.current();
        ServiceRegistryUtil.getService(ArtifactManager.class).storeStepDirectories(stepId, directories);
    }

    public static void stepArtifact(UUID stepId, String... directories) {
        ServiceRegistryUtil.getService(ArtifactManager.class).storeStepDirectories(stepId, directories);
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
}
