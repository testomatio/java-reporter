package io.testomat.junit.methodexporter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;
import io.testomat.junit.exception.MethodExporterException;
import io.testomat.junit.methodexporter.parser.FileParser;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

@DisplayName("FileParser Tests")
class FileParserTest {

    private FileParser fileParser;

    @BeforeEach
    void setUp() {
        fileParser = new FileParser();
    }

    @Nested
    @DisplayName("Parse File Success Tests")
    class ParseFileSuccessTests {

        @Test
        @DisplayName("Should parse existing Java file successfully")
        void shouldParseExistingJavaFileSuccessfully(@TempDir Path tempDir) throws IOException {
            // Given
            String javaContent = "public class TestClass {\n" +
                    "    @Test\n" +
                    "    public void testMethod() {\n" +
                    "        // test code\n" +
                    "    }\n" +
                    "}";

            Path testFile = tempDir.resolve("TestClass.java");
            java.nio.file.Files.write(testFile, javaContent.getBytes());
            String filepath = testFile.toString();

            // When
            CompilationUnit result = fileParser.parseFile(filepath);

            // Then
            assertNotNull(result);
            assertTrue(result.toString().contains("TestClass"));
            assertTrue(result.toString().contains("testMethod"));
        }

        @Test
        @DisplayName("Should parse file with complex Java structure")
        void shouldParseFileWithComplexJavaStructure(@TempDir Path tempDir) throws IOException {
            // Given
            String complexJavaContent = "package com.example.test;\n" +
                    "\n" +
                    "import org.junit.jupiter.api.Test;\n" +
                    "import org.junit.jupiter.api.DisplayName;\n" +
                    "\n" +
                    "public class ComplexTestClass {\n" +
                    "    \n" +
                    "    @Test\n" +
                    "    @DisplayName(\"Test with parameters\")\n" +
                    "    public void testWithParameters(String param1, int param2) {\n" +
                    "        // complex test logic\n" +
                    "    }\n" +
                    "    \n" +
                    "    private void helperMethod() {\n" +
                    "        // helper code\n" +
                    "    }\n" +
                    "}";

            Path testFile = tempDir.resolve("ComplexTestClass.java");
            java.nio.file.Files.write(testFile, complexJavaContent.getBytes());
            String filepath = testFile.toString();

            // When
            CompilationUnit result = fileParser.parseFile(filepath);

            // Then
            assertNotNull(result);
            assertTrue(result.toString().contains("package com.example.test"));
            assertTrue(result.toString().contains("ComplexTestClass"));
            assertTrue(result.toString().contains("testWithParameters"));
            assertTrue(result.toString().contains("helperMethod"));
        }

        @Test
        @DisplayName("Should parse empty Java class")
        void shouldParseEmptyJavaClass(@TempDir Path tempDir) throws IOException {
            // Given
            String emptyClassContent = "public class EmptyClass {\n}";

            Path testFile = tempDir.resolve("EmptyClass.java");
            java.nio.file.Files.write(testFile, emptyClassContent.getBytes());
            String filepath = testFile.toString();

            // When
            CompilationUnit result = fileParser.parseFile(filepath);

            // Then
            assertNotNull(result);
            assertTrue(result.toString().contains("EmptyClass"));
        }
    }

    @Nested
    @DisplayName("File Not Found Tests")
    class FileNotFoundTests {

        @Test
        @DisplayName("Should return null when file does not exist")
        void shouldReturnNullWhenFileDoesNotExist() {
            // Given
            String nonExistentFile = "path/to/nonexistent/file.java";

            // When
            CompilationUnit result = fileParser.parseFile(nonExistentFile);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("Should return null for empty filepath")
        void shouldReturnNullForEmptyFilepath() {
            // Given
            String emptyFilepath = "";

            // When
            CompilationUnit result = fileParser.parseFile(emptyFilepath);

            // Then
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("Should throw MethodExporterException when JavaParser fails")
        void shouldThrowMethodExporterExceptionWhenJavaParserFails(@TempDir Path tempDir) throws IOException {
            // Given
            String invalidJavaContent = "invalid java syntax {{{";

            Path testFile = tempDir.resolve("InvalidClass.java");
            java.nio.file.Files.write(testFile, invalidJavaContent.getBytes());
            String filepath = testFile.toString();

            // When & Then
            MethodExporterException exception = assertThrows(
                    MethodExporterException.class,
                    () -> fileParser.parseFile(filepath)
            );

            assertEquals("Failed to parse file " + filepath, exception.getMessage());
            assertNotNull(exception.getCause());
        }

        @Test
        @DisplayName("Should throw MethodExporterException when Paths.get fails")
        void shouldThrowMethodExporterExceptionWhenPathsGetFails() {
            // Given
            try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {
                String filepath = "some/path/file.java";

                mockedPaths.when(() -> Paths.get(filepath))
                        .thenThrow(new InvalidPathException(filepath, "Invalid path"));

                // When & Then
                MethodExporterException exception = assertThrows(
                        MethodExporterException.class,
                        () -> fileParser.parseFile(filepath)
                );

                assertEquals("Failed to parse file " + filepath, exception.getMessage());
                assertTrue(exception.getCause() instanceof InvalidPathException);
            }
        }

        @Test
        @DisplayName("Should wrap JavaParser ParseProblemException")
        void shouldWrapJavaParserParseProblemException(@TempDir Path tempDir) throws IOException {
            // Given - create file with content that will cause parse problems
            String problematicContent = "class Test { public void method( }"; // Missing closing parenthesis

            Path testFile = tempDir.resolve("ProblematicClass.java");
            java.nio.file.Files.write(testFile, problematicContent.getBytes());
            String filepath = testFile.toString();

            // When & Then
            MethodExporterException exception = assertThrows(
                    MethodExporterException.class,
                    () -> fileParser.parseFile(filepath)
            );

            assertEquals("Failed to parse file " + filepath, exception.getMessage());
            assertTrue(exception.getCause() instanceof ParseProblemException);
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    class ThreadSafetyTests {

        @Test
        @DisplayName("Should handle concurrent parsing requests safely")
        void shouldHandleConcurrentParsingRequestsSafely(@TempDir Path tempDir) throws Exception {
            // Given
            int numberOfThreads = 5;
            int numberOfFiles = 3;
            List<Path> testFiles = new ArrayList<>();

            // Create multiple test files
            for (int i = 0; i < numberOfFiles; i++) {
                String content = "public class TestClass" + i + " {\n" +
                        "    @Test\n" +
                        "    public void testMethod" + i + "() {\n" +
                        "        // test code " + i + "\n" +
                        "    }\n" +
                        "}";

                Path testFile = tempDir.resolve("TestClass" + i + ".java");
                java.nio.file.Files.write(testFile, content.getBytes());
                testFiles.add(testFile);
            }

            ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(numberOfThreads);
            List<Future<CompilationUnit>> futures = new ArrayList<>();

            // When - submit concurrent parsing tasks
            for (int i = 0; i < numberOfThreads; i++) {
                Path fileToProcess = testFiles.get(i % numberOfFiles);

                Future<CompilationUnit> future = executor.submit(() -> {
                    try {
                        startLatch.await(); // Wait for all threads to be ready
                        return fileParser.parseFile(fileToProcess.toString());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        endLatch.countDown();
                    }
                });
                futures.add(future);
            }

            startLatch.countDown(); // Start all threads simultaneously
            endLatch.await(10, TimeUnit.SECONDS); // Wait for completion

            // Then - verify all parsing completed successfully
            for (Future<CompilationUnit> future : futures) {
                CompilationUnit result = future.get();
                assertNotNull(result);
                assertTrue(result.toString().contains("TestClass"));
            }

            executor.shutdown();
        }

        @Test
        @DisplayName("Should handle concurrent parsing of same file")
        void shouldHandleConcurrentParsingOfSameFile(@TempDir Path tempDir) throws Exception {
            // Given
            String javaContent = "public class SharedTestClass {\n" +
                    "    @Test\n" +
                    "    public void sharedTestMethod() {\n" +
                    "        // shared test code\n" +
                    "    }\n" +
                    "}";

            Path sharedFile = tempDir.resolve("SharedTestClass.java");
            java.nio.file.Files.write(sharedFile, javaContent.getBytes());
            String filepath = sharedFile.toString();

            int numberOfThreads = 10;
            ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
            CountDownLatch startLatch = new CountDownLatch(1);
            List<Future<CompilationUnit>> futures = new ArrayList<>();

            // When - multiple threads parse the same file
            for (int i = 0; i < numberOfThreads; i++) {
                Future<CompilationUnit> future = executor.submit(() -> {
                    try {
                        startLatch.await();
                        return fileParser.parseFile(filepath);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
                futures.add(future);
            }

            startLatch.countDown();

            // Then - all threads should get valid results
            for (Future<CompilationUnit> future : futures) {
                CompilationUnit result = future.get(5, TimeUnit.SECONDS);
                assertNotNull(result);
                assertTrue(result.toString().contains("SharedTestClass"));
                assertTrue(result.toString().contains("sharedTestMethod"));
            }

            executor.shutdown();
        }
    }

    @Nested
    @DisplayName("Path Handling Tests")
    class PathHandlingTests {

        @Test
        @DisplayName("Should handle null filepath gracefully")
        void shouldHandleNullFilepathGracefully() {
            // When & Then
            MethodExporterException exception = assertThrows(
                    MethodExporterException.class,
                    () -> fileParser.parseFile(null)
            );

            assertEquals("Failed to parse file null", exception.getMessage());
        }

        @Test
        @DisplayName("Should handle relative paths")
        void shouldHandleRelativePaths(@TempDir Path tempDir) throws IOException {
            // Given
            String javaContent = "public class RelativePathTest {}";
            Path testFile = tempDir.resolve("RelativePathTest.java");
            java.nio.file.Files.write(testFile, javaContent.getBytes());

            // Create relative path from current directory
            Path currentDir = Paths.get("").toAbsolutePath();
            Path relativePath = currentDir.relativize(testFile);

            // When
            CompilationUnit result = fileParser.parseFile(relativePath.toString());

            // Then
            assertNotNull(result);
            assertTrue(result.toString().contains("RelativePathTest"));
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should parse real Java test file with annotations")
        void shouldParseRealJavaTestFileWithAnnotations(@TempDir Path tempDir) throws IOException {
            // Given
            String realTestContent = "package com.example.test;\n" +
                    "\n" +
                    "import org.junit.jupiter.api.Test;\n" +
                    "import org.junit.jupiter.api.DisplayName;\n" +
                    "import org.junit.jupiter.api.BeforeEach;\n" +
                    "import org.junit.jupiter.api.AfterEach;\n" +
                    "import static org.junit.jupiter.api.Assertions.*;\n" +
                    "\n" +
                    "public class RealTestClass {\n" +
                    "    \n" +
                    "    private String testData;\n" +
                    "    \n" +
                    "    @BeforeEach\n" +
                    "    void setUp() {\n" +
                    "        testData = \"initialized\";\n" +
                    "    }\n" +
                    "    \n" +
                    "    @Test\n" +
                    "    @DisplayName(\"Should test basic functionality\")\n" +
                    "    void shouldTestBasicFunctionality() {\n" +
                    "        assertNotNull(testData);\n" +
                    "        assertEquals(\"initialized\", testData);\n" +
                    "    }\n" +
                    "    \n" +
                    "    @Test\n" +
                    "    void shouldTestAnotherFeature() {\n" +
                    "        assertTrue(testData.length() > 0);\n" +
                    "    }\n" +
                    "    \n" +
                    "    @AfterEach\n" +
                    "    void tearDown() {\n" +
                    "        testData = null;\n" +
                    "    }\n" +
                    "}";

            Path testFile = tempDir.resolve("RealTestClass.java");
            java.nio.file.Files.write(testFile, realTestContent.getBytes());
            String filepath = testFile.toString();

            // When
            CompilationUnit result = fileParser.parseFile(filepath);

            // Then
            assertNotNull(result);

            // Verify package structure
            assertTrue(result.toString().contains("package com.example.test"));

            // Verify imports
            assertTrue(result.toString().contains("import org.junit.jupiter.api.Test"));
            assertTrue(result.toString().contains("import org.junit.jupiter.api.DisplayName"));

            // Verify class and methods
            assertTrue(result.toString().contains("class RealTestClass"));
            assertTrue(result.toString().contains("shouldTestBasicFunctionality"));
            assertTrue(result.toString().contains("shouldTestAnotherFeature"));

            // Verify annotations
            assertTrue(result.toString().contains("@Test"));
            assertTrue(result.toString().contains("@DisplayName"));
            assertTrue(result.toString().contains("@BeforeEach"));
            assertTrue(result.toString().contains("@AfterEach"));
        }

        @Test
        @DisplayName("Should handle file parsing workflow end-to-end")
        void shouldHandleFileParsingWorkflowEndToEnd(@TempDir Path tempDir) throws IOException {
            // Given - simulate complete workflow
            String[] testFiles = {
                    "TestClass1.java",
                    "TestClass2.java",
                    "UtilityClass.java"
            };

            String[] testContents = {
                    "public class TestClass1 { @Test public void test1() {} }",
                    "public class TestClass2 { @Test public void test2() {} }",
                    "public class UtilityClass { public void helper() {} }"
            };

            List<CompilationUnit> results = new ArrayList<>();

            // When - parse all files
            for (int i = 0; i < testFiles.length; i++) {
                Path testFile = tempDir.resolve(testFiles[i]);
                java.nio.file.Files.write(testFile, testContents[i].getBytes());

                CompilationUnit result = fileParser.parseFile(testFile.toString());
                results.add(result);
            }

            // Then - verify all files parsed successfully
            assertEquals(3, results.size());

            for (int i = 0; i < results.size(); i++) {
                CompilationUnit result = results.get(i);
                assertNotNull(result, "File " + testFiles[i] + " should be parsed successfully");

                String expectedClassName = testFiles[i].replace(".java", "");
                assertTrue(result.toString().contains(expectedClassName),
                        "Parsed content should contain class " + expectedClassName);
            }
        }
    }
}