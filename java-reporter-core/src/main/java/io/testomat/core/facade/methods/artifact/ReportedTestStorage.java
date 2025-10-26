package io.testomat.core.facade.methods.artifact;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread-safe storage for reported test data with artifact linking capabilities.
 * Maintains test execution results and allows linking artifacts to specific tests by RID.
 */
public class ReportedTestStorage {
    private static final Logger log = LoggerFactory.getLogger(ReportedTestStorage.class);
    private static final List<Map<String, Object>> STORAGE = new CopyOnWriteArrayList<>();

    /**
     * Stores test execution data.
     *
     * @param body test data map containing test results and metadata
     */
    public static void store(Map<String, Object> body) {
        STORAGE.add(body);
        log.debug("Stored body: {}", body);
    }

    /**
     * Returns all stored test data.
     *
     * @return list of test data maps
     */
    public static List<Map<String, Object>> getStorage() {
        return STORAGE;
    }

    /**
     * Links artifacts to their corresponding tests using RID matching.
     *
     * @param artifactLinkData list of artifact link data to associate with tests
     */
    public static void linkArtifactsToTests(List<ArtifactLinkData> artifactLinkData) {
        for (ArtifactLinkData data : artifactLinkData) {
            STORAGE.stream()
                    .filter(body -> data.getRid().equals(body.get("rid")))
                    .forEach(body -> body.put("artifacts", data.getLinks()));
        }
        for (ArtifactLinkData data : artifactLinkData) {
            log.debug("Linked: testId - {}, testName - {}, rid - {}, links - {}",
                    data.getTestId(), data.getTestName(), data.getRid(), data.getLinks().get(0));
        }
    }
}
