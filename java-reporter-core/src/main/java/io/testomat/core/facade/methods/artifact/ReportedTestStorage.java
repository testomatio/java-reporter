package io.testomat.core.facade.methods.artifact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
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
     * Stores test execution metadata in the internal storage.
     * <p>
     * The method adds a new entry containing {@code test_id} and {@code rid}
     * only if an entry with the same values does not already exist.
     * The check and insertion are performed atomically using synchronization
     * to prevent duplicates in concurrent environments.
     *
     * @param body a map containing test execution data; expected to include
     *             {@code "test_id"} and {@code "rid"} keys
     */
    public static void store(Map<String, Object> body) {

        Object testId = body.get("test_id");
        Object rid = body.get("rid");

        synchronized (STORAGE) {

            boolean exists = STORAGE.stream().anyMatch(m ->
                Objects.equals(m.get("test_id"), testId) &&
                    Objects.equals(m.get("rid"), rid)
            );

            if (!exists) {
                Map<String, Object> map = new HashMap<>();
                map.put("test_id", testId);
                map.put("rid", rid);

                STORAGE.add(map);
            }
        }

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
        Map<Object, Map<String, Object>> index =
            STORAGE.stream()
                .collect(Collectors.toMap(
                    m -> m.get("rid"),
                    m -> m
                ));

        for (ArtifactLinkData data : artifactLinkData) {
            Map<String, Object> body = index.get(data.getRid());
            if (body != null) {
                @SuppressWarnings("unchecked")
                List<String> artifacts =
                    (List<String>) body.computeIfAbsent("artifacts", k -> new ArrayList<>());
                artifacts.addAll(data.getLinks());
            }
        }

        for (ArtifactLinkData data : artifactLinkData) {
            log.debug("Linked: testId - {}, testName - {}, rid - {}, links - {}",
                    data.getTestId(), data.getTestName(), data.getRid(), data.getLinks().get(0));
        }
    }
}
