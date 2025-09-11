package io.testomat.core.facade;

import io.testomat.core.util.ServiceRegistryUtil;
import io.testomat.core.artifact.ArtifactManager;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Testomatio {
    private static final ConcurrentHashMap<String, List<String>> ARTIFACT_STORAGE = new ConcurrentHashMap<>();

    public static void artifact(Method testMethod, String... directories) {
        ServiceRegistryUtil.getService(ArtifactManager.class).manageArtifact(testMethod, directories);
    }
}
