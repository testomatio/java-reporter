package io.testomat.core.facade.methods.artifact;

import io.testomat.core.step.StepData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-local storage for temporarily holding artifact file paths during test execution.
 * Ensures thread safety when multiple tests run concurrently.
 */
public class TempArtifactDirectoriesStorage {
    public static final ThreadLocal<List<String>> DIRECTORIES =
            ThreadLocal.withInitial(ArrayList::new);
    public static final Map<Long, Map<UUID, StepData>> STEP_DATA = new ConcurrentHashMap<>();

    public static void store(String dir) {
        DIRECTORIES.get().add(dir);
    }

    public static void stepStore(UUID stepId, String dir) {
        long threadId = Thread.currentThread().getId();

        STEP_DATA
            .computeIfAbsent(threadId, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(stepId, k -> new StepData())
            .getDirectories()
                .add(dir);
    }
}
