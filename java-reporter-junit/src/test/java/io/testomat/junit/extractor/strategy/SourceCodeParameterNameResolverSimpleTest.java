package io.testomat.junit.extractor.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simplified tests for SourceCodeParameterNameResolver that work with real reflection.
 * Tests the actual behavior without complex mocking.
 */
@DisplayName("SourceCodeParameterNameResolver Simple Tests")
class SourceCodeParameterNameResolverSimpleTest {

    private SourceCodeParameterNameResolver resolver;

    @BeforeEach
    void setUp() {
        // Use the singleton instance for realistic testing
        resolver = SourceCodeParameterNameResolver.getInstance();
        resolver.clearCache(); // Start with clean cache for each test
    }

    @Nested
    @DisplayName("Basic Functionality Tests")
    class BasicFunctionalityTests {

        @Test
        @DisplayName("Should handle null method gracefully")
        void shouldHandleNullMethodGracefully() {
            // When
            List<String> result = resolver.resolveParameterNames(null);
            
            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should resolve parameter names for real method")
        void shouldResolveParameterNamesForRealMethod() throws Exception {
            // Given
            Method method = TestClass.class.getMethod("testMethod", String.class, int.class);

            // When
            List<String> parameterNames = resolver.resolveParameterNames(method);

            // Then
            assertNotNull(parameterNames);
            assertEquals(2, parameterNames.size());
            
            // With -parameters flag, should get actual names: productName, maxCount
            // Without -parameters flag, should get synthetic names: arg0, arg1
            // Either is acceptable - the key is that it's consistent and not generic param0, param1
            String firstParam = parameterNames.get(0);
            String secondParam = parameterNames.get(1);
            
            assertTrue(
                ("productName".equals(firstParam) && "maxCount".equals(secondParam)) ||
                ("arg0".equals(firstParam) && "arg1".equals(secondParam)),
                "Expected either source names (productName, maxCount) or reflection names (arg0, arg1), " +
                "but got: " + parameterNames
            );
        }

        @Test
        @DisplayName("Should handle method with no parameters")
        void shouldHandleMethodWithNoParameters() throws Exception {
            // Given
            Method method = TestClass.class.getMethod("noParamMethod");

            // When
            List<String> parameterNames = resolver.resolveParameterNames(method);

            // Then
            assertNotNull(parameterNames);
            assertTrue(parameterNames.isEmpty());
        }

        @Test
        @DisplayName("Should get individual parameter names by index")
        void shouldGetIndividualParameterNamesByIndex() throws Exception {
            // Given
            Method method = TestClass.class.getMethod("testMethod", String.class, int.class);

            // When
            String param0 = resolver.getParameterName(method, 0);
            String param1 = resolver.getParameterName(method, 1);

            // Then
            assertNotNull(param0);
            assertNotNull(param1);
            assertNotEquals("param0", param0); // Should not be generic fallback
            assertNotEquals("param1", param1); // Should not be generic fallback
            
            // Should be either source names or reflection names
            assertTrue(
                ("productName".equals(param0) && "maxCount".equals(param1)) ||
                ("arg0".equals(param0) && "arg1".equals(param1)),
                "Expected meaningful parameter names, got: " + param0 + ", " + param1
            );
        }

        @Test
        @DisplayName("Should return generic name for out of bounds index")
        void shouldReturnGenericNameForOutOfBoundsIndex() throws Exception {
            // Given
            Method method = TestClass.class.getMethod("testMethod", String.class, int.class);

            // When & Then
            assertEquals("param2", resolver.getParameterName(method, 2)); // Out of bounds
            assertEquals("param-1", resolver.getParameterName(method, -1)); // Negative
            assertEquals("param10", resolver.getParameterName(method, 10)); // Way out of bounds
        }
    }

    @Nested
    @DisplayName("Caching Tests")
    class CachingTests {

        @Test
        @DisplayName("Should cache results consistently")
        void shouldCacheResultsConsistently() throws Exception {
            // Given
            Method method = TestClass.class.getMethod("testMethod", String.class, int.class);

            // When - multiple calls
            List<String> result1 = resolver.resolveParameterNames(method);
            List<String> result2 = resolver.resolveParameterNames(method);
            List<String> result3 = resolver.resolveParameterNames(method);

            // Then - should be identical due to caching
            assertEquals(result1, result2);
            assertEquals(result2, result3);
            assertEquals(result1, result3);
        }

        @Test
        @DisplayName("Should handle cache clearing")
        void shouldHandleCacheClearing() throws Exception {
            // Given
            Method method = TestClass.class.getMethod("testMethod", String.class, int.class);

            // When
            List<String> resultBeforeClear = resolver.resolveParameterNames(method);
            assertEquals(1, resolver.getCacheSize());
            
            resolver.clearCache();
            assertEquals(0, resolver.getCacheSize());
            
            List<String> resultAfterClear = resolver.resolveParameterNames(method);
            assertEquals(1, resolver.getCacheSize());

            // Then - should be consistent even after cache clear
            assertEquals(resultBeforeClear, resultAfterClear);
        }

        @Test
        @DisplayName("Should cache different methods separately")
        void shouldCacheDifferentMethodsSeparately() throws Exception {
            // Given
            Method method1 = TestClass.class.getMethod("testMethod", String.class, int.class);
            Method method2 = TestClass.class.getMethod("singleParamMethod", String.class);

            // When
            List<String> result1 = resolver.resolveParameterNames(method1);
            List<String> result2 = resolver.resolveParameterNames(method2);

            // Then
            assertEquals(2, result1.size());
            assertEquals(1, result2.size());
            assertEquals(2, resolver.getCacheSize()); // Two different methods cached
            
            // Results should be different
            assertNotEquals(result1, result2);
        }
    }

    @Nested
    @DisplayName("Real World Scenarios")
    class RealWorldScenarios {

        @Test
        @DisplayName("Should work with parameterized test methods")
        void shouldWorkWithParameterizedTestMethods() throws Exception {
            // Given
            Method method = TestClass.class.getMethod("parameterizedTestMethod", String.class);

            // When
            List<String> parameterNames = resolver.resolveParameterNames(method);

            // Then
            assertNotNull(parameterNames);
            assertEquals(1, parameterNames.size());
            
            String paramName = parameterNames.get(0);
            // Should be either source name or reflection name, not generic
            assertTrue(
                "inputValue".equals(paramName) || "arg0".equals(paramName),
                "Expected meaningful parameter name, got: " + paramName
            );
        }

        @Test
        @DisplayName("Should handle complex parameter types")
        void shouldHandleComplexParameterTypes() throws Exception {
            // Given  
            Method method = TestClass.class.getMethod("complexParameterMethod", 
                java.util.List.class, java.util.Map.class, String[].class);

            // When
            List<String> parameterNames = resolver.resolveParameterNames(method);

            // Then
            assertNotNull(parameterNames);
            assertEquals(3, parameterNames.size());
            
            // Each parameter should have a meaningful name
            for (int i = 0; i < parameterNames.size(); i++) {
                String paramName = parameterNames.get(i);
                assertFalse(paramName.startsWith("param"), 
                           "Parameter " + i + " should not use generic naming, got: " + paramName);
            }
        }
    }

    // Test class for reflection-based testing
    public static class TestClass {
        
        public void testMethod(String productName, int maxCount) {
            // Test method with meaningful parameter names
        }
        
        public void singleParamMethod(String value) {
            // Single parameter method
        }
        
        public void noParamMethod() {
            // No parameter method
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"test1", "test2"})
        public void parameterizedTestMethod(String inputValue) {
            // Parameterized test method
        }
        
        public void complexParameterMethod(java.util.List<String> items, 
                                         java.util.Map<String, Object> metadata, 
                                         String[] tags) {
            // Method with complex parameter types
        }
    }
}