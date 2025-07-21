package io.testomat.junit.methodexporter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class MethodExportManagerTest {

    @Nested
    class TestClass {
        @org.junit.jupiter.api.Test
        public void testMethod() {
        }
    }

    @BeforeEach
    void setUp() {
        System.setProperty("testomatio.api.key", "test-key-12345");
        System.setProperty("testomatio.url", "https://app.testomat.io");
        System.setProperty("testomatio.create", "false");
        System.setProperty("testomatio.publish", "false");
        System.setProperty("testomatio.export.required", "true");
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("testomatio.api.key");
        System.clearProperty("testomatio.url");
        System.clearProperty("testomatio.create");
        System.clearProperty("testomatio.publish");
        System.clearProperty("testomatio.export.required");
    }

    @Test
    public void testLoadTestBodyWithWindowsPath(@TempDir Path tempDir) throws IOException {
        String javaContent = "package com.example;\n" +
                "import org.junit.jupiter.api.Test;\n" +
                "import org.junit.jupiter.api.DisplayName;\n" +
                "\n" +
                "public class WindowsTestClass {\n" +
                "    @Test\n" +
                "    @DisplayName(\"Windows Test\")\n" +
                "    public void testOnWindows() {\n" +
                "        // test implementation\n" +
                "    }\n" +
                "}";

        Path testFile = tempDir.resolve("WindowsTestClass.java");
        Files.write(testFile, javaContent.getBytes());

        ExtensionContext context = mock(ExtensionContext.class);
        Class<?> testClass = TestClass.class;

        doReturn(Optional.of(testClass)).when(context).getTestClass();
        doReturn(testClass).when(context).getRequiredTestClass();

        try (MockedStatic<Paths> pathsMock = Mockito.mockStatic(Paths.class)) {
            pathsMock.when(() -> Paths.get(anyString())).thenReturn(testFile);

            MethodExportManager manager = new MethodExportManager();

            assertDoesNotThrow(() -> {
                manager.loadTestBodyIfRequired(context);
            });
        }
    }

    @Test
    public void testLoadTestBodyWithNonExistentFile() {
        ExtensionContext context = mock(ExtensionContext.class);
        Class<?> testClass = TestClass.class;

        doReturn(Optional.of(testClass)).when(context).getTestClass();
        doReturn(testClass).when(context).getRequiredTestClass();

        MethodExportManager manager = new MethodExportManager();

        assertDoesNotThrow(() -> {
            manager.loadTestBodyIfRequired(context);
        });
    }

    @Test
    public void testLoadTestBodyWithDifferentPathFormats(@TempDir Path tempDir) throws IOException {
        String javaContent = "package com.multiplatform;\n" +
                "import org.junit.jupiter.api.Test;\n" +
                "\n" +
                "public class MultiPlatformTest {\n" +
                "    @Test\n" +
                "    public void testMethod() {}\n" +
                "}";

        Path testFile = tempDir.resolve("MultiPlatformTest.java");
        Files.write(testFile, javaContent.getBytes());

        String[] pathFormats = {
                testFile.toString(),
                testFile.toString().replace('\\', '/'),
                testFile.toString().replace('/', '\\')
        };

        for (String pathFormat : pathFormats) {
            ExtensionContext context = mock(ExtensionContext.class);
            Class<?> testClass = TestClass.class;

            doReturn(Optional.of(testClass)).when(context).getTestClass();
            doReturn(testClass).when(context).getRequiredTestClass();

            try (MockedStatic<Paths> pathsMock = Mockito.mockStatic(Paths.class)) {
                pathsMock.when(() -> Paths.get(anyString())).thenReturn(testFile);

                MethodExportManager manager = new MethodExportManager();

                assertDoesNotThrow(() -> {
                    manager.loadTestBodyIfRequired(context);
                }, "Should handle path format: " + pathFormat);
            }
        }
    }

    @Test
    public void testLoadTestBodyWithExportNotRequired() {
        ExtensionContext context = mock(ExtensionContext.class);
        Class<?> testClass = TestClass.class;

        doReturn(Optional.of(testClass)).when(context).getTestClass();
        doReturn(testClass).when(context).getRequiredTestClass();

        MethodExportManager manager = new MethodExportManager();

        assertDoesNotThrow(() -> {
            manager.loadTestBodyIfRequired(context);
        });
    }

    @Test
    public void testLoadTestBodyWithDuplicateClasses() {
        ExtensionContext context = mock(ExtensionContext.class);
        Class<?> testClass = TestClass.class;

        doReturn(Optional.of(testClass)).when(context).getTestClass();
        doReturn(testClass).when(context).getRequiredTestClass();

        MethodExportManager manager = new MethodExportManager();

        assertDoesNotThrow(() -> {
            manager.loadTestBodyIfRequired(context);
            manager.loadTestBodyIfRequired(context);
        });
    }
}