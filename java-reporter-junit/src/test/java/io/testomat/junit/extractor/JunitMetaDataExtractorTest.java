package io.testomat.junit.extractor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

/**
 * Comprehensive unit tests for JunitMetaDataExtractor class.
 * Tests the public extractTestMetadata method with detailed failure diagnostics.
 */
@DisplayName("JunitMetaDataExtractor Tests")
class JunitMetaDataExtractorTest {

    private JunitMetaDataExtractor extractor;
    private ExtensionContext mockContext;

    @BeforeEach
    void setUp() {
        extractor = new JunitMetaDataExtractor();
        mockContext = mock(ExtensionContext.class);
    }

    @Nested
    @DisplayName("Successful Extraction Scenarios")
    class SuccessfulExtractionTests {

        @Test
        @DisplayName("Should extract metadata with both Title and TestId annotations")
        void shouldExtractMetadataWithBothAnnotations() throws NoSuchMethodException {
            // Given
            Method testMethod = TestMethodsForTesting.class.getMethod("methodWithBothAnnotations");
            Class<?> testClass = TestMethodsForTesting.class;
            String displayName = "Display Name from JUnit";

            when(mockContext.getTestMethod()).thenReturn(Optional.of(testMethod));
            when(mockContext.getTestClass()).thenReturn(Optional.of(testClass));
            when(mockContext.getDisplayName()).thenReturn(displayName);

            // When
            TestMetadata metadata = extractor.extractTestMetadata(mockContext);

            // Then
            assertNotNull(metadata,
                    "TestMetadata should not be null when extracting from valid context");

            assertAll("Verify all metadata fields for method with both annotations",
                    () -> assertEquals("Custom Test Title", metadata.getTitle(),
                            String.format("Expected title from @Title annotation to be 'Custom Test Title', but was '%s'",
                                    metadata.getTitle())),
                    () -> assertEquals("TEST-001", metadata.getTestId(),
                            String.format("Expected testId from @TestId annotation to be 'TEST-001', but was '%s'",
                                    metadata.getTestId())),
                    () -> assertEquals("TestMethodsForTesting", metadata.getSuiteTitle(),
                            String.format("Expected suiteTitle to be 'TestMethodsForTesting' (class simple name), but was '%s'",
                                    metadata.getSuiteTitle())),
                    () -> assertEquals("TestMethodsForTesting.java", metadata.getFile(),
                            String.format("Expected file to be 'TestMethodsForTesting.java' (class name + .java), but was '%s'",
                                    metadata.getFile()))
            );
        }

        @Test
        @DisplayName("Should extract metadata with only Title annotation")
        void shouldExtractMetadataWithOnlyTitleAnnotation() throws NoSuchMethodException {
            // Given
            Method testMethod = TestMethodsForTesting.class.getMethod("methodWithOnlyTitle");
            Class<?> testClass = TestMethodsForTesting.class;
            String displayName = "Display Name from JUnit";

            when(mockContext.getTestMethod()).thenReturn(Optional.of(testMethod));
            when(mockContext.getTestClass()).thenReturn(Optional.of(testClass));
            when(mockContext.getDisplayName()).thenReturn(displayName);

            System.out.println("Testing method with only @Title annotation: " + testMethod.getName());

            // When
            TestMetadata metadata = extractor.extractTestMetadata(mockContext);

            // Then
            assertNotNull(metadata,
                    "TestMetadata should not be null when extracting from method with @Title annotation");

            assertAll("Verify metadata fields for method with only @Title annotation",
                    () -> assertEquals("Only Title Annotation", metadata.getTitle(),
                            String.format("Expected title from @Title annotation to be 'Only Title Annotation', but was '%s'. " +
                                            "Method annotations: %s", metadata.getTitle(),
                                    java.util.Arrays.toString(testMethod.getAnnotations()))),
                    () -> assertNull(metadata.getTestId(),
                            String.format("Expected testId to be null when @TestId annotation is missing, but was '%s'",
                                    metadata.getTestId())),
                    () -> assertEquals("TestMethodsForTesting", metadata.getSuiteTitle(),
                            String.format("Expected suiteTitle to be 'TestMethodsForTesting', but was '%s'",
                                    metadata.getSuiteTitle())),
                    () -> assertEquals("TestMethodsForTesting.java", metadata.getFile(),
                            String.format("Expected file to be 'TestMethodsForTesting.java', but was '%s'",
                                    metadata.getFile()))
            );
        }

        @Test
        @DisplayName("Should extract metadata with only TestId annotation")
        void shouldExtractMetadataWithOnlyTestIdAnnotation() throws NoSuchMethodException {
            // Given
            Method testMethod = TestMethodsForTesting.class.getMethod("methodWithOnlyTestId");
            Class<?> testClass = TestMethodsForTesting.class;
            String displayName = "JUnit Display Name";

            when(mockContext.getTestMethod()).thenReturn(Optional.of(testMethod));
            when(mockContext.getTestClass()).thenReturn(Optional.of(testClass));
            when(mockContext.getDisplayName()).thenReturn(displayName);

            System.out.println("Testing method with only @TestId annotation: " + testMethod.getName());
            System.out.println("Expected to fall back to display name: " + displayName);

            // When
            TestMetadata metadata = extractor.extractTestMetadata(mockContext);

            // Then
            assertNotNull(metadata,
                    "TestMetadata should not be null when extracting from method with @TestId annotation");

            assertAll("Verify metadata fields for method with only @TestId annotation",
                    () -> assertEquals(displayName, metadata.getTitle(),
                            String.format("Expected title to fall back to display name '%s' when @Title is missing, but was '%s'. " +
                                            "Method has @Title annotation: %s", displayName, metadata.getTitle(),
                                    testMethod.isAnnotationPresent(Title.class))),
                    () -> assertEquals("TEST-002", metadata.getTestId(),
                            String.format("Expected testId from @TestId annotation to be 'TEST-002', but was '%s'. " +
                                            "Method annotations: %s", metadata.getTestId(),
                                    java.util.Arrays.toString(testMethod.getAnnotations()))),
                    () -> assertEquals("TestMethodsForTesting", metadata.getSuiteTitle(),
                            String.format("Expected suiteTitle to be 'TestMethodsForTesting', but was '%s'",
                                    metadata.getSuiteTitle())),
                    () -> assertEquals("TestMethodsForTesting.java", metadata.getFile(),
                            String.format("Expected file to be 'TestMethodsForTesting.java', but was '%s'",
                                    metadata.getFile()))
            );
        }

        @Test
        @DisplayName("Should extract metadata without any annotations")
        void shouldExtractMetadataWithoutAnnotations() throws NoSuchMethodException {
            // Given
            Method testMethod = TestMethodsForTesting.class.getMethod("methodWithoutAnnotations");
            Class<?> testClass = TestMethodsForTesting.class;
            String displayName = "Plain JUnit Test Method";

            when(mockContext.getTestMethod()).thenReturn(Optional.of(testMethod));
            when(mockContext.getTestClass()).thenReturn(Optional.of(testClass));
            when(mockContext.getDisplayName()).thenReturn(displayName);

            System.out.println("Testing method without any custom annotations: " + testMethod.getName());
            System.out.println("Method annotations count: " + testMethod.getAnnotations().length);
            System.out.println("Expected to use display name for title: " + displayName);

            // When
            TestMetadata metadata = extractor.extractTestMetadata(mockContext);

            // Then
            assertNotNull(metadata,
                    "TestMetadata should not be null when extracting from method without custom annotations");

            assertAll("Verify metadata fields for method without any custom annotations",
                    () -> assertEquals(displayName, metadata.getTitle(),
                            String.format("Expected title to use display name '%s' when no @Title annotation present, but was '%s'. " +
                                            "Method has %d annotations: %s", displayName, metadata.getTitle(),
                                    testMethod.getAnnotations().length, java.util.Arrays.toString(testMethod.getAnnotations()))),
                    () -> assertNull(metadata.getTestId(),
                            String.format("Expected testId to be null when no @TestId annotation present, but was '%s'. " +
                                    "Method has @TestId: %s", metadata.getTestId(), testMethod.isAnnotationPresent(TestId.class))),
                    () -> assertEquals("TestMethodsForTesting", metadata.getSuiteTitle(),
                            String.format("Expected suiteTitle to be 'TestMethodsForTesting', but was '%s'",
                                    metadata.getSuiteTitle())),
                    () -> assertEquals("TestMethodsForTesting.java", metadata.getFile(),
                            String.format("Expected file to be 'TestMethodsForTesting.java', but was '%s'",
                                    metadata.getFile()))
            );
        }

        @Test
        @DisplayName("Should handle test class with simple name correctly")
        void shouldHandleTestClassWithSimpleNameCorrectly() throws NoSuchMethodException {
            // Given
            Method testMethod = TestMethodsForTesting.class.getMethod("methodWithoutAnnotations");
            Class<?> testClass = TestMethodsForTesting.class;
            String displayName = "Test Display Name";

            when(mockContext.getTestMethod()).thenReturn(Optional.of(testMethod));
            when(mockContext.getTestClass()).thenReturn(Optional.of(testClass));
            when(mockContext.getDisplayName()).thenReturn(displayName);

            System.out.println("Testing class name extraction:");
            System.out.println("Full class name: " + testClass.getName());
            System.out.println("Simple class name: " + testClass.getSimpleName());

            // When
            TestMetadata metadata = extractor.extractTestMetadata(mockContext);

            // Then
            assertNotNull(metadata, "TestMetadata should not be null");

            assertAll("Verify class name handling",
                    () -> assertEquals("TestMethodsForTesting", metadata.getSuiteTitle(),
                            String.format("Expected suiteTitle to use class simple name 'TestMethodsForTesting', but was '%s'. " +
                                            "Full class name: '%s', Simple name: '%s'", metadata.getSuiteTitle(),
                                    testClass.getName(), testClass.getSimpleName())),
                    () -> assertEquals("TestMethodsForTesting.java", metadata.getFile(),
                            String.format("Expected file to be 'TestMethodsForTesting.java' (simple name + .java), but was '%s'",
                                    metadata.getFile()))
            );
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Scenarios")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should throw NoMethodInContextException when test method is missing")
        void shouldThrowExceptionWhenTestMethodIsMissing() {
            // Given
            String displayName = "Test without method";
            when(mockContext.getTestMethod()).thenReturn(Optional.empty());
            when(mockContext.getDisplayName()).thenReturn(displayName);

            System.out.println("Testing scenario with missing test method in context");
            System.out.println("Context display name: " + displayName);

            // When & Then
            NoMethodInContextException exception = assertThrows(
                    NoMethodInContextException.class,
                    () -> extractor.extractTestMetadata(mockContext),
                    "Should throw NoMethodInContextException when ExtensionContext.getTestMethod() returns empty Optional"
            );

            assertEquals("No test method found in " + displayName, exception.getMessage(),
                    String.format("Exception message should include display name. Expected: 'No test method found in %s', " +
                            "but was: '%s'", displayName, exception.getMessage()));
        }

        @Test
        @DisplayName("Should handle missing test class gracefully")
        void shouldHandleMissingTestClassGracefully() throws NoSuchMethodException {
            // Given
            Method testMethod = TestMethodsForTesting.class.getMethod("methodWithoutAnnotations");
            String displayName = "Test Display Name";

            when(mockContext.getTestMethod()).thenReturn(Optional.of(testMethod));
            when(mockContext.getTestClass()).thenReturn(Optional.empty());
            when(mockContext.getDisplayName()).thenReturn(displayName);

            System.out.println("Testing scenario with missing test class in context");
            System.out.println("Method: " + testMethod.getName());
            System.out.println("Expected fallback to 'Unknown' for suiteTitle");

            // When
            TestMetadata metadata = extractor.extractTestMetadata(mockContext);

            // Then
            assertNotNull(metadata, "TestMetadata should not be null even when test class is missing");

            assertAll("Verify fallback behavior when test class is missing",
                    () -> assertEquals(displayName, metadata.getTitle(),
                            String.format("Expected title to be '%s', but was '%s'", displayName, metadata.getTitle())),
                    () -> assertEquals("Unknown", metadata.getSuiteTitle(),
                            String.format("Expected suiteTitle to fallback to 'Unknown' when test class is missing, but was '%s'",
                                    metadata.getSuiteTitle())),
                    () -> assertEquals("Unknown.java", metadata.getFile(),
                            String.format("Expected file to be 'Unknown.java' when test class is missing, but was '%s'",
                                    metadata.getFile()))
            );
        }

        @Test
        @DisplayName("Should handle null ExtensionContext")
        void shouldHandleNullExtensionContext() {
            System.out.println("Testing null ExtensionContext scenario");

            // When & Then
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> extractor.extractTestMetadata(null),
                    "Should throw NullPointerException when ExtensionContext is null"
            );

            System.out.println("Exception thrown as expected: " + exception.getClass().getSimpleName());
        }

        @Test
        @DisplayName("Should handle null display name")
        void shouldHandleNullDisplayName() throws NoSuchMethodException {
            // Given
            Method testMethod = TestMethodsForTesting.class.getMethod("methodWithoutAnnotations");
            Class<?> testClass = TestMethodsForTesting.class;

            when(mockContext.getTestMethod()).thenReturn(Optional.of(testMethod));
            when(mockContext.getTestClass()).thenReturn(Optional.of(testClass));
            when(mockContext.getDisplayName()).thenReturn(null);

            System.out.println("Testing null display name scenario");
            System.out.println("Method: " + testMethod.getName());
            System.out.println("Expected title to be null when no @Title annotation and display name is null");

            // When
            TestMetadata metadata = extractor.extractTestMetadata(mockContext);

            // Then
            assertNotNull(metadata, "TestMetadata should not be null even when display name is null");

            assertAll("Verify behavior with null display name",
                    () -> assertNull(metadata.getTitle(),
                            String.format("Expected title to be null when no @Title annotation and display name is null, " +
                                            "but was '%s'. Method has @Title: %s", metadata.getTitle(),
                                    testMethod.isAnnotationPresent(Title.class))),
                    () -> assertEquals("TestMethodsForTesting", metadata.getSuiteTitle(),
                            String.format("Expected suiteTitle to be 'TestMethodsForTesting' even with null display name, " +
                                    "but was '%s'", metadata.getSuiteTitle()))
            );
        }

        @Test
        @DisplayName("Should handle empty annotation values")
        void shouldHandleEmptyAnnotationValues() throws NoSuchMethodException {
            // Given
            Method testMethod = TestMethodsForTesting.class.getMethod("methodWithEmptyAnnotations");
            Class<?> testClass = TestMethodsForTesting.class;
            String displayName = "Test with empty annotations";

            when(mockContext.getTestMethod()).thenReturn(Optional.of(testMethod));
            when(mockContext.getTestClass()).thenReturn(Optional.of(testClass));
            when(mockContext.getDisplayName()).thenReturn(displayName);

            System.out.println("Testing empty annotation values scenario");
            System.out.println("Method: " + testMethod.getName());

            // Verify the method actually has empty annotations
            Title titleAnnotation = testMethod.getAnnotation(Title.class);
            TestId testIdAnnotation = testMethod.getAnnotation(TestId.class);
            System.out.println("@Title value: '" + (titleAnnotation != null ? titleAnnotation.value() : "null") + "'");
            System.out.println("@TestId value: '" + (testIdAnnotation != null ? testIdAnnotation.value() : "null") + "'");

            // When
            TestMetadata metadata = extractor.extractTestMetadata(mockContext);

            // Then
            assertNotNull(metadata, "TestMetadata should not be null with empty annotation values");

            assertAll("Verify handling of empty annotation values",
                    () -> assertEquals("", metadata.getTitle(),
                            String.format("Expected title to be empty string from @Title(\"\") annotation, but was '%s'. " +
                                            "Annotation value: '%s'", metadata.getTitle(),
                                    titleAnnotation != null ? titleAnnotation.value() : "null")),
                    () -> assertEquals("", metadata.getTestId(),
                            String.format("Expected testId to be empty string from @TestId(\"\") annotation, but was '%s'. " +
                                            "Annotation value: '%s'", metadata.getTestId(),
                                    testIdAnnotation != null ? testIdAnnotation.value() : "null")),
                    () -> assertEquals("TestMethodsForTesting", metadata.getSuiteTitle(),
                            String.format("Expected suiteTitle to be 'TestMethodsForTesting', but was '%s'",
                                    metadata.getSuiteTitle()))
            );
        }
    }

    @Nested
    @DisplayName("Special Characters and Unicode Tests")
    class SpecialCharactersTests {

        @Test
        @DisplayName("Should handle special characters in annotations")
        void shouldHandleSpecialCharactersInAnnotations() throws NoSuchMethodException {
            // Given
            Method testMethod = TestMethodsForTesting.class.getMethod("methodWithSpecialCharacters");
            Class<?> testClass = TestMethodsForTesting.class;
            String displayName = "Test with special chars";

            when(mockContext.getTestMethod()).thenReturn(Optional.of(testMethod));
            when(mockContext.getTestClass()).thenReturn(Optional.of(testClass));
            when(mockContext.getDisplayName()).thenReturn(displayName);

            // Verify actual annotation values
            Title titleAnnotation = testMethod.getAnnotation(Title.class);
            TestId testIdAnnotation = testMethod.getAnnotation(TestId.class);
            System.out.println("Testing special characters in annotations:");
            System.out.println("@Title value: '" + titleAnnotation.value() + "'");
            System.out.println("@TestId value: '" + testIdAnnotation.value() + "'");

            // When
            TestMetadata metadata = extractor.extractTestMetadata(mockContext);

            // Then
            String expectedTitle = "Test with special chars: áéíóú ñ üöä 中文 🚀";
            String expectedTestId = "TEST-SPECIAL-001";

            assertNotNull(metadata, "TestMetadata should not be null with special characters");

            assertAll("Verify special characters are preserved in metadata",
                    () -> assertEquals(expectedTitle, metadata.getTitle(),
                            String.format("Expected title to preserve special characters '%s', but was '%s'. " +
                                            "Actual annotation value: '%s'", expectedTitle, metadata.getTitle(),
                                    titleAnnotation.value())),
                    () -> assertEquals(expectedTestId, metadata.getTestId(),
                            String.format("Expected testId to be '%s', but was '%s'. " +
                                            "Actual annotation value: '%s'", expectedTestId, metadata.getTestId(),
                                    testIdAnnotation.value()))
            );
        }

        @Test
        @DisplayName("Should handle unicode characters in display name")
        void shouldHandleUnicodeCharactersInDisplayName() throws NoSuchMethodException {
            // Given
            Method testMethod = TestMethodsForTesting.class.getMethod("methodWithoutAnnotations");
            Class<?> testClass = TestMethodsForTesting.class;
            String displayName = "Тест з українськими символами 🇺🇦";

            when(mockContext.getTestMethod()).thenReturn(Optional.of(testMethod));
            when(mockContext.getTestClass()).thenReturn(Optional.of(testClass));
            when(mockContext.getDisplayName()).thenReturn(displayName);

            System.out.println("Testing unicode characters in display name:");
            System.out.println("Display name: '" + displayName + "'");
            System.out.println("Display name length: " + displayName.length());

            // When
            TestMetadata metadata = extractor.extractTestMetadata(mockContext);

            // Then
            assertNotNull(metadata, "TestMetadata should not be null with unicode display name");

            assertEquals(displayName, metadata.getTitle(),
                    String.format("Expected title to preserve unicode characters '%s', but was '%s'. " +
                                    "Expected length: %d, Actual length: %d", displayName, metadata.getTitle(),
                            displayName.length(), metadata.getTitle() != null ? metadata.getTitle().length() : 0));
        }

        @Test
        @DisplayName("Should handle newlines and tabs in annotations")
        void shouldHandleNewlinesAndTabsInAnnotations() throws NoSuchMethodException {
            // Given
            Method testMethod = TestMethodsForTesting.class.getMethod("methodWithNewlinesAndTabs");
            Class<?> testClass = TestMethodsForTesting.class;
            String displayName = "Test with formatting";

            when(mockContext.getTestMethod()).thenReturn(Optional.of(testMethod));
            when(mockContext.getTestClass()).thenReturn(Optional.of(testClass));
            when(mockContext.getDisplayName()).thenReturn(displayName);

            // Verify actual annotation values
            Title titleAnnotation = testMethod.getAnnotation(Title.class);
            TestId testIdAnnotation = testMethod.getAnnotation(TestId.class);
            System.out.println("Testing newlines and tabs in annotations:");
            System.out.println("@Title value (escaped): '" + titleAnnotation.value().replace("\n", "\\n").replace("\t", "\\t") + "'");
            System.out.println("@TestId value (escaped): '" + testIdAnnotation.value().replace("\n", "\\n").replace("\t", "\\t") + "'");

            // When
            TestMetadata metadata = extractor.extractTestMetadata(mockContext);

            // Then
            String expectedTitle = "Title with\nnewline and\ttab";
            String expectedTestId = "TEST\n\tFORMAT";

            assertNotNull(metadata, "TestMetadata should not be null with formatting characters");

            assertAll("Verify newlines and tabs are preserved in metadata",
                    () -> assertEquals(expectedTitle, metadata.getTitle(),
                            String.format("Expected title to preserve formatting '%s', but was '%s'. " +
                                            "Expected (escaped): '%s', Actual (escaped): '%s'", expectedTitle, metadata.getTitle(),
                                    expectedTitle.replace("\n", "\\n").replace("\t", "\\t"),
                                    metadata.getTitle() != null ? metadata.getTitle().replace("\n", "\\n").replace("\t", "\\t") : "null")),
                    () -> assertEquals(expectedTestId, metadata.getTestId(),
                            String.format("Expected testId to preserve formatting '%s', but was '%s'. " +
                                            "Expected (escaped): '%s', Actual (escaped): '%s'", expectedTestId, metadata.getTestId(),
                                    expectedTestId.replace("\n", "\\n").replace("\t", "\\t"),
                                    metadata.getTestId() != null ? metadata.getTestId().replace("\n", "\\n").replace("\t", "\\t") : "null"))
            );
        }
    }

    @Nested
    @DisplayName("Nested Class Scenarios")
    class NestedClassTests {

        @Test
        @DisplayName("Should handle nested test class correctly")
        void shouldHandleNestedTestClassCorrectly() throws NoSuchMethodException {
            // Given
            Method testMethod = NestedTestClass.class.getMethod("nestedTestMethod");
            Class<?> testClass = NestedTestClass.class;
            String displayName = "Nested test method";

            when(mockContext.getTestMethod()).thenReturn(Optional.of(testMethod));
            when(mockContext.getTestClass()).thenReturn(Optional.of(testClass));
            when(mockContext.getDisplayName()).thenReturn(displayName);

            System.out.println("Testing nested class scenario:");
            System.out.println("Nested class full name: " + testClass.getName());
            System.out.println("Nested class simple name: " + testClass.getSimpleName());
            System.out.println("Method: " + testMethod.getName());

            // When
            TestMetadata metadata = extractor.extractTestMetadata(mockContext);

            // Then
            assertNotNull(metadata, "TestMetadata should not be null for nested class");

            assertAll("Verify nested class metadata extraction",
                    () -> assertEquals("Nested Test Title", metadata.getTitle(),
                            String.format("Expected title from @Title annotation to be 'Nested Test Title', but was '%s'",
                                    metadata.getTitle())),
                    () -> assertEquals("NESTED-001", metadata.getTestId(),
                            String.format("Expected testId from @TestId annotation to be 'NESTED-001', but was '%s'",
                                    metadata.getTestId())),
                    () -> assertEquals("NestedTestClass", metadata.getSuiteTitle(),
                            String.format("Expected suiteTitle to be 'NestedTestClass' (nested class simple name), but was '%s'. " +
                                    "Full class name: '%s'", metadata.getSuiteTitle(), testClass.getName())),
                    () -> assertEquals("NestedTestClass.java", metadata.getFile(),
                            String.format("Expected file to be 'NestedTestClass.java', but was '%s'",
                                    metadata.getFile()))
            );
        }

        @Nested
        @DisplayName("Inner Nested Class")
        class NestedTestClass {
            @Title("Nested Test Title")
            @TestId("NESTED-001")
            public void nestedTestMethod() {
                // Test method for nested class testing
            }
        }
    }

    @Nested
    @DisplayName("Integration and Performance Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should extract metadata from real test method efficiently")
        void shouldExtractMetadataFromRealTestMethodEfficiently() throws NoSuchMethodException {
            // Given - Use a stable test method from our test class instead of self-reference
            Method realTestMethod = TestMethodsForTesting.class.getMethod("methodWithBothAnnotations");
            Class<?> testClass = TestMethodsForTesting.class;
            String displayName = "Performance test for real method";

            when(mockContext.getTestMethod()).thenReturn(Optional.of(realTestMethod));
            when(mockContext.getTestClass()).thenReturn(Optional.of(testClass));
            when(mockContext.getDisplayName()).thenReturn(displayName);

            System.out.println("Testing performance with real method:");
            System.out.println("Method: " + realTestMethod.getName());
            System.out.println("Class: " + testClass.getSimpleName());
            System.out.println("Method declaring class: " + realTestMethod.getDeclaringClass().getSimpleName());

            // When - Measure performance
            long startTime = System.nanoTime(); // Use nanoTime for better precision
            TestMetadata metadata = extractor.extractTestMetadata(mockContext);
            long endTime = System.nanoTime();
            long durationNanos = endTime - startTime;
            long durationMillis = durationNanos / 1_000_000;

            System.out.println("Extraction completed in: " + durationMillis + "ms (" + durationNanos + "ns)");

            // Then
            assertNotNull(metadata, "TestMetadata should not be null for real test method");

            assertAll("Verify real method metadata extraction and performance",
                    () -> assertEquals("Custom Test Title", metadata.getTitle(),
                            String.format("Expected title to be 'Custom Test Title' from @Title annotation, but was '%s'. " +
                                    "Display name was: '%s'", metadata.getTitle(), displayName)),
                    () -> assertEquals("TEST-001", metadata.getTestId(),
                            String.format("Expected testId to be 'TEST-001' from @TestId annotation, but was '%s'",
                                    metadata.getTestId())),
                    () -> assertEquals("TestMethodsForTesting", metadata.getSuiteTitle(),
                            String.format("Expected suiteTitle to be 'TestMethodsForTesting', but was '%s'. " +
                                    "Test class simple name: '%s'", metadata.getSuiteTitle(), testClass.getSimpleName())),
                    () -> assertEquals("TestMethodsForTesting.java", metadata.getFile(),
                            String.format("Expected file to be 'TestMethodsForTesting.java', but was '%s'",
                                    metadata.getFile())),
                    () -> assertTrue(durationMillis < 100,
                            String.format("Metadata extraction should be fast (< 100ms), but took %d ms (%d ns)",
                                    durationMillis, durationNanos))
            );
        }

        @Test
        @DisplayName("Should handle multiple consecutive extractions")
        void shouldHandleMultipleConsecutiveExtractions() throws NoSuchMethodException {
            // Given
            Method testMethod = TestMethodsForTesting.class.getMethod("methodWithBothAnnotations");
            Class<?> testClass = TestMethodsForTesting.class;
            String displayName = "Multiple extractions test";

            when(mockContext.getTestMethod()).thenReturn(Optional.of(testMethod));
            when(mockContext.getTestClass()).thenReturn(Optional.of(testClass));
            when(mockContext.getDisplayName()).thenReturn(displayName);

            System.out.println("Testing multiple consecutive extractions for consistency");

            // When - Extract metadata multiple times
            TestMetadata metadata1 = extractor.extractTestMetadata(mockContext);
            TestMetadata metadata2 = extractor.extractTestMetadata(mockContext);
            TestMetadata metadata3 = extractor.extractTestMetadata(mockContext);

            // Then - All extractions should be consistent
            assertAll("Verify consistency across multiple extractions",
                    () -> assertEquals(metadata1.getTitle(), metadata2.getTitle(),
                            String.format("Title should be consistent: extraction1='%s', extraction2='%s'",
                                    metadata1.getTitle(), metadata2.getTitle())),
                    () -> assertEquals(metadata1.getTestId(), metadata2.getTestId(),
                            String.format("TestId should be consistent: extraction1='%s', extraction2='%s'",
                                    metadata1.getTestId(), metadata2.getTestId())),
                    () -> assertEquals(metadata1.getSuiteTitle(), metadata2.getSuiteTitle(),
                            String.format("SuiteTitle should be consistent: extraction1='%s', extraction2='%s'",
                                    metadata1.getSuiteTitle(), metadata2.getSuiteTitle())),
                    () -> assertEquals(metadata1.getFile(), metadata2.getFile(),
                            String.format("File should be consistent: extraction1='%s', extraction2='%s'",
                                    metadata1.getFile(), metadata2.getFile())),
                    // Third extraction vs second
                    () -> assertEquals(metadata2.getTitle(), metadata3.getTitle(),
                            String.format("Title should be consistent: extraction2='%s', extraction3='%s'",
                                    metadata2.getTitle(), metadata3.getTitle())),
                    () -> assertEquals(metadata2.getTestId(), metadata3.getTestId(),
                            String.format("TestId should be consistent: extraction2='%s', extraction3='%s'",
                                    metadata2.getTestId(), metadata3.getTestId())),
                    () -> assertEquals(metadata2.getSuiteTitle(), metadata3.getSuiteTitle(),
                            String.format("SuiteTitle should be consistent: extraction2='%s', extraction3='%s'",
                                    metadata2.getSuiteTitle(), metadata3.getSuiteTitle())),
                    () -> assertEquals(metadata2.getFile(), metadata3.getFile(),
                            String.format("File should be consistent: extraction2='%s', extraction3='%s'",
                                    metadata2.getFile(), metadata3.getFile()))
            );
        }
    }

    /**
     * Test class containing various test methods with different annotation combinations
     */
    public static class TestMethodsForTesting {

        @Title("Custom Test Title")
        @TestId("TEST-001")
        public void methodWithBothAnnotations() {
            // Method with both annotations
        }

        @Title("Only Title Annotation")
        public void methodWithOnlyTitle() {
            // Method with only title annotation
        }

        @TestId("TEST-002")
        public void methodWithOnlyTestId() {
            // Method with only test ID annotation
        }

        public void methodWithoutAnnotations() {
            // Method without any annotations
        }

        @Title("")
        @TestId("")
        public void methodWithEmptyAnnotations() {
            // Method with empty annotation values
        }

        @Title("Test with special chars: áéíóú ñ üöä 中文 🚀")
        @TestId("TEST-SPECIAL-001")
        public void methodWithSpecialCharacters() {
            // Method with special characters in annotations
        }

        @Title("Title with\nnewline and\ttab")
        @TestId("TEST\n\tFORMAT")
        public void methodWithNewlinesAndTabs() {
            // Method with formatting characters in annotations
        }
    }
}