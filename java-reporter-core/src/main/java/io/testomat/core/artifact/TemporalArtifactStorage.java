package io.testomat.core.artifact;

import java.util.ArrayList;
import java.util.List;

public class TemporalArtifactStorage {
    public static final ThreadLocal<List<String>> DIRECTORIES = ThreadLocal.withInitial(ArrayList::new);

    public static void store(String dir) {
        DIRECTORIES.get().add(dir);
    }

    public static void cleanup() {
        for (String dir : DIRECTORIES.get()) {
            dir = null;
        }
    }
}
