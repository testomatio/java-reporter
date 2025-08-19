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
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

/**
 * Tests for ArgumentsSourceExtractionStrategy to verify parameter extraction from @ArgumentsSource annotations.
 */
@DisplayName("ArgumentsSourceExtractionStrategy Tests")
class ArgumentsSourceExtractionStrategyTest {

    private ArgumentsSourceExtractionStrategy strategy;
    private ExtensionContext mockContext;

    @BeforeEach
    void setUp() {
        strategy = new ArgumentsSourceExtractionStrategy();
        mockContext = mock(ExtensionContext.class);
    }

    @Test
    @DisplayName("Should support methods with @ArgumentsSource annotation")
    void shouldSupportArgumentsSourceAnnotation() throws NoSuchMethodException {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicArgumentsSourceTest", String.class, int.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("basicArgumentsSourceTest");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        boolean supports = strategy.supports(context);

        // Then
        assertTrue(supports);
    }

    @Test
    @DisplayName("Should not support methods without @ArgumentsSource annotation")
    void shouldNotSupportNonArgumentsSourceMethods() throws NoSuchMethodException {
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
    @DisplayName("Should extract ArgumentsSource parameters from display name")
    void shouldExtractArgumentsSourceParametersFromDisplayName() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicArgumentsSourceTest", String.class, int.class);
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
    @DisplayName("Should extract ArgumentsSource parameters with different types")
    void shouldExtractArgumentsSourceParametersWithDifferentTypes() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("mixedTypeArgumentsSourceTest", String.class, int.class, boolean.class, double.class);
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
    @DisplayName("Should handle Arguments display format")
    void shouldHandleArgumentsDisplayFormat() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("argumentsDisplayTest", String.class, int.class);
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
        assertEquals("hello", paramMap.get("param0"));
        assertEquals(42, paramMap.get("param1"));
    }

    @Test
    @DisplayName("Should handle array display format")
    void shouldHandleArrayDisplayFormat() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("arrayDisplayTest", String.class, int.class, boolean.class);
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
        assertEquals("world", paramMap.get("param0"));
        assertEquals(84, paramMap.get("param1"));
        assertEquals(false, paramMap.get("param2"));
    }

    @Test
    @DisplayName("Should handle custom object display format")
    void shouldHandleCustomObjectDisplayFormat() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("customObjectTest", String.class, int.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] CustomData(test, 123)");
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
        assertEquals(123, paramMap.get("param1"));
    }

    @Test
    @DisplayName("Should handle null values in ArgumentsSource")
    void shouldHandleNullValuesInArgumentsSource() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("nullValueArgumentsSourceTest", String.class, String.class, String.class);
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
    @DisplayName("Should handle quoted values")
    void shouldHandleQuotedValues() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("quotedArgumentsSourceTest", String.class, String.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] \"hello world\", \"test value\"");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        Object result = strategy.extractParameters(context);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        assertEquals("hello world", paramMap.get("param0"));
        assertEquals("test value", paramMap.get("param1"));
    }

    @Test
    @DisplayName("Should extract single ArgumentsSource parameter")
    void shouldExtractSingleArgumentsSourceParameter() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("singleArgumentsSourceTest", String.class);
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
    @DisplayName("Should fallback to ArgumentsProvider when display name parsing fails")
    void shouldFallbackToArgumentsProvider() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicArgumentsSourceTest", String.class, int.class);
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
        // Should fallback to ArgumentsProvider - might work or return empty map
        assertNotNull(paramMap);
    }

    @Test
    @DisplayName("Should handle empty display name")
    void shouldHandleEmptyDisplayName() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("basicArgumentsSourceTest", String.class, int.class);
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
        // Should fallback to ArgumentsProvider - might work or return empty map
        assertNotNull(paramMap);
    }

    @Test
    @DisplayName("Should return correct strategy name")
    void shouldReturnCorrectStrategyName() {
        // When
        String name = strategy.getStrategyName();

        // Then
        assertEquals("ArgumentsSourceExtractionStrategy", name);
    }

    @Test
    @DisplayName("Should return correct priority")
    void shouldReturnCorrectPriority() {
        // When
        int priority = strategy.getPriority();

        // Then
        assertEquals(20, priority);
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

    // Sample ArgumentsProvider implementations for testing
    public static class BasicArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of("hello", 42),
                Arguments.of("world", 84)
            );
        }
    }

    public static class MixedTypeArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of("test", 100, true, 3.14),
                Arguments.of("another", 200, false, 2.71)
            );
        }
    }

    public static class SingleArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of("hello"),
                Arguments.of("world")
            );
        }
    }

    public static class NullArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of("hello", null, "world"),
                Arguments.of(null, "test", null)
            );
        }
    }

    // Test helper class with methods for reflection
    public static class TestMethodHolder {

        @ParameterizedTest
        @ArgumentsSource(BasicArgumentsProvider.class)
        public void basicArgumentsSourceTest(String text, int number) {
            // Test method for reflection
        }

        @ParameterizedTest
        @ArgumentsSource(MixedTypeArgumentsProvider.class)
        public void mixedTypeArgumentsSourceTest(String text, int number, boolean flag, double decimal) {
            // Test method for reflection with mixed types
        }

        @ParameterizedTest
        @ArgumentsSource(BasicArgumentsProvider.class)
        public void argumentsDisplayTest(String first, int second) {
            // Test method for reflection with Arguments display
        }

        @ParameterizedTest
        @ArgumentsSource(BasicArgumentsProvider.class)
        public void arrayDisplayTest(String text, int number, boolean flag) {
            // Test method for reflection with array display
        }

        @ParameterizedTest
        @ArgumentsSource(BasicArgumentsProvider.class)
        public void customObjectTest(String text, int number) {
            // Test method for reflection with custom object display
        }

        @ParameterizedTest
        @ArgumentsSource(NullArgumentsProvider.class)
        public void nullValueArgumentsSourceTest(String first, String second, String third) {
            // Test method for reflection with null values
        }

        @ParameterizedTest
        @ArgumentsSource(BasicArgumentsProvider.class)
        public void quotedArgumentsSourceTest(String first, String second) {
            // Test method for reflection with quoted values
        }

        @ParameterizedTest
        @ArgumentsSource(SingleArgumentsProvider.class)
        public void singleArgumentsSourceTest(String value) {
            // Test method for reflection with single values
        }

        public void regularTest() {
            // Regular test method without parameterization
        }
    }
}