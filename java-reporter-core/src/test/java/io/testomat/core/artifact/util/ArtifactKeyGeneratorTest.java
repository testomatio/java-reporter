package io.testomat.core.artifact.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.testomat.core.facade.methods.artifact.util.ArtifactKeyGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ArtifactKeyGenerator Tests")
class ArtifactKeyGeneratorTest {

    private ArtifactKeyGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new ArtifactKeyGenerator();
        ArtifactKeyGenerator.initializeRunId("test-run-123");
    }

    @Test
    @DisplayName("Should initialize run ID correctly")
    void testInitializeRunId() {
        ArtifactKeyGenerator.initializeRunId("run-456");

        String key = generator.generateKey("/path/to/file.txt", "rid-789", "TestCase");

        assertNotNull(key);
        assertTrue(key.startsWith("run-456/"));
    }

    @Test
    @DisplayName("Should generate key with all components")
    void testGenerateKeyWithAllComponents() {
        String key = generator.generateKey("/path/to/artifact.png", "rid-123", "MyTest");

        assertNotNull(key);
        assertTrue(key.contains("test-run-123"));
        assertTrue(key.contains("MyTest"));
        assertTrue(key.contains("rid-123"));
        assertTrue(key.contains("artifact.png"));
    }

    @Test
    @DisplayName("Should include run ID in generated key")
    void testKeyContainsRunId() {
        String key = generator.generateKey("/tmp/screenshot.png", "rid-001", "TestLogin");

        assertTrue(key.startsWith("test-run-123/"));
    }

    @Test
    @DisplayName("Should include test name in generated key")
    void testKeyContainsTestName() {
        String key = generator.generateKey("/tmp/file.txt", "rid-002", "TestCheckout");

        assertTrue(key.contains("TestCheckout"));
    }

    @Test
    @DisplayName("Should include request ID in generated key")
    void testKeyContainsRequestId() {
        String key = generator.generateKey("/tmp/file.txt", "rid-003", "TestPayment");

        assertTrue(key.contains("rid-003"));
    }

    @Test
    @DisplayName("Should include filename in generated key")
    void testKeyContainsFilename() {
        String key = generator.generateKey("/tmp/logs/error.log", "rid-004", "TestError");

        assertTrue(key.endsWith("error.log"));
    }

    @Test
    @DisplayName("Should extract filename from full path")
    void testExtractFilenameFromPath() {
        String key = generator.generateKey("/home/user/documents/report.pdf", "rid-005", "TestReport");

        assertTrue(key.endsWith("report.pdf"));
        assertTrue(key.contains("test-run-123"));
    }

    @Test
    @DisplayName("Should handle Windows-style paths")
    void testHandleWindowsStylePaths() {
        String key = generator.generateKey("C:\\Users\\Test\\image.jpg", "rid-006", "TestImage");

        assertTrue(key.endsWith("image.jpg"));
    }

    @Test
    @DisplayName("Should handle paths without directories")
    void testHandlePathsWithoutDirectories() {
        String key = generator.generateKey("simple.txt", "rid-007", "TestSimple");

        assertTrue(key.endsWith("simple.txt"));
    }

    @Test
    @DisplayName("Should use separator in key format")
    void testUseSeparatorInKeyFormat() {
        String key = generator.generateKey("/path/to/file.txt", "rid-008", "TestSeparator");

        // Check that key components are separated by "/"
        assertTrue(key.contains("/"));

        // Format should be: runId/testName::rid/filename
        String[] parts = key.split("/");
        assertTrue(parts.length >= 3);
    }

    @Test
    @DisplayName("Should format key as runId/testName::rid/filename")
    void testKeyFormat() {
        ArtifactKeyGenerator.initializeRunId("run-999");
        String key = generator.generateKey("/data/artifact.png", "rid-999", "TestFormat");

        // Expected format: run-999/TestFormat::rid-999/artifact.png
        assertEquals("run-999/TestFormat::rid-999/artifact.png", key);
    }

    @Test
    @DisplayName("Should handle special characters in test name")
    void testHandleSpecialCharactersInTestName() {
        String key = generator.generateKey("/tmp/file.txt", "rid-010", "Test With Spaces");

        assertNotNull(key);
        assertTrue(key.contains("Test With Spaces"));
    }

    @Test
    @DisplayName("Should handle special characters in filename")
    void testHandleSpecialCharactersInFilename() {
        String key = generator.generateKey("/tmp/file with spaces.txt", "rid-011", "TestFile");

        assertNotNull(key);
        assertTrue(key.contains("file with spaces.txt"));
    }

    @Test
    @DisplayName("Should handle numeric run ID")
    void testHandleNumericRunId() {
        ArtifactKeyGenerator.initializeRunId(12345);
        String key = generator.generateKey("/tmp/file.txt", "rid-012", "TestNumeric");

        assertTrue(key.startsWith("12345/"));
    }

    @Test
    @DisplayName("Should handle empty directory path")
    void testHandleEmptyDirectoryPath() {
        String key = generator.generateKey("", "rid-013", "TestEmpty");

        assertNotNull(key);
    }

    @Test
    @DisplayName("Should handle file path with extension")
    void testHandleFilePathWithExtension() {
        String key = generator.generateKey("/logs/test.log.gz", "rid-014", "TestLog");

        assertTrue(key.endsWith("test.log.gz"));
    }

    @Test
    @DisplayName("Should handle file path without extension")
    void testHandleFilePathWithoutExtension() {
        String key = generator.generateKey("/data/README", "rid-015", "TestReadme");

        assertTrue(key.endsWith("README"));
    }

    @Test
    @DisplayName("Should generate different keys for different inputs")
    void testGenerateDifferentKeysForDifferentInputs() {
        String key1 = generator.generateKey("/path1/file1.txt", "rid-016", "Test1");
        String key2 = generator.generateKey("/path2/file2.txt", "rid-017", "Test2");

        assertNotNull(key1);
        assertNotNull(key2);
        assertTrue(!key1.equals(key2));
    }

    @Test
    @DisplayName("Should maintain consistent format across multiple calls")
    void testConsistentFormat() {
        String key1 = generator.generateKey("/data/file.txt", "rid-018", "TestConsistent");
        String key2 = generator.generateKey("/data/file.txt", "rid-018", "TestConsistent");

        assertEquals(key1, key2);
    }
}
