package io.testomat.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TestMetadata Tests")
class TestMetadataTest {

    @Test
    @DisplayName("Should create TestMetadata with all fields")
    void testConstructor() {
        TestMetadata metadata = new TestMetadata(
                "Test Title",
                "test-123",
                "Suite Title",
                "TestFile.java"
        );

        assertEquals("Test Title", metadata.getTitle());
        assertEquals("test-123", metadata.getTestId());
        assertEquals("Suite Title", metadata.getSuiteTitle());
        assertEquals("TestFile.java", metadata.getFile());
    }

    @Test
    @DisplayName("Should create TestMetadata with null testId")
    void testConstructorWithNullTestId() {
        TestMetadata metadata = new TestMetadata(
                "Test Title",
                null,
                "Suite Title",
                "TestFile.java"
        );

        assertEquals("Test Title", metadata.getTitle());
        assertNull(metadata.getTestId());
        assertEquals("Suite Title", metadata.getSuiteTitle());
        assertEquals("TestFile.java", metadata.getFile());
    }

    @Test
    @DisplayName("Should update TestMetadata using setters")
    void testSetters() {
        TestMetadata metadata = new TestMetadata(
                "Initial Title",
                "test-123",
                "Initial Suite",
                "InitialFile.java"
        );

        metadata.setTitle("Updated Title");
        metadata.setTestId("test-456");
        metadata.setSuiteTitle("Updated Suite");
        metadata.setFile("UpdatedFile.java");

        assertEquals("Updated Title", metadata.getTitle());
        assertEquals("test-456", metadata.getTestId());
        assertEquals("Updated Suite", metadata.getSuiteTitle());
        assertEquals("UpdatedFile.java", metadata.getFile());
    }

    @Test
    @DisplayName("Should handle empty string values")
    void testEmptyStringValues() {
        TestMetadata metadata = new TestMetadata(
                "",
                "",
                "",
                ""
        );

        assertEquals("", metadata.getTitle());
        assertEquals("", metadata.getTestId());
        assertEquals("", metadata.getSuiteTitle());
        assertEquals("", metadata.getFile());
    }

    @Test
    @DisplayName("Should handle special characters in title")
    void testSpecialCharactersInTitle() {
        String specialTitle = "Test with \"quotes\" and special chars: @#$%";

        TestMetadata metadata = new TestMetadata(
                specialTitle,
                "test-123",
                "Suite",
                "Test.java"
        );

        assertEquals(specialTitle, metadata.getTitle());
    }

    @Test
    @DisplayName("Should handle file paths with different separators")
    void testFilePathsWithDifferentSeparators() {
        TestMetadata metadata1 = new TestMetadata(
                "Test",
                "test-1",
                "Suite",
                "src/test/java/TestFile.java"
        );

        TestMetadata metadata2 = new TestMetadata(
                "Test",
                "test-2",
                "Suite",
                "src\\test\\java\\TestFile.java"
        );

        assertEquals("src/test/java/TestFile.java", metadata1.getFile());
        assertEquals("src\\test\\java\\TestFile.java", metadata2.getFile());
    }

    @Test
    @DisplayName("Should handle long suite titles")
    void testLongSuiteTitle() {
        String longSuite = "com.example.project.module.package.subpackage.TestSuiteClassName";

        TestMetadata metadata = new TestMetadata(
                "Test",
                "test-123",
                longSuite,
                "Test.java"
        );

        assertEquals(longSuite, metadata.getSuiteTitle());
    }

    @Test
    @DisplayName("Should allow updating testId from null to value")
    void testUpdateTestIdFromNull() {
        TestMetadata metadata = new TestMetadata(
                "Test",
                null,
                "Suite",
                "Test.java"
        );

        assertNull(metadata.getTestId());

        metadata.setTestId("test-789");

        assertEquals("test-789", metadata.getTestId());
    }

    @Test
    @DisplayName("Should allow updating testId to null")
    void testUpdateTestIdToNull() {
        TestMetadata metadata = new TestMetadata(
                "Test",
                "test-123",
                "Suite",
                "Test.java"
        );

        assertEquals("test-123", metadata.getTestId());

        metadata.setTestId(null);

        assertNull(metadata.getTestId());
    }
}
