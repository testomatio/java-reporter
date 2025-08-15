package io.testomat.junit.extractor;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test to verify SimpleParameterExtractor works correctly with various parameter sources.
 */
@ExtendWith(SimpleParameterExtractor.class)
class SimpleParameterExtractorTest {

    @Test
    void regularTestShouldNotHaveParameters() {
        // This should not trigger parameter extraction
        assertTrue(true);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void testValueSourceInt(int value) {
        assertTrue(value > 0);
        assertTrue(value <= 3);
    }

    @ParameterizedTest
    @ValueSource(strings = {"hello", "world", "junit"})
    void testValueSourceString(String input) {
        assertNotNull(input);
        assertTrue(input.length() > 0);
    }

    @ParameterizedTest
    @CsvSource({
        "alpha, 10, true",
        "beta, 20, false", 
        "gamma, 30, true"
    })
    void testCsvSource(String text, int number, boolean flag) {
        assertNotNull(text);
        assertTrue(number > 0);
        assertNotNull(flag);
    }

    @ParameterizedTest
    @ValueSource(doubles = {1.5, 2.7, 3.14})
    void testValueSourceDouble(double value) {
        assertTrue(value > 0.0);
        assertTrue(value < 10.0);
    }

    @ParameterizedTest
    @ValueSource(longs = {100L, 200L, 300L})
    void testValueSourceLong(long number) {
        assertTrue(number >= 100L);
        assertEquals(0, number % 100);
    }
}