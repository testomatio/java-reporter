package io.testomat.core.facade.methods.artifact.client;

import java.nio.file.Path;
import java.util.UUID;

/**
 * Static entry point for asynchronous artifact uploads.
 */
public final class UploadPipeline {

    /**
     * Shared artifact upload manager instance.
     */
    private static volatile ArtifactUploadManager manager;

    private UploadPipeline() {
    }

    /**
     * Returns the shared upload manager instance, creating it lazily if necessary.
     */
    private static ArtifactUploadManager getManager() {
        if (manager == null) {
            synchronized (UploadPipeline.class) {
                if (manager == null) {
                    manager = new ArtifactUploadManager();
                }
            }
        }
        return manager;
    }

    /**
     * Publishes artifact upload tasks for asynchronous processing.
     */
    public static void publish(UUID stepId, String... directories) {
        if (directories == null) {
            return;
        }
        for (String dir : directories) {
            if (dir == null || dir.isBlank()) {
                continue;
            }
            getManager().publish(stepId, Path.of(dir));
        }
    }

    /**
     * Gracefully shuts down the upload pipeline.
     */
    public static void shutdown() {
        if (manager != null) {
            manager.shutdown();
        }
    }
}
