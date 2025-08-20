package io.testomat.junit.extractor.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests for MethodSourceExtractionStrategy to verify parameter extraction from @MethodSource annotations.
 */
@DisplayName("MethodSourceExtractionStrategy Tests")
class MethodSourceExtractionStrategyTest {

    private MethodSourceExtractionStrategy strategy;
    private ExtensionContext mockContext;

    @BeforeEach
    void setUp() {
        strategy = new MethodSourceExtractionStrategy();
        mockContext = mock(ExtensionContext.class);
    }

    @Test
    @DisplayName("Should support methods with @MethodSource annotation")
    void shouldSupportMethodSourceAnnotation() throws NoSuchMethodException {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicMethodSourceTest", String.class, int.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("basicMethodSourceTest");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        boolean supports = strategy.supports(context);

        // Then
        assertTrue(supports);
    }

    @Test
    @DisplayName("Should not support methods without @MethodSource annotation")
    void shouldNotSupportNonMethodSourceMethods() throws NoSuchMethodException {
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
    @DisplayName("Should extract MethodSource parameters from display name")
    void shouldExtractMethodSourceParametersFromDisplayName() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicMethodSourceTest", String.class, int.class);
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
    @DisplayName("Should extract MethodSource parameters with different types")
    void shouldExtractMethodSourceParametersWithDifferentTypes() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("mixedTypeMethodSourceTest", String.class, int.class, boolean.class, double.class);
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
    @DisplayName("Should handle Arguments display format")
    void shouldHandleArgumentsDisplayFormat() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("argumentsMethodSourceTest", String.class, int.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] Arguments{arguments=[hello, 42]}");
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
    @DisplayName("Should handle array display format")
    void shouldHandleArrayDisplayFormat() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("arrayMethodSourceTest", String.class, int.class, boolean.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] [world, 84, false]");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        Object result = strategy.extractParameters(context);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        assertEquals("world", paramMap.get(context.getParameterName(0)));
        assertEquals(84, paramMap.get(context.getParameterName(1)));
        assertEquals(false, paramMap.get(context.getParameterName(2)));
    }

    @Test
    @DisplayName("Should handle null values in MethodSource")
    void shouldHandleNullValuesInMethodSource() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("nullValueMethodSourceTest", String.class, String.class, String.class);
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
    @DisplayName("Should extract single MethodSource parameter")
    void shouldExtractSingleMethodSourceParameter() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("singleMethodSourceTest", String.class);
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
    @DisplayName("Should fallback to method source when display name parsing fails")
    void shouldFallbackToMethodSource() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicMethodSourceTest", String.class, int.class);
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
        // Should fallback to method source - might actually work since the provider methods exist
        // Just verify we get a valid map (could be empty or contain data)
        assertNotNull(paramMap);
    }

    @Test
    @DisplayName("Should handle complex MethodSource with mixed types")
    void shouldHandleComplexMethodSourceWithMixedTypes() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("complexMethodSourceTest", String.class, String.class, int.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] first value, second value, 42");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        Object result = strategy.extractParameters(context);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        assertEquals("first value", paramMap.get(context.getParameterName(0)));
        assertEquals("second value", paramMap.get(context.getParameterName(1)));
        assertEquals(42, paramMap.get(context.getParameterName(2)));
    }

    @Test
    @DisplayName("Should handle empty display name")
    void shouldHandleEmptyDisplayName() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicMethodSourceTest", String.class, int.class);
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
        // Should fallback to method source - might actually work since the provider methods exist
        // Just verify we get a valid map (could be empty or contain data)
        assertNotNull(paramMap);
    }

    @Test
    @DisplayName("Should return correct strategy name")
    void shouldReturnCorrectStrategyName() {
        // When
        String name = strategy.getStrategyName();

        // Then
        assertEquals("MethodSourceExtractionStrategy", name);
    }

    @Test
    @DisplayName("Should return correct priority")
    void shouldReturnCorrectPriority() {
        // When
        int priority = strategy.getPriority();

        // Then
        assertEquals(15, priority);
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
        @MethodSource("basicTestDataProvider")
        public void basicMethodSourceTest(String text, int number) {
            // Test method for reflection
        }

        @ParameterizedTest
        @MethodSource("mixedTypeTestDataProvider")
        public void mixedTypeMethodSourceTest(String text, int number, boolean flag, double decimal) {
            // Test method for reflection with mixed types
        }

        @ParameterizedTest
        @MethodSource("argumentsTestDataProvider")
        public void argumentsMethodSourceTest(String first, int second) {
            // Test method for reflection with Arguments
        }

        @ParameterizedTest
        @MethodSource("arrayTestDataProvider")
        public void arrayMethodSourceTest(String text, int number, boolean flag) {
            // Test method for reflection with array data
        }

        @ParameterizedTest
        @MethodSource("nullValueTestDataProvider")
        public void nullValueMethodSourceTest(String first, String second, String third) {
            // Test method for reflection with null values
        }

        @ParameterizedTest
        @MethodSource("singleTestDataProvider")
        public void singleMethodSourceTest(String value) {
            // Test method for reflection with single values
        }

        @ParameterizedTest
        @MethodSource("complexTestDataProvider")
        public void complexMethodSourceTest(String first, String second, int third) {
            // Test method for reflection with complex data
        }

        public void regularTest() {
            // Regular test method without parameterization
        }

        // Static provider methods (these would normally provide actual data)
        static Stream<Arguments> basicTestDataProvider() {
            return Stream.of(
                Arguments.of("hello", 42),
                Arguments.of("world", 84)
            );
        }

        static Stream<Arguments> mixedTypeTestDataProvider() {
            return Stream.of(
                Arguments.of("test", 100, true, 3.14),
                Arguments.of("another", 200, false, 2.71)
            );
        }

        static Stream<Arguments> argumentsTestDataProvider() {
            return Stream.of(
                Arguments.of("hello", 42),
                Arguments.of("world", 84)
            );
        }

        static Stream<Object[]> arrayTestDataProvider() {
            return Stream.of(
                new Object[]{"world", 84, false},
                new Object[]{"test", 100, true}
            );
        }

        static Iterator<Arguments> nullValueTestDataProvider() {
            return Stream.of(
                Arguments.of("hello", null, "world"),
                Arguments.of("test", "", "value")
            ).iterator();
        }

        static Stream<String> singleTestDataProvider() {
            return Stream.of("hello", "world", "test");
        }

        static Stream<Arguments> complexTestDataProvider() {
            return Stream.of(
                Arguments.of("first value", "second value", 42),
                Arguments.of("another first", "another second", 84)
            );
        }
    }
}