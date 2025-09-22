package io.testomat.core.facade;

import io.testomat.core.artifact.ArtifactManager;

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
}
