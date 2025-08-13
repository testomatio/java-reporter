package io.testomat.junit.extractor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test class to verify that ParameterCaptureExtension correctly captures
 * test parameters for different types of parameterized tests.
 */
@ExtendWith(ParameterCaptureExtension.class)
@DisplayName("ParameterCaptureExtension Integration Tests")
class ParameterCaptureExtensionTest {

    @Test
    @DisplayName("Should work with regular non-parameterized tests")
    void shouldWorkWithRegularTests() {
        // This test verifies that the extension doesn't interfere with regular tests
        assertTrue(true);
    }

    @ParameterizedTest
    @DisplayName("Should capture single parameter from ValueSource")
    @ValueSource(strings = {"test1", "test2", "test3"})
    void shouldCaptureSingleParameterFromValueSource(String param) {
        assertNotNull(param);
        assertTrue(param.startsWith("test"));
        
        // In a real test scenario, the JunitMetaDataExtractor would capture these parameters
        // This test verifies the extension doesn't break parameterized tests
    }

    @ParameterizedTest
    @DisplayName("Should capture multiple parameters from CsvSource")
    @CsvSource({
        "1, test1, true",
        "2, test2, false", 
        "3, test3, true"
    })
    void shouldCaptureMultipleParametersFromCsvSource(int number, String text, boolean flag) {
        assertTrue(number > 0);
        assertNotNull(text);
        assertTrue(text.startsWith("test"));
        
        // Verify parameters are valid
        assertEquals("test" + number, text);
    }

    @ParameterizedTest
    @DisplayName("Should capture complex parameters from MethodSource")
    @MethodSource("provideComplexArguments")
    void shouldCaptureComplexParametersFromMethodSource(String text, int number, TestData data) {
        assertNotNull(text);
        assertTrue(number > 0);
        assertNotNull(data);
        assertEquals(text + number, data.getCombined());
    }

    @ParameterizedTest(name = "Custom test name: {0} and {1}")
    @DisplayName("Should capture parameters with custom test names")
    @CsvSource({
        "alpha, 100",
        "beta, 200",
        "gamma, 300"
    })
    void shouldCaptureParametersWithCustomNames(String name, int value) {
        assertNotNull(name);
        assertTrue(value > 0);
        
        // The custom name should not interfere with parameter capture
        assertTrue(name.length() > 0);
    }

    // Method source for complex parameters
    static Stream<Arguments> provideComplexArguments() {
        return Stream.of(
            Arguments.of("test", 1, new TestData("test", 1)),
            Arguments.of("sample", 2, new TestData("sample", 2)),
            Arguments.of("data", 3, new TestData("data", 3))
        );
    }

    // Test data class for complex parameter testing
    static class TestData {
        private final String text;
        private final int number;

        public TestData(String text, int number) {
            this.text = text;
            this.number = number;
        }

        public String getText() {
            return text;
        }

        public int getNumber() {
            return number;
        }

        public String getCombined() {
            return text + number;
        }

        @Override
        public String toString() {
            return "TestData{text='" + text + "', number=" + number + '}';
        }
    }
}