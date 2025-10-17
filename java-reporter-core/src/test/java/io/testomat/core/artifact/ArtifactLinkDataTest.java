package io.testomat.core.artifact;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ArtifactLinkData Tests")
class ArtifactLinkDataTest {

    @Test
    @DisplayName("Should create ArtifactLinkData with all fields")
    void testConstructor() {
        List<String> links = Arrays.asList(
                "https://s3.amazonaws.com/artifact1.png",
                "https://s3.amazonaws.com/artifact2.log"
        );

        ArtifactLinkData data = new ArtifactLinkData(
                "Test Name",
                "rid-123",
                "test-123",
                links
        );

        assertEquals("Test Name", data.getTestName());
        assertEquals("rid-123", data.getRid());
        assertEquals("test-123", data.getTestId());
        assertEquals(2, data.getLinks().size());
        assertEquals(links, data.getLinks());
    }

    @Test
    @DisplayName("Should handle single artifact link")
    void testSingleArtifactLink() {
        List<String> links = Arrays.asList("https://s3.amazonaws.com/screenshot.png");

        ArtifactLinkData data = new ArtifactLinkData(
                "Screenshot Test",
                "rid-456",
                "test-456",
                links
        );

        assertEquals(1, data.getLinks().size());
        assertEquals("https://s3.amazonaws.com/screenshot.png", data.getLinks().get(0));
    }

    @Test
    @DisplayName("Should handle empty links list")
    void testEmptyLinksList() {
        List<String> links = Collections.emptyList();

        ArtifactLinkData data = new ArtifactLinkData(
                "Test with no artifacts",
                "rid-789",
                "test-789",
                links
        );

        assertNotNull(data.getLinks());
        assertEquals(0, data.getLinks().size());
    }

    @Test
    @DisplayName("Should update RID using setter")
    void testSetRid() {
        List<String> links = Arrays.asList("https://s3.amazonaws.com/artifact.png");

        ArtifactLinkData data = new ArtifactLinkData(
                "Test",
                "initial-rid",
                "test-123",
                links
        );

        assertEquals("initial-rid", data.getRid());

        data.setRid("updated-rid");

        assertEquals("updated-rid", data.getRid());
    }

    @Test
    @DisplayName("Should handle null test name")
    void testNullTestName() {
        List<String> links = Arrays.asList("https://s3.amazonaws.com/artifact.png");

        ArtifactLinkData data = new ArtifactLinkData(
                null,
                "rid-123",
                "test-123",
                links
        );

        assertNull(data.getTestName());
        assertEquals("rid-123", data.getRid());
        assertEquals("test-123", data.getTestId());
    }

    @Test
    @DisplayName("Should handle null test ID")
    void testNullTestId() {
        List<String> links = Arrays.asList("https://s3.amazonaws.com/artifact.png");

        ArtifactLinkData data = new ArtifactLinkData(
                "Test Name",
                "rid-123",
                null,
                links
        );

        assertEquals("Test Name", data.getTestName());
        assertEquals("rid-123", data.getRid());
        assertNull(data.getTestId());
    }

    @Test
    @DisplayName("Should handle null RID")
    void testNullRid() {
        List<String> links = Arrays.asList("https://s3.amazonaws.com/artifact.png");

        ArtifactLinkData data = new ArtifactLinkData(
                "Test Name",
                null,
                "test-123",
                links
        );

        assertEquals("Test Name", data.getTestName());
        assertNull(data.getRid());
        assertEquals("test-123", data.getTestId());
    }

    @Test
    @DisplayName("Should handle multiple artifact links of different types")
    void testMultipleDifferentArtifactTypes() {
        List<String> links = Arrays.asList(
                "https://s3.amazonaws.com/screenshot.png",
                "https://s3.amazonaws.com/video.mp4",
                "https://s3.amazonaws.com/logs.txt",
                "https://s3.amazonaws.com/data.json"
        );

        ArtifactLinkData data = new ArtifactLinkData(
                "Test with various artifacts",
                "rid-999",
                "test-999",
                links
        );

        assertEquals(4, data.getLinks().size());
        assertEquals("https://s3.amazonaws.com/screenshot.png", data.getLinks().get(0));
        assertEquals("https://s3.amazonaws.com/video.mp4", data.getLinks().get(1));
        assertEquals("https://s3.amazonaws.com/logs.txt", data.getLinks().get(2));
        assertEquals("https://s3.amazonaws.com/data.json", data.getLinks().get(3));
    }

    @Test
    @DisplayName("Should handle long test names")
    void testLongTestName() {
        String longName = "This is a very long test name that contains a lot of information "
                + "about what the test is doing and might include parameterized data "
                + "and other contextual information that makes it quite verbose";

        List<String> links = Arrays.asList("https://s3.amazonaws.com/artifact.png");

        ArtifactLinkData data = new ArtifactLinkData(
                longName,
                "rid-123",
                "test-123",
                links
        );

        assertEquals(longName, data.getTestName());
    }

    @Test
    @DisplayName("Should handle special characters in test name")
    void testSpecialCharactersInTestName() {
        String specialName = "Test with \"quotes\" and special chars: @#$%^&*()";

        List<String> links = Arrays.asList("https://s3.amazonaws.com/artifact.png");

        ArtifactLinkData data = new ArtifactLinkData(
                specialName,
                "rid-123",
                "test-123",
                links
        );

        assertEquals(specialName, data.getTestName());
    }

    @Test
    @DisplayName("Should update RID from null to value")
    void testUpdateRidFromNull() {
        List<String> links = Arrays.asList("https://s3.amazonaws.com/artifact.png");

        ArtifactLinkData data = new ArtifactLinkData(
                "Test",
                null,
                "test-123",
                links
        );

        assertNull(data.getRid());

        data.setRid("new-rid-789");

        assertEquals("new-rid-789", data.getRid());
    }

    @Test
    @DisplayName("Should update RID to null")
    void testUpdateRidToNull() {
        List<String> links = Arrays.asList("https://s3.amazonaws.com/artifact.png");

        ArtifactLinkData data = new ArtifactLinkData(
                "Test",
                "rid-123",
                "test-123",
                links
        );

        assertEquals("rid-123", data.getRid());

        data.setRid(null);

        assertNull(data.getRid());
    }

    @Test
    @DisplayName("Should handle artifact links with query parameters")
    void testArtifactLinksWithQueryParams() {
        List<String> links = Arrays.asList(
                "https://s3.amazonaws.com/artifact.png?version=1&token=abc123",
                "https://s3.amazonaws.com/data.json?timestamp=1234567890"
        );

        ArtifactLinkData data = new ArtifactLinkData(
                "Test",
                "rid-123",
                "test-123",
                links
        );

        assertEquals(2, data.getLinks().size());
        assertEquals("https://s3.amazonaws.com/artifact.png?version=1&token=abc123",
                data.getLinks().get(0));
        assertEquals("https://s3.amazonaws.com/data.json?timestamp=1234567890",
                data.getLinks().get(1));
    }
}
