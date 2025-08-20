package io.testomat.junit.extractor.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;

/**
 * Tests for NullSourceExtractionStrategy to verify parameter extraction from @NullSource annotations.
 */
@DisplayName("NullSourceExtractionStrategy Tests")
class NullSourceExtractionStrategyTest {

    private NullSourceExtractionStrategy strategy;
    private ExtensionContext mockContext;

    @BeforeEach
    void setUp() {
        strategy = new NullSourceExtractionStrategy();
        mockContext = mock(ExtensionContext.class);
        reset(mockContext); // Reset mock between tests
    }

    @Test
    @DisplayName("Should support methods with @NullSource annotation")
    void shouldSupportNullSourceAnnotation() throws NoSuchMethodException {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicNullSourceTest", String.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("basicNullSourceTest");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        boolean supports = strategy.supports(context);

        // Then
        assertTrue(supports);
    }

    @Test
    @DisplayName("Should not support methods without @NullSource annotation")
    void shouldNotSupportNonNullSourceMethods() throws NoSuchMethodException {
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
    @DisplayName("Should extract null parameter from display name - 'null'")
    void shouldExtractNullParameterFromDisplayNameNull() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicNullSourceTest", String.class);
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
        assertTrue(paramMap.containsKey(context.getParameterName(0)));
        assertNull(paramMap.get(context.getParameterName(0)));
    }

    @Test
    @DisplayName("Should extract null parameter from display name - 'NULL'")
    void shouldExtractNullParameterFromDisplayNameUpper() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicNullSourceTest", String.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] NULL");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        Object result = strategy.extractParameters(context);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        assertTrue(paramMap.containsKey(context.getParameterName(0)));
        assertNull(paramMap.get(context.getParameterName(0)));
    }

    @Test
    @DisplayName("Should extract null parameter from display name - '<null>'")
    void shouldExtractNullParameterFromDisplayNameAngleBrackets() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicNullSourceTest", String.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] <null>");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        Object result = strategy.extractParameters(context);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        assertTrue(paramMap.containsKey(context.getParameterName(0)));
        assertNull(paramMap.get(context.getParameterName(0)));
    }

    @Test
    @DisplayName("Should extract null parameter from empty display name content")
    void shouldExtractNullParameterFromEmptyDisplayContent() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicNullSourceTest", String.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] ");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        Object result = strategy.extractParameters(context);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        assertTrue(paramMap.containsKey(context.getParameterName(0)));
        assertNull(paramMap.get(context.getParameterName(0)));
    }

    @Test
    @DisplayName("Should fallback to null when display name parsing fails")
    void shouldFallbackToNullWhenDisplayNameParsingFails() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicNullSourceTest", String.class);
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
        assertTrue(paramMap.containsKey(context.getParameterName(0)));
        assertNull(paramMap.get(context.getParameterName(0)));
    }

    @Test
    @DisplayName("Should handle empty display name")
    void shouldHandleEmptyDisplayName() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicNullSourceTest", String.class);
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
        assertTrue(paramMap.containsKey(context.getParameterName(0)));
        assertNull(paramMap.get(context.getParameterName(0)));
    }

    @Test
    @DisplayName("Should handle different parameter types with null")
    void shouldHandleDifferentParameterTypesWithNull() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("integerNullSourceTest", Integer.class);
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
        assertTrue(paramMap.containsKey(context.getParameterName(0)));
        assertNull(paramMap.get(context.getParameterName(0)));
    }

    @Test
    @DisplayName("Should handle custom object parameter with null")
    void shouldHandleCustomObjectParameterWithNull() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("customObjectNullSourceTest", Object.class);
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
        assertTrue(paramMap.containsKey(context.getParameterName(0)));
        assertNull(paramMap.get(context.getParameterName(0)));
    }

    @Test
    @DisplayName("Should return correct strategy name")
    void shouldReturnCorrectStrategyName() {
        // When
        String name = strategy.getStrategyName();

        // Then
        assertEquals("NullSourceExtractionStrategy", name);
    }

    @Test
    @DisplayName("Should return correct priority")
    void shouldReturnCorrectPriority() {
        // When
        int priority = strategy.getPriority();

        // Then
        assertEquals(5, priority);
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

    @Test
    @DisplayName("Should handle non-null display name content gracefully")
    void shouldHandleNonNullDisplayNameContentGracefully() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicNullSourceTest", String.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] some value");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        Object result = strategy.extractParameters(context);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        assertTrue(paramMap.containsKey(context.getParameterName(0)));
        // Should fallback to null since @NullSource always provides null
        assertNull(paramMap.get(context.getParameterName(0)));
    }

    @Test
    @DisplayName("Should create generic parameter map when method is null")
    void shouldCreateGenericParameterMapWhenMethodIsNull() throws Exception {
        // Given
        when(mockContext.getTestMethod()).thenReturn(Optional.empty());
        when(mockContext.getDisplayName()).thenReturn("[1] null");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        Object result = strategy.extractParameters(context);

        // Then
        // Should return null when context is invalid (no method)
        assertNull(result);
    }

    // Test helper class with methods for reflection
    public static class TestMethodHolder {

        @ParameterizedTest
        @NullSource
        public void basicNullSourceTest(String input) {
            // Test method for reflection with String parameter
        }

        @ParameterizedTest
        @NullSource
        public void integerNullSourceTest(Integer number) {
            // Test method for reflection with Integer parameter
        }

        @ParameterizedTest
        @NullSource
        public void customObjectNullSourceTest(Object data) {
            // Test method for reflection with Object parameter
        }

        public void regularTest() {
            // Regular test method without parameterization
        }
    }
}