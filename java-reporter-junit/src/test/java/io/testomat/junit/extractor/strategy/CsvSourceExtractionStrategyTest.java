package io.testomat.junit.extractor.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

/**
 * Tests for CsvSourceExtractionStrategy to verify parameter extraction from @CsvSource annotations.
 */
@DisplayName("CsvSourceExtractionStrategy Tests")
class CsvSourceExtractionStrategyTest {

    private CsvSourceExtractionStrategy strategy;
    private ExtensionContext mockContext;

    @BeforeEach
    void setUp() {
        strategy = new CsvSourceExtractionStrategy();
        mockContext = mock(ExtensionContext.class);
    }

    @Test
    @DisplayName("Should support methods with @CsvSource annotation")
    void shouldSupportCsvSourceAnnotation() throws NoSuchMethodException {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicCsvTest", String.class, int.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("basicCsvTest");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        boolean supports = strategy.supports(context);

        // Then
        assertTrue(supports);
    }

    @Test
    @DisplayName("Should not support methods without @CsvSource annotation")
    void shouldNotSupportNonCsvSourceMethods() throws NoSuchMethodException {
        // Given
        Method method = TestMethodHolder.class.getMethod("regularTest");
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("regularTest");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        boolean supports = strategy.supports(context);

        // Then
        assertFalse(supports);
    }

    @Test
    @DisplayName("Should extract CSV parameters from display name")
    void shouldExtractCsvParametersFromDisplayName() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicCsvTest", String.class, int.class);
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
        assertEquals("hello", paramMap.get("param0"));
        assertEquals(42, paramMap.get("param1"));
    }

    @Test
    @DisplayName("Should extract CSV parameters with different types")
    void shouldExtractCsvParametersWithDifferentTypes() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("mixedTypeCsvTest", String.class, int.class, boolean.class, double.class);
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
        assertEquals("test", paramMap.get("param0"));
        assertEquals(100, paramMap.get("param1"));
        assertEquals(true, paramMap.get("param2"));
        assertEquals(3.14, paramMap.get("param3"));
    }

    @Test
    @DisplayName("Should handle quoted CSV values")
    void shouldHandleQuotedCsvValues() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("quotedCsvTest", String.class, String.class);
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
        assertEquals("hello, world", paramMap.get("param0"));
        assertEquals("test value", paramMap.get("param1"));
    }

    @Test
    @DisplayName("Should handle null values in CSV")
    void shouldHandleNullValuesInCsv() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("nullValueCsvTest", String.class, String.class, String.class);
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
        assertEquals("hello", paramMap.get("param0"));
        assertNull(paramMap.get("param1"));
        assertEquals("world", paramMap.get("param2"));
    }

    @Test
    @DisplayName("Should handle custom delimiter")
    void shouldHandleCustomDelimiter() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("customDelimiterCsvTest", String.class, int.class);
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
        assertEquals("hello", paramMap.get("param0"));
        assertEquals(42, paramMap.get("param1"));
    }

    @Test
    @DisplayName("Should handle empty values in CSV")
    void shouldHandleEmptyValuesInCsv() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("emptyValueCsvTest", String.class, String.class, String.class);
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
        assertEquals("hello", paramMap.get("param0"));
        assertEquals("", paramMap.get("param1"));
        assertEquals("world", paramMap.get("param2"));
    }

    @Test
    @DisplayName("Should extract single CSV parameter")
    void shouldExtractSingleCsvParameter() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("singleCsvTest", String.class);
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
        assertEquals("hello", paramMap.get("param0"));
    }

    @Test
    @DisplayName("Should fallback to annotation values when display name parsing fails")
    void shouldFallbackToAnnotationValues() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicCsvTest", String.class, int.class);
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
        assertEquals("test1", paramMap.get("param0")); // Should get first value from annotation
        assertEquals(100, paramMap.get("param1"));
    }

    @Test
    @DisplayName("Should handle complex CSV with spaces and quotes")
    void shouldHandleComplexCsvWithSpacesAndQuotes() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("complexCsvTest", String.class, String.class, int.class);
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
        assertEquals("first, value", paramMap.get("param0"));
        assertEquals("second value", paramMap.get("param1"));
        assertEquals(42, paramMap.get("param2"));
    }

    @Test
    @DisplayName("Should handle empty display name")
    void shouldHandleEmptyDisplayName() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicCsvTest", String.class, int.class);
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
        assertEquals("test1", paramMap.get("param0")); // Should fallback to annotation
        assertEquals(100, paramMap.get("param1"));
    }

    @Test
    @DisplayName("Should return correct strategy name")
    void shouldReturnCorrectStrategyName() {
        // When
        String name = strategy.getStrategyName();

        // Then
        assertEquals("CsvSourceExtractionStrategy", name);
    }

    @Test
    @DisplayName("Should return correct priority")
    void shouldReturnCorrectPriority() {
        // When
        int priority = strategy.getPriority();

        // Then
        assertEquals(10, priority);
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
        @CsvSource({"test1, 100", "test2, 200", "test3, 300"})
        public void basicCsvTest(String text, int number) {
            // Test method for reflection
        }

        @ParameterizedTest
        @CsvSource({"test, 100, true, 3.14", "another, 200, false, 2.71"})
        public void mixedTypeCsvTest(String text, int number, boolean flag, double decimal) {
            // Test method for reflection with mixed types
        }

        @ParameterizedTest
        @CsvSource({"\"hello, world\", \"test value\"", "\"another\", \"value\""})
        public void quotedCsvTest(String first, String second) {
            // Test method for reflection with quoted values
        }

        @ParameterizedTest
        @CsvSource({"hello, null, world", "test, , value"})
        public void nullValueCsvTest(String first, String second, String third) {
            // Test method for reflection with null values
        }

        @ParameterizedTest
        @CsvSource(value = {"hello; 42", "world; 84"}, delimiter = ';')
        public void customDelimiterCsvTest(String text, int number) {
            // Test method for reflection with custom delimiter
        }

        @ParameterizedTest
        @CsvSource({"hello, , world", "test, , value"})
        public void emptyValueCsvTest(String first, String second, String third) {
            // Test method for reflection with empty values
        }

        @ParameterizedTest
        @CsvSource({"hello", "world", "test"})
        public void singleCsvTest(String value) {
            // Test method for reflection with single CSV values
        }

        @ParameterizedTest
        @CsvSource({"\"first, value\", second value, 42", "\"another, test\", third value, 84"})
        public void complexCsvTest(String first, String second, int third) {
            // Test method for reflection with complex CSV
        }

        public void regularTest() {
            // Regular test method without parameterization
        }
    }
}