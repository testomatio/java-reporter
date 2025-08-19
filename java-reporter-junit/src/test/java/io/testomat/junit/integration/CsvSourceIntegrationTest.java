package io.testomat.junit.integration;

import io.testomat.junit.extractor.strategy.ParameterExtractorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration tests for CsvSourceExtractionStrategy with actual parameterized test scenarios.
 */
public class CsvSourceIntegrationTest {

    @Test
    void shouldExtractCsvParametersInRealScenario() throws Exception {
        // Given
        ParameterExtractorService service = new ParameterExtractorService();
        ExtensionContext mockContext = mock(ExtensionContext.class);
        
        Method method = TestClass.class.getMethod("mixedCsvTest", String.class, int.class, boolean.class);
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
        assertEquals("hello", paramMap.get("param0"));
        assertEquals(42, paramMap.get("param1"));
        assertEquals(true, paramMap.get("param2"));
    }

    @Test
    void shouldExtractQuotedCsvParametersInRealScenario() throws Exception {
        // Given
        ParameterExtractorService service = new ParameterExtractorService();
        ExtensionContext mockContext = mock(ExtensionContext.class);
        
        Method method = TestClass.class.getMethod("quotedCsvTest", String.class, String.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] \"hello, world\", \"test value\"");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        // When
        Object result = service.extractParameters(mockContext);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        assertEquals("hello, world", paramMap.get("param0"));
        assertEquals("test value", paramMap.get("param1"));
    }

    @Test
    void shouldExtractCustomDelimiterCsvParametersInRealScenario() throws Exception {
        // Given
        ParameterExtractorService service = new ParameterExtractorService();
        ExtensionContext mockContext = mock(ExtensionContext.class);
        
        Method method = TestClass.class.getMethod("customDelimiterCsvTest", String.class, int.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] hello; 42");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        // When
        Object result = service.extractParameters(mockContext);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        assertEquals("hello", paramMap.get("param0"));
        assertEquals(42, paramMap.get("param1"));
    }

    // Test class with actual parameterized methods
    public static class TestClass {

        @ParameterizedTest
        @CsvSource({"hello, 42, true", "world, 100, false", "test, 0, true"})
        public void mixedCsvTest(String text, int number, boolean flag) {
            // Real parameterized test method
        }

        @ParameterizedTest
        @CsvSource({"\"hello, world\", \"test value\"", "\"another\", \"value\""})
        public void quotedCsvTest(String first, String second) {
            // Real parameterized test method with quoted values
        }

        @ParameterizedTest
        @CsvSource(value = {"hello; 42", "world; 84"}, delimiter = ';')
        public void customDelimiterCsvTest(String text, int number) {
            // Real parameterized test method with custom delimiter
        }
    }
}