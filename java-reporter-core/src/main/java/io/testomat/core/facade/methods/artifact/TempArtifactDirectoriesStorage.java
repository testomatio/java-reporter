package io.testomat.core.facade.methods.artifact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-local storage for temporarily holding artifact file paths during test execution.
 * Ensures thread safety when multiple tests run concurrently.
 */
public class TempArtifactDirectoriesStorage {
    public static final ThreadLocal<List<String>> DIRECTORIES = ThreadLocal.withInitial(ArrayList::new);
    public static final Map<UUID, List<String>> STEP_DIRECTORIES = new ConcurrentHashMap<>();

    public static void store(String dir) {
        DIRECTORIES.get().add(dir);
    }
    public static void stepStore(UUID stepId, String dir) {
        STEP_DIRECTORIES
            .computeIfAbsent(stepId,
            k -> Collections.synchronizedList(new ArrayList<>()))
            .add(dir);
    }
}
