package io.testomat.cucumber.extractor;

import io.cucumber.plugin.event.TestCase;
import io.testomat.core.model.TestMetadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("CucumberMetaDataExtractor Tests")
class CucumberMetaDataExtractorTest {

    private CucumberMetaDataExtractor extractor;

    @Mock
    private TestCase mockTestCase;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        extractor = new CucumberMetaDataExtractor();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mockitoCloseable != null) {
            mockitoCloseable.close();
        }
    }

    @Test
    @DisplayName("Should extract metadata with title tag and test id")
    void shouldExtractMetadataWithTitleTagAndTestId() throws Exception {
        // Given
        List<String> tags = Arrays.asList("@title:Custom_Test_Title", "@T12345678", "@other");
        URI testUri = new URI("file:///features/test.feature");
        
        when(mockTestCase.getTags()).thenReturn(tags);
        when(mockTestCase.getUri()).thenReturn(testUri);
        when(mockTestCase.getName()).thenReturn("Test Name");

        // When
        TestMetadata metadata = extractor.extractTestMetadata(mockTestCase);

        // Then
        assertNotNull(metadata);
        assertEquals("Custom Test Title", metadata.getTitle());
        assertEquals("@T12345678", metadata.getTestId());
        assertEquals("test", metadata.getSuiteTitle());
        assertEquals("test.feature", metadata.getFile());
    }

    @Test
    @DisplayName("Should extract metadata without title tag - fallback to test name")
    void shouldExtractMetadataWithoutTitleTagFallbackToTestName() throws Exception {
        // Given
        List<String> tags = Arrays.asList("@T87654321", "@other");
        URI testUri = new URI("file:///features/login.feature");
        
        when(mockTestCase.getTags()).thenReturn(tags);
        when(mockTestCase.getUri()).thenReturn(testUri);
        when(mockTestCase.getName()).thenReturn("User can login successfully");

        // When
        TestMetadata metadata = extractor.extractTestMetadata(mockTestCase);

        // Then
        assertNotNull(metadata);
        assertEquals("User can login successfully", metadata.getTitle()); // From getName()
        assertEquals("@T87654321", metadata.getTestId());
        assertEquals("login", metadata.getSuiteTitle());
        assertEquals("login.feature", metadata.getFile());
    }

    @Test
    @DisplayName("Should extract metadata without test id")
    void shouldExtractMetadataWithoutTestId() throws Exception {
        // Given
        List<String> tags = Arrays.asList("@title:No_ID_Test", "@other", "@smoke");
        URI testUri = new URI("file:///features/registration.feature");
        
        when(mockTestCase.getTags()).thenReturn(tags);
        when(mockTestCase.getUri()).thenReturn(testUri);
        when(mockTestCase.getName()).thenReturn("Test Name");

        // When
        TestMetadata metadata = extractor.extractTestMetadata(mockTestCase);

        // Then
        assertNotNull(metadata);
        assertEquals("No ID Test", metadata.getTitle());
        assertNull(metadata.getTestId());
        assertEquals("registration", metadata.getSuiteTitle());
        assertEquals("registration.feature", metadata.getFile());
    }

    @Test
    @DisplayName("Should handle test case with null tags")
    void shouldHandleTestCaseWithNullTags() throws Exception {
        // Given
        URI testUri = new URI("file:///features/test.feature");
        
        when(mockTestCase.getTags()).thenReturn(null);
        when(mockTestCase.getUri()).thenReturn(testUri);
        when(mockTestCase.getName()).thenReturn("Test without tags");

        // When
        TestMetadata metadata = extractor.extractTestMetadata(mockTestCase);

        // Then
        assertNotNull(metadata);
        assertEquals("Test without tags", metadata.getTitle());
        assertNull(metadata.getTestId());
        assertEquals("test", metadata.getSuiteTitle());
        assertEquals("test.feature", metadata.getFile());
    }

    @Test
    @DisplayName("Should handle test case with empty tags")
    void shouldHandleTestCaseWithEmptyTags() throws Exception {
        // Given
        URI testUri = new URI("file:///features/empty.feature");
        
        when(mockTestCase.getTags()).thenReturn(Collections.emptyList());
        when(mockTestCase.getUri()).thenReturn(testUri);
        when(mockTestCase.getName()).thenReturn("Test with empty tags");

        // When
        TestMetadata metadata = extractor.extractTestMetadata(mockTestCase);

        // Then
        assertNotNull(metadata);
        assertEquals("Test with empty tags", metadata.getTitle());
        assertNull(metadata.getTestId());
        assertEquals("empty", metadata.getSuiteTitle());
        assertEquals("empty.feature", metadata.getFile());
    }

    @Test
    @DisplayName("Should handle test case with null name")
    void shouldHandleTestCaseWithNullName() throws Exception {
        // Given
        List<String> tags = Arrays.asList("@T11111111");
        URI testUri = new URI("file:///features/nullname.feature");
        
        when(mockTestCase.getTags()).thenReturn(tags);
        when(mockTestCase.getUri()).thenReturn(testUri);
        when(mockTestCase.getName()).thenReturn(null);

        // When
        TestMetadata metadata = extractor.extractTestMetadata(mockTestCase);

        // Then
        assertNotNull(metadata);
        assertEquals("Unknown test", metadata.getTitle()); // Fallback for null name
        assertEquals("@T11111111", metadata.getTestId());
        assertEquals("nullname", metadata.getSuiteTitle());
        assertEquals("nullname.feature", metadata.getFile());
    }

    @Test
    @DisplayName("Should handle classpath URI")
    void shouldHandleClasspathUri() throws Exception {
        // Given
        List<String> tags = Arrays.asList("@title:Classpath_Test");
        URI testUri = new URI("classpath:features/classpath.feature");
        
        when(mockTestCase.getTags()).thenReturn(tags);
        when(mockTestCase.getUri()).thenReturn(testUri);
        when(mockTestCase.getName()).thenReturn("Test Name");

        // When
        TestMetadata metadata = extractor.extractTestMetadata(mockTestCase);

        // Then
        assertNotNull(metadata);
        assertEquals("Classpath Test", metadata.getTitle());
        assertNull(metadata.getTestId());
        assertEquals("classpath", metadata.getSuiteTitle());
        assertEquals("classpath.feature", metadata.getFile());
    }

    @Test
    @DisplayName("Should handle URI without path")
    void shouldHandleUriWithoutPath() throws Exception {
        // Given
        List<String> tags = Arrays.asList("@title:No_Path_Test");
        URI testUri = new URI("jar:file://test.jar!/features/nopath.feature");
        
        when(mockTestCase.getTags()).thenReturn(tags);
        when(mockTestCase.getUri()).thenReturn(testUri);
        when(mockTestCase.getName()).thenReturn("Test Name");

        // When
        TestMetadata metadata = extractor.extractTestMetadata(mockTestCase);

        // Then
        assertNotNull(metadata);
        assertEquals("No Path Test", metadata.getTitle());
        assertNull(metadata.getTestId());
        assertNotNull(metadata.getSuiteTitle());
        assertNotNull(metadata.getFile());
    }

    @Test
    @DisplayName("Should handle file without extension")
    void shouldHandleFileWithoutExtension() throws Exception {
        // Given
        List<String> tags = Arrays.asList("@title:No_Extension_Test");
        URI testUri = new URI("file:///features/noextension");
        
        when(mockTestCase.getTags()).thenReturn(tags);
        when(mockTestCase.getUri()).thenReturn(testUri);
        when(mockTestCase.getName()).thenReturn("Test Name");

        // When
        TestMetadata metadata = extractor.extractTestMetadata(mockTestCase);

        // Then
        assertNotNull(metadata);
        assertEquals("No Extension Test", metadata.getTitle());
        assertNull(metadata.getTestId());
        assertEquals("noextension", metadata.getSuiteTitle()); // Same as filename
        assertEquals("noextension", metadata.getFile());
    }

    @Test
    @DisplayName("Should handle complex URI path")
    void shouldHandleComplexUriPath() throws Exception {
        // Given
        List<String> tags = Arrays.asList("@title:Complex_Path_Test", "@T99999999");
        URI testUri = new URI("file:///src/test/resources/features/complex/path/test.feature");
        
        when(mockTestCase.getTags()).thenReturn(tags);
        when(mockTestCase.getUri()).thenReturn(testUri);
        when(mockTestCase.getName()).thenReturn("Test Name");

        // When
        TestMetadata metadata = extractor.extractTestMetadata(mockTestCase);

        // Then
        assertNotNull(metadata);
        assertEquals("Complex Path Test", metadata.getTitle());
        assertEquals("@T99999999", metadata.getTestId());
        assertEquals("test", metadata.getSuiteTitle());
        assertEquals("test.feature", metadata.getFile());
    }

    @Test
    @DisplayName("Should handle multiple title tags - use first one")
    void shouldHandleMultipleTitleTags() throws Exception {
        // Given
        List<String> tags = Arrays.asList(
            "@title:First_Title", 
            "@title:Second_Title", 
            "@T12345678"
        );
        URI testUri = new URI("file:///features/multiple.feature");
        
        when(mockTestCase.getTags()).thenReturn(tags);
        when(mockTestCase.getUri()).thenReturn(testUri);
        when(mockTestCase.getName()).thenReturn("Test Name");

        // When
        TestMetadata metadata = extractor.extractTestMetadata(mockTestCase);

        // Then
        assertNotNull(metadata);
        assertEquals("First Title", metadata.getTitle()); // First title tag wins
        assertEquals("@T12345678", metadata.getTestId());
        assertEquals("multiple", metadata.getSuiteTitle());
        assertEquals("multiple.feature", metadata.getFile());
    }

    @Test
    @DisplayName("Should handle empty title tag")
    void shouldHandleEmptyTitleTag() throws Exception {
        // Given
        List<String> tags = Arrays.asList("@title:", "@T11111111");
        URI testUri = new URI("file:///features/emptytitle.feature");
        
        when(mockTestCase.getTags()).thenReturn(tags);
        when(mockTestCase.getUri()).thenReturn(testUri);
        when(mockTestCase.getName()).thenReturn("Fallback Test Name");

        // When
        TestMetadata metadata = extractor.extractTestMetadata(mockTestCase);

        // Then
        assertNotNull(metadata);
        assertEquals("", metadata.getTitle()); // Empty title from tag
        assertEquals("@T11111111", metadata.getTestId());
        assertEquals("emptytitle", metadata.getSuiteTitle());
        assertEquals("emptytitle.feature", metadata.getFile());
    }

    @Test
    @DisplayName("Should handle case insensitive title tag")
    void shouldHandleCaseInsensitiveTitleTag() throws Exception {
        // Given
        List<String> tags = Arrays.asList("@TITLE:Case_Insensitive_Test", "@T22222222");
        URI testUri = new URI("file:///features/case.feature");
        
        when(mockTestCase.getTags()).thenReturn(tags);
        when(mockTestCase.getUri()).thenReturn(testUri);
        when(mockTestCase.getName()).thenReturn("Test Name");

        // When
        TestMetadata metadata = extractor.extractTestMetadata(mockTestCase);

        // Then
        assertNotNull(metadata);
        assertEquals("Case Insensitive Test", metadata.getTitle());
        assertEquals("@T22222222", metadata.getTestId());
        assertEquals("case", metadata.getSuiteTitle());
        assertEquals("case.feature", metadata.getFile());
    }

    @Test
    @DisplayName("Should handle null tags in list")
    void shouldHandleNullTagsInList() throws Exception {
        // Given
        List<String> tags = Arrays.asList(null, "@title:Null_Tags_Test", null, "@T33333333", null);
        URI testUri = new URI("file:///features/nulltags.feature");
        
        when(mockTestCase.getTags()).thenReturn(tags);
        when(mockTestCase.getUri()).thenReturn(testUri);
        when(mockTestCase.getName()).thenReturn("Test Name");

        // When
        TestMetadata metadata = extractor.extractTestMetadata(mockTestCase);

        // Then
        assertNotNull(metadata);
        assertEquals("Null Tags Test", metadata.getTitle());
        assertEquals("@T33333333", metadata.getTestId());
        assertEquals("nulltags", metadata.getSuiteTitle());
        assertEquals("nulltags.feature", metadata.getFile());
    }
}