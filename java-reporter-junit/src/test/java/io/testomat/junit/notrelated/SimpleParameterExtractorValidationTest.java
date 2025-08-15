package io.testomat.junit.notrelated;

import static org.junit.jupiter.api.Assertions.*;

import io.testomat.junit.extractor.SimpleParameterExtractor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Simple test to validate that SimpleParameterExtractor can capture parameters.
 */
@ExtendWith(SimpleParameterExtractor.class)
public class SimpleParameterExtractorValidationTest {

    @Test
    void regularTestShouldNotCaptureParameters() {
        // Regular test - should not trigger parameter capture
        assertTrue(true);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void testValueSourceIntCapture(int value) {
//         System.out.println("Test executing with parameter: " + value);
        assertTrue(value > 0);
        assertTrue(value <= 3);
    }

    @ParameterizedTest
    @ValueSource(strings = {"hello", "world"})
    void testValueSourceStringCapture(String input) {
//         System.out.println("Test executing with parameter: " + input);
        assertNotNull(input);
        assertTrue(input.length() > 0);
    }
}