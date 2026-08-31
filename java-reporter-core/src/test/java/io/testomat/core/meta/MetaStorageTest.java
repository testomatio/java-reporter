package io.testomat.core.meta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.testomat.core.facade.methods.meta.MetaStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

@DisplayName("MetaStorage Tests")
class MetaStorageTest {

    @BeforeEach
    void setUp() {
        // Clean storage before each test
        MetaStorage.clearTempMetaStorage();
        MetaStorage.clearLinkedMetaStorage();
    }

    @AfterEach
    void tearDown() {
        // Clean storage after each test
        MetaStorage.clearTempMetaStorage();
        MetaStorage.clearLinkedMetaStorage();
    }

    @Test
    @DisplayName("Should initialize TEMP_META_STORAGE with empty map")
    void testTempMetaStorageInitialization() {
        Map<String, String> tempStorage = MetaStorage.getTempMetaStorage();

        assertNotNull(tempStorage);
        assertTrue(tempStorage.isEmpty());
    }

    @Test
    @DisplayName("Should initialize LINKED_META_STORAGE with empty map")
    void testLinkedMetaStorageInitialization() {
        Map<String, Map<String, String>> linkedStorage = MetaStorage.getLinkedMetaStorage();

        assertNotNull(linkedStorage);
        assertTrue(linkedStorage.isEmpty());
    }

    @Test
    @DisplayName("Should store data in TEMP_META_STORAGE")
    void testStoreTempMetaData() {
        Map<String, String> tempStorage = MetaStorage.getTempMetaStorage();
        tempStorage.put("key1", "value1");

        assertEquals("value1", tempStorage.get("key1"));
        assertEquals(1, tempStorage.size());
    }

    @Test
    @DisplayName("Should store multiple entries in TEMP_META_STORAGE")
    void testStoreMultipleTempMetaData() {
        Map<String, String> tempStorage = MetaStorage.getTempMetaStorage();
        tempStorage.put("key1", "value1");
        tempStorage.put("key2", "value2");
        tempStorage.put("key3", "value3");

        assertEquals(3, tempStorage.size());
        assertEquals("value1", tempStorage.get("key1"));
        assertEquals("value2", tempStorage.get("key2"));
        assertEquals("value3", tempStorage.get("key3"));
    }

    @Test
    @DisplayName("Should store data in LINKED_META_STORAGE")
    void testStoreLinkedMetaData() {
        Map<String, String> metaData = Map.of("key1", "value1");
        MetaStorage.getLinkedMetaStorage().put("test-id", metaData);

        assertFalse(MetaStorage.getLinkedMetaStorage().isEmpty());
        assertEquals(1, MetaStorage.getLinkedMetaStorage().size());
        assertEquals(metaData, MetaStorage.getLinkedMetaStorage().get("test-id"));
    }

    @Test
    @DisplayName("Should store multiple test metadata in LINKED_META_STORAGE")
    void testStoreMultipleLinkedMetaData() {
        Map<String, String> meta1 = Map.of("key1", "value1");
        Map<String, String> meta2 = Map.of("key2", "value2");

        MetaStorage.getLinkedMetaStorage().put("test-1", meta1);
        MetaStorage.getLinkedMetaStorage().put("test-2", meta2);

        assertEquals(2, MetaStorage.getLinkedMetaStorage().size());
        assertEquals(meta1, MetaStorage.getLinkedMetaStorage().get("test-1"));
        assertEquals(meta2, MetaStorage.getLinkedMetaStorage().get("test-2"));
    }

    @Test
    @DisplayName("Should isolate TEMP_META_STORAGE between threads")
    void testTempMetaStorageThreadIsolation() throws InterruptedException {
        Thread thread1 = new Thread(() -> {
            Map<String, String> storage = MetaStorage.getTempMetaStorage();
            storage.put("thread1", "value1");
            assertEquals("value1", storage.get("thread1"));
            assertNull(storage.get("thread2"));
        });

        Thread thread2 = new Thread(() -> {
            Map<String, String> storage = MetaStorage.getTempMetaStorage();
            storage.put("thread2", "value2");
            assertEquals("value2", storage.get("thread2"));
            assertNull(storage.get("thread1"));
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();
    }

    @Test
    @DisplayName("Should allow concurrent access to LINKED_META_STORAGE")
    void testLinkedMetaStorageConcurrency() throws InterruptedException {
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                Map<String, String> meta = Map.of("key" + index, "value" + index);
                MetaStorage.getLinkedMetaStorage().put("test-" + index, meta);
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        assertEquals(threadCount, MetaStorage.getLinkedMetaStorage().size());
    }

    @Test
    @DisplayName("Should clear TEMP_META_STORAGE")
    void testClearTempMetaStorage() {
        Map<String, String> tempStorage = MetaStorage.getTempMetaStorage();
        tempStorage.put("key1", "value1");
        tempStorage.put("key2", "value2");

        tempStorage.clear();

        assertTrue(tempStorage.isEmpty());
    }

    @Test
    @DisplayName("Should clear LINKED_META_STORAGE")
    void testClearLinkedMetaStorage() {
        MetaStorage.getLinkedMetaStorage().put("test-1", Map.of("key1", "value1"));
        MetaStorage.getLinkedMetaStorage().put("test-2", Map.of("key2", "value2"));

        MetaStorage.clearLinkedMetaStorage();

        assertTrue(MetaStorage.getLinkedMetaStorage().isEmpty());
    }

    @Test
    @DisplayName("Should update existing value in TEMP_META_STORAGE")
    void testUpdateTempMetaStorageValue() {
        Map<String, String> tempStorage = MetaStorage.getTempMetaStorage();
        tempStorage.put("key1", "value1");
        tempStorage.put("key1", "updated-value");

        assertEquals("updated-value", tempStorage.get("key1"));
        assertEquals(1, tempStorage.size());
    }

    @Test
    @DisplayName("Should update existing entry in LINKED_META_STORAGE")
    void testUpdateLinkedMetaStorageEntry() {
        Map<String, String> meta1 = Map.of("key1", "value1");
        Map<String, String> meta2 = Map.of("key2", "value2");

        MetaStorage.getLinkedMetaStorage().put("test-1", meta1);
        MetaStorage.getLinkedMetaStorage().put("test-1", meta2);

        assertEquals(1, MetaStorage.getLinkedMetaStorage().size());
        assertEquals(meta2, MetaStorage.getLinkedMetaStorage().get("test-1"));
    }

    @Test
    @DisplayName("Should remove entry from TEMP_META_STORAGE")
    void testRemoveTempMetaStorageEntry() {
        Map<String, String> tempStorage = MetaStorage.getTempMetaStorage();
        tempStorage.put("key1", "value1");
        tempStorage.put("key2", "value2");

        tempStorage.remove("key1");

        assertEquals(1, tempStorage.size());
        assertNull(tempStorage.get("key1"));
        assertEquals("value2", tempStorage.get("key2"));
    }

    @Test
    @DisplayName("Should remove entry from LINKED_META_STORAGE")
    void testRemoveLinkedMetaStorageEntry() {
        MetaStorage.getLinkedMetaStorage().put("test-1", Map.of("key1", "value1"));
        MetaStorage.getLinkedMetaStorage().put("test-2", Map.of("key2", "value2"));

        MetaStorage.getLinkedMetaStorage().remove("test-1");

        assertEquals(1, MetaStorage.getLinkedMetaStorage().size());
        assertNull(MetaStorage.getLinkedMetaStorage().get("test-1"));
        assertNotNull(MetaStorage.getLinkedMetaStorage().get("test-2"));
    }

    @Test
    @DisplayName("Should support complex metadata structures")
    void testComplexMetadataStructures() {
        Map<String, String> complexMeta = Map.of(
                "environment", "production",
                "browser", "chrome",
                "version", "120.0",
                "os", "windows"
        );

        MetaStorage.getLinkedMetaStorage().put("test-complex", complexMeta);

        Map<String, String> retrieved = MetaStorage.getLinkedMetaStorage().get("test-complex");
        assertNotNull(retrieved);
        assertEquals(4, retrieved.size());
        assertEquals("production", retrieved.get("environment"));
        assertEquals("chrome", retrieved.get("browser"));
    }

    @Test
    @DisplayName("Should handle ThreadLocal cleanup properly")
    void testThreadLocalCleanup() {
        Map<String, String> tempStorage = MetaStorage.getTempMetaStorage();
        tempStorage.put("key1", "value1");

        MetaStorage.clearTempMetaStorage();

        Map<String, String> newStorage = MetaStorage.getTempMetaStorage();
        assertTrue(newStorage.isEmpty());
    }
}
