package io.testomat.cucumber.constructor;

import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.testomat.core.model.ExceptionDetails;
import io.testomat.core.model.TestResult;
import io.testomat.cucumber.extractor.TestDataExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CucumberTestResultConstructorTest {

    @Mock
    private TestDataExtractor testDataExtractor;

    @Mock
    private TestCaseFinished testCaseFinished;

    @Mock
    private TestCase testCase;

    @Mock
    private Result result;

    private CucumberTestResultConstructor constructor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        constructor = new CucumberTestResultConstructor(testDataExtractor);
    }

    @Test
    void shouldCreateDefaultConstructor() {
        CucumberTestResultConstructor defaultConstructor = new CucumberTestResultConstructor();
        assertNotNull(defaultConstructor);
    }

    @Test
    void shouldConstructTestRunResultWithAllFields() {
        // Given
        URI testUri = URI.create("file:///test/path/TestFeature.feature");
        UUID testCaseId = UUID.randomUUID();
        ExceptionDetails exceptionDetails = new ExceptionDetails("Test error", "Stack trace");
        Map<Object, Object> example = new HashMap<>();
        example.put("key", "value");

        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getUri()).thenReturn(testUri);
        when(testCase.getName()).thenReturn("Test Scenario");
        when(testCase.getTestSteps()).thenReturn(Collections.emptyList());

        when(testDataExtractor.extractExceptionDetails(testCaseFinished)).thenReturn(exceptionDetails);
        when(testDataExtractor.getNormalizedStatus(testCaseFinished)).thenReturn("PASSED");
        when(testDataExtractor.createExample(testCaseFinished)).thenReturn(example);
        when(testDataExtractor.extractTestId(testCaseFinished)).thenReturn("@T12345678");
        when(testDataExtractor.generateRid(testCaseFinished)).thenReturn("file:///test/path/TestFeature.feature.Test Scenario");
        when(testDataExtractor.extractFileName(testCaseFinished)).thenReturn("file:///test/path/TestFeature.feature");
        when(testDataExtractor.extractTitle(testCaseFinished)).thenReturn("Test Title");

        // When
        TestResult result = constructor.constructTestRunResult(testCaseFinished);

        // Then
        assertNotNull(result);
        assertEquals("PASSED", result.getStatus());
        assertEquals("file:///test/path/TestFeature.feature", result.getSuiteTitle());
        assertEquals(example, result.getExample());
        assertEquals("@T12345678", result.getTestId());
        assertEquals("file:///test/path/TestFeature.feature", result.getFile());
        assertEquals("Test Title", result.getTitle());
        assertEquals("file:///test/path/TestFeature.feature.Test Scenario", result.getRid());
        assertEquals("Test error", result.getMessage());
        assertEquals("Stack trace", result.getStack());
    }

    @Test
    void shouldConstructTestRunResultWithEmptyExceptionDetails() {
        // Given
        URI testUri = URI.create("file:///test/path/TestFeature.feature");
        UUID testCaseId = UUID.randomUUID();
        ExceptionDetails emptyExceptionDetails = ExceptionDetails.empty();
        Map<Object, Object> emptyExample = new HashMap<>();

        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getUri()).thenReturn(testUri);
        when(testCase.getName()).thenReturn("Test Scenario");
        when(testCase.getTestSteps()).thenReturn(Collections.emptyList());

        when(testDataExtractor.extractExceptionDetails(testCaseFinished)).thenReturn(emptyExceptionDetails);
        when(testDataExtractor.getNormalizedStatus(testCaseFinished)).thenReturn("FAILED");
        when(testDataExtractor.createExample(testCaseFinished)).thenReturn(emptyExample);
        when(testDataExtractor.extractTestId(testCaseFinished)).thenReturn(null);
        when(testDataExtractor.generateRid(testCaseFinished)).thenReturn("file:///test/path/TestFeature.feature.Test Scenario");
        when(testDataExtractor.extractFileName(testCaseFinished)).thenReturn(null);
        when(testDataExtractor.extractTitle(testCaseFinished)).thenReturn("Unknown test");

        // When
        TestResult result = constructor.constructTestRunResult(testCaseFinished);

        // Then
        assertNotNull(result);
        assertEquals("FAILED", result.getStatus());
        assertEquals("file:///test/path/TestFeature.feature", result.getSuiteTitle());
        assertEquals(emptyExample, result.getExample());
        assertNull(result.getTestId());
        assertNull(result.getFile());
        assertEquals("Unknown test", result.getTitle());
        assertEquals("file:///test/path/TestFeature.feature.Test Scenario", result.getRid());
        assertNull(result.getMessage());
        assertNull(result.getStack());
    }

    @Test
    void shouldVerifyAllExtractorMethodsAreCalled() {
        // Given
        URI testUri = URI.create("file:///test.feature");
        UUID testCaseId = UUID.randomUUID();
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getUri()).thenReturn(testUri);
        when(testCase.getId()).thenReturn(testCaseId);
        
        when(testDataExtractor.extractExceptionDetails(any())).thenReturn(ExceptionDetails.empty());
        when(testDataExtractor.getNormalizedStatus(any())).thenReturn("PASSED");
        when(testDataExtractor.createExample(any())).thenReturn(new HashMap<>());
        when(testDataExtractor.extractTestId(any(TestCaseFinished.class))).thenReturn(null);
        when(testDataExtractor.extractFileName(any())).thenReturn("file:///test.feature");
        when(testDataExtractor.extractTitle(any())).thenReturn("Test");

        // When
        constructor.constructTestRunResult(testCaseFinished);

        // Then
        verify(testDataExtractor).extractExceptionDetails(testCaseFinished);
        verify(testDataExtractor).getNormalizedStatus(testCaseFinished);
        verify(testDataExtractor).createExample(testCaseFinished);
        verify(testDataExtractor).extractTestId(testCaseFinished);
        verify(testDataExtractor).generateRid(testCaseFinished);
        verify(testDataExtractor).extractFileName(testCaseFinished);
        verify(testDataExtractor).extractTitle(testCaseFinished);
    }
}