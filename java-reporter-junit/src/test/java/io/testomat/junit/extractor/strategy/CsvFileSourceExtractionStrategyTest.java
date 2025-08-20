package io.testomat.junit.extractor.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.testomat.junit.extractor.strategy.handlers.CsvFileSourceHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

/**
 * Tests for CsvFileSourceExtractionStrategy to verify parameter extraction from @CsvFileSource annotations.
 */
@DisplayName("CsvFileSourceExtractionStrategy Tests")
class CsvFileSourceExtractionStrategyTest {

    private CsvFileSourceHandler strategy;
    private ExtensionContext mockContext;

    @BeforeEach
    void setUp() {
        strategy = new CsvFileSourceHandler();
        mockContext = mock(ExtensionContext.class);
    }


    @Test
    @DisplayName("Should extract CSV file parameters from display name")
    void shouldExtractCsvFileParametersFromDisplayName() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicCsvFileTest", String.class, int.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] hello, 42");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        Object result = strategy.extractParameters(context);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        assertEquals("hello", paramMap.get(context.getParameterName(0)));
        assertEquals(42, paramMap.get(context.getParameterName(1)));
    }

    @Test
    @DisplayName("Should extract CSV file parameters with different types")
    void shouldExtractCsvFileParametersWithDifferentTypes() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("mixedTypeCsvFileTest", String.class, int.class, boolean.class, double.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] test, 100, true, 3.14");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        Object result = strategy.extractParameters(context);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        assertEquals("test", paramMap.get(context.getParameterName(0)));
        assertEquals(100, paramMap.get(context.getParameterName(1)));
        assertEquals(true, paramMap.get(context.getParameterName(2)));
        assertEquals(3.14, paramMap.get(context.getParameterName(3)));
    }

    @Test
    @DisplayName("Should handle quoted CSV file values")
    void shouldHandleQuotedCsvFileValues() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("quotedCsvFileTest", String.class, String.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] \"hello, world\", \"test value\"");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        Object result = strategy.extractParameters(context);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        assertEquals("hello, world", paramMap.get(context.getParameterName(0)));
        assertEquals("test value", paramMap.get(context.getParameterName(1)));
    }

    @Test
    @DisplayName("Should handle null values in CSV file")
    void shouldHandleNullValuesInCsvFile() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("nullValueCsvFileTest", String.class, String.class, String.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] hello, null, world");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        Object result = strategy.extractParameters(context);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        assertEquals("hello", paramMap.get(context.getParameterName(0)));
        assertNull(paramMap.get(context.getParameterName(1)));
        assertEquals("world", paramMap.get(context.getParameterName(2)));
    }

    @Test
    @DisplayName("Should handle custom delimiter")
    void shouldHandleCustomDelimiter() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("customDelimiterCsvFileTest", String.class, int.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] hello; 42");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        Object result = strategy.extractParameters(context);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        assertEquals("hello", paramMap.get(context.getParameterName(0)));
        assertEquals(42, paramMap.get(context.getParameterName(1)));
    }

    @Test
    @DisplayName("Should handle empty values in CSV file")
    void shouldHandleEmptyValuesInCsvFile() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("emptyValueCsvFileTest", String.class, String.class, String.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] hello, , world");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        Object result = strategy.extractParameters(context);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        assertEquals("hello", paramMap.get(context.getParameterName(0)));
        assertEquals("", paramMap.get(context.getParameterName(1)));
        assertEquals("world", paramMap.get(context.getParameterName(2)));
    }

    @Test
    @DisplayName("Should extract single CSV file parameter")
    void shouldExtractSingleCsvFileParameter() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("singleCsvFileTest", String.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] hello");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        Object result = strategy.extractParameters(context);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        assertEquals("hello", paramMap.get(context.getParameterName(0)));
    }

    @Test
    @DisplayName("Should fallback to file values when display name parsing fails")
    void shouldFallbackToFileValues() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicCsvFileTest", String.class, int.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("unparseable display name");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        Object result = strategy.extractParameters(context);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        // Should fallback to empty map since we can't read actual files in unit tests
        assertTrue(paramMap.isEmpty());
    }

    @Test
    @DisplayName("Should handle complex CSV file with spaces and quotes")
    void shouldHandleComplexCsvFileWithSpacesAndQuotes() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("complexCsvFileTest", String.class, String.class, int.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] \"first, value\", second value, 42");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        Object result = strategy.extractParameters(context);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        assertEquals("first, value", paramMap.get(context.getParameterName(0)));
        assertEquals("second value", paramMap.get(context.getParameterName(1)));
        assertEquals(42, paramMap.get(context.getParameterName(2)));
    }

    @Test
    @DisplayName("Should handle empty display name")
    void shouldHandleEmptyDisplayName() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicCsvFileTest", String.class, int.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        Object result = strategy.extractParameters(context);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        // Should fallback to empty map since we can't read actual files in unit tests
        assertTrue(paramMap.isEmpty());
    }

    @Test
    @DisplayName("Should have correct strategy name")
    void shouldHaveCorrectStrategyName() {
        // When
        String strategyName = strategy.getStrategyName();
        
        // Then
        assertEquals("CsvFileSourceExtractionStrategy", strategyName);
    }


    @Test
    @DisplayName("Should handle invalid context gracefully")
    void shouldHandleInvalidContextGracefully() throws Exception {
        // Given
        when(mockContext.getTestMethod()).thenReturn(Optional.empty());
        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        Object result = strategy.extractParameters(context);

        // Then
        assertNull(result);
    }

    // Test helper class with methods for reflection
    public static class TestMethodHolder {

        @ParameterizedTest
        @CsvFileSource(resources = "/test-data.csv")
        public void basicCsvFileTest(String name, int value) {
            // Test method for reflection
        }

        @ParameterizedTest
        @CsvFileSource(resources = "/mixed-data.csv")
        public void mixedTypeCsvFileTest(String description, int count, boolean isEnabled, double percentage) {
            // Test method for reflection with mixed types
        }

        @ParameterizedTest
        @CsvFileSource(resources = "/quoted-data.csv")
        public void quotedCsvFileTest(String firstName, String lastName) {
            // Test method for reflection with quoted values
        }

        @ParameterizedTest
        @CsvFileSource(resources = "/null-data.csv")
        public void nullValueCsvFileTest(String username, String email, String phone) {
            // Test method for reflection with null values
        }

        @ParameterizedTest
        @CsvFileSource(resources = "/custom-delimiter-data.csv", delimiter = ';')
        public void customDelimiterCsvFileTest(String category, int priority) {
            // Test method for reflection with custom delimiter
        }

        @ParameterizedTest
        @CsvFileSource(resources = "/empty-data.csv")
        public void emptyValueCsvFileTest(String title, String description, String status) {
            // Test method for reflection with empty values
        }

        @ParameterizedTest
        @CsvFileSource(resources = "/single-data.csv")
        public void singleCsvFileTest(String identifier) {
            // Test method for reflection with single CSV values
        }

        @ParameterizedTest
        @CsvFileSource(resources = "/complex-data.csv")
        public void complexCsvFileTest(String product, String vendor, int quantity) {
            // Test method for reflection with complex CSV
        }

        public void regularTest() {
            // Regular test method without parameterization
        }
    }
}