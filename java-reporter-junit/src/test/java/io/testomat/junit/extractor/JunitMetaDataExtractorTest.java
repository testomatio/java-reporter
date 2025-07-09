package io.testomat.junit.extractor;

import io.testomat.core.annotation.TestId;
import io.testomat.core.annotation.Title;
import io.testomat.core.exception.NoMethodInContextException;
import io.testomat.core.model.TestMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("JunitMetaDataExtractor Tests")
class JunitMetaDataExtractorTest {

    private JunitMetaDataExtractor extractor;

    @Mock
    private ExtensionContext mockContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        extractor = new JunitMetaDataExtractor();
    }

    @Test
    @DisplayName("Should extract metadata from method with both @Title and @TestId annotations")
    void shouldExtractMetadataWithBothAnnotations() throws NoSuchMethodException {
        // Given
        Method testMethod = TestMethods.class.getMethod("methodWithBothAnnotations");
        when(mockContext.getTestMethod()).thenReturn(Optional.of(testMethod));
        when(mockContext.getTestClass()).thenReturn(Optional.of(TestMethods.class));

        // When
        TestMetadata metadata = extractor.extractTestMetadata(mockContext);

        // Then
        assertNotNull(metadata);
        assertEquals("Custom Test Title", metadata.getTitle());
        assertEquals("TEST-123", metadata.getTestId());
        assertEquals("TestMethods", metadata.getSuiteTitle());
        assertEquals("TestMethods.java", metadata.getFile());
    }

    @Test
    @DisplayName("Should extract metadata from method with only @Title annotation")
    void shouldExtractMetadataWithOnlyTitleAnnotation() throws NoSuchMethodException {
        // Given
        Method testMethod = TestMethods.class.getMethod("methodWithOnlyTitle");
        when(mockContext.getTestMethod()).thenReturn(Optional.of(testMethod));
        when(mockContext.getTestClass()).thenReturn(Optional.of(TestMethods.class));

        // When
        TestMetadata metadata = extractor.extractTestMetadata(mockContext);

        // Then
        assertNotNull(metadata);
        assertEquals("Title Only Test", metadata.getTitle());
        assertNull(metadata.getTestId());
        assertEquals("TestMethods", metadata.getSuiteTitle());
        assertEquals("TestMethods.java", metadata.getFile());
    }

    @Test
    @DisplayName("Should extract metadata from method with only @TestId annotation")
    void shouldExtractMetadataWithOnlyTestIdAnnotation() throws NoSuchMethodException {
        // Given
        Method testMethod = TestMethods.class.getMethod("methodWithOnlyTestId");
        String displayName = "Method With Only Test ID";
        
        when(mockContext.getTestMethod()).thenReturn(Optional.of(testMethod));
        when(mockContext.getTestClass()).thenReturn(Optional.of(TestMethods.class));
        when(mockContext.getDisplayName()).thenReturn(displayName);

        // When
        TestMetadata metadata = extractor.extractTestMetadata(mockContext);

        // Then
        assertNotNull(metadata);
        assertEquals(displayName, metadata.getTitle());
        assertEquals("TEST-456", metadata.getTestId());
        assertEquals("TestMethods", metadata.getSuiteTitle());
        assertEquals("TestMethods.java", metadata.getFile());
    }

    @Test
    @DisplayName("Should extract metadata from method without annotations using display name")
    void shouldExtractMetadataWithoutAnnotations() throws NoSuchMethodException {
        // Given
        Method testMethod = TestMethods.class.getMethod("methodWithoutAnnotations");
        String displayName = "Method Without Annotations()";
        
        when(mockContext.getTestMethod()).thenReturn(Optional.of(testMethod));
        when(mockContext.getTestClass()).thenReturn(Optional.of(TestMethods.class));
        when(mockContext.getDisplayName()).thenReturn(displayName);

        // When
        TestMetadata metadata = extractor.extractTestMetadata(mockContext);

        // Then
        assertNotNull(metadata);
        assertEquals(displayName, metadata.getTitle());
        assertNull(metadata.getTestId());
        assertEquals("TestMethods", metadata.getSuiteTitle());
        assertEquals("TestMethods.java", metadata.getFile());
    }

    @Test
    @DisplayName("Should use 'Unknown' suite title when test class is not present")
    void shouldUseUnknownSuiteTitleWhenClassNotPresent() throws NoSuchMethodException {
        // Given
        Method testMethod = TestMethods.class.getMethod("methodWithBothAnnotations");
        when(mockContext.getTestMethod()).thenReturn(Optional.of(testMethod));
        when(mockContext.getTestClass()).thenReturn(Optional.empty());

        // When
        TestMetadata metadata = extractor.extractTestMetadata(mockContext);

        // Then
        assertNotNull(metadata);
        assertEquals("Custom Test Title", metadata.getTitle());
        assertEquals("TEST-123", metadata.getTestId());
        assertEquals("Unknown", metadata.getSuiteTitle());
        assertEquals("Unknown.java", metadata.getFile());
    }

    @Test
    @DisplayName("Should throw NoMethodInContextException when test method is not present")
    void shouldThrowExceptionWhenMethodNotPresent() {
        // Given
        String displayName = "Test Display Name";
        when(mockContext.getTestMethod()).thenReturn(Optional.empty());
        when(mockContext.getDisplayName()).thenReturn(displayName);

        // When & Then
        NoMethodInContextException exception = assertThrows(
            NoMethodInContextException.class,
            () -> extractor.extractTestMetadata(mockContext)
        );
        
        assertEquals("No test method found in " + displayName, exception.getMessage());
    }

    @Test
    @DisplayName("Should handle null ExtensionContext gracefully")
    void shouldHandleNullContext() {
        // When & Then
        assertThrows(
            NullPointerException.class,
            () -> extractor.extractTestMetadata(null)
        );
    }

    @Test
    @DisplayName("Should extract metadata from nested test class")
    void shouldExtractMetadataFromNestedClass() throws NoSuchMethodException {
        // Given
        Method testMethod = NestedTestClass.class.getMethod("nestedTestMethod");
        when(mockContext.getTestMethod()).thenReturn(Optional.of(testMethod));
        when(mockContext.getTestClass()).thenReturn(Optional.of(NestedTestClass.class));
        when(mockContext.getDisplayName()).thenReturn("Nested Test Method");

        // When
        TestMetadata metadata = extractor.extractTestMetadata(mockContext);

        // Then
        assertNotNull(metadata);
        assertEquals("Nested Test Method", metadata.getTitle());
        assertEquals("NESTED-001", metadata.getTestId());
        assertEquals("NestedTestClass", metadata.getSuiteTitle());
        assertEquals("NestedTestClass.java", metadata.getFile());
    }

    @Test
    @DisplayName("Should handle empty title annotation")
    void shouldHandleEmptyTitleAnnotation() throws NoSuchMethodException {
        // Given
        Method testMethod = TestMethods.class.getMethod("methodWithEmptyTitle");
        String displayName = "Method With Empty Title";
        
        when(mockContext.getTestMethod()).thenReturn(Optional.of(testMethod));
        when(mockContext.getTestClass()).thenReturn(Optional.of(TestMethods.class));
        when(mockContext.getDisplayName()).thenReturn(displayName);

        // When
        TestMetadata metadata = extractor.extractTestMetadata(mockContext);

        // Then
        assertNotNull(metadata);
        assertEquals("", metadata.getTitle()); // Empty string from annotation
        assertEquals("EMPTY-TITLE", metadata.getTestId());
        assertEquals("TestMethods", metadata.getSuiteTitle());
        assertEquals("TestMethods.java", metadata.getFile());
    }

    @Test
    @DisplayName("Should handle empty testId annotation")
    void shouldHandleEmptyTestIdAnnotation() throws NoSuchMethodException {
        // Given
        Method testMethod = TestMethods.class.getMethod("methodWithEmptyTestId");
        
        when(mockContext.getTestMethod()).thenReturn(Optional.of(testMethod));
        when(mockContext.getTestClass()).thenReturn(Optional.of(TestMethods.class));

        // When
        TestMetadata metadata = extractor.extractTestMetadata(mockContext);

        // Then
        assertNotNull(metadata);
        assertEquals("Test With Empty ID", metadata.getTitle());
        assertEquals("", metadata.getTestId()); // Empty string from annotation
        assertEquals("TestMethods", metadata.getSuiteTitle());
        assertEquals("TestMethods.java", metadata.getFile());
    }

    @Test
    @DisplayName("Should extract metadata from class with complex name")
    void shouldExtractMetadataFromClassWithComplexName() throws NoSuchMethodException {
        // Given
        Method testMethod = ComplexNamedTestClass.class.getMethod("testMethod");
        when(mockContext.getTestMethod()).thenReturn(Optional.of(testMethod));
        when(mockContext.getTestClass()).thenReturn(Optional.of(ComplexNamedTestClass.class));
        when(mockContext.getDisplayName()).thenReturn("Complex Test");

        // When
        TestMetadata metadata = extractor.extractTestMetadata(mockContext);

        // Then
        assertNotNull(metadata);
        assertEquals("Complex Test", metadata.getTitle());
        assertNull(metadata.getTestId());
        assertEquals("ComplexNamedTestClass", metadata.getSuiteTitle());
        assertEquals("ComplexNamedTestClass.java", metadata.getFile());
    }

    // Test helper classes
    private static class TestMethods {

        @Title("Custom Test Title")
        @TestId("TEST-123")
        public void methodWithBothAnnotations() {}

        @Title("Title Only Test")
        public void methodWithOnlyTitle() {}

        @TestId("TEST-456")
        public void methodWithOnlyTestId() {}

        public void methodWithoutAnnotations() {}

        @Title("")
        @TestId("EMPTY-TITLE")
        public void methodWithEmptyTitle() {}

        @Title("Test With Empty ID")
        @TestId("")
        public void methodWithEmptyTestId() {}
    }

    private static class NestedTestClass {
        @TestId("NESTED-001")
        public void nestedTestMethod() {}
    }

    private static class ComplexNamedTestClass {
        public void testMethod() {}
    }
}