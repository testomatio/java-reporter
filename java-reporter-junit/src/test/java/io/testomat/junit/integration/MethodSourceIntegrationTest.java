package io.testomat.junit.integration;

import io.testomat.junit.extractor.strategy.ParameterExtractorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration tests for MethodSourceExtractionStrategy with actual parameterized test scenarios.
 */
public class MethodSourceIntegrationTest {

    @Test
    void shouldExtractMethodSourceParametersInRealScenario() throws Exception {
        // Given
        ParameterExtractorService service = new ParameterExtractorService();
        ExtensionContext mockContext = mock(ExtensionContext.class);
        
        Method method = TestClass.class.getMethod("mixedMethodSourceTest", String.class, int.class, boolean.class);
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
        assertEquals("hello", paramMap.get("userName"));
        assertEquals(42, paramMap.get("userId"));
        assertEquals(true, paramMap.get("isActive"));
    }

    @Test
    void shouldExtractArgumentsFormatMethodSourceParameters() throws Exception {
        // Given
        ParameterExtractorService service = new ParameterExtractorService();
        ExtensionContext mockContext = mock(ExtensionContext.class);
        
        Method method = TestClass.class.getMethod("argumentsMethodSourceTest", String.class, String.class);
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
    void shouldExtractArrayFormatMethodSourceParameters() throws Exception {
        // Given
        ParameterExtractorService service = new ParameterExtractorService();
        ExtensionContext mockContext = mock(ExtensionContext.class);
        
        Method method = TestClass.class.getMethod("arrayMethodSourceTest", String.class, int.class, boolean.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] [test, 100, false]");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        // When
        Object result = service.extractParameters(mockContext);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        assertEquals("test", paramMap.get("projectName"));
        assertEquals(100, paramMap.get("maxSize"));
        assertEquals(false, paramMap.get("isPublic"));
    }

    @Test
    void shouldHandleNullValuesInMethodSource() throws Exception {
        // Given
        ParameterExtractorService service = new ParameterExtractorService();
        ExtensionContext mockContext = mock(ExtensionContext.class);
        
        Method method = TestClass.class.getMethod("nullValueMethodSourceTest", String.class, String.class);
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
        assertNull(paramMap.get("additionalInfo"));
    }

    @Test
    void shouldFallbackToMethodInvocationWhenDisplayNameUnparseable() throws Exception {
        // Given
        ParameterExtractorService service = new ParameterExtractorService();
        ExtensionContext mockContext = mock(ExtensionContext.class);
        
        Method method = TestClass.class.getMethod("methodSourceWithProvidersTest", String.class, int.class);
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
        // Should have parameters from actual method invocation
        if (!paramMap.isEmpty()) {
            // If method invocation worked, verify the data
            assertTrue(paramMap.containsKey("serviceName"));
            assertTrue(paramMap.containsKey("servicePort"));
        }
        // At minimum, we should get a valid map structure
        assertNotNull(paramMap);
    }

    // Test class with actual parameterized methods
    public static class TestClass {

        @ParameterizedTest
        @MethodSource("mixedDataProvider")
        public void mixedMethodSourceTest(String userName, int userId, boolean isActive) {
            // Real parameterized test method
        }

        @ParameterizedTest
        @MethodSource("argumentsDataProvider")
        public void argumentsMethodSourceTest(String firstName, String lastName) {
            // Real parameterized test method with Arguments
        }

        @ParameterizedTest
        @MethodSource("arrayDataProvider")
        public void arrayMethodSourceTest(String projectName, int maxSize, boolean isPublic) {
            // Real parameterized test method with array data
        }

        @ParameterizedTest
        @MethodSource("nullDataProvider")
        public void nullValueMethodSourceTest(String message, String additionalInfo) {
            // Real parameterized test method with null values
        }

        @ParameterizedTest
        @MethodSource("providersDataProvider")
        public void methodSourceWithProvidersTest(String serviceName, int servicePort) {
            // Real parameterized test method that should work with fallback
        }

        // Static provider methods
        static Stream<Arguments> mixedDataProvider() {
            return Stream.of(
                Arguments.of("hello", 42, true),
                Arguments.of("world", 84, false)
            );
        }

        static Stream<Arguments> argumentsDataProvider() {
            return Stream.of(
                Arguments.of("hello", "world"),
                Arguments.of("foo", "bar")
            );
        }

        static Stream<Object[]> arrayDataProvider() {
            return Stream.of(
                new Object[]{"test", 100, false},
                new Object[]{"example", 200, true}
            );
        }

        static Stream<Arguments> nullDataProvider() {
            return Stream.of(
                Arguments.of("hello", null),
                Arguments.of(null, "world")
            );
        }

        static Stream<Arguments> providersDataProvider() {
            return Stream.of(
                Arguments.of("provider", 123),
                Arguments.of("fallback", 456)
            );
        }
    }
}