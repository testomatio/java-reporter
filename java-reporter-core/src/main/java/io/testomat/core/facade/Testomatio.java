package io.testomat.core.facade;

import io.testomat.core.artifact.ArtifactManager;

public class Testomatio {

    public static void artifact(String... directories) {
        ServiceRegistryUtil.getService(ArtifactManager.class).storeDirectories(directories);
    }
}
