package io.testomat.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TestMetadata Tests")
class TestMetadataTest {

    @Test
    @DisplayName("Should create TestMetadata with all fields")
    void testConstructor() {

        Link link = Link.test("T-123");
        List<Link> links = Arrays.asList(link);

        TestMetadata metadata = new TestMetadata(
            "Test Title",
            "test-123",
            "Suite Title",
            "TestFile.java",
            links
        );

        assertEquals("Test Title", metadata.getTitle());
        assertEquals("test-123", metadata.getTestId());
        assertEquals("Suite Title", metadata.getSuiteTitle());
        assertEquals("TestFile.java", metadata.getFile());

        assertNotNull(metadata.getLinks());
        assertEquals(1, metadata.getLinks().size());
        assertEquals("T-123", metadata.getLinks().get(0).getTest());
    }

    @Test
    @DisplayName("Should create TestMetadata with null testId")
    void testConstructorWithNullTestId() {

        Link link = Link.label("Smoke");

        TestMetadata metadata = new TestMetadata(
            "Test Title",
            null,
            "Suite Title",
            "TestFile.java",
            Arrays.asList(link)
        );

        assertEquals("Test Title", metadata.getTitle());
        assertNull(metadata.getTestId());
        assertEquals("Suite Title", metadata.getSuiteTitle());
        assertEquals("TestFile.java", metadata.getFile());

        assertNotNull(metadata.getLinks());
        assertEquals("Smoke", metadata.getLinks().get(0).getLabel());
    }

    @Test
    @DisplayName("Should update TestMetadata using setters")
    void testSetters() {

        TestMetadata metadata = new TestMetadata(
            "Initial Title",
            "test-123",
            "Initial Suite",
            "InitialFile.java",
            null
        );

        metadata.setTitle("Updated Title");
        metadata.setTestId("test-456");
        metadata.setSuiteTitle("Updated Suite");
        metadata.setFile("UpdatedFile.java");

        Link link = Link.test("T-999");
        metadata.setLinks(Arrays.asList(link));

        assertEquals("Updated Title", metadata.getTitle());
        assertEquals("test-456", metadata.getTestId());
        assertEquals("Updated Suite", metadata.getSuiteTitle());
        assertEquals("UpdatedFile.java", metadata.getFile());

        assertNotNull(metadata.getLinks());
        assertEquals(1, metadata.getLinks().size());
        assertEquals("T-999", metadata.getLinks().get(0).getTest());
    }

    @Test
    @DisplayName("Should handle empty string values")
    void testEmptyStringValues() {

        TestMetadata metadata = new TestMetadata(
            "",
            "",
            "",
            "",
            Collections.emptyList()
        );

        assertEquals("", metadata.getTitle());
        assertEquals("", metadata.getTestId());
        assertEquals("", metadata.getSuiteTitle());
        assertEquals("", metadata.getFile());

        assertNotNull(metadata.getLinks());
        assertTrue(metadata.getLinks().isEmpty());
    }

    @Test
    @DisplayName("Should handle special characters in title")
    void testSpecialCharactersInTitle() {

        String specialTitle = "Test with \"quotes\" and special chars: @#$%";

        TestMetadata metadata = new TestMetadata(
            specialTitle,
            "test-123",
            "Suite",
            "Test.java",
            null
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
            "src/test/java/TestFile.java",
            null
        );

        TestMetadata metadata2 = new TestMetadata(
            "Test",
            "test-2",
            "Suite",
            "src\\test\\java\\TestFile.java",
            null
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
            "Test.java",
            null
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
            "Test.java",
            null
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
            "Test.java",
            null
        );

        assertEquals("test-123", metadata.getTestId());

        metadata.setTestId(null);

        assertNull(metadata.getTestId());
    }

    @Test
    @DisplayName("Should allow null links")
    void testNullLinks() {

        TestMetadata metadata = new TestMetadata(
            "Test",
            "test-123",
            "Suite",
            "Test.java",
            null
        );

        assertNull(metadata.getLinks());
    }

    @Test
    @DisplayName("Should update links")
    void testUpdateLinks() {

        TestMetadata metadata = new TestMetadata(
            "Test",
            "test-123",
            "Suite",
            "Test.java",
            null
        );

        Link link1 = Link.test("T-1");
        Link link2 = Link.label("Regression");

        metadata.setLinks(Arrays.asList(link1, link2));

        assertNotNull(metadata.getLinks());
        assertEquals(2, metadata.getLinks().size());

        assertEquals("T-1", metadata.getLinks().get(0).getTest());
        assertEquals("Regression", metadata.getLinks().get(1).getLabel());
    }
}