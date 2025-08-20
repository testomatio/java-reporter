package io.testomat.junit.extractor.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.testomat.junit.extractor.strategy.handlers.ValueSourceHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests for ValueSourceExtractionStrategy to verify parameter extraction from @ValueSource annotations.
 */
@DisplayName("ValueSourceExtractionStrategy Tests")
class ValueSourceExtractionStrategyTest {

    private ValueSourceHandler strategy;
    private ExtensionContext mockContext;

    @BeforeEach
    void setUp() {
        strategy = new ValueSourceHandler();
        mockContext = mock(ExtensionContext.class);
    }

    @Test
    @DisplayName("Should have correct strategy name")
    void shouldHaveCorrectStrategyName() {
        // When
        String strategyName = strategy.getStrategyName();

        // Then
        assertEquals("ValueSourceExtractionStrategy", strategyName);
    }

    @Test
    @DisplayName("Should extract string parameter from display name")
    void shouldExtractStringParameterFromDisplayName() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("stringValueSourceTest", String.class);
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
        String expectedParamName = context.getParameterName(0);
        assertEquals("hello", paramMap.get(expectedParamName));
    }

    @Test
    @DisplayName("Should extract integer parameter from display name")
    void shouldExtractIntegerParameterFromDisplayName() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("intValueSourceTest", int.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[2] 42");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        Object result = strategy.extractParameters(context);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        String expectedParamName = context.getParameterName(0);
        assertEquals(42, paramMap.get(expectedParamName));
    }

    @Test
    @DisplayName("Should extract boolean parameter from display name")
    void shouldExtractBooleanParameterFromDisplayName() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("booleanValueSourceTest", boolean.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] true");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        Object result = strategy.extractParameters(context);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        String expectedParamName = context.getParameterName(0);
        assertEquals(true, paramMap.get(expectedParamName));
    }

    @Test
    @DisplayName("Should extract double parameter from display name")
    void shouldExtractDoubleParameterFromDisplayName() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("doubleValueSourceTest", double.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] 3.14");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        Object result = strategy.extractParameters(context);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        String expectedParamName = context.getParameterName(0);
        assertEquals(3.14, paramMap.get(expectedParamName));
    }

    @Test
    @DisplayName("Should handle null parameter from display name")
    void shouldHandleNullParameterFromDisplayName() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("stringValueSourceTest", String.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] null");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        Object result = strategy.extractParameters(context);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        String expectedParamName = context.getParameterName(0);
        assertNull(paramMap.get(expectedParamName));
    }

    @Test
    @DisplayName("Should create parameter map for multiple parameter methods")
    void shouldCreateParameterMapForMultipleParameterMethods() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("multipleParameterWithValueSourceTest", String.class, int.class);
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
        String expectedParamName = context.getParameterName(0);
        assertEquals("hello", paramMap.get(expectedParamName));
    }

    @Test
    @DisplayName("Should fallback to annotation values when display name parsing fails")
    void shouldFallbackToAnnotationValues() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("stringValueSourceTest", String.class);
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
        String expectedParamName = context.getParameterName(0);
        assertEquals("test1", paramMap.get(expectedParamName)); // Should get first value from annotation
    }

    @Test
    @DisplayName("Should handle empty display name")
    void shouldHandleEmptyDisplayName() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("stringValueSourceTest", String.class);
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
        String expectedParamName = context.getParameterName(0);
        assertEquals("test1", paramMap.get(expectedParamName)); // Should fallback to annotation
    }

    @Test
    @DisplayName("Should return correct strategy name")
    void shouldReturnCorrectStrategyName() {
        // When
        String name = strategy.getStrategyName();

        // Then
        assertEquals("ValueSourceExtractionStrategy", name);
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
        @ValueSource(strings = {"test1", "test2", "test3"})
        public void stringValueSourceTest(String inputValue) {
            // Test method for reflection
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3})
        public void intValueSourceTest(int count) {
            // Test method for reflection
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        public void booleanValueSourceTest(boolean enabled) {
            // Test method for reflection
        }

        @ParameterizedTest
        @ValueSource(doubles = {1.1, 2.2, 3.3})
        public void doubleValueSourceTest(double price) {
            // Test method for reflection
        }

        public void regularTest() {
            // Regular test method without parameterization
        }

        @ParameterizedTest
        @ValueSource(strings = {"test1", "test2"})
        public void multipleParameterWithValueSourceTest(String productName, int maxCount) {
            // Method with multiple parameters and @ValueSource (for testing parameter map creation)
        }

        public void multipleParameterTest(String productName, int maxCount) {
            // Method with multiple parameters (for testing parameter map creation)
        }
    }
}