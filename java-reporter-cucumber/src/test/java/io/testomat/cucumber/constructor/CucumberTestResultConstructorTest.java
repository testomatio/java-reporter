package io.testomat.cucumber.constructor;

import static io.testomat.core.constants.CommonConstants.FAILED;
import static io.testomat.core.constants.CommonConstants.PASSED;
import static io.testomat.core.constants.CommonConstants.SKIPPED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.TestCaseFinished;
import io.testomat.core.model.TestMetadata;
import io.testomat.core.model.TestResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@DisplayName("CucumberTestResultConstructor Tests")
class CucumberTestResultConstructorTest {

    private CucumberTestResultConstructor constructor;

    @Mock
    private TestResultWrapper mockWrapper;

    @Mock
    private TestMetadata mockMetadata;

    @Mock
    private TestCaseFinished mockEvent;

    @Mock
    private Result mockResult;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        constructor = new CucumberTestResultConstructor();

        when(mockMetadata.getTitle()).thenReturn("Test Method");
        when(mockMetadata.getTestId()).thenReturn("test-123");
        when(mockMetadata.getSuiteTitle()).thenReturn("TestSuite");
        when(mockMetadata.getFile()).thenReturn("TestSuite.feature");

        when(mockWrapper.getTestMetadata()).thenReturn(mockMetadata);
        when(mockWrapper.getStatus()).thenReturn(PASSED);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mockitoCloseable != null) {
            mockitoCloseable.close();
        }
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when wrapper is null")
    void shouldThrowExceptionWhenWrapperIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> constructor.constructTestRunResult(null)
        );

        assertEquals("TestRunResultWrapper cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when metadata is null")
    void shouldThrowExceptionWhenMetadataIsNull() {
        // Given
        when(mockWrapper.getTestMetadata()).thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> constructor.constructTestRunResult(mockWrapper)
        );

        assertEquals("TestMetadata cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should create TestResult with exception details when cucumber event has error")
    void shouldCreateTestResultWithExceptionDetails() {
        // Given
        RuntimeException testException = new RuntimeException("Cucumber test failed");

        when(mockWrapper.getCucumberTestCaseFinished()).thenReturn(mockEvent);
        when(mockEvent.getResult()).thenReturn(mockResult);
        when(mockResult.getError()).thenReturn(testException);

        // When
        TestResult result = constructor.constructTestRunResult(mockWrapper);

        // Then
        assertNotNull(result);
        assertEquals("Test Method", result.getTitle());
        assertEquals("test-123", result.getTestId());
        assertEquals("TestSuite", result.getSuiteTitle());
        assertEquals("TestSuite.feature", result.getFile());
        assertEquals(PASSED, result.getStatus());
        assertEquals("Cucumber test failed", result.getMessage());
        assertNotNull(result.getStack());
        assertTrue(result.getStack().contains("RuntimeException"));
        assertTrue(result.getStack().contains("Cucumber test failed"));
    }

    @Test
    @DisplayName("Should create TestResult with empty exception details when no error in result")
    void shouldCreateTestResultWithEmptyExceptionDetailsWhenNoError() {
        // Given
        when(mockWrapper.getCucumberTestCaseFinished()).thenReturn(mockEvent);
        when(mockEvent.getResult()).thenReturn(mockResult);
        when(mockResult.getError()).thenReturn(null);

        // When
        TestResult result = constructor.constructTestRunResult(mockWrapper);

        // Then
        assertNotNull(result);
        assertEquals("Test Method", result.getTitle());
        assertEquals("test-123", result.getTestId());
        assertEquals("TestSuite", result.getSuiteTitle());
        assertEquals("TestSuite.feature", result.getFile());
        assertEquals(PASSED, result.getStatus());
        assertNull(result.getMessage());
        assertNull(result.getStack());
    }

    @Test
    @DisplayName("Should create TestResult with empty exception details when no cucumber event")
    void shouldCreateTestResultWithEmptyExceptionDetailsWhenNoEvent() {
        // Given
        when(mockWrapper.getCucumberTestCaseFinished()).thenReturn(null);

        // When
        TestResult result = constructor.constructTestRunResult(mockWrapper);

        // Then
        assertNotNull(result);
        assertEquals("Test Method", result.getTitle());
        assertEquals("test-123", result.getTestId());
        assertEquals("TestSuite", result.getSuiteTitle());
        assertEquals("TestSuite.feature", result.getFile());
        assertEquals(PASSED, result.getStatus());
        assertNull(result.getMessage());
        assertNull(result.getStack());
    }

    @Test
    @DisplayName("Should handle failed test status correctly")
    void shouldHandleFailedTestStatus() {
        // Given
        AssertionError assertionError = new AssertionError("Expected <true> but was <false>");

        when(mockWrapper.getStatus()).thenReturn(FAILED);
        when(mockWrapper.getCucumberTestCaseFinished()).thenReturn(mockEvent);
        when(mockEvent.getResult()).thenReturn(mockResult);
        when(mockResult.getError()).thenReturn(assertionError);

        // When
        TestResult result = constructor.constructTestRunResult(mockWrapper);

        // Then
        assertNotNull(result);
        assertEquals(FAILED, result.getStatus());
        assertEquals("Expected <true> but was <false>", result.getMessage());
        assertNotNull(result.getStack());
        assertTrue(result.getStack().contains("AssertionError"));
    }

    @Test
    @DisplayName("Should handle skipped test status correctly")
    void shouldHandleSkippedTestStatus() {
        // Given
        when(mockWrapper.getStatus()).thenReturn(SKIPPED);
        when(mockWrapper.getCucumberTestCaseFinished()).thenReturn(null);

        // When
        TestResult result = constructor.constructTestRunResult(mockWrapper);

        // Then
        assertNotNull(result);
        assertEquals(SKIPPED, result.getStatus());
        assertNull(result.getMessage());
        assertNull(result.getStack());
    }

    @Test
    @DisplayName("Should handle metadata with null testId correctly")
    void shouldHandleMetadataWithNullTestId() {
        // Given
        when(mockMetadata.getTestId()).thenReturn(null);
        when(mockWrapper.getCucumberTestCaseFinished()).thenReturn(null);

        // When
        TestResult result = constructor.constructTestRunResult(mockWrapper);

        // Then
        assertNotNull(result);
        assertNull(result.getTestId());
        assertEquals("Test Method", result.getTitle());
        assertEquals("TestSuite", result.getSuiteTitle());
    }

    @Test
    @DisplayName("Should preserve all metadata fields in result")
    void shouldPreserveAllMetadataFields() {
        // Given
        when(mockMetadata.getTitle()).thenReturn("Custom Cucumber Test");
        when(mockMetadata.getTestId()).thenReturn("custom-id-456");
        when(mockMetadata.getSuiteTitle()).thenReturn("CustomFeature");
        when(mockMetadata.getFile()).thenReturn("custom.feature");
        when(mockWrapper.getCucumberTestCaseFinished()).thenReturn(null);

        // When
        TestResult result = constructor.constructTestRunResult(mockWrapper);

        // Then
        assertNotNull(result);
        assertEquals("Custom Cucumber Test", result.getTitle());
        assertEquals("custom-id-456", result.getTestId());
        assertEquals("CustomFeature", result.getSuiteTitle());
        assertEquals("custom.feature", result.getFile());
        assertEquals(PASSED, result.getStatus());
    }

    @Test
    @DisplayName("Should handle nested exceptions correctly")
    void shouldHandleNestedExceptions() {
        // Given
        RuntimeException cause = new RuntimeException("Root cause");
        RuntimeException wrapper = new RuntimeException("Wrapper exception", cause);

        when(mockWrapper.getCucumberTestCaseFinished()).thenReturn(mockEvent);
        when(mockEvent.getResult()).thenReturn(mockResult);
        when(mockResult.getError()).thenReturn(wrapper);

        // When
        TestResult result = constructor.constructTestRunResult(mockWrapper);

        // Then
        assertNotNull(result);
        assertEquals("Wrapper exception", result.getMessage());
        assertNotNull(result.getStack());
        assertTrue(result.getStack().contains("Wrapper exception"));
        assertTrue(result.getStack().contains("Root cause"));
        assertTrue(result.getStack().contains("Caused by:"));
    }

    @Test
    @DisplayName("Should handle exception without message")
    void shouldHandleExceptionWithoutMessage() {
        // Given
        RuntimeException exceptionWithoutMessage = new RuntimeException();

        when(mockWrapper.getCucumberTestCaseFinished()).thenReturn(mockEvent);
        when(mockEvent.getResult()).thenReturn(mockResult);
        when(mockResult.getError()).thenReturn(exceptionWithoutMessage);

        // When
        TestResult result = constructor.constructTestRunResult(mockWrapper);

        // Then
        assertNotNull(result);
        assertNull(result.getMessage());
        assertNotNull(result.getStack());
        assertTrue(result.getStack().contains("RuntimeException"));
    }

    @Test
    @DisplayName("Should handle different exception types correctly")
    void shouldHandleDifferentExceptionTypes() {
        // Given
        NullPointerException npe = new NullPointerException("Null pointer error");

        when(mockWrapper.getCucumberTestCaseFinished()).thenReturn(mockEvent);
        when(mockEvent.getResult()).thenReturn(mockResult);
        when(mockResult.getError()).thenReturn(npe);

        // When
        TestResult result = constructor.constructTestRunResult(mockWrapper);

        // Then
        assertNotNull(result);
        assertEquals("Null pointer error", result.getMessage());
        assertNotNull(result.getStack());
        assertTrue(result.getStack().contains("NullPointerException"));
        assertTrue(result.getStack().contains("Null pointer error"));
    }

    @Test
    @DisplayName("Should create stack trace with proper format")
    void shouldCreateStackTraceWithProperFormat() {
        // Given
        IllegalStateException exception = new IllegalStateException("Invalid state");

        when(mockWrapper.getCucumberTestCaseFinished()).thenReturn(mockEvent);
        when(mockEvent.getResult()).thenReturn(mockResult);
        when(mockResult.getError()).thenReturn(exception);

        // When
        TestResult result = constructor.constructTestRunResult(mockWrapper);

        // Then
        assertNotNull(result);
        assertNotNull(result.getStack());

        String stack = result.getStack();
        assertTrue(stack.contains("IllegalStateException: Invalid state"));
        assertTrue(stack.contains("at "));
        // Stack trace should contain method calls
        assertTrue(stack.split("\n").length > 1);
    }
}