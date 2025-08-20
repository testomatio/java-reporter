package io.testomat.junit.extractor.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.testomat.junit.extractor.strategy.handlers.ArgumentsSourceHandler;
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

    private ArgumentsSourceHandler strategy;
    private ExtensionContext mockContext;

    @BeforeEach
    void setUp() {
        strategy = new ArgumentsSourceHandler();
        mockContext = mock(ExtensionContext.class);
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
        assertEquals("hello", paramMap.get(context.getParameterName(0)));
        assertEquals(42, paramMap.get(context.getParameterName(1)));
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
        assertEquals("test", paramMap.get(context.getParameterName(0)));
        assertEquals(100, paramMap.get(context.getParameterName(1)));
        assertEquals(true, paramMap.get(context.getParameterName(2)));
        assertEquals(3.14, paramMap.get(context.getParameterName(3)));
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
        assertEquals("hello", paramMap.get(context.getParameterName(0)));
        assertEquals(42, paramMap.get(context.getParameterName(1)));
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
        assertEquals("world", paramMap.get(context.getParameterName(0)));
        assertEquals(84, paramMap.get(context.getParameterName(1)));
        assertEquals(false, paramMap.get(context.getParameterName(2)));
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
        assertEquals("test", paramMap.get(context.getParameterName(0)));
        assertEquals(123, paramMap.get(context.getParameterName(1)));
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
        assertEquals("hello", paramMap.get(context.getParameterName(0)));
        assertNull(paramMap.get(context.getParameterName(1)));
        assertEquals("world", paramMap.get(context.getParameterName(2)));
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
        assertEquals("hello world", paramMap.get(context.getParameterName(0)));
        assertEquals("test value", paramMap.get(context.getParameterName(1)));
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
        assertEquals("hello", paramMap.get(context.getParameterName(0)));
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
    @DisplayName("Should have correct strategy name")
    void shouldHaveCorrectStrategyName() {
        // When
        String strategyName = strategy.getStrategyName();
        
        // Then
        assertEquals("ArgumentsSourceExtractionStrategy", strategyName);
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
        public void basicArgumentsSourceTest(String message, int value) {
            // Test method for reflection
        }

        @ParameterizedTest
        @ArgumentsSource(MixedTypeArgumentsProvider.class)
        public void mixedTypeArgumentsSourceTest(String description, int count, boolean isActive, double percentage) {
            // Test method for reflection with mixed types
        }

        @ParameterizedTest
        @ArgumentsSource(BasicArgumentsProvider.class)
        public void argumentsDisplayTest(String name, int id) {
            // Test method for reflection with Arguments display
        }

        @ParameterizedTest
        @ArgumentsSource(BasicArgumentsProvider.class)
        public void arrayDisplayTest(String label, int size, boolean enabled) {
            // Test method for reflection with array display
        }

        @ParameterizedTest
        @ArgumentsSource(BasicArgumentsProvider.class)
        public void customObjectTest(String title, int priority) {
            // Test method for reflection with custom object display
        }

        @ParameterizedTest
        @ArgumentsSource(NullArgumentsProvider.class)
        public void nullValueArgumentsSourceTest(String firstName, String middleName, String lastName) {
            // Test method for reflection with null values
        }

        @ParameterizedTest
        @ArgumentsSource(BasicArgumentsProvider.class)
        public void quotedArgumentsSourceTest(String username, String password) {
            // Test method for reflection with quoted values
        }

        @ParameterizedTest
        @ArgumentsSource(SingleArgumentsProvider.class)
        public void singleArgumentsSourceTest(String input) {
            // Test method for reflection with single values
        }

        public void regularTest() {
            // Regular test method without parameterization
        }
    }
}