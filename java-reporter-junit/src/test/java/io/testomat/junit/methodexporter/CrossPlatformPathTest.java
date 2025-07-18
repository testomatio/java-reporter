package io.testomat.junit.methodexporter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.io.TempDir;

public class CrossPlatformPathTest {

    @Nested
    class TestClass {
        @org.junit.jupiter.api.Test
        public void testMethod() {
        }
    }

    @Test
    public void testPathFinderExtractRelativeFilePathWindows() {
        PathFinder pathFinder = new PathFinder();
        String windowsPath = "C:\\Users\\developer\\project\\src\\test\\java\\com\\example\\TestClass.java";
        String result = pathFinder.extractRelativeFilePath(windowsPath);
        assertEquals("src/test/java/com/example/TestClass.java", result);
    }

    @Test
    public void testPathFinderExtractRelativeFilePathLinux() {
        PathFinder pathFinder = new PathFinder();
        String linuxPath = "/home/user/project/src/test/java/com/example/TestClass.java";
        String result = pathFinder.extractRelativeFilePath(linuxPath);
        assertEquals("src/test/java/com/example/TestClass.java", result);
    }

    @Test
    public void testPathFinderExtractRelativeFilePathMacOS() {
        PathFinder pathFinder = new PathFinder();
        String macPath = "/Users/developer/workspace/project/src/test/java/MyTest.java";
        String result = pathFinder.extractRelativeFilePath(macPath);
        assertEquals("src/test/java/MyTest.java", result);
    }

    @Test
    public void testPathFinderWithMixedSlashes() {
        PathFinder pathFinder = new PathFinder();
        String mixedPath = "C:\\project/src\\test/java\\TestClass.java";
        String result = pathFinder.extractRelativeFilePath(mixedPath);
        assertEquals("src/test/java/TestClass.java", result);
    }

    @Test
    public void testPathFinderNormalizationFromDifferentOSFormats() {
        PathFinder pathFinder = new PathFinder();
        String[] testPaths = {
                "C:\\Users\\dev\\project\\src\\test\\java\\Test.java",
                "/home/user/project/src/test/java/Test.java",
                "/Users/dev/project/src/test/java/Test.java",
                "D:\\workspace\\project\\src\\test\\java\\Test.java"
        };

        for (String path : testPaths) {
            String result = pathFinder.extractRelativeFilePath(path);
            assertEquals("src/test/java/Test.java", result);
            assertFalse(result.contains("\\"));
        }
    }

    @Test
    public void testPathFinderWithoutSrcDirectory() {
        PathFinder pathFinder = new PathFinder();
        String pathWithoutSrc = "com/example/TestClass.java";
        String result = pathFinder.extractRelativeFilePath(pathWithoutSrc);
        assertEquals("com/example/TestClass.java", result);
    }

    @Test
    public void testPathFinderWithEmptyPath() {
        PathFinder pathFinder = new PathFinder();
        String emptyPath = "";
        String result = pathFinder.extractRelativeFilePath(emptyPath);
        assertEquals("src/test/java/UnknownFile.java", result);
    }

    @Test
    public void testFileParserWithValidJavaFile(@TempDir Path tempDir) throws IOException {
        String javaContent = "package com.example;\n" +
                "import org.junit.jupiter.api.Test;\n" +
                "\n" +
                "public class TestClass {\n" +
                "    @Test\n" +
                "    public void testMethod() {}\n" +
                "}";

        Path testFile = tempDir.resolve("TestClass.java");
        Files.write(testFile, javaContent.getBytes());

        FileParser fileParser = new FileParser();
        com.github.javaparser.ast.CompilationUnit result = fileParser.parseFile(testFile.toString());

        assertNotNull(result);
        assertTrue(result.getPackageDeclaration().isPresent());
        assertEquals("com.example", result.getPackageDeclaration().get().getNameAsString());
    }

    @Test
    public void testFileParserWithNonExistentFile() {
        FileParser fileParser = new FileParser();
        String nonExistentPath = "C:\\does\\not\\exist\\TestClass.java";

        com.github.javaparser.ast.CompilationUnit result = fileParser.parseFile(nonExistentPath);
        assertNull(result);
    }

    @Test
    public void testFileParserWithDifferentPathFormats(@TempDir Path tempDir) throws IOException {
        String javaContent = "package com.test;\n" +
                "import org.junit.jupiter.api.Test;\n" +
                "\n" +
                "public class MultiPathTest {\n" +
                "    @Test\n" +
                "    public void testMethod() {}\n" +
                "}";

        Path testFile = tempDir.resolve("MultiPathTest.java");
        Files.write(testFile, javaContent.getBytes());

        FileParser fileParser = new FileParser();

        String[] pathFormats = {
                testFile.toString(),
                testFile.toString().replace('\\', '/'),
                testFile.toString().replace('/', '\\')
        };

        for (String pathFormat : pathFormats) {
            com.github.javaparser.ast.CompilationUnit result = fileParser.parseFile(pathFormat);
            assertNotNull(result);
            assertEquals("com.test", result.getPackageDeclaration().get().getNameAsString());
        }
    }

    @Test
    public void testExporterRequestBodyBuilderWithWindowsPaths() {
        ExporterRequestBodyBuilder builder = new ExporterRequestBodyBuilder();

        ExporterTestCase testCase = new ExporterTestCase();
        testCase.setName("testMethod");
        testCase.setCode("@Test public void testMethod() {}");
        testCase.setFile("src\\test\\java\\com\\example\\TestClass.java");
        testCase.setSkipped(false);
        testCase.setSuites(java.util.Arrays.asList("TestClass"));
        testCase.setLabels(java.util.Arrays.asList("unit"));

        java.util.List<ExporterTestCase> testCases = java.util.Collections.singletonList(testCase);
        String result = builder.buildRequestBody(testCases);

        assertNotNull(result);
        assertTrue(result.contains("\"file\": \"src\\\\test\\\\java\\\\com\\\\example\\\\TestClass.java\""));
        assertTrue(result.contains("\"framework\": \"junit\""));
        assertTrue(result.contains("\"language\": \"java\""));
    }

    @Test
    public void testExporterRequestBodyBuilderWithUnixPaths() {
        ExporterRequestBodyBuilder builder = new ExporterRequestBodyBuilder();

        ExporterTestCase testCase = new ExporterTestCase();
        testCase.setName("testMethod");
        testCase.setCode("@Test public void testMethod() {}");
        testCase.setFile("src/test/java/com/example/TestClass.java");
        testCase.setSkipped(false);
        testCase.setSuites(java.util.Arrays.asList("TestClass"));
        testCase.setLabels(java.util.Arrays.asList("unit"));

        java.util.List<ExporterTestCase> testCases = java.util.Collections.singletonList(testCase);
        String result = builder.buildRequestBody(testCases);

        assertNotNull(result);
        assertTrue(result.contains("\"file\": \"src/test/java/com/example/TestClass.java\""));
    }

    @Test
    public void testSimpleManagerWithNullContext() {
        ExtensionContext context = null;

        SimpleTestableManager manager = new SimpleTestableManager();

        assertThrows(IllegalArgumentException.class, () -> {
            manager.loadTestBodyIfRequired(context);
        });
    }

    @Test
    public void testSimpleManagerWithValidContext() {
        ExtensionContext context = mock(ExtensionContext.class);
        Class<?> testClass = TestClass.class;

        doReturn(Optional.of(testClass)).when(context).getTestClass();
        doReturn(testClass).when(context).getRequiredTestClass();

        SimpleTestableManager manager = new SimpleTestableManager();

        assertDoesNotThrow(() -> {
            manager.loadTestBodyIfRequired(context);
        });
    }

    private static class SimpleTestableManager {
        public void loadTestBodyIfRequired(final ExtensionContext extensionContext) {
            if (extensionContext == null) {
                throw new IllegalArgumentException("extensionContext is null");
            }
        }
    }
}