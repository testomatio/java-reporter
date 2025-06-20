package com.testomatio.reporter.property_config.provider;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultPropertyStorageTest {

    @Test
    void testDefaultsMapIsInitialized() {
        assertNotNull(DefaultPropertyStorage.DEFAULTS);
        assertFalse(DefaultPropertyStorage.DEFAULTS.isEmpty());
    }

    @Test
    void testAllExpectedPropertiesPresent() {
        assertTrue(DefaultPropertyStorage.DEFAULTS.containsKey("testomatio.batch.size"));
        assertTrue(DefaultPropertyStorage.DEFAULTS.containsKey("testomatio.batch.flush.interval"));
        assertTrue(DefaultPropertyStorage.DEFAULTS.containsKey("testomatio.url"));
        assertTrue(DefaultPropertyStorage.DEFAULTS.containsKey("testomatio.run.title"));
        assertTrue(DefaultPropertyStorage.DEFAULTS.containsKey("testomatio.log.level"));
        assertTrue(DefaultPropertyStorage.DEFAULTS.containsKey("testomatio.log.file"));
        assertTrue(DefaultPropertyStorage.DEFAULTS.containsKey("testomatio.log.console"));
    }

    @Test
    void testDefaultPropertyValues() {
        assertEquals("10", DefaultPropertyStorage.DEFAULTS.get("testomatio.batch.size"));
        assertEquals("10", DefaultPropertyStorage.DEFAULTS.get("testomatio.batch.flush.interval"));
        assertEquals("https://app.testomat.io/", DefaultPropertyStorage.DEFAULTS.get("testomatio.url"));
        assertEquals("Default Run Title", DefaultPropertyStorage.DEFAULTS.get("testomatio.run.title"));
        assertEquals("INFO", DefaultPropertyStorage.DEFAULTS.get("testomatio.log.level"));
        assertEquals("logs/testomatio.log", DefaultPropertyStorage.DEFAULTS.get("testomatio.log.file"));
        assertEquals("true", DefaultPropertyStorage.DEFAULTS.get("testomatio.log.console"));
    }

    @Test
    void testMapSize() {
        assertEquals(7, DefaultPropertyStorage.DEFAULTS.size());
    }

    @Test
    void testMapIsImmutable() {
        // Test that we cannot modify the map
        assertThrows(UnsupportedOperationException.class, () -> {
            DefaultPropertyStorage.DEFAULTS.put("new.key", "new.value");
        });

        assertThrows(UnsupportedOperationException.class, () -> {
            DefaultPropertyStorage.DEFAULTS.remove("testomatio.batch.size");
        });

        assertThrows(UnsupportedOperationException.class, () -> {
            DefaultPropertyStorage.DEFAULTS.clear();
        });
    }
}