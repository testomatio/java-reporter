package io.testomat.junit.extractor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests specifically for parameter extraction functionality in JunitMetaDataExtractor.
 * This tests the integration between ParameterCaptureExtension and JunitMetaDataExtractor.
 */
@DisplayName("JunitMetaDataExtractor Parameter Extraction Tests")
class JunitMetaDataExtractorParameterTest {

    @Mock
    private ExtensionContext mockExtensionContext;

    private JunitMetaDataExtractor extractor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        extractor = new JunitMetaDataExtractor();
    }

    @Test
    @DisplayName("Should detect parameterized test correctly")
    void shouldDetectParameterizedTestCorrectly() throws NoSuchMethodException {
        // Given
        Method parameterizedMethod = TestMethodHolder.class.getMethod("parameterizedTestMethod", String.class);
        when(mockExtensionContext.getTestMethod()).thenReturn(Optional.of(parameterizedMethod));

        // When
        boolean isParameterized = extractor.isParameterizedTest(mockExtensionContext);

        // Then
        assertTrue(isParameterized);
    }

    @Test
    @DisplayName("Should extract parameters when manually stored")
    void shouldExtractParametersWhenManuallyStored() throws NoSuchMethodException {
        // Given
        Method parameterizedMethod = TestMethodHolder.class.getMethod("parameterizedTestMethod", String.class);
        when(mockExtensionContext.getTestMethod()).thenReturn(Optional.of(parameterizedMethod));
        when(mockExtensionContext.getUniqueId()).thenReturn("test-unique-id");

        // Manually store parameters as the ParameterCaptureExtension would
        Object[] testParameters = {"testValue"};
        ParameterCaptureExtension.storeParameters(mockExtensionContext, testParameters);

        // When
        Object extractedParameters = extractor.extractTestParameters(mockExtensionContext);

        // Then
        assertNotNull(extractedParameters);
        assertEquals("testValue", extractedParameters);

        // Cleanup
        ParameterCaptureExtension.cleanupParameters(mockExtensionContext);
    }

    @Test
    @DisplayName("Should extract multiple parameters as map")
    void shouldExtractMultipleParametersAsMap() throws NoSuchMethodException {
        // Given
        Method parameterizedMethod = TestMethodHolder.class.getMethod("multiParameterTestMethod", String.class, Integer.class);
        when(mockExtensionContext.getTestMethod()).thenReturn(Optional.of(parameterizedMethod));
        when(mockExtensionContext.getUniqueId()).thenReturn("test-unique-id-multi");

        // Manually store multiple parameters
        Object[] testParameters = {"testValue", 42};
        ParameterCaptureExtension.storeParameters(mockExtensionContext, testParameters);

        // When
        Object extractedParameters = extractor.extractTestParameters(mockExtensionContext);

        // Then
        assertNotNull(extractedParameters);
        assertTrue(extractedParameters instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) extractedParameters;
        assertEquals("testValue", paramMap.get("param0"));
        assertEquals(42, paramMap.get("param1"));

        // Cleanup
        ParameterCaptureExtension.cleanupParameters(mockExtensionContext);
    }

    @Test
    @DisplayName("Should handle display name parsing as fallback")
    void shouldHandleDisplayNameParsingAsFallback() throws NoSuchMethodException {
        // Given
        Method parameterizedMethod = TestMethodHolder.class.getMethod("parameterizedTestMethod", String.class);
        when(mockExtensionContext.getTestMethod()).thenReturn(Optional.of(parameterizedMethod));
        when(mockExtensionContext.getUniqueId()).thenReturn("test-unique-id-fallback");
        when(mockExtensionContext.getDisplayName()).thenReturn("parameterizedTestMethod [testValue]");

        // When (no manually stored parameters, should fallback to display name parsing)
        Object extractedParameters = extractor.extractTestParameters(mockExtensionContext);

        // Then
        assertNotNull(extractedParameters);
        assertEquals("testValue", extractedParameters);
    }

    @Test
    @DisplayName("Should handle complex display name formats")
    void shouldHandleComplexDisplayNameFormats() throws NoSuchMethodException {
        // Given
        Method parameterizedMethod = TestMethodHolder.class.getMethod("multiParameterTestMethod", String.class, Integer.class);
        when(mockExtensionContext.getTestMethod()).thenReturn(Optional.of(parameterizedMethod));
        when(mockExtensionContext.getUniqueId()).thenReturn("test-unique-id-complex");
        when(mockExtensionContext.getDisplayName()).thenReturn("multiParameterTestMethod [value1, 123]");

        // When
        Object extractedParameters = extractor.extractTestParameters(mockExtensionContext);

        // Then
        assertNotNull(extractedParameters);
        assertTrue(extractedParameters instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) extractedParameters;
        assertEquals("value1", paramMap.get("param0"));
        assertEquals(123L, paramMap.get("param1")); // Numbers are parsed as Long
    }

    // Test helper class with parameterized test methods
    public static class TestMethodHolder {
        
        @ParameterizedTest
        @ValueSource(strings = {"test1", "test2"})
        public void parameterizedTestMethod(String param) {
            // Test method for reflection
        }
        
        @ParameterizedTest
        @CsvSource({"value1,123", "value2,456"})
        public void multiParameterTestMethod(String stringParam, Integer intParam) {
            // Test method for reflection
        }
    }
}