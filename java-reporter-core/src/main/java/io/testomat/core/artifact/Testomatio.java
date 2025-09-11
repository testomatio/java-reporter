package io.testomat.core.artifact;

import io.testomat.core.ServiceRegistry;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Testomatio {
    private static final ConcurrentHashMap<String, List<String>> ARTIFACT_STORAGE = new ConcurrentHashMap<>();

    public static void artifact(Method testMethod, String... directories) {
        ServiceRegistry.getService(ArtifactManager.class).manageArtifact(testMethod, directories);
    }
}
