package io.testomat.core.artifact.util;

import java.nio.file.Paths;

public class ArtifactKeyGenerator {
    private static String runId;
    private static final String SEPARATOR = "/";

    public static void initializeRunId(Object runId) {
        ArtifactKeyGenerator.runId = runId.toString();
    }

    public String generateKey(String dir, String rid, String testName) {
        return runId
                + SEPARATOR
                + rid
                + SEPARATOR
                + Paths.get(dir).getFileName();
    }
}
