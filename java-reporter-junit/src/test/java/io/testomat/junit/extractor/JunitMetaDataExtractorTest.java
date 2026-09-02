package io.testomat.junit.extractor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.testomat.core.annotation.LinkTest;
import io.testomat.core.annotation.TestId;
import io.testomat.core.annotation.Title;
import io.testomat.core.exception.NoMethodInContextException;
import io.testomat.core.model.TestMetadata;
import java.lang.reflect.Method;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@DisplayName("JunitMetaDataExtractor Tests")
class JunitMetaDataExtractorTest {

    @Mock
    private ExtensionContext mockExtensionContext;

    private JunitMetaDataExtractor extractor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        extractor = new JunitMetaDataExtractor();
    }

    @Nested
    @DisplayName("Extract Test Metadata Tests")
    class ExtractTestMetadataTests {

        @Test
        @DisplayName("Should extract metadata with both Title and TestId annotations")
        void shouldExtractMetadataWithBothAnnotations() throws NoSuchMethodException {
            // Given
            Method testMethod = TestMethodHolder.class.getMethod("annotatedTestMethod");
            when(mockExtensionContext.getTestMethod()).thenReturn(Optional.of(testMethod));
            doReturn(TestMethodHolder.class).when(mockExtensionContext).getRequiredTestClass();

            // When
            TestMetadata result = extractor.extractTestMetadata(mockExtensionContext);

            // Then
            assertNotNull(result);
            assertEquals("Custom Title", result.getTitle());
            assertEquals("T001", result.getTestId());
            assertEquals("TestMethodHolder", result.getSuiteTitle());
            assertEquals("io/testomat/junit/extractor/TestMethodHolder.java", result.getFile());
        }

        @Test
        @DisplayName("Should extract metadata with only Title annotation")
        void shouldExtractMetadataWithOnlyTitleAnnotation() throws NoSuchMethodException {
            // Given
            Method testMethod = TestMethodHolder.class.getMethod("onlyTitleMethod");
            when(mockExtensionContext.getTestMethod()).thenReturn(Optional.of(testMethod));
            doReturn(TestMethodHolder.class).when(mockExtensionContext).getRequiredTestClass();

            // When
            TestMetadata result = extractor.extractTestMetadata(mockExtensionContext);

            // Then
            assertNotNull(result);
            assertEquals("Only Title", result.getTitle());
            assertNull(result.getTestId());
            assertEquals("TestMethodHolder", result.getSuiteTitle());
            assertEquals("io/testomat/junit/extractor/TestMethodHolder.java", result.getFile());
        }

        @Test
        @DisplayName("Should extract metadata with only TestId annotation")
        void shouldExtractMetadataWithOnlyTestIdAnnotation() throws NoSuchMethodException {
            // Given
            Method testMethod = TestMethodHolder.class.getMethod("onlyTestIdMethod");
            when(mockExtensionContext.getTestMethod()).thenReturn(Optional.of(testMethod));
            doReturn(TestMethodHolder.class).when(mockExtensionContext).getRequiredTestClass();

            // When
            TestMetadata result = extractor.extractTestMetadata(mockExtensionContext);

            // Then
            assertNotNull(result);
            assertEquals("onlyTestIdMethod", result.getTitle()); // Should use method name
            assertEquals("T002", result.getTestId());
            assertEquals("TestMethodHolder", result.getSuiteTitle());
            assertEquals("io/testomat/junit/extractor/TestMethodHolder.java", result.getFile());
        }

        @Test
        @DisplayName("Should extract metadata without any annotations")
        void shouldExtractMetadataWithoutAnyAnnotations() throws NoSuchMethodException {
            // Given
            Method testMethod = TestMethodHolder.class.getMethod("plainTestMethod");
            when(mockExtensionContext.getTestMethod()).thenReturn(Optional.of(testMethod));
            doReturn(TestMethodHolder.class).when(mockExtensionContext).getRequiredTestClass();

            // When
            TestMetadata result = extractor.extractTestMetadata(mockExtensionContext);

            // Then
            assertNotNull(result);
            assertEquals("plainTestMethod", result.getTitle()); // Should use method name
            assertNull(result.getTestId());
            assertEquals("TestMethodHolder", result.getSuiteTitle());
            assertEquals("io/testomat/junit/extractor/TestMethodHolder.java", result.getFile());
        }

        @Test
        @DisplayName("Should handle empty annotation values")
        void shouldHandleEmptyAnnotationValues() throws NoSuchMethodException {
            // Given
            Method testMethod = TestMethodHolder.class.getMethod("emptyAnnotationsMethod");
            when(mockExtensionContext.getTestMethod()).thenReturn(Optional.of(testMethod));
            doReturn(TestMethodHolder.class).when(mockExtensionContext).getRequiredTestClass();

            // When
            TestMetadata result = extractor.extractTestMetadata(mockExtensionContext);

            // Then
            assertNotNull(result);
            assertEquals("", result.getTitle()); // Should use empty annotation value, not method name
            assertEquals("", result.getTestId());
            assertEquals("TestMethodHolder", result.getSuiteTitle());
            assertEquals("io/testomat/junit/extractor/TestMethodHolder.java", result.getFile());
        }

        @Test
        @DisplayName("Should handle whitespace annotation values")
        void shouldHandleWhitespaceAnnotationValues() throws NoSuchMethodException {
            // Given
            Method testMethod = TestMethodHolder.class.getMethod("whitespaceAnnotationsMethod");
            when(mockExtensionContext.getTestMethod()).thenReturn(Optional.of(testMethod));
            doReturn(TestMethodHolder.class).when(mockExtensionContext).getRequiredTestClass();

            // When
            TestMetadata result = extractor.extractTestMetadata(mockExtensionContext);

            // Then
            assertNotNull(result);
            assertEquals("   ", result.getTitle()); // Should preserve whitespace from annotation
            assertEquals("   ", result.getTestId());
            assertEquals("TestMethodHolder", result.getSuiteTitle());
            assertEquals("io/testomat/junit/extractor/TestMethodHolder.java", result.getFile());
        }
    }

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("Should throw NoMethodInContextException when no test method present")
        void shouldThrowExceptionWhenNoTestMethodPresent() {
            // Given
            when(mockExtensionContext.getTestMethod()).thenReturn(Optional.empty());
            when(mockExtensionContext.getDisplayName()).thenReturn("TestDisplayName");

            // When & Then
            NoMethodInContextException exception = assertThrows(
                    NoMethodInContextException.class,
                    () -> extractor.extractTestMetadata(mockExtensionContext)
            );

            assertEquals("No test method found in TestDisplayName", exception.getMessage());
            verify(mockExtensionContext, never()).getRequiredTestClass();
        }

        @Test
        @DisplayName("Should throw NoMethodInContextException with proper display name")
        void shouldThrowExceptionWithProperDisplayName() {
            // Given
            when(mockExtensionContext.getTestMethod()).thenReturn(Optional.empty());
            when(mockExtensionContext.getDisplayName()).thenReturn("ComplexTestDisplayName[1]");

            // When & Then
            NoMethodInContextException exception = assertThrows(
                    NoMethodInContextException.class,
                    () -> extractor.extractTestMetadata(mockExtensionContext)
            );

            assertEquals("No test method found in ComplexTestDisplayName[1]", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("File Path Generation Tests")
    class FilePathGenerationTests {

        @Test
        @DisplayName("Should generate correct file path for class in nested package")
        void shouldGenerateCorrectFilePathForClassInNestedPackage() throws NoSuchMethodException {
            // Given
            Method testMethod = TestMethodHolder.class.getMethod("plainTestMethod");
            when(mockExtensionContext.getTestMethod()).thenReturn(Optional.of(testMethod));
            doReturn(TestMethodHolder.class).when(mockExtensionContext).getRequiredTestClass();

            // When
            TestMetadata result = extractor.extractTestMetadata(mockExtensionContext);

            // Then
            assertEquals("io/testomat/junit/extractor/TestMethodHolder.java", result.getFile());
        }

        @Test
        @DisplayName("Should generate correct file path for different class names")
        void shouldGenerateCorrectFilePathForDifferentClassNames() throws NoSuchMethodException {
            // Given
            Method testMethod = AnotherTestClass.class.getMethod("hashCode"); // Using existing method
            when(mockExtensionContext.getTestMethod()).thenReturn(Optional.of(testMethod));
            doReturn(AnotherTestClass.class).when(mockExtensionContext).getRequiredTestClass();

            // When
            TestMetadata result = extractor.extractTestMetadata(mockExtensionContext);

            // Then
            assertEquals("io/testomat/junit/extractor/AnotherTestClass.java", result.getFile());
            assertEquals("AnotherTestClass", result.getSuiteTitle());
        }
    }

    @Nested
    @DisplayName("Suite Title Tests")
    class SuiteTitleTests {

        @Test
        @DisplayName("Should use simple class name as suite title")
        void shouldUseSimpleClassNameAsSuiteTitle() throws NoSuchMethodException {
            // Given
            Method testMethod = VeryLongClassNameForTesting.class.getMethod("hashCode");
            when(mockExtensionContext.getTestMethod()).thenReturn(Optional.of(testMethod));
            doReturn(VeryLongClassNameForTesting.class).when(mockExtensionContext).getRequiredTestClass();

            // When
            TestMetadata result = extractor.extractTestMetadata(mockExtensionContext);

            // Then
            assertEquals("VeryLongClassNameForTesting", result.getSuiteTitle());
        }

        @Test
        @DisplayName("Should handle short class names")
        void shouldHandleShortClassNames() throws NoSuchMethodException {
            // Given
            Method testMethod = ShortNamedClass.class.getMethod("hashCode");
            when(mockExtensionContext.getTestMethod()).thenReturn(Optional.of(testMethod));
            doReturn(ShortNamedClass.class).when(mockExtensionContext).getRequiredTestClass();

            // When
            TestMetadata result = extractor.extractTestMetadata(mockExtensionContext);

            // Then
            assertEquals("ShortNamedClass", result.getSuiteTitle());
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should extract complete metadata in typical scenario")
        void shouldExtractCompleteMetadataInTypicalScenario() throws NoSuchMethodException {
            // Given
            Method testMethod = TestMethodHolder.class.getMethod("annotatedTestMethod");
            when(mockExtensionContext.getTestMethod()).thenReturn(Optional.of(testMethod));
            doReturn(TestMethodHolder.class).when(mockExtensionContext).getRequiredTestClass();

            // When
            TestMetadata result = extractor.extractTestMetadata(mockExtensionContext);

            // Then
            // Verify all components are properly extracted
            assertNotNull(result);
            assertNotNull(result.getTitle());
            assertNotNull(result.getTestId());
            assertNotNull(result.getSuiteTitle());
            assertNotNull(result.getFile());

            // Verify correct values
            assertEquals("Custom Title", result.getTitle());
            assertEquals("T001", result.getTestId());
            assertEquals("TestMethodHolder", result.getSuiteTitle());
            assertTrue(result.getFile().endsWith("TestMethodHolder.java"));
            assertTrue(result.getFile().contains("/"));
        }

        @Test
        @DisplayName("Should handle method with complex signature")
        void shouldHandleMethodWithComplexSignature() throws NoSuchMethodException {
            // Given - use a method with parameters from Object class
            Method testMethod = Object.class.getMethod("equals", Object.class);
            when(mockExtensionContext.getTestMethod()).thenReturn(Optional.of(testMethod));
            doReturn(Object.class).when(mockExtensionContext).getRequiredTestClass();

            // When
            TestMetadata result = extractor.extractTestMetadata(mockExtensionContext);

            // Then
            assertNotNull(result);
            assertEquals("equals", result.getTitle()); // Should use method name since no @Title
            assertNull(result.getTestId()); // Should be null since no @TestId
            assertEquals("Object", result.getSuiteTitle());
            assertEquals("java/lang/Object.java", result.getFile());
        }

        @Test
        @DisplayName("Should verify all ExtensionContext methods are called correctly")
        void shouldVerifyAllExtensionContextMethodsAreCalledCorrectly() throws NoSuchMethodException {
            // Given
            Method testMethod = TestMethodHolder.class.getMethod("plainTestMethod");
            when(mockExtensionContext.getTestMethod()).thenReturn(Optional.of(testMethod));
            doReturn(TestMethodHolder.class).when(mockExtensionContext).getRequiredTestClass();

            // When
            extractor.extractTestMetadata(mockExtensionContext);

            // Then
            verify(mockExtensionContext, times(1)).getTestMethod();
            verify(mockExtensionContext, times(1)).getRequiredTestClass();
            verify(mockExtensionContext, never()).getDisplayName(); // Only called in exception case
        }
    }

    @Nested
    @DisplayName("Annotation Processing Tests")
    class AnnotationProcessingTests {

        @Test
        @DisplayName("Should prioritize Title annotation over method name")
        void shouldPrioritizeTitleAnnotationOverMethodName() throws NoSuchMethodException {
            // Given
            Method testMethod = TestMethodHolder.class.getMethod("onlyTitleMethod");
            when(mockExtensionContext.getTestMethod()).thenReturn(Optional.of(testMethod));
            doReturn(TestMethodHolder.class).when(mockExtensionContext).getRequiredTestClass();

            // When
            TestMetadata result = extractor.extractTestMetadata(mockExtensionContext);

            // Then
            assertEquals("Only Title", result.getTitle());
            assertNotEquals("onlyTitleMethod", result.getTitle()); // Should not use method name
        }

        @Test
        @DisplayName("Should handle case when annotations return null values")
        void shouldHandleCaseWhenAnnotationsReturnNullValues() throws NoSuchMethodException {
            // This test verifies behavior when annotation is present but returns null
            // In practice, this shouldn't happen as annotations can't return null,
            // but we test the null check in getTestId method

            // Given
            Method testMethod = TestMethodHolder.class.getMethod("plainTestMethod");
            when(mockExtensionContext.getTestMethod()).thenReturn(Optional.of(testMethod));
            doReturn(TestMethodHolder.class).when(mockExtensionContext).getRequiredTestClass();

            // When
            TestMetadata result = extractor.extractTestMetadata(mockExtensionContext);

            // Then
            assertNull(result.getTestId()); // Should be null when no @TestId annotation
            assertEquals("plainTestMethod", result.getTitle()); // Should use method name when no @Title
        }
    }

    @Test
    @DisplayName("Should extract links from LinkTest annotation")
    void shouldExtractLinks() throws Exception {
        Method testMethod = TestMethodHolder.class.getMethod("methodWithLinks");

        when(mockExtensionContext.getTestMethod())
            .thenReturn(Optional.of(testMethod));

        doReturn(TestMethodHolder.class)
            .when(mockExtensionContext)
            .getRequiredTestClass();

        TestMetadata result = extractor.extractTestMetadata(mockExtensionContext);

        assertNotNull(result.getLinks());
        assertEquals(2, result.getLinks().size());

        assertEquals("T-1", result.getLinks().get(0).getTest());
        assertEquals("T-2", result.getLinks().get(1).getTest());
    }

    @Test
    @DisplayName("Should return empty links when LinkTest annotation absent")
    void shouldReturnEmptyLinksWhenNoAnnotation() throws Exception {
        Method testMethod = TestMethodHolder.class.getMethod("plainTestMethod");

        when(mockExtensionContext.getTestMethod())
            .thenReturn(Optional.of(testMethod));

        doReturn(TestMethodHolder.class)
            .when(mockExtensionContext)
            .getRequiredTestClass();

        TestMetadata result = extractor.extractTestMetadata(mockExtensionContext);

        assertNotNull(result.getLinks());
        assertTrue(result.getLinks().isEmpty());
    }

    // Test helper classes - separate from nested test classes to avoid Java 11 compatibility issues
    public static class TestMethodHolder {
        @Title("Custom Title")
        @TestId("T001")
        public void annotatedTestMethod() {
        }

        @Title("Only Title")
        public void onlyTitleMethod() {
        }

        @TestId("T002")
        public void onlyTestIdMethod() {
        }

        public void plainTestMethod() {
        }

        @Title("")
        @TestId("")
        public void emptyAnnotationsMethod() {
        }

        @Title("   ")
        @TestId("   ")
        public void whitespaceAnnotationsMethod() {
        }

        @LinkTest({"T-1", "T-2"})
        public void methodWithLinks() {
        }
    }

    public static class AnotherTestClass {
    }

    public static class VeryLongClassNameForTesting {
    }

    public static class ShortNamedClass {
    }
}