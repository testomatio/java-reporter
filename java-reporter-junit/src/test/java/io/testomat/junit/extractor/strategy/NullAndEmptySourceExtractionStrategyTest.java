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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

/**
 * Tests for NullAndEmptySourceExtractionStrategy to verify parameter extraction from @NullAndEmptySource annotations.
 */
@DisplayName("NullAndEmptySourceExtractionStrategy Tests")
class NullAndEmptySourceExtractionStrategyTest {

    private NullAndEmptySourceExtractionStrategy strategy;
    private ExtensionContext mockContext;

    @BeforeEach
    void setUp() {
        strategy = new NullAndEmptySourceExtractionStrategy();
        mockContext = mock(ExtensionContext.class);
        reset(mockContext); // Reset mock between tests
    }

    @Test
    @DisplayName("Should support methods with @NullAndEmptySource annotation")
    void shouldSupportNullAndEmptySourceAnnotation() throws NoSuchMethodException {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicNullAndEmptySourceTest", String.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("basicNullAndEmptySourceTest");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        boolean supports = strategy.supports(context);

        // Then
        assertTrue(supports);
    }

    @Test
    @DisplayName("Should not support methods without @NullAndEmptySource annotation")
    void shouldNotSupportNonNullAndEmptySourceMethods() throws NoSuchMethodException {
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
    @DisplayName("Should extract null parameter from display name")
    void shouldExtractNullParameterFromDisplayName() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicNullAndEmptySourceTest", String.class);
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
    @DisplayName("Should extract null parameter from NULL display")
    void shouldExtractNullParameterFromNullDisplay() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicNullAndEmptySourceTest", String.class);
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
    @DisplayName("Should extract null parameter from angle bracket display")
    void shouldExtractNullParameterFromAngleBracketDisplay() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicNullAndEmptySourceTest", String.class);
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
    @DisplayName("Should extract empty string parameter from display name")
    void shouldExtractEmptyStringParameterFromDisplayName() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicNullAndEmptySourceTest", String.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[2] \"\"");
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
        Method method = TestMethodHolder.class.getMethod("basicNullAndEmptySourceTest", String.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[2] ''");
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
        Method method = TestMethodHolder.class.getMethod("arrayNullAndEmptySourceTest", String[].class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[2] []");
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
    @DisplayName("Should extract null array parameter from display name")
    void shouldExtractNullArrayParameterFromDisplayName() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("arrayNullAndEmptySourceTest", String[].class);
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
    @DisplayName("Should extract empty collection parameter from display name")
    void shouldExtractEmptyCollectionParameterFromDisplayName() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("listNullAndEmptySourceTest", List.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[2] []");
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
        Method method = TestMethodHolder.class.getMethod("mapNullAndEmptySourceTest", Map.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[2] {}");
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
        Method method = TestMethodHolder.class.getMethod("basicNullAndEmptySourceTest", String.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[2] ");
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
        Method method = TestMethodHolder.class.getMethod("basicNullAndEmptySourceTest", String.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[2] <empty>");
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
    @DisplayName("Should fallback to null value for String type")
    void shouldFallbackToNullValueForStringType() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicNullAndEmptySourceTest", String.class);
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
        // Fallback should be null (first value from @NullAndEmptySource)
        assertNull(paramMap.get(context.getParameterName(0)));
    }

    @Test
    @DisplayName("Should fallback to null value for array type")
    void shouldFallbackToNullValueForArrayType() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("arrayNullAndEmptySourceTest", String[].class);
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
        // Fallback should be null (first value from @NullAndEmptySource)
        assertNull(paramMap.get(context.getParameterName(0)));
    }

    @Test
    @DisplayName("Should handle empty display name")
    void shouldHandleEmptyDisplayName() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicNullAndEmptySourceTest", String.class);
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
        // Should fallback to null
        assertNull(paramMap.get(context.getParameterName(0)));
    }

    @Test
    @DisplayName("Should handle non-null-or-empty display name content gracefully")
    void shouldHandleNonNullOrEmptyDisplayNameContentGracefully() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicNullAndEmptySourceTest", String.class);
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
        // Should fallback to null since display name doesn't match null/empty patterns
        assertNull(paramMap.get(context.getParameterName(0)));
    }

    @Test
    @DisplayName("Should return correct strategy name")
    void shouldReturnCorrectStrategyName() {
        // When
        String name = strategy.getStrategyName();

        // Then
        assertEquals("NullAndEmptySourceExtractionStrategy", name);
    }

    @Test
    @DisplayName("Should return correct priority")
    void shouldReturnCorrectPriority() {
        // When
        int priority = strategy.getPriority();

        // Then
        assertEquals(4, priority);
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
        @NullAndEmptySource
        public void basicNullAndEmptySourceTest(String text) {
            // Test method for reflection with String parameter
        }

        @ParameterizedTest
        @NullAndEmptySource
        public void arrayNullAndEmptySourceTest(String[] items) {
            // Test method for reflection with array parameter
        }

        @ParameterizedTest
        @NullAndEmptySource
        public void listNullAndEmptySourceTest(List<String> collection) {
            // Test method for reflection with List parameter
        }

        @ParameterizedTest
        @NullAndEmptySource
        public void setNullAndEmptySourceTest(Set<String> elements) {
            // Test method for reflection with Set parameter
        }

        @ParameterizedTest
        @NullAndEmptySource
        public void mapNullAndEmptySourceTest(Map<String, String> dictionary) {
            // Test method for reflection with Map parameter
        }

        public void regularTest() {
            // Regular test method without parameterization
        }
    }
}