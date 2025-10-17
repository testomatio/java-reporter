package io.testomat.core.artifact;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@DisplayName("ArtifactLinkDataStorage Tests")
class ArtifactLinkDataStorageTest {

    @BeforeEach
    void setUp() {
        // Clean storage before each test
        ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE.clear();
    }

    @AfterEach
    void tearDown() {
        // Clean storage after each test
        ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE.clear();
    }

    @Test
    @DisplayName("Should initialize with empty storage")
    void testInitializedEmpty() {
        List<ArtifactLinkData> storage = ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE;

        assertNotNull(storage);
        assertTrue(storage.isEmpty());
    }

    @Test
    @DisplayName("Should store artifact link data")
    void testStoreArtifactLinkData() {
        ArtifactLinkData data = new ArtifactLinkData("Test", "rid-1", "test-1",
                List.of("http://example.com/artifact.png"));

        ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE.add(data);

        assertFalse(ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE.isEmpty());
        assertEquals(1, ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE.size());
    }

    @Test
    @DisplayName("Should store multiple artifact link data items")
    void testStoreMultipleArtifactLinkData() {
        ArtifactLinkData data1 = new ArtifactLinkData("Test1", "rid-1", "test-1",
                List.of("http://example.com/1.png"));
        ArtifactLinkData data2 = new ArtifactLinkData("Test2", "rid-2", "test-2",
                List.of("http://example.com/2.png"));
        ArtifactLinkData data3 = new ArtifactLinkData("Test3", "rid-3", "test-3",
                List.of("http://example.com/3.png"));

        ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE.add(data1);
        ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE.add(data2);
        ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE.add(data3);

        assertEquals(3, ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE.size());
    }

    @Test
    @DisplayName("Should retrieve stored artifact link data")
    void testRetrieveStoredData() {
        ArtifactLinkData data = new ArtifactLinkData("TestName", "rid-123", "test-123",
                List.of("http://example.com/artifact.png"));

        ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE.add(data);
        ArtifactLinkData retrieved = ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE.get(0);

        assertNotNull(retrieved);
        assertEquals("TestName", retrieved.getTestName());
        assertEquals("rid-123", retrieved.getRid());
        assertEquals("test-123", retrieved.getTestId());
    }

    @Test
    @DisplayName("Should clear all stored data")
    void testClearStorage() {
        ArtifactLinkData data1 = new ArtifactLinkData("Test1", "rid-1", "test-1",
                List.of("http://example.com/1.png"));
        ArtifactLinkData data2 = new ArtifactLinkData("Test2", "rid-2", "test-2",
                List.of("http://example.com/2.png"));

        ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE.add(data1);
        ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE.add(data2);

        assertEquals(2, ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE.size());

        ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE.clear();

        assertTrue(ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE.isEmpty());
    }

    @Test
    @DisplayName("Should be thread-safe with CopyOnWriteArrayList")
    void testThreadSafety() throws InterruptedException {
        int threadCount = 10;
        int itemsPerThread = 5;
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < itemsPerThread; j++) {
                    String testName = "thread-" + threadIndex + "-test-" + j;
                    String rid = "rid-" + threadIndex + "-" + j;
                    String testId = "test-" + threadIndex + "-" + j;
                    ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE.add(
                            new ArtifactLinkData(testName, rid, testId,
                                    List.of("http://example.com/artifact.png"))
                    );
                }
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        assertEquals(threadCount * itemsPerThread,
                ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE.size());
    }

    @Test
    @DisplayName("Should allow removal of specific items")
    void testRemoveSpecificItem() {
        ArtifactLinkData data1 = new ArtifactLinkData("Test1", "rid-1", "test-1",
                List.of("http://example.com/1.png"));
        ArtifactLinkData data2 = new ArtifactLinkData("Test2", "rid-2", "test-2",
                List.of("http://example.com/2.png"));
        ArtifactLinkData data3 = new ArtifactLinkData("Test3", "rid-3", "test-3",
                List.of("http://example.com/3.png"));

        ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE.add(data1);
        ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE.add(data2);
        ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE.add(data3);

        ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE.remove(data2);

        assertEquals(2, ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE.size());
        assertFalse(ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE.contains(data2));
        assertTrue(ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE.contains(data1));
        assertTrue(ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE.contains(data3));
    }

    @Test
    @DisplayName("Should maintain order of insertion")
    void testOrderOfInsertion() {
        ArtifactLinkData data1 = new ArtifactLinkData("Test1", "rid-1", "test-1",
                List.of("http://example.com/1.png"));
        ArtifactLinkData data2 = new ArtifactLinkData("Test2", "rid-2", "test-2",
                List.of("http://example.com/2.png"));
        ArtifactLinkData data3 = new ArtifactLinkData("Test3", "rid-3", "test-3",
                List.of("http://example.com/3.png"));

        ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE.add(data1);
        ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE.add(data2);
        ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE.add(data3);

        List<ArtifactLinkData> stored = ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE;

        assertEquals("Test1", stored.get(0).getTestName());
        assertEquals("Test2", stored.get(1).getTestName());
        assertEquals("Test3", stored.get(2).getTestName());
    }

    @Test
    @DisplayName("Should support iteration over stored items")
    void testIteration() {
        ArtifactLinkData data1 = new ArtifactLinkData("Test1", "rid-1", "test-1",
                List.of("http://example.com/1.png"));
        ArtifactLinkData data2 = new ArtifactLinkData("Test2", "rid-2", "test-2",
                List.of("http://example.com/2.png"));

        ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE.add(data1);
        ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE.add(data2);

        int count = 0;
        for (ArtifactLinkData data : ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE) {
            assertNotNull(data);
            count++;
        }

        assertEquals(2, count);
    }
}
