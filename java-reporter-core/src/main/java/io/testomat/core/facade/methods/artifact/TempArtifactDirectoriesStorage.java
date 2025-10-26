package io.testomat.core.facade.methods.artifact;

import java.util.ArrayList;
import java.util.List;

/**
 * Thread-local storage for temporarily holding artifact file paths during test execution.
 * Ensures thread safety when multiple tests run concurrently.
 */
public class TempArtifactDirectoriesStorage {
    public static final ThreadLocal<List<String>> DIRECTORIES = ThreadLocal.withInitial(ArrayList::new);

    public static void store(String dir) {
        DIRECTORIES.get().add(dir);
    }
}
