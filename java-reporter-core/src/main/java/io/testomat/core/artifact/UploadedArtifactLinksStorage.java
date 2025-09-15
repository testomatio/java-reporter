package io.testomat.core.artifact;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UploadedArtifactLinksStorage {
    public static final Map<String, List<String>> LINK_STORAGE = new ConcurrentHashMap<>();

    public static void store(String rid, List<String> links) {
        if (links.isEmpty()) {
            return;
        }
        LINK_STORAGE.put(rid, links);
    }

    public static Map<String, List<String>> getLinkStorage() {
        return LINK_STORAGE;
    }
}
