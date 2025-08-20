package io.testomat.junit.integration;

import io.testomat.junit.extractor.strategy.ParameterExtractorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration tests to verify that the new source code parameter naming
 * works correctly with the complete parameter extraction pipeline.
 */
public class SourceCodeParameterNamingIntegrationTest {

    @Test
    void shouldUseSourceCodeParameterNamesForSingleParameter() throws Exception {
        // Given
        ParameterExtractorService service = new ParameterExtractorService();
        ExtensionContext mockContext = mock(ExtensionContext.class);
        
        Method method = TestClass.class.getMethod("valueSourceTest", String.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] hello");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        // When
        Object result = service.extractParameters(mockContext);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        
        // Should use actual parameter name 'inputValue' from source code
        assertTrue(paramMap.containsKey("inputValue"), 
                  "Should use actual parameter name 'inputValue'. Actual keys: " + paramMap.keySet());
        assertEquals("hello", paramMap.get("inputValue"));
    }

    @Test
    void shouldUseSourceCodeParameterNamesForMultipleParameters() throws Exception {
        // Given
        ParameterExtractorService service = new ParameterExtractorService();
        ExtensionContext mockContext = mock(ExtensionContext.class);
        
        Method method = TestClass.class.getMethod("csvSourceTest", String.class, int.class, boolean.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] product, 100, true");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        // When
        Object result = service.extractParameters(mockContext);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        
        assertEquals(3, paramMap.size());
        
        // Should use actual parameter names from source code
        assertTrue(paramMap.containsKey("productName"), 
                  "Should have 'productName' parameter. Actual keys: " + paramMap.keySet());
        assertTrue(paramMap.containsKey("price"), 
                  "Should have 'price' parameter. Actual keys: " + paramMap.keySet());
        assertTrue(paramMap.containsKey("inStock"), 
                  "Should have 'inStock' parameter. Actual keys: " + paramMap.keySet());
        
        // Verify values are correct
        assertEquals("product", paramMap.get("productName"));
        assertEquals(100, paramMap.get("price"));
        assertEquals(true, paramMap.get("inStock"));
    }

    @Test
    void shouldHandleParameterNamingConsistentlyAcrossMultipleCalls() throws Exception {
        // Given
        ParameterExtractorService service = new ParameterExtractorService();
        ExtensionContext mockContext = mock(ExtensionContext.class);
        
        Method method = TestClass.class.getMethod("valueSourceTest", String.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] test");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        // When - multiple calls
        Object result1 = service.extractParameters(mockContext);
        Object result2 = service.extractParameters(mockContext);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap1 = (Map<String, Object>) result1;
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap2 = (Map<String, Object>) result2;
        
        // Should have consistent parameter names across calls (due to caching)
        assertEquals(paramMap1.keySet(), paramMap2.keySet());
        assertEquals(paramMap1.get(paramMap1.keySet().iterator().next()), 
                    paramMap2.get(paramMap2.keySet().iterator().next()));
    }

    @Test
    void shouldProvideParameterNamesForComplexTypes() throws Exception {
        // Given
        ParameterExtractorService service = new ParameterExtractorService();
        ExtensionContext mockContext = mock(ExtensionContext.class);
        
        // Use a method that actually has a parameter source annotation
        Method method = TestClass.class.getMethod("csvSourceTest", String.class, int.class, boolean.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] product, 100, true");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        // When
        Object result = service.extractParameters(mockContext);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        
        assertEquals(3, paramMap.size());
        
        // Should handle parameter types with meaningful names
        assertTrue(paramMap.containsKey("productName"), 
                  "Should have 'productName' parameter. Actual keys: " + paramMap.keySet());
        assertTrue(paramMap.containsKey("price"), 
                  "Should have 'price' parameter. Actual keys: " + paramMap.keySet());
        assertTrue(paramMap.containsKey("inStock"), 
                  "Should have 'inStock' parameter. Actual keys: " + paramMap.keySet());
        
        // Verify values are correct
        assertEquals("product", paramMap.get("productName"));
        assertEquals(100, paramMap.get("price"));
        assertEquals(true, paramMap.get("inStock"));
    }

    // Test class with parameterized methods for testing
    public static class TestClass {
        
        @ParameterizedTest
        @ValueSource(strings = {"test1", "test2", "test3"})
        public void valueSourceTest(String inputValue) {
            // Test method with meaningful parameter name 'inputValue'
        }
        
        @ParameterizedTest
        @CsvSource({
            "product, 100, true",
            "service, 50, false"
        })
        public void csvSourceTest(String productName, int price, boolean inStock) {
            // Test method with meaningful parameter names
        }
        
        public void complexParameterTest(java.util.List<String> items, java.util.Map<String, Object> metadata) {
            // Test method with complex parameter types
        }
    }
}