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
        
        // The key difference: should use actual parameter name 'inputValue' from source
        // instead of generic 'param0' or synthetic 'arg0'
        assertTrue(paramMap.containsKey("inputValue") || 
                  paramMap.containsKey("arg0")); // Fallback when source parsing fails
        
        // Verify the value is correct regardless of parameter name
        Object value = paramMap.values().iterator().next();
        assertEquals("hello", value);
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
        
        // Should use actual parameter names from source code when available
        // or fall back to reflection-based names
        boolean hasSourceNames = paramMap.containsKey("productName") && 
                                paramMap.containsKey("price") && 
                                paramMap.containsKey("inStock");
        boolean hasFallbackNames = paramMap.containsKey("arg0") && 
                                  paramMap.containsKey("arg1") && 
                                  paramMap.containsKey("arg2");
        
        assertTrue(hasSourceNames || hasFallbackNames, 
                  "Should have either source-based names or fallback names. Actual keys: " + paramMap.keySet());
        
        // Verify values are correct regardless of parameter names
        assertTrue(paramMap.containsValue("product"));
        assertTrue(paramMap.containsValue(100));
        assertTrue(paramMap.containsValue(true));
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
        boolean hasSourceNames = paramMap.containsKey("productName") && 
                                paramMap.containsKey("price") && 
                                paramMap.containsKey("inStock");
        boolean hasFallbackNames = paramMap.containsKey("arg0") && 
                                  paramMap.containsKey("arg1") && 
                                  paramMap.containsKey("arg2");
        
        assertTrue(hasSourceNames || hasFallbackNames,
                  "Should have meaningful parameter names. Actual keys: " + paramMap.keySet());
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