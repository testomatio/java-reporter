package io.testomat.core.artifact;

import java.util.ArrayList;
import java.util.List;

public class TempArtifactDirectoriesStorage {
    public static final ThreadLocal<List<String>> DIRECTORIES = ThreadLocal.withInitial(ArrayList::new);

    public static void store(String dir) {
        DIRECTORIES.get().add(dir);
    }
}
