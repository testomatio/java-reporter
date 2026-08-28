package io.testomat.core.facade.methods.artifact.util;

import java.nio.file.Paths;

/**
 * Utility class for generating S3 object keys for artifact uploads.
 * Creates hierarchical key structure based on run ID, test name, and file path.
 */
public class ArtifactKeyGenerator {
    private static String runId;
    private static final String SEPARATOR = "/";

    public static void initializeRunId(Object runId) {
        ArtifactKeyGenerator.runId = runId.toString();
    }

    public String generateKey(String dir, String rid, String testName) {
        return runId
                + SEPARATOR
                + testName + "::"
                + rid
                + SEPARATOR
                + Paths.get(dir).getFileName();
    }
}
