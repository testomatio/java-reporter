package com.testomatio.reporter.property_config.provider;

import com.testomatio.reporter.property_config.util.DefaultPropertiesStorage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultPropertiesStorageTest {

    @Test
    void testDefaultsMapIsInitialized() {
        assertNotNull(DefaultPropertiesStorage.DEFAULTS);
        assertFalse(DefaultPropertiesStorage.DEFAULTS.isEmpty());
    }

    @Test
    void testAllExpectedPropertiesPresent() {
        assertTrue(DefaultPropertiesStorage.DEFAULTS.containsKey("testomatio.batch.size"));
        assertTrue(DefaultPropertiesStorage.DEFAULTS.containsKey("testomatio.batch.flush.interval"));
        assertTrue(DefaultPropertiesStorage.DEFAULTS.containsKey("testomatio.url"));
        assertTrue(DefaultPropertiesStorage.DEFAULTS.containsKey("testomatio.run.title"));
        assertTrue(DefaultPropertiesStorage.DEFAULTS.containsKey("testomatio.log.level"));
        assertTrue(DefaultPropertiesStorage.DEFAULTS.containsKey("testomatio.log.file"));
        assertTrue(DefaultPropertiesStorage.DEFAULTS.containsKey("testomatio.log.console"));
    }

    @Test
    void testDefaultPropertyValues() {
        assertEquals("10", DefaultPropertiesStorage.DEFAULTS.get("testomatio.batch.size"));
        assertEquals("10", DefaultPropertiesStorage.DEFAULTS.get("testomatio.batch.flush.interval"));
        assertEquals("https://app.testomat.io/", DefaultPropertiesStorage.DEFAULTS.get("testomatio.url"));
        assertEquals("Default Run Title", DefaultPropertiesStorage.DEFAULTS.get("testomatio.run.title"));
        assertEquals("INFO", DefaultPropertiesStorage.DEFAULTS.get("testomatio.log.level"));
        assertEquals("log/testomatio.log", DefaultPropertiesStorage.DEFAULTS.get("testomatio.log.file"));
        assertEquals("true", DefaultPropertiesStorage.DEFAULTS.get("testomatio.log.console"));
    }

    @Test
    void testMapSize() {
        assertEquals(7, DefaultPropertiesStorage.DEFAULTS.size());
    }

    @Test
    void testMapIsImmutable() {
        assertThrows(UnsupportedOperationException.class, () -> {
            DefaultPropertiesStorage.DEFAULTS.put("new.key", "new.value");
        });

        assertThrows(UnsupportedOperationException.class, () -> {
            DefaultPropertiesStorage.DEFAULTS.remove("testomatio.batch.size");
        });

        assertThrows(UnsupportedOperationException.class, () -> {
            DefaultPropertiesStorage.DEFAULTS.clear();
        });
    }
}