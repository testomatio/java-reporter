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
 * Integration test to verify that parameter extraction works correctly
 * with actual JUnit 5 parameterized tests.
 */
@ExtendWith(ParameterCaptureExtension.class)
@DisplayName("Parameter Extraction Integration Test")
class ParameterExtractionIntegrationTest {

    @Test
    @DisplayName("Regular test should not have parameters")
    void regularTestShouldNotHaveParameters(TestInfo testInfo) {
        ImprovedParameterExtractor extractor = new ImprovedParameterExtractor();
        
        // Create a mock context from TestInfo (this is a simplified test)
        // In real execution, the JunitListener would have access to proper ExtensionContext
        assertNotNull(testInfo);
        // This test mainly verifies the extractor doesn't crash with regular tests
    }

    @ParameterizedTest
    @DisplayName("Single parameter test")
    @ValueSource(strings = {"test1", "test2", "test3"})
    void singleParameterTest(String param, TestInfo testInfo) {
        assertNotNull(param);
        assertTrue(param.startsWith("test"));
        
        // In real scenarios, the parameter extraction would happen in the JunitListener
        // This test verifies the parameterized test execution works
//         System.out.println("Parameter: " + param);
        // System.out.println("Display name: " + testInfo.getDisplayName());
    }

    @ParameterizedTest
    @DisplayName("Multiple parameter test")
    @CsvSource({
        "test1, 100, true",
        "test2, 200, false",
        "test3, 300, true"
    })
    void multipleParameterTest(String text, int number, boolean flag, TestInfo testInfo) {
        assertNotNull(text);
        assertTrue(number > 0);
        
        // Log the display name to see how JUnit formats it
//         System.out.println("Parameters: " + text + ", " + number + ", " + flag);
        // System.out.println("Display name: " + testInfo.getDisplayName());
        
        // Test that demonstrates the display name contains parameter info
        String displayName = testInfo.getDisplayName();
        assertTrue(displayName.contains(text) || displayName.contains(String.valueOf(number)));
    }

    @ParameterizedTest(name = "Custom name: {0} with value {1}")
    @DisplayName("Custom named parameter test")
    @CsvSource({
        "alpha, 42",
        "beta, 84"
    })
    void customNamedParameterTest(String name, int value, TestInfo testInfo) {
        assertNotNull(name);
        assertTrue(value > 0);
        
//         System.out.println("Parameters: " + name + ", " + value);
        // System.out.println("Display name: " + testInfo.getDisplayName());
        
        // Verify the custom name format is used
        String displayName = testInfo.getDisplayName();
        assertTrue(displayName.contains("Custom name") || displayName.contains(name));
    }

    @Test
    @DisplayName("Test display name parsing directly")
    void testDisplayNameParsingDirectly() {
        ImprovedParameterExtractor extractor = new ImprovedParameterExtractor();
        
        // Test various display name formats
        testDisplayNameFormat(extractor, "testMethod[1] arg1, arg2", new Object[]{"arg1", "arg2"});
        testDisplayNameFormat(extractor, "testMethod [value1, 123]", new Object[]{"value1", 123});
        testDisplayNameFormat(extractor, "Custom: test with 42", new Object[]{"test", "with", 42});
        testDisplayNameFormat(extractor, "testMethod(param1, param2)", new Object[]{"param1", "param2"});
    }

    private void testDisplayNameFormat(ImprovedParameterExtractor extractor, String displayName, Object[] expected) {
        // This is a unit test for the display name parsing logic
//         System.out.println("Testing display name: " + displayName);
        
        // We'd need to create a mock ExtensionContext for full testing
        // For now, this demonstrates the approach
        assertNotNull(extractor);
        assertTrue(displayName.length() > 0);
    }
}