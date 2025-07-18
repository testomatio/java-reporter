package io.testomat.junit.methodexporter;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PathFinderTest {

    private final PathFinder pathFinder = new PathFinder();

    @Nested
    class TestClass {
        @org.junit.jupiter.api.Test
        public void testMethod() {
        }
    }

    @Test
    public void testExtractRelativeFilePathWindows() {
        String windowsPath = "C:\\Users\\developer\\project\\src\\test\\java\\com\\example\\TestClass.java";
        String result = pathFinder.extractRelativeFilePath(windowsPath);
        assertEquals("src/test/java/com/example/TestClass.java", result);
    }

    @Test
    public void testExtractRelativeFilePathWindowsWithDriveLetter() {
        String windowsPath = "D:\\workspace\\myproject\\src\\test\\java\\MyTest.java";
        String result = pathFinder.extractRelativeFilePath(windowsPath);
        assertEquals("src/test/java/MyTest.java", result);
    }

    @Test
    public void testExtractRelativeFilePathLinux() {
        String linuxPath = "/home/user/project/src/test/java/com/example/TestClass.java";
        String result = pathFinder.extractRelativeFilePath(linuxPath);
        assertEquals("src/test/java/com/example/TestClass.java", result);
    }

    @Test
    public void testExtractRelativeFilePathMacOS() {
        String macPath = "/Users/developer/workspace/project/src/test/java/MyTest.java";
        String result = pathFinder.extractRelativeFilePath(macPath);
        assertEquals("src/test/java/MyTest.java", result);
    }

    @Test
    public void testExtractRelativeFilePathWithoutSrcDirectory() {
        String pathWithoutSrc = "com/example/TestClass.java";
        String result = pathFinder.extractRelativeFilePath(pathWithoutSrc);
        assertEquals("com/example/TestClass.java", result);
    }

    @Test
    public void testExtractRelativeFilePathEmpty() {
        String emptyPath = "";
        String result = pathFinder.extractRelativeFilePath(emptyPath);
        assertEquals("src/test/java/UnknownFile.java", result);
    }

    @Test
    public void testExtractRelativeFilePathWithMixedSlashes() {
        String mixedPath = "C:\\project/src\\test/java\\TestClass.java";
        String result = pathFinder.extractRelativeFilePath(mixedPath);
        assertEquals("src/test/java/TestClass.java", result);
    }

    @Test
    public void testGetTestClassFilePathWithTestClass() {
        ExtensionContext context = mock(ExtensionContext.class);
        Class<?> testClass = TestClass.class;

        doReturn(Optional.of(testClass)).when(context).getTestClass();
        doReturn(testClass).when(context).getRequiredTestClass();

        String result = pathFinder.getTestClassFilePath(context);

        assertNotNull(result);
        assertTrue(result.contains("src/test/java/") || result.contains("TestClass"));
    }

    @Test
    public void testPathNormalizationFromDifferentOSFormats() {
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
}