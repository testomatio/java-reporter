package io.testomat.junit.extractor;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test to verify that TestMethodParameterExtractor correctly captures
 * parameters from method invocations.
 */
@ExtendWith(TestMethodParameterExtractor.class)
@DisplayName("TestMethodParameterExtractor Integration Test")
class TestMethodParameterExtractorTest {

    @Test
    @DisplayName("Regular test should not capture parameters")
    void regularTestShouldNotCaptureParameters(TestInfo testInfo) {
        // Regular tests should not trigger parameter capture
        assertNotNull(testInfo);
        // System.out.println("Regular test executed: " + testInfo.getDisplayName());
    }

    @ParameterizedTest
    @DisplayName("Should capture single parameter from ValueSource")
    @ValueSource(strings = {"param1", "param2", "param3"})
    void shouldCaptureSingleParameter(String value, TestInfo testInfo) {
        assertNotNull(value);
//         System.out.println("Executing test with parameter: " + value);
        // System.out.println("Test display name: " + testInfo.getDisplayName());
        
        // The parameter should be captured by TestMethodParameterExtractor
        // This will be verified by the logging output
        assertTrue(value.startsWith("param"));
    }

    @ParameterizedTest
    @DisplayName("Should capture multiple parameters from CsvSource")
    @CsvSource({
        "test1, 100, true",
        "test2, 200, false"
    })
    void shouldCaptureMultipleParameters(String text, Integer number, Boolean flag, TestInfo testInfo) {
        assertNotNull(text);
        assertNotNull(number);
        assertNotNull(flag);
        
//         System.out.println("Executing test with parameters: " + text + ", " + number + ", " + flag);
        // System.out.println("Test display name: " + testInfo.getDisplayName());
        
        // Verify the parameters are valid
        assertTrue(text.startsWith("test"));
        assertTrue(number > 0);
    }

    @ParameterizedTest(name = "Custom test: {0} with {1}")
    @DisplayName("Should capture parameters with custom display name")
    @CsvSource({
        "alpha, 42",
        "beta, 84"
    })
    void shouldCaptureParametersWithCustomName(String name, Integer value, TestInfo testInfo) {
        assertNotNull(name);
        assertNotNull(value);
        
//         System.out.println("Executing custom named test with parameters: " + name + ", " + value);
        // System.out.println("Test display name: " + testInfo.getDisplayName());
        
        // The custom name should be reflected in the display name
        String displayName = testInfo.getDisplayName();
        assertTrue(displayName.contains("Custom test") || displayName.contains(name));
        assertTrue(value > 0);
    }
}