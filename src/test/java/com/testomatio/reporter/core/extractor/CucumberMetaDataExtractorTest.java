package com.testomatio.reporter.core.extractor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.testomatio.reporter.model.TestMetadata;
import io.cucumber.plugin.event.TestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class CucumberMetaDataExtractorTest {

    private CucumberMetaDataExtractor extractor;

    @Mock
    private TestCase testCase;

    @BeforeEach
    void setUp() {
        extractor = new CucumberMetaDataExtractor();
    }

    @Test
    void extractTestMetadata_withNullTestCase_shouldReturnUnknownValues() {
        TestMetadata result = extractor.extractTestMetadata(null);

        assertEquals(CucumberMetaDataExtractor.UNKNOWN_TEST, result.getTitle());
        assertNull(result.getTestId());
        assertEquals(CucumberMetaDataExtractor.UNKNOWN_SUITE, result.getSuiteTitle());
        assertEquals(CucumberMetaDataExtractor.UNKNOWN_FILE + ".feature", result.getFile());
    }

    @Test
    void extractTestMetadata_withValidTestCase_shouldExtractAllMetadata() throws Exception {
        // Given
        String scenarioName = "Test scenario name";
        List<String> tags = Arrays.asList("@T12345678", "@title:Custom_Test_Title", "@other");
        URI uri = new URI("file:/path/to/test.feature");

        when(testCase.getName()).thenReturn(scenarioName);
        when(testCase.getTags()).thenReturn(tags);
        when(testCase.getUri()).thenReturn(uri);

        // When
        TestMetadata result = extractor.extractTestMetadata(testCase);

        // Then
        assertEquals("Custom Test Title", result.getTitle());
        assertEquals("@T12345678", result.getTestId());
        assertEquals("test", result.getSuiteTitle());
        assertEquals("test.feature", result.getFile());
    }

    @Test
    void extractTestMetadata_withTestIdAndTitleFromTags_shouldUseTagValues() throws Exception {
        // Given
        String scenarioName = "Original scenario name";
        List<String> tags = Arrays.asList("@T87654321", "@title:Tagged_Title", "@smoke");
        URI uri = new URI("file:/features/login.feature");

        when(testCase.getName()).thenReturn(scenarioName);
        when(testCase.getTags()).thenReturn(tags);
        when(testCase.getUri()).thenReturn(uri);

        // When
        TestMetadata result = extractor.extractTestMetadata(testCase);

        // Then
        assertEquals("Tagged Title", result.getTitle());
        assertEquals("@T87654321", result.getTestId());
        assertEquals("login", result.getSuiteTitle());
        assertEquals("login.feature", result.getFile());
    }

    @Test
    void extractTestMetadata_withoutTitleTag_shouldUseScenarioName() throws Exception {
        // Given
        String scenarioName = "Login with valid credentials";
        List<String> tags = Arrays.asList("@T12345678", "@smoke");
        URI uri = new URI("file:/features/authentication.feature");

        when(testCase.getName()).thenReturn(scenarioName);
        when(testCase.getTags()).thenReturn(tags);
        when(testCase.getUri()).thenReturn(uri);

        // When
        TestMetadata result = extractor.extractTestMetadata(testCase);

        // Then
        assertEquals("Login with valid credentials", result.getTitle());
        assertEquals("@T12345678", result.getTestId());
        assertEquals("authentication", result.getSuiteTitle());
        assertEquals("authentication.feature", result.getFile());
    }

    @Test
    void extractTestMetadata_withoutTestIdTag_shouldReturnNullTestId() throws Exception {
        // Given
        String scenarioName = "Test without ID";
        List<String> tags = Arrays.asList("@smoke", "@regression");
        URI uri = new URI("file:/features/test.feature");

        when(testCase.getName()).thenReturn(scenarioName);
        when(testCase.getTags()).thenReturn(tags);
        when(testCase.getUri()).thenReturn(uri);

        // When
        TestMetadata result = extractor.extractTestMetadata(testCase);

        // Then
        assertEquals("Test without ID", result.getTitle());
        assertNull(result.getTestId());
        assertEquals("test", result.getSuiteTitle());
        assertEquals("test.feature", result.getFile());
    }

    @Test
    void extractTestMetadata_withEmptyTags_shouldUseDefaults() throws Exception {
        // Given
        String scenarioName = "Empty tags scenario";
        List<String> tags = Collections.emptyList();
        URI uri = new URI("file:/features/empty.feature");

        when(testCase.getName()).thenReturn(scenarioName);
        when(testCase.getTags()).thenReturn(tags);
        when(testCase.getUri()).thenReturn(uri);

        // When
        TestMetadata result = extractor.extractTestMetadata(testCase);

        // Then
        assertEquals("Empty tags scenario", result.getTitle());
        assertNull(result.getTestId());
        assertEquals("empty", result.getSuiteTitle());
        assertEquals("empty.feature", result.getFile());
    }

    @Test
    void extractTestMetadata_withNullTags_shouldHandleGracefully() throws Exception {
        // Given
        String scenarioName = "Null tags scenario";
        URI uri = new URI("file:/features/null.feature");

        when(testCase.getName()).thenReturn(scenarioName);
        when(testCase.getTags()).thenReturn(null);
        when(testCase.getUri()).thenReturn(uri);

        // When
        TestMetadata result = extractor.extractTestMetadata(testCase);

        // Then
        assertEquals("Null tags scenario", result.getTitle());
        assertNull(result.getTestId());
        assertEquals("null", result.getSuiteTitle());
        assertEquals("null.feature", result.getFile());
    }

    @Test
    void extractTestMetadata_withInvalidTestIdFormat_shouldReturnNullTestId() throws Exception {
        // Given
        String scenarioName = "Invalid ID format";
        List<String> tags = Arrays.asList("@T123", "@TID12345678", "@invalidformat");
        URI uri = new URI("file:/features/invalid.feature");

        when(testCase.getName()).thenReturn(scenarioName);
        when(testCase.getTags()).thenReturn(tags);
        when(testCase.getUri()).thenReturn(uri);

        // When
        TestMetadata result = extractor.extractTestMetadata(testCase);

        // Then
        assertEquals("Invalid ID format", result.getTitle());
        assertNull(result.getTestId());
        assertEquals("invalid", result.getSuiteTitle());
        assertEquals("invalid.feature", result.getFile());
    }

    @Test
    void extractTestMetadata_withNullUri_shouldReturnUnknownFileInfo() {
        // Given
        String scenarioName = "Null URI scenario";
        List<String> tags = Arrays.asList("@T12345678");

        when(testCase.getName()).thenReturn(scenarioName);
        when(testCase.getTags()).thenReturn(tags);
        when(testCase.getUri()).thenReturn(null);

        // When
        TestMetadata result = extractor.extractTestMetadata(testCase);

        // Then
        assertEquals("Null URI scenario", result.getTitle());
        assertEquals("@T12345678", result.getTestId());
        assertEquals(CucumberMetaDataExtractor.UNKNOWN_SUITE, result.getSuiteTitle());
        assertEquals(CucumberMetaDataExtractor.UNKNOWN_FILE + ".feature", result.getFile());
    }

    @Test
    void extractTestMetadata_withComplexUriPath_shouldExtractCorrectFileName() throws Exception {
        // Given
        String scenarioName = "Complex path scenario";
        List<String> tags = Arrays.asList("@T12345678");
        URI uri = new URI("file:/very/deep/path/to/features/complex.feature");

        when(testCase.getName()).thenReturn(scenarioName);
        when(testCase.getTags()).thenReturn(tags);
        when(testCase.getUri()).thenReturn(uri);

        // When
        TestMetadata result = extractor.extractTestMetadata(testCase);

        // Then
        assertEquals("Complex path scenario", result.getTitle());
        assertEquals("@T12345678", result.getTestId());
        assertEquals("complex", result.getSuiteTitle());
        assertEquals("complex.feature", result.getFile());
    }

    @Test
    void extractTestMetadata_withMultipleTitleTags_shouldUseFirstOne() throws Exception {
        // Given
        String scenarioName = "Multiple titles scenario";
        List<String> tags = Arrays.asList("@title:First_Title", "@title:Second_Title", "@T12345678");
        URI uri = new URI("file:/features/multiple.feature");

        when(testCase.getName()).thenReturn(scenarioName);
        when(testCase.getTags()).thenReturn(tags);
        when(testCase.getUri()).thenReturn(uri);

        // When
        TestMetadata result = extractor.extractTestMetadata(testCase);

        // Then
        assertEquals("First Title", result.getTitle());
        assertEquals("@T12345678", result.getTestId());
        assertEquals("multiple", result.getSuiteTitle());
        assertEquals("multiple.feature", result.getFile());
    }

    @Test
    void extractTestMetadata_withFileNameWithoutExtension_shouldHandleCorrectly() throws Exception {
        // Given
        String scenarioName = "No extension scenario";
        List<String> tags = Arrays.asList("@T12345678");
        URI uri = new URI("file:/features/noextension");

        when(testCase.getName()).thenReturn(scenarioName);
        when(testCase.getTags()).thenReturn(tags);
        when(testCase.getUri()).thenReturn(uri);

        // When
        TestMetadata result = extractor.extractTestMetadata(testCase);

        // Then
        assertEquals("No extension scenario", result.getTitle());
        assertEquals("@T12345678", result.getTestId());
        assertEquals("noextension", result.getSuiteTitle());
        assertEquals("noextension", result.getFile());
    }

    @Test
    void extractTestMetadata_withNullScenarioName_shouldReturnUnknownTest() throws Exception {
        // Given
        List<String> tags = Arrays.asList("@T12345678");
        URI uri = new URI("file:/features/test.feature");

        when(testCase.getName()).thenReturn(null);
        when(testCase.getTags()).thenReturn(tags);
        when(testCase.getUri()).thenReturn(uri);

        // When
        TestMetadata result = extractor.extractTestMetadata(testCase);

        // Then
        assertEquals(CucumberMetaDataExtractor.UNKNOWN_TEST, result.getTitle());
        assertEquals("@T12345678", result.getTestId());
        assertEquals("test", result.getSuiteTitle());
        assertEquals("test.feature", result.getFile());
    }
}