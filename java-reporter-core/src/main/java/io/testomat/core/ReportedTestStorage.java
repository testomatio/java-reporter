package io.testomat.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class ReportedTestStorage {
    private static final List<Map<String, Object>> STORAGE = new CopyOnWriteArrayList<>();

    public static void store(Map<String, Object> body) {
        STORAGE.add(body);
    }

    public static List<Map<String, Object>> getStorage() {
        return STORAGE;
    }

    public static void linkArtifactsToTests(List<ArtifactLinkData> artifactLinkData) {
        for (ArtifactLinkData data : artifactLinkData) {
            STORAGE.stream()
                   .filter(body -> data.getRid().equals(body.get("rid")))
                   .forEach(body -> body.put("artifacts", data.getLinks()));
        }
    }
}
