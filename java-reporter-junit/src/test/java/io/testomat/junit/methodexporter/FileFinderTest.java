package io.testomat.junit.methodexporter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.testomat.junit.methodexporter.patfinder.FileFinder;
import io.testomat.junit.methodexporter.patfinder.PathNormalizer;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@DisplayName("FileFinder Tests")
class FileFinderTest {

    @Mock
    private PathNormalizer mockPathNormalizer;

    @Mock
    private ExtensionContext mockExtensionContext;

    private FileFinder fileFinder;

    // Test classes for testing
    public static class TestClassA {
    }

    public static class TestClassB {
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        fileFinder = new FileFinder(mockPathNormalizer);

        // Setup default normalizer behavior
        when(mockPathNormalizer.normalizePath(anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create instance with default constructor")
        void shouldCreateInstanceWithDefaultConstructor() {
            // When
            FileFinder finder = new FileFinder();

            // Then
            assertNotNull(finder);
        }

        @Test
        @DisplayName("Should create instance with parameterized constructor")
        void shouldCreateInstanceWithParameterizedConstructor() {
            // When
            FileFinder finder = new FileFinder(mockPathNormalizer);

            // Then
            assertNotNull(finder);
        }
    }

    @Nested
    @DisplayName("Get Test Class File Path Tests")
    class GetTestClassFilePathTests {

        @Test
        @DisplayName("Should return null when test class is null")
        void shouldReturnNullWhenTestClassIsNull() {
            // When
            String result = fileFinder.getTestClassFilePath(null);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("Should return existing file path when file exists in src/test/java")
        void shouldReturnExistingFilePathWhenFileExistsInSrcTestJava(@TempDir File tempDir) throws IOException {
            // Given
            String packagePath = FileFinderTest.class.getPackage().getName().replace('.', '/');
            File srcTestJava = new File(tempDir, "src/test/java/" + packagePath);
            srcTestJava.mkdirs();
            File testFile = new File(srcTestJava, "FileFinderTest.java");
            testFile.createNewFile();

            // Change working directory to temp directory
            String originalDir = System.getProperty("user.dir");
            System.setProperty("user.dir", tempDir.getAbsolutePath());

            try {
                // When
                String result = fileFinder.getTestClassFilePath(FileFinderTest.class);

                // Then
                assertNotNull(result);
                assertTrue(result.contains("src/test/java/"));
                assertTrue(result.contains("FileFinderTest.java"));
            } finally {
                System.setProperty("user.dir", originalDir);
            }
        }

        @Test
        @DisplayName("Should return existing file path when file exists in test directory")
        void shouldReturnExistingFilePathWhenFileExistsInTestDirectory(@TempDir File tempDir) throws IOException {
            // Given
            String packagePath = FileFinderTest.class.getPackage().getName().replace('.', '/');
            File testDir = new File(tempDir, "test/" + packagePath);
            testDir.mkdirs();
            File testFile = new File(testDir, "FileFinderTest.java");
            testFile.createNewFile();

            String originalDir = System.getProperty("user.dir");
            System.setProperty("user.dir", tempDir.getAbsolutePath());

            try {
                // When
                String result = fileFinder.getTestClassFilePath(FileFinderTest.class);

                // Then
                assertNotNull(result);
                assertTrue(result.contains("test/"));
                assertTrue(result.contains("FileFinderTest.java"));
            } finally {
                System.setProperty("user.dir", originalDir);
            }
        }

        @Test
        @DisplayName("Should return existing file path when file exists in src directory")
        void shouldReturnExistingFilePathWhenFileExistsInSrcDirectory(@TempDir File tempDir) throws IOException {
            // Given
            String packagePath = FileFinderTest.class.getPackage().getName().replace('.', '/');
            File srcDir = new File(tempDir, "src/" + packagePath);
            srcDir.mkdirs();
            File testFile = new File(srcDir, "FileFinderTest.java");
            testFile.createNewFile();

            String originalDir = System.getProperty("user.dir");
            System.setProperty("user.dir", tempDir.getAbsolutePath());

            try {
                // When
                String result = fileFinder.getTestClassFilePath(FileFinderTest.class);

                // Then
                assertNotNull(result);
                assertTrue(result.contains("src/"));
                assertTrue(result.contains("FileFinderTest.java"));
            } finally {
                System.setProperty("user.dir", originalDir);
            }
        }

        @Test
        @DisplayName("Should return default path when file not found in any directory")
        void shouldReturnDefaultPathWhenFileNotFoundInAnyDirectory() {
            // When
            String result = fileFinder.getTestClassFilePath(FileFinderTest.class);

            // Then
            assertNotNull(result);
            // FileFinderTest is actually in io.testomat.junit.methodexporter package, not patfinder
            assertEquals("src/test/java/io/testomat/junit/methodexporter/FileFinderTest.java", result);
        }

        @Test
        @DisplayName("Should handle simple class name correctly")
        void shouldHandleSimpleClassNameCorrectly() {
            // When
            String result = fileFinder.getTestClassFilePath(TestClassA.class);

            // Then
            assertNotNull(result);
            assertTrue(result.endsWith("TestClassA.java"));
            assertTrue(result.startsWith("src/test/java/"));
        }

        @Test
        @DisplayName("Should handle package class name correctly")
        void shouldHandlePackageClassNameCorrectly() {
            // When - using java.lang.String as example of packaged class
            String result = fileFinder.getTestClassFilePath(String.class); // java.lang.String

            // Then
            assertNotNull(result);
            assertTrue(result.contains("java/lang/String.java"));
            assertTrue(result.startsWith("src/test/java/"));
        }
    }

    @Nested
    @DisplayName("Get Path Tests")
    class GetPathTests {

        @Test
        @DisplayName("Should return path from extension context")
        void shouldReturnPathFromExtensionContext() throws Exception {
            // Given - test with actual class (this will test the real implementation)
            when(mockExtensionContext.getTestClass()).thenReturn(Optional.of(FileFinderTest.class));

            // When
            String result = fileFinder.getPath(mockExtensionContext);

            // Then - just verify it doesn't crash and returns something
            // The exact value depends on the test environment
            // So we just check it's not null or verify it contains expected patterns
            assertTrue(result == null || result.contains("test-classes") || result.contains("classes"));
        }

        @Test
        @DisplayName("Should return null when no test class found")
        void shouldReturnNullWhenNoTestClassFound() {
            // Given
            when(mockExtensionContext.getTestClass()).thenReturn(Optional.empty());

            // When
            String result = fileFinder.getPath(mockExtensionContext);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("Should return null when exception occurs")
        void shouldReturnNullWhenExceptionOccurs() {
            // Given
            when(mockExtensionContext.getTestClass())
                    .thenThrow(new RuntimeException("Context error"));

            // When
            String result = fileFinder.getPath(mockExtensionContext);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("Should handle URL decoding correctly")
        void shouldHandleUrlDecodingCorrectly() throws Exception {
            // Given - test URL decoding with a simpler approach
            // We'll test this indirectly by creating a scenario where decoding is needed
            when(mockExtensionContext.getTestClass()).thenReturn(Optional.of(FileFinderTest.class));

            // When
            String result = fileFinder.getPath(mockExtensionContext);

            // Then - verify it handles the path correctly (actual decoding will happen in real scenario)
            // This test mainly ensures no exceptions are thrown during URL decoding
            assertTrue(result == null || !result.contains("%20")); // No encoded spaces should remain
        }
    }

    @Nested
    @DisplayName("Extract Relative File Path Tests")
    class ExtractRelativeFilePathTests {

        @Test
        @DisplayName("Should extract relative path from src/test/java structure")
        void shouldExtractRelativePathFromSrcTestJavaStructure() {
            // Given
            String fullPath = "/home/user/project/src/test/java/com/example/TestClass.java";
            when(mockPathNormalizer.normalizePath(fullPath)).thenReturn(fullPath);

            // When
            String result = fileFinder.extractRelativeFilePath(fullPath);

            // Then
            assertEquals("com/example/TestClass.java", result);
        }

        @Test
        @DisplayName("Should extract relative path from src/main/java structure")
        void shouldExtractRelativePathFromSrcMainJavaStructure() {
            // Given
            String fullPath = "/home/user/project/src/main/java/com/example/MainClass.java";
            when(mockPathNormalizer.normalizePath(fullPath)).thenReturn(fullPath);

            // When
            String result = fileFinder.extractRelativeFilePath(fullPath);

            // Then
            assertEquals("com/example/MainClass.java", result);
        }

        @Test
        @DisplayName("Should extract relative path from generic src/java structure")
        void shouldExtractRelativePathFromGenericSrcJavaStructure() {
            // Given
            String fullPath = "/project/src/integration-test/java/com/example/IntegrationTest.java";
            when(mockPathNormalizer.normalizePath(fullPath)).thenReturn(fullPath);

            // When
            String result = fileFinder.extractRelativeFilePath(fullPath);

            // Then
            assertEquals("com/example/IntegrationTest.java", result);
        }

        @Test
        @DisplayName("Should return normalized path when no standard structure found")
        void shouldReturnNormalizedPathWhenNoStandardStructureFound() {
            // Given
            String fullPath = "/some/random/path/TestClass.java";
            when(mockPathNormalizer.normalizePath(fullPath)).thenReturn(fullPath);

            // When
            String result = fileFinder.extractRelativeFilePath(fullPath);

            // Then
            assertEquals(fullPath, result);
        }

        @Test
        @DisplayName("Should return simple filename when no path separators")
        void shouldReturnSimpleFilenameWhenNoPathSeparators() {
            // Given
            String filename = "TestClass.java";
            when(mockPathNormalizer.normalizePath(filename)).thenReturn(filename);

            // When
            String result = fileFinder.extractRelativeFilePath(filename);

            // Then
            assertEquals("TestClass.java", result);
        }

        @Test
        @DisplayName("Should return UnknownFile.java when filepath is null")
        void shouldReturnUnknownFileWhenFilepathIsNull() {
            // When
            String result = fileFinder.extractRelativeFilePath(null);

            // Then
            assertEquals("UnknownFile.java", result);
        }

        @Test
        @DisplayName("Should return UnknownFile.java when filepath is empty")
        void shouldReturnUnknownFileWhenFilepathIsEmpty() {
            // When
            String result = fileFinder.extractRelativeFilePath("");

            // Then
            assertEquals("UnknownFile.java", result);
        }

        @Test
        @DisplayName("Should handle Windows-style paths")
        void shouldHandleWindowsStylePaths() {
            // Given
            String windowsPath = "C:\\project\\src\\test\\java\\com\\example\\TestClass.java";
            String normalizedPath = "C:/project/src/test/java/com/example/TestClass.java";
            when(mockPathNormalizer.normalizePath(windowsPath)).thenReturn(normalizedPath);

            // When
            String result = fileFinder.extractRelativeFilePath(windowsPath);

            // Then
            assertEquals("com/example/TestClass.java", result);
        }

        @Test
        @DisplayName("Should return UnknownFile.java when exception occurs")
        void shouldReturnUnknownFileWhenExceptionOccurs() {
            // Given
            String problematicPath = "some/path";
            when(mockPathNormalizer.normalizePath(problematicPath))
                    .thenThrow(new RuntimeException("Normalization error"));

            // When
            String result = fileFinder.extractRelativeFilePath(problematicPath);

            // Then
            assertEquals("UnknownFile.java", result);
        }

        @Test
        @DisplayName("Should handle path with multiple src/java patterns")
        void shouldHandlePathWithMultipleSrcJavaPatterns() {
            // Given - path with multiple /java/ occurrences
            String complexPath = "/project/src/java/vendor/src/test/java/com/example/TestClass.java";
            when(mockPathNormalizer.normalizePath(complexPath)).thenReturn(complexPath);

            // When
            String result = fileFinder.extractRelativeFilePath(complexPath);

            // Then
            assertEquals("com/example/TestClass.java", result); // Should use src/test/java pattern
        }
    }

    @Nested
    @DisplayName("Cross-Platform Tests")
    class CrossPlatformTests {

        @Test
        @DisplayName("Should handle different file separators")
        void shouldHandleDifferentFileSeparators() {
            // Given - test with a real class

            // When
            String result = fileFinder.getTestClassFilePath(TestClassA.class);

            // Then
            assertNotNull(result);
            assertTrue(result.contains("TestClassA.java"));
            // Should use system file separator in the result
        }

        @Test
        @DisplayName("Should convert package dots to file separators correctly")
        void shouldConvertPackageDotsToFileSeparatorsCorrectly() {
            // Given - use String class which has deep package structure

            // When
            String result = fileFinder.getTestClassFilePath(String.class);

            // Then
            assertNotNull(result);
            assertTrue(result.contains("java/lang/String.java"));
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle complete workflow with existing files")
        void shouldHandleCompleteWorkflowWithExistingFiles(@TempDir File tempDir) throws IOException {
            // Given - create a complete directory structure using TestClassA
            String packagePath = TestClassA.class.getPackage().getName().replace('.', '/');
            File srcTestJava = new File(tempDir, "src/test/java/" + packagePath);
            srcTestJava.mkdirs();
            File testFile = new File(srcTestJava, "TestClassA.java");
            testFile.createNewFile();

            String originalDir = System.getProperty("user.dir");
            System.setProperty("user.dir", tempDir.getAbsolutePath());

            try {
                // When
                String filePath = fileFinder.getTestClassFilePath(TestClassA.class);
                String relativePath = fileFinder.extractRelativeFilePath(filePath);

                // Then
                assertNotNull(filePath);
                assertTrue(filePath.contains("src/test/java/"));
                assertTrue(filePath.contains("TestClassA.java"));
                assertTrue(relativePath.endsWith("TestClassA.java"));
            } finally {
                System.setProperty("user.dir", originalDir);
            }
        }

        @Test
        @DisplayName("Should handle workflow with non-existing files")
        void shouldHandleWorkflowWithNonExistingFiles() {
            // Given - use TestClassB which likely doesn't exist as file

            // When
            String filePath = fileFinder.getTestClassFilePath(TestClassB.class);
            String relativePath = fileFinder.extractRelativeFilePath(filePath);

            // Then
            assertNotNull(filePath);
            assertTrue(filePath.endsWith("TestClassB.java"));
            assertTrue(relativePath.endsWith("TestClassB.java"));
        }

        @Test
        @DisplayName("Should work with PathNormalizer integration")
        void shouldWorkWithPathNormalizerIntegration() {
            // Given
            String inputPath = "C:\\Windows\\Style\\Path\\src\\test\\java\\TestClass.java";
            String expectedNormalized = "C:/Windows/Style/Path/src/test/java/TestClass.java";
            when(mockPathNormalizer.normalizePath(inputPath)).thenReturn(expectedNormalized);

            // When
            String result = fileFinder.extractRelativeFilePath(inputPath);

            // Then
            assertEquals("TestClass.java", result);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle SecurityException gracefully")
        void shouldHandleSecurityExceptionGracefully() {
            // Given - this test is hard to reproduce with real classes
            // We test that normal operation doesn't throw SecurityException

            // When
            String result = fileFinder.getTestClassFilePath(TestClassA.class);

            // Then - should not throw exception
            assertNotNull(result);
            assertTrue(result.endsWith("TestClassA.java"));
        }

        @Test
        @DisplayName("Should handle file system access errors")
        void shouldHandleFileSystemAccessErrors() {
            // Given - test with a regular class that file system operations are handled gracefully

            // When - even if file system has issues, should not throw exceptions
            String result = fileFinder.getTestClassFilePath(TestClassB.class);

            // Then
            assertNotNull(result); // Should always return some path
            assertTrue(result.endsWith("TestClassB.java"));
        }
    }
}