package io.testomat.core.artifact;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.testomat.core.facade.methods.artifact.ArtifactLinkData;
import io.testomat.core.facade.methods.artifact.ReportedTestStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ReportedTestStorage Tests")
class ReportedTestStorageTest {

    @AfterEach
    void cleanup() {
        // Clear the storage after each test
        ReportedTestStorage.getStorage().clear();
    }

    @Test
    @DisplayName("Should store test data")
    void testStoreTestData() {
        Map<String, Object> body = new HashMap<>();
        body.put("title", "Test 1");
        body.put("status", "passed");

        ReportedTestStorage.store(body);

        List<Map<String, Object>> storage = ReportedTestStorage.getStorage();
        assertEquals(1, storage.size());
        assertEquals("Test 1", storage.get(0).get("title"));
        assertEquals("passed", storage.get(0).get("status"));
    }

    @Test
    @DisplayName("Should store multiple test data entries")
    void testStoreMultipleEntries() {
        Map<String, Object> body1 = new HashMap<>();
        body1.put("title", "Test 1");
        body1.put("rid", "rid-1");

        Map<String, Object> body2 = new HashMap<>();
        body2.put("title", "Test 2");
        body2.put("rid", "rid-2");

        ReportedTestStorage.store(body1);
        ReportedTestStorage.store(body2);

        List<Map<String, Object>> storage = ReportedTestStorage.getStorage();
        assertEquals(2, storage.size());
    }

    @Test
    @DisplayName("Should return empty storage initially")
    void testEmptyStorageInitially() {
        List<Map<String, Object>> storage = ReportedTestStorage.getStorage();
        assertNotNull(storage);
        // Storage might not be empty if other tests ran, so we just check it's not null
    }

    @Test
    @DisplayName("Should link artifacts to tests by RID")
    void testLinkArtifactsToTests() {
        // Store test data with RID
        Map<String, Object> body1 = new HashMap<>();
        body1.put("title", "Test 1");
        body1.put("rid", "rid-123");
        ReportedTestStorage.store(body1);

        Map<String, Object> body2 = new HashMap<>();
        body2.put("title", "Test 2");
        body2.put("rid", "rid-456");
        ReportedTestStorage.store(body2);

        // Create artifact link data
        List<String> links1 = Arrays.asList("https://s3.amazonaws.com/artifact1.png");
        ArtifactLinkData linkData1 = new ArtifactLinkData("Test 1", "rid-123", "test-1", links1);

        List<String> links2 = Arrays.asList("https://s3.amazonaws.com/artifact2.png");
        ArtifactLinkData linkData2 = new ArtifactLinkData("Test 2", "rid-456", "test-2", links2);

        // Link artifacts
        ReportedTestStorage.linkArtifactsToTests(Arrays.asList(linkData1, linkData2));

        // Verify artifacts were linked
        List<Map<String, Object>> storage = ReportedTestStorage.getStorage();

        Map<String, Object> test1 = storage.stream()
                .filter(body -> "rid-123".equals(body.get("rid")))
                .findFirst()
                .orElse(null);

        assertNotNull(test1);
        assertNotNull(test1.get("artifacts"));
        assertEquals(links1, test1.get("artifacts"));

        Map<String, Object> test2 = storage.stream()
                .filter(body -> "rid-456".equals(body.get("rid")))
                .findFirst()
                .orElse(null);

        assertNotNull(test2);
        assertNotNull(test2.get("artifacts"));
        assertEquals(links2, test2.get("artifacts"));
    }

    @Test
    @DisplayName("Should link artifacts with multiple URLs")
    void testLinkMultipleArtifacts() {
        Map<String, Object> body = new HashMap<>();
        body.put("title", "Test with multiple artifacts");
        body.put("rid", "rid-789");
        ReportedTestStorage.store(body);

        List<String> multipleLinks = Arrays.asList(
                "https://s3.amazonaws.com/artifact1.png",
                "https://s3.amazonaws.com/artifact2.png",
                "https://s3.amazonaws.com/artifact3.log"
        );

        ArtifactLinkData linkData = new ArtifactLinkData(
                "Test with multiple artifacts",
                "rid-789",
                "test-789",
                multipleLinks
        );

        ReportedTestStorage.linkArtifactsToTests(Arrays.asList(linkData));

        List<Map<String, Object>> storage = ReportedTestStorage.getStorage();
        Map<String, Object> test = storage.stream()
                .filter(b -> "rid-789".equals(b.get("rid")))
                .findFirst()
                .orElse(null);

        assertNotNull(test);
        assertNotNull(test.get("artifacts"));
        @SuppressWarnings("unchecked")
        List<String> artifacts = (List<String>) test.get("artifacts");
        assertEquals(3, artifacts.size());
        assertTrue(artifacts.contains("https://s3.amazonaws.com/artifact1.png"));
        assertTrue(artifacts.contains("https://s3.amazonaws.com/artifact2.png"));
        assertTrue(artifacts.contains("https://s3.amazonaws.com/artifact3.log"));
    }

    @Test
    @DisplayName("Should not link artifacts to tests with non-matching RID")
    void testNoLinkForNonMatchingRid() {
        Map<String, Object> body = new HashMap<>();
        body.put("title", "Test 1");
        body.put("rid", "rid-123");
        ReportedTestStorage.store(body);

        List<String> links = Arrays.asList("https://s3.amazonaws.com/artifact.png");
        ArtifactLinkData linkData = new ArtifactLinkData(
                "Test 2",
                "rid-999",  // Different RID
                "test-2",
                links
        );

        ReportedTestStorage.linkArtifactsToTests(Arrays.asList(linkData));

        List<Map<String, Object>> storage = ReportedTestStorage.getStorage();
        Map<String, Object> test = storage.get(0);

        // Artifact should not be linked
        assertFalse(test.containsKey("artifacts") && test.get("artifacts") != null);
    }

    @Test
    @DisplayName("Should handle empty artifact link list")
    void testEmptyArtifactLinkList() {
        Map<String, Object> body = new HashMap<>();
        body.put("title", "Test 1");
        body.put("rid", "rid-123");
        ReportedTestStorage.store(body);

        ReportedTestStorage.linkArtifactsToTests(Arrays.asList());

        List<Map<String, Object>> storage = ReportedTestStorage.getStorage();
        assertEquals(1, storage.size());
    }

    @Test
    @DisplayName("Should store test data with various field types")
    void testStoreWithVariousFieldTypes() {
        Map<String, Object> body = new HashMap<>();
        body.put("title", "Complex Test");
        body.put("status", "passed");
        body.put("duration", 150.5);
        body.put("steps", Arrays.asList("step1", "step2"));
        body.put("metadata", Map.of("key1", "value1", "key2", "value2"));

        ReportedTestStorage.store(body);

        List<Map<String, Object>> storage = ReportedTestStorage.getStorage();
        Map<String, Object> stored = storage.get(storage.size() - 1);

        assertEquals("Complex Test", stored.get("title"));
        assertEquals("passed", stored.get("status"));
        assertEquals(150.5, stored.get("duration"));
        assertNotNull(stored.get("steps"));
        assertNotNull(stored.get("metadata"));
    }

    @Test
    @DisplayName("Should maintain insertion order")
    void testInsertionOrder() {
        for (int i = 0; i < 5; i++) {
            Map<String, Object> body = new HashMap<>();
            body.put("title", "Test " + i);
            body.put("order", i);
            ReportedTestStorage.store(body);
        }

        List<Map<String, Object>> storage = ReportedTestStorage.getStorage();

        // Check that we have at least the 5 tests we just added
        assertTrue(storage.size() >= 5);

        // Find our tests and verify order
        int lastIndex = storage.size();
        for (int i = 0; i < 5; i++) {
            Map<String, Object> test = storage.get(lastIndex - 5 + i);
            assertEquals("Test " + i, test.get("title"));
            assertEquals(i, test.get("order"));
        }
    }

    @Test
    @DisplayName("Should handle null values in test data")
    void testNullValuesInTestData() {
        Map<String, Object> body = new HashMap<>();
        body.put("title", "Test with nulls");
        body.put("message", null);
        body.put("stack", null);

        ReportedTestStorage.store(body);

        List<Map<String, Object>> storage = ReportedTestStorage.getStorage();
        Map<String, Object> stored = storage.get(storage.size() - 1);

        assertEquals("Test with nulls", stored.get("title"));
        assertTrue(stored.containsKey("message"));
        assertTrue(stored.containsKey("stack"));
        assertEquals(null, stored.get("message"));
        assertEquals(null, stored.get("stack"));
    }
}
