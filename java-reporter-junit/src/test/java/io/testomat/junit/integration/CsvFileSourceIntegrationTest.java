package io.testomat.junit.integration;

import io.testomat.junit.extractor.strategy.ParameterExtractorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration tests for CsvFileSourceExtractionStrategy with actual parameterized test scenarios.
 */
public class CsvFileSourceIntegrationTest {

    @Test
    void shouldExtractCsvFileParametersInRealScenario() throws Exception {
        // Given
        ParameterExtractorService service = new ParameterExtractorService();
        ExtensionContext mockContext = mock(ExtensionContext.class);
        
        Method method = TestClass.class.getMethod("mixedCsvFileTest", String.class, int.class, boolean.class);
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
        assertEquals("hello", paramMap.get("productCode"));
        assertEquals(42, paramMap.get("stockLevel"));
        assertEquals(true, paramMap.get("isActive"));
    }

    @Test
    void shouldExtractQuotedCsvFileParametersInRealScenario() throws Exception {
        // Given
        ParameterExtractorService service = new ParameterExtractorService();
        ExtensionContext mockContext = mock(ExtensionContext.class);
        
        Method method = TestClass.class.getMethod("quotedCsvFileTest", String.class, String.class);
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
        assertEquals("hello, world", paramMap.get("category"));
        assertEquals("test value", paramMap.get("description"));
    }

    @Test
    void shouldExtractCustomDelimiterCsvFileParametersInRealScenario() throws Exception {
        // Given
        ParameterExtractorService service = new ParameterExtractorService();
        ExtensionContext mockContext = mock(ExtensionContext.class);
        
        Method method = TestClass.class.getMethod("customDelimiterCsvFileTest", String.class, int.class);
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
        assertEquals("hello", paramMap.get("itemType"));
        assertEquals(42, paramMap.get("itemCount"));
    }

    @Test
    void shouldHandleFileNotFoundGracefully() throws Exception {
        // Given
        ParameterExtractorService service = new ParameterExtractorService();
        ExtensionContext mockContext = mock(ExtensionContext.class);
        
        Method method = TestClass.class.getMethod("nonExistentFileCsvTest", String.class, int.class);
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
        // Should return empty map when file doesn't exist and display name can't be parsed
        assertTrue(paramMap.isEmpty());
    }

    @Test
    void shouldFallbackToDisplayNameWhenFileNotAccessible() throws Exception {
        // Given
        ParameterExtractorService service = new ParameterExtractorService();
        ExtensionContext mockContext = mock(ExtensionContext.class);
        
        Method method = TestClass.class.getMethod("nonExistentFileCsvTest", String.class, int.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] world, 84");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        // When
        Object result = service.extractParameters(mockContext);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        assertEquals("world", paramMap.get("itemType"));
        assertEquals(84, paramMap.get("itemCount"));
    }

    // Test class with actual parameterized methods
    public static class TestClass {

        @ParameterizedTest
        @CsvFileSource(resources = "/test-data.csv")
        public void mixedCsvFileTest(String productCode, int stockLevel, boolean isActive) {
            // Real parameterized test method
        }

        @ParameterizedTest
        @CsvFileSource(resources = "/quoted-data.csv")
        public void quotedCsvFileTest(String category, String description) {
            // Real parameterized test method with quoted values
        }

        @ParameterizedTest
        @CsvFileSource(resources = "/custom-delimiter.csv", delimiter = ';')
        public void customDelimiterCsvFileTest(String itemType, int itemCount) {
            // Real parameterized test method with custom delimiter
        }

        @ParameterizedTest
        @CsvFileSource(resources = "/non-existent.csv")
        public void nonExistentFileCsvTest(String itemType, int itemCount) {
            // Real parameterized test method with non-existent file
        }
    }
}