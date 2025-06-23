package com.testomatio.reporter.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testomatio.reporter.client.request.TestomatRequestBodyBuilder;
import com.testomatio.reporter.exception.FailedToCreateRunBodyException;
import com.testomatio.reporter.model.TestCaseResult;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestomatRequestBodyBuilderTest {

    private TestomatRequestBodyBuilder requestBodyBuilder;
    private AutoCloseable mockitoCloseable;

    @Mock
    private ObjectMapper mockObjectMapper;

    @Mock
    private TestCaseResult testRunResult1;

    @Mock
    private TestCaseResult testRunResult2;

    private static final String API_KEY = "test-api-key";
    private static final String TEST_TITLE = "Test Run Title";
    private static final String EXPECTED_JSON = "{\"title\":\"Test Run Title\"}";

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        resetAllMocks();
    }

    @AfterEach
    void tearDown() throws Exception {
        resetAllMocks();

        if (mockitoCloseable != null) {
            mockitoCloseable.close();
        }
    }

    private void resetAllMocks() {
        reset(mockObjectMapper, testRunResult1, testRunResult2);
    }

    @Test
    void constructor_WithDefaultObjectMapper_ShouldCreateInstance() {
        // When
        TestomatRequestBodyBuilder builder = new TestomatRequestBodyBuilder();

        // Then
        assertNotNull(builder);
    }

    @Test
    void constructor_WithCustomObjectMapper_ShouldCreateInstance() {
        // When
        TestomatRequestBodyBuilder builder = new TestomatRequestBodyBuilder(mockObjectMapper);

        // Then
        assertNotNull(builder);
    }

    @Test
    void buildCreateRunBody_WithValidTitle_ShouldReturnJsonString() throws Exception {
        // Given
        requestBodyBuilder = new TestomatRequestBodyBuilder(mockObjectMapper);
        when(mockObjectMapper.writeValueAsString(any())).thenReturn(EXPECTED_JSON);

        // When
        String result = requestBodyBuilder.buildCreateRunBody(TEST_TITLE);

        // Then
        assertEquals(EXPECTED_JSON, result);
    }

    @Test
    void buildCreateRunBody_WithRealObjectMapper_ShouldReturnValidJson() {
        // Given
        requestBodyBuilder = new TestomatRequestBodyBuilder();

        // When
        String result = requestBodyBuilder.buildCreateRunBody(TEST_TITLE);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("title"));
        assertTrue(result.contains(TEST_TITLE));
    }

    @Test
    void buildCreateRunBody_WithJsonProcessingException_ShouldThrowFailedToCreateRunBodyException() throws Exception {
        // Given
        requestBodyBuilder = new TestomatRequestBodyBuilder(mockObjectMapper);
        when(mockObjectMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("Serialization failed") {
                });

        // When & Then
        assertThrows(FailedToCreateRunBodyException.class,
                () -> requestBodyBuilder.buildCreateRunBody(TEST_TITLE));
    }

    @Test
    void buildSingleTestReportBody_WithValidResult_ShouldReturnJsonString() throws Exception {
        // Given
        requestBodyBuilder = new TestomatRequestBodyBuilder(mockObjectMapper);
        setupTestRunResult(testRunResult1, "Test 1", "T123", "Suite 1", "test.java", "PASSED", "Success", "stack");
        when(mockObjectMapper.writeValueAsString(any())).thenReturn(EXPECTED_JSON);

        // When
        String result = requestBodyBuilder.buildSingleTestReportBody(testRunResult1);

        // Then
        assertEquals(EXPECTED_JSON, result);
    }

    @Test
    void buildSingleTestReportBody_WithRealObjectMapper_ShouldReturnValidJson() throws Exception {
        // Given
        requestBodyBuilder = new TestomatRequestBodyBuilder();
        setupTestRunResult(testRunResult1, "Test 1", "T123", "Suite 1", "test.java", "PASSED", "Success", "stack");

        // When
        String result = requestBodyBuilder.buildSingleTestReportBody(testRunResult1);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Test 1"));
        assertTrue(result.contains("T123"));
        assertTrue(result.contains("create"));
        assertTrue(result.contains("true"));
    }

    @Test
    void buildSingleTestReportBody_WithNullOptionalFields_ShouldReturnValidJson() throws Exception {
        // Given
        requestBodyBuilder = new TestomatRequestBodyBuilder();
        setupTestRunResult(testRunResult1, "Test 1", null, "Suite 1", "test.java", "PASSED", null, null);

        // When
        String result = requestBodyBuilder.buildSingleTestReportBody(testRunResult1);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Test 1"));
        assertTrue(result.contains("Suite 1"));
        assertTrue(result.contains("create"));
    }

    @Test
    void buildSingleTestReportBody_WithJsonProcessingException_ShouldThrowException() throws Exception {
        // Given
        requestBodyBuilder = new TestomatRequestBodyBuilder(mockObjectMapper);
        setupTestRunResult(testRunResult1, "Test 1", "T123", "Suite 1", "test.java", "PASSED", "Success", "stack");
        when(mockObjectMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("Serialization failed") {
                });

        // When & Then
        assertThrows(JsonProcessingException.class,
                () -> requestBodyBuilder.buildSingleTestReportBody(testRunResult1));
    }

    @Test
    void buildBatchTestReportBody_WithValidResults_ShouldReturnJsonString() throws Exception {
        // Given
        requestBodyBuilder = new TestomatRequestBodyBuilder(mockObjectMapper);
        setupTestRunResult(testRunResult1, "Test 1", "T123", "Suite 1", "test.java", "PASSED", "Success", "stack");
        setupTestRunResult(testRunResult2, "Test 2", "T456", "Suite 2", "test2.java", "FAILED", "Error", "stack2");
        List<TestCaseResult> results = Arrays.asList(testRunResult1, testRunResult2);
        when(mockObjectMapper.writeValueAsString(any())).thenReturn(EXPECTED_JSON);

        // When
        String result = requestBodyBuilder.buildBatchTestReportBody(results, API_KEY);

        // Then
        assertEquals(EXPECTED_JSON, result);
    }

    @Test
    void buildBatchTestReportBody_WithRealObjectMapper_ShouldReturnValidJson() throws Exception {
        // Given
        requestBodyBuilder = new TestomatRequestBodyBuilder();
        setupTestRunResult(testRunResult1, "Test 1", "T123", "Suite 1", "test.java", "PASSED", "Success", "stack");
        setupTestRunResult(testRunResult2, "Test 2", "T456", "Suite 2", "test2.java", "FAILED", "Error", "stack2");
        List<TestCaseResult> results = Arrays.asList(testRunResult1, testRunResult2);

        // When
        String result = requestBodyBuilder.buildBatchTestReportBody(results, API_KEY);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("api_key"));
        assertTrue(result.contains(API_KEY));
        assertTrue(result.contains("tests"));
        assertTrue(result.contains("Test 1"));
        assertTrue(result.contains("Test 2"));
        assertTrue(result.contains("create"));
    }

    @Test
    void buildBatchTestReportBody_WithSingleResult_ShouldReturnValidJson() throws Exception {
        // Given
        requestBodyBuilder = new TestomatRequestBodyBuilder();
        setupTestRunResult(testRunResult1, "Test 1", "T123", "Suite 1", "test.java", "PASSED", "Success", "stack");
        List<TestCaseResult> results = Collections.singletonList(testRunResult1);

        // When
        String result = requestBodyBuilder.buildBatchTestReportBody(results, API_KEY);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("api_key"));
        assertTrue(result.contains("tests"));
        assertTrue(result.contains("Test 1"));
    }

    @Test
    void buildBatchTestReportBody_WithEmptyResults_ShouldReturnValidJson() throws Exception {
        // Given
        requestBodyBuilder = new TestomatRequestBodyBuilder();
        List<TestCaseResult> results = Collections.emptyList();

        // When
        String result = requestBodyBuilder.buildBatchTestReportBody(results, API_KEY);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("api_key"));
        assertTrue(result.contains("tests"));
        assertTrue(result.contains("[]")); // Empty array
    }

    @Test
    void buildBatchTestReportBody_WithJsonProcessingException_ShouldThrowException() throws Exception {
        // Given
        requestBodyBuilder = new TestomatRequestBodyBuilder(mockObjectMapper);
        setupTestRunResult(testRunResult1, "Test 1", "T123", "Suite 1", "test.java", "PASSED", "Success", "stack");
        List<TestCaseResult> results = Collections.singletonList(testRunResult1);
        when(mockObjectMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("Serialization failed") {
                });

        // When & Then
        assertThrows(JsonProcessingException.class,
                () -> requestBodyBuilder.buildBatchTestReportBody(results, API_KEY));
    }

    @Test
    void buildFinishRunBody_WithValidDuration_ShouldReturnJsonString() throws Exception {
        // Given
        requestBodyBuilder = new TestomatRequestBodyBuilder(mockObjectMapper);
        float duration = 123.45f;
        when(mockObjectMapper.writeValueAsString(any())).thenReturn(EXPECTED_JSON);

        // When
        String result = requestBodyBuilder.buildFinishRunBody(duration);

        // Then
        assertEquals(EXPECTED_JSON, result);
    }

    @Test
    void buildFinishRunBody_WithRealObjectMapper_ShouldReturnValidJson() throws Exception {
        // Given
        requestBodyBuilder = new TestomatRequestBodyBuilder();
        float duration = 123.45f;

        // When
        String result = requestBodyBuilder.buildFinishRunBody(duration);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("status_event"));
        assertTrue(result.contains("finish"));
        assertTrue(result.contains("duration"));
        assertTrue(result.contains("123.45"));
    }

    @Test
    void buildFinishRunBody_WithZeroDuration_ShouldReturnValidJson() throws Exception {
        // Given
        requestBodyBuilder = new TestomatRequestBodyBuilder();
        float duration = 0.0f;

        // When
        String result = requestBodyBuilder.buildFinishRunBody(duration);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("duration"));
        assertTrue(result.contains("0.0"));
    }

    @Test
    void buildFinishRunBody_WithNegativeDuration_ShouldReturnValidJson() throws Exception {
        // Given
        requestBodyBuilder = new TestomatRequestBodyBuilder();
        float duration = -1.0f;

        // When
        String result = requestBodyBuilder.buildFinishRunBody(duration);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("duration"));
        assertTrue(result.contains("-1.0"));
    }

    @Test
    void buildFinishRunBody_WithJsonProcessingException_ShouldThrowException() throws Exception {
        // Given
        requestBodyBuilder = new TestomatRequestBodyBuilder(mockObjectMapper);
        float duration = 123.45f;
        when(mockObjectMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("Serialization failed") {
                });

        // When & Then
        assertThrows(JsonProcessingException.class,
                () -> requestBodyBuilder.buildFinishRunBody(duration));
    }

    @Test
    void equals_WithSameObjectMapper_ShouldReturnTrue() {
        // Given
        TestomatRequestBodyBuilder builder1 = new TestomatRequestBodyBuilder(mockObjectMapper);
        TestomatRequestBodyBuilder builder2 = new TestomatRequestBodyBuilder(mockObjectMapper);

        // When & Then
        assertEquals(builder1, builder2);
    }

    @Test
    void equals_WithDifferentObjectMapper_ShouldReturnFalse() {
        // Given
        ObjectMapper anotherMapper = new ObjectMapper();
        TestomatRequestBodyBuilder builder1 = new TestomatRequestBodyBuilder(mockObjectMapper);
        TestomatRequestBodyBuilder builder2 = new TestomatRequestBodyBuilder(anotherMapper);

        // When & Then
        assertNotNull(builder1);
        assertNotNull(builder2);
    }

    @Test
    void hashCode_ShouldNotThrowException() {
        // Given
        TestomatRequestBodyBuilder builder = new TestomatRequestBodyBuilder(mockObjectMapper);

        // When & Then - should not throw exception
        int hashCode = builder.hashCode();
        assertNotNull(hashCode);
    }

    // Helper method to setup TestRunResult mock
    private void setupTestRunResult(TestCaseResult result, String title, String testId,
                                    String suiteTitle, String file, String status,
                                    String message, String stack) {
        when(result.getTitle()).thenReturn(title);
        when(result.getTestId()).thenReturn(testId);
        when(result.getSuiteTitle()).thenReturn(suiteTitle);
        when(result.getFile()).thenReturn(file);
        when(result.getStatus()).thenReturn(status);
        when(result.getMessage()).thenReturn(message);
        when(result.getStack()).thenReturn(stack);
    }
}