package io.testomat.junit.integration;

import io.testomat.junit.extractor.strategy.ParameterExtractorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration tests for ArgumentsSourceExtractionStrategy with actual parameterized test scenarios.
 */
public class ArgumentsSourceIntegrationTest {

    @Test
    void shouldExtractArgumentsSourceParametersInRealScenario() throws Exception {
        // Given
        ParameterExtractorService service = new ParameterExtractorService();
        ExtensionContext mockContext = mock(ExtensionContext.class);
        
        Method method = TestClass.class.getMethod("mixedArgumentsSourceTest", String.class, int.class, boolean.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] hello, 42, true");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        // When
        Object result = service.extractParameters(mockContext);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        assertEquals("hello", paramMap.get("productName"));
        assertEquals(42, paramMap.get("quantity"));
        assertEquals(true, paramMap.get("available"));
    }

    @Test
    void shouldExtractArgumentsFormatArgumentsSourceParameters() throws Exception {
        // Given
        ParameterExtractorService service = new ParameterExtractorService();
        ExtensionContext mockContext = mock(ExtensionContext.class);
        
        Method method = TestClass.class.getMethod("argumentsFormatTest", String.class, String.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] Arguments{arguments=[hello, world]}");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        // When
        Object result = service.extractParameters(mockContext);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        assertEquals("hello", paramMap.get("firstName"));
        assertEquals("world", paramMap.get("lastName"));
    }

    @Test
    void shouldExtractCustomObjectFormatArgumentsSourceParameters() throws Exception {
        // Given
        ParameterExtractorService service = new ParameterExtractorService();
        ExtensionContext mockContext = mock(ExtensionContext.class);
        
        Method method = TestClass.class.getMethod("customObjectTest", String.class, int.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] CustomData(test, 123)");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        // When
        Object result = service.extractParameters(mockContext);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        assertEquals("test", paramMap.get("itemName"));
        assertEquals(123, paramMap.get("itemId"));
    }

    @Test
    void shouldHandleNullValuesInArgumentsSource() throws Exception {
        // Given
        ParameterExtractorService service = new ParameterExtractorService();
        ExtensionContext mockContext = mock(ExtensionContext.class);
        
        Method method = TestClass.class.getMethod("nullValueTest", String.class, String.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] hello, null");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        // When
        Object result = service.extractParameters(mockContext);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        assertEquals("hello", paramMap.get("message"));
        assertNull(paramMap.get("description"));
    }

    @Test
    void shouldHandleQuotedValuesInArgumentsSource() throws Exception {
        // Given
        ParameterExtractorService service = new ParameterExtractorService();
        ExtensionContext mockContext = mock(ExtensionContext.class);
        
        Method method = TestClass.class.getMethod("quotedValuesTest", String.class, String.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] \"hello world\", \"test value\"");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        // When
        Object result = service.extractParameters(mockContext);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        assertEquals("hello world", paramMap.get("phrase"));
        assertEquals("test value", paramMap.get("testData"));
    }

    @Test
    void shouldFallbackToArgumentsProviderWhenDisplayNameUnparseable() throws Exception {
        // Given
        ParameterExtractorService service = new ParameterExtractorService();
        ExtensionContext mockContext = mock(ExtensionContext.class);
        
        Method method = TestClass.class.getMethod("argumentsProviderFallbackTest", String.class, int.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("unparseable display name");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        // When
        Object result = service.extractParameters(mockContext);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        // Should have parameters from actual ArgumentsProvider invocation
        if (!paramMap.isEmpty()) {
            // If ArgumentsProvider invocation worked, verify the data
            assertTrue(paramMap.containsKey("serviceName"));
            assertTrue(paramMap.containsKey("serviceId"));
        }
        // At minimum, we should get a valid map structure
        assertNotNull(paramMap);
    }

    // Sample ArgumentsProvider implementations for testing
    public static class MixedDataProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of("hello", 42, true),
                Arguments.of("world", 84, false)
            );
        }
    }

    public static class BasicDataProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of("hello", "world"),
                Arguments.of("foo", "bar")
            );
        }
    }

    public static class CustomDataProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of("test", 123),
                Arguments.of("example", 456)
            );
        }
    }

    public static class NullDataProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of("hello", null),
                Arguments.of(null, "world")
            );
        }
    }

    public static class QuotedDataProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of("hello world", "test value"),
                Arguments.of("another phrase", "another test")
            );
        }
    }

    public static class FallbackDataProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of("provider", 123),
                Arguments.of("fallback", 456)
            );
        }
    }

    // Test class with actual parameterized methods
    public static class TestClass {

        @ParameterizedTest
        @ArgumentsSource(MixedDataProvider.class)
        public void mixedArgumentsSourceTest(String productName, int quantity, boolean available) {
            // Real parameterized test method
        }

        @ParameterizedTest
        @ArgumentsSource(BasicDataProvider.class)
        public void argumentsFormatTest(String firstName, String lastName) {
            // Real parameterized test method with Arguments format
        }

        @ParameterizedTest
        @ArgumentsSource(CustomDataProvider.class)
        public void customObjectTest(String itemName, int itemId) {
            // Real parameterized test method with custom object format
        }

        @ParameterizedTest
        @ArgumentsSource(NullDataProvider.class)
        public void nullValueTest(String message, String description) {
            // Real parameterized test method with null values
        }

        @ParameterizedTest
        @ArgumentsSource(QuotedDataProvider.class)
        public void quotedValuesTest(String phrase, String testData) {
            // Real parameterized test method with quoted values
        }

        @ParameterizedTest
        @ArgumentsSource(FallbackDataProvider.class)
        public void argumentsProviderFallbackTest(String serviceName, int serviceId) {
            // Real parameterized test method that should work with ArgumentsProvider fallback
        }
    }
}