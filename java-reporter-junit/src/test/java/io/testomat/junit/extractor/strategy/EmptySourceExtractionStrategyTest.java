package io.testomat.junit.extractor.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import io.testomat.junit.extractor.strategy.handlers.EmptySourceHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;

/**
 * Tests for EmptySourceExtractionStrategy to verify parameter extraction from @EmptySource annotations.
 */
@DisplayName("EmptySourceExtractionStrategy Tests")
class EmptySourceExtractionStrategyTest {

    private EmptySourceHandler strategy;
    private ExtensionContext mockContext;

    @BeforeEach
    void setUp() {
        strategy = new EmptySourceHandler();
        mockContext = mock(ExtensionContext.class);
        reset(mockContext); // Reset mock between tests
    }

    @Test
    @DisplayName("Should support methods with @EmptySource annotation")
    void shouldSupportEmptySourceAnnotation() throws NoSuchMethodException {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicEmptySourceTest", String.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("basicEmptySourceTest");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        boolean supports = strategy.supports(context);

        // Then
        assertTrue(supports);
    }

    @Test
    @DisplayName("Should not support methods without @EmptySource annotation")
    void shouldNotSupportNonEmptySourceMethods() throws NoSuchMethodException {
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
    @DisplayName("Should extract empty string parameter from display name")
    void shouldExtractEmptyStringParameterFromDisplayName() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicEmptySourceTest", String.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] \"\"");
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
        assertEquals("", paramMap.get(context.getParameterName(0)));
    }

    @Test
    @DisplayName("Should extract empty string parameter from single quotes display")
    void shouldExtractEmptyStringParameterFromSingleQuotes() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicEmptySourceTest", String.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] ''");
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
        assertEquals("", paramMap.get(context.getParameterName(0)));
    }

    @Test
    @DisplayName("Should extract empty array parameter from display name")
    void shouldExtractEmptyArrayParameterFromDisplayName() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("arrayEmptySourceTest", String[].class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] []");
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
        assertEquals("[]", paramMap.get(context.getParameterName(0)));
    }

    @Test
    @DisplayName("Should extract empty collection parameter from display name")
    void shouldExtractEmptyCollectionParameterFromDisplayName() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("listEmptySourceTest", List.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] []");
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
        assertEquals("[]", paramMap.get(context.getParameterName(0)));
    }

    @Test
    @DisplayName("Should extract empty map parameter from display name")
    void shouldExtractEmptyMapParameterFromDisplayName() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("mapEmptySourceTest", Map.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] {}");
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
        assertEquals("{}", paramMap.get(context.getParameterName(0)));
    }

    @Test
    @DisplayName("Should extract empty parameter from empty display content")
    void shouldExtractEmptyParameterFromEmptyDisplayContent() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicEmptySourceTest", String.class);
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
        assertEquals("", paramMap.get(context.getParameterName(0)));
    }

    @Test
    @DisplayName("Should extract empty parameter from angle bracket display")
    void shouldExtractEmptyParameterFromAngleBracketDisplay() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicEmptySourceTest", String.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] <empty>");
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
        assertEquals("", paramMap.get(context.getParameterName(0)));
    }

    @Test
    @DisplayName("Should fallback to appropriate empty value for String type")
    void shouldFallbackToEmptyValueForStringType() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicEmptySourceTest", String.class);
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
        assertEquals("", paramMap.get(context.getParameterName(0)));
    }

    @Test
    @DisplayName("Should fallback to appropriate empty value for array type")
    void shouldFallbackToEmptyValueForArrayType() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("arrayEmptySourceTest", String[].class);
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
        assertEquals("[]", paramMap.get(context.getParameterName(0)));
    }

    @Test
    @DisplayName("Should fallback to appropriate empty value for collection type")
    void shouldFallbackToEmptyValueForCollectionType() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("listEmptySourceTest", List.class);
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
        assertEquals("[]", paramMap.get(context.getParameterName(0)));
    }

    @Test
    @DisplayName("Should fallback to appropriate empty value for map type")
    void shouldFallbackToEmptyValueForMapType() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("mapEmptySourceTest", Map.class);
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
        assertEquals("{}", paramMap.get(context.getParameterName(0)));
    }

    @Test
    @DisplayName("Should handle empty display name")
    void shouldHandleEmptyDisplayName() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicEmptySourceTest", String.class);
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
        assertEquals("", paramMap.get(context.getParameterName(0)));
    }

    @Test
    @DisplayName("Should handle non-empty display name content gracefully")
    void shouldHandleNonEmptyDisplayNameContentGracefully() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicEmptySourceTest", String.class);
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
        // Should fallback to empty string since @EmptySource always provides empty values
        assertEquals("", paramMap.get(context.getParameterName(0)));
    }

    @Test
    @DisplayName("Should return correct strategy name")
    void shouldReturnCorrectStrategyName() {
        // When
        String name = strategy.getStrategyName();

        // Then
        assertEquals("EmptySourceExtractionStrategy", name);
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

    // Test helper class with methods for reflection
    public static class TestMethodHolder {

        @ParameterizedTest
        @EmptySource
        public void basicEmptySourceTest(String text) {
            // Test method for reflection with String parameter
        }

        @ParameterizedTest
        @EmptySource
        public void arrayEmptySourceTest(String[] items) {
            // Test method for reflection with array parameter
        }

        @ParameterizedTest
        @EmptySource
        public void listEmptySourceTest(List<String> collection) {
            // Test method for reflection with List parameter
        }

        @ParameterizedTest
        @EmptySource
        public void setEmptySourceTest(Set<String> elements) {
            // Test method for reflection with Set parameter
        }

        @ParameterizedTest
        @EmptySource
        public void mapEmptySourceTest(Map<String, String> dictionary) {
            // Test method for reflection with Map parameter
        }

        public void regularTest() {
            // Regular test method without parameterization
        }
    }
}