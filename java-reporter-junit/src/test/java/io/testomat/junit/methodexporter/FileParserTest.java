package io.testomat.junit.methodexporter;

import com.github.javaparser.ast.CompilationUnit;
import io.testomat.junit.methodexporter.parser.FileParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

public class FileParserTest {

    private final FileParser fileParser = new FileParser();

    @Test
    public void testParseFileWithUnixPath(@TempDir Path tempDir) throws IOException {
        String javaContent = "package com.example;\n" +
                "import org.junit.jupiter.api.Test;\n" +
                "\n" +
                "public class TestClass {\n" +
                "    @Test\n" +
                "    public void testMethod() {}\n" +
                "}";
        
        Path testFile = tempDir.resolve("TestClass.java");
        Files.write(testFile, javaContent.getBytes());
        
        String unixStylePath = testFile.toString().replace('\\', '/');
        
        CompilationUnit result = fileParser.parseFile(unixStylePath);
        
        assertNotNull(result);
        assertTrue(result.getPackageDeclaration().isPresent());
        assertEquals("com.example", result.getPackageDeclaration().get().getNameAsString());
    }

    @Test
    public void testParseFileNonExistentPath() {
        String nonExistentPath = "C:\\does\\not\\exist\\TestClass.java";
        
        CompilationUnit result = fileParser.parseFile(nonExistentPath);
        
        assertNull(result);
    }

    @Test
    public void testParseFileNonExistentUnixPath() {
        String nonExistentPath = "/does/not/exist/TestClass.java";
        
        CompilationUnit result = fileParser.parseFile(nonExistentPath);
        
        assertNull(result);
    }

    @Test
    public void testParseFileWithRelativePath(@TempDir Path tempDir) throws IOException {
        String javaContent = "package com.test;\n" +
                "import org.junit.jupiter.api.Test;\n" +
                "\n" +
                "public class RelativeTest {\n" +
                "    @Test\n" +
                "    public void testSomething() {}\n" +
                "}";
        
        Path testFile = tempDir.resolve("RelativeTest.java");
        Files.write(testFile, javaContent.getBytes());
        
        String relativePath = testFile.getFileName().toString();
        
        try (MockedStatic<Paths> pathsMock = Mockito.mockStatic(Paths.class)) {
            pathsMock.when(() -> Paths.get(relativePath)).thenReturn(testFile);
            
            CompilationUnit result = fileParser.parseFile(relativePath);
            
            assertNotNull(result);
            assertEquals("com.test", result.getPackageDeclaration().get().getNameAsString());
        }
    }

    @Test
    public void testParseFileWithMalformedJavaContent(@TempDir Path tempDir) throws IOException {
        String malformedContent = "package com.example\n" +
                "import org.junit.jupiter.api.Test;\n" +
                "\n" +
                "public class MalformedClass {\n" +
                "    @Test\n" +
                "    public void testMethod() {\n" +
                "        // missing closing brace\n" +
                "}";
        
        Path testFile = tempDir.resolve("MalformedClass.java");
        Files.write(testFile, malformedContent.getBytes());
        
        assertThrows(MethodExporterException.class, () -> {
            fileParser.parseFile(testFile.toString());
        });
    }

    @Test
    public void testParseFileThreadSafety(@TempDir Path tempDir) throws IOException {
        String javaContent = "package com.example;\n" +
                "import org.junit.jupiter.api.Test;\n" +
                "\n" +
                "public class ThreadSafeTest {\n" +
                "    @Test\n" +
                "    public void testMethod() {}\n" +
                "}";
        
        Path testFile = tempDir.resolve("ThreadSafeTest.java");
        Files.write(testFile, javaContent.getBytes());
        
        Thread[] threads = new Thread[5];
        CompilationUnit[] results = new CompilationUnit[5];
        
        for (int i = 0; i < 5; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                results[index] = fileParser.parseFile(testFile.toString());
            });
        }
        
        for (Thread thread : threads) {
            thread.start();
        }
        
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                fail("Thread interrupted: " + e.getMessage());
            }
        }
        
        for (CompilationUnit result : results) {
            assertNotNull(result);
            assertEquals("com.example", result.getPackageDeclaration().get().getNameAsString());
        }
    }
}