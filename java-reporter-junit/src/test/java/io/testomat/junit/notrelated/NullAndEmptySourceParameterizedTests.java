package io.testomat.junit.notrelated;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.testomat.junit.extractor.SimpleParameterExtractor;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(SimpleParameterExtractor.class)
public class NullAndEmptySourceParameterizedTests {

    @ParameterizedTest
    @NullSource
    void testNullStringHandling(String input) {
        assertNull(input);
        assertThrows(NullPointerException.class, () -> input.length());
    }

    @ParameterizedTest
    @EmptySource
    void testEmptyStringHandling(String input) {
        assertNotNull(input);
        assertEquals(0, input.length());
        assertTrue(input.isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testNullAndEmptyStringHandling(String input) {
        if (input == null) {
            assertNull(input);
        } else {
            assertNotNull(input);
            assertTrue(input.isEmpty());
            assertEquals(0, input.length());
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n", "  ", "\t", "\n", "  ", "\t", "\n"})
    void testBlankStringValidation(String input) {
        if (input == null) {
            assertNull(input);
        } else if (input.isEmpty()) {
            assertTrue(input.isEmpty());
        } else {
            assertTrue(input.isBlank());
            assertFalse(input.isEmpty());
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"valid", "test", "data", "valid", "test", "data", "valid", "test", "data"})
    void testStringProcessingWithNullAndEmpty(String input) {
        String result = processString(input);
        if (input == null || input.isEmpty()) {
            assertEquals("default", result);
        } else {
            assertEquals(input.toUpperCase(), result);
        }
    }

    private String processString(String input) {
        if (input == null || input.isEmpty()) {
            return "default";
        }
        return input.toUpperCase();
    }
}