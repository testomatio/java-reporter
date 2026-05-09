package io.reporter.testng.constructor;

import static io.testomat.core.constants.CommonConstants.FAILED;
import static io.testomat.core.constants.CommonConstants.PASSED;
import static io.testomat.core.constants.CommonConstants.SKIPPED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import io.testomat.core.model.Link;
import io.testomat.core.model.TestMetadata;
import io.testomat.core.model.TestResult;
import io.testomat.core.step.StepStorage;
import io.testomat.core.step.TestStep;
import io.testomat.testng.constructor.TestNgTestResultConstructor;
import io.testomat.testng.constructor.TestResultWrapper;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.ITestResult;

class TestNgTestResultConstructorTest {

    private TestNgTestResultConstructor constructor;

    @Mock
    private TestResultWrapper mockWrapper;

    @Mock
    private TestMetadata mockMetadata;

    @Mock
    private ITestResult mockTestResult;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        constructor = new TestNgTestResultConstructor();

        when(mockMetadata.getTitle()).thenReturn("Test Method");
        when(mockMetadata.getTestId()).thenReturn("test-123");
        when(mockMetadata.getSuiteTitle()).thenReturn("TestSuite");
        when(mockMetadata.getFile()).thenReturn("TestSuite.java");

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
    @DisplayName("Should create TestResult with reason when reason is provided")
    void shouldCreateTestResultWithReason() {
        // Given
        String reason = "Test was skipped due to configuration";
        when(mockWrapper.getReason()).thenReturn(reason);
        when(mockWrapper.getMessage()).thenReturn(null);

        // When
        TestResult result = constructor.constructTestRunResult(mockWrapper);

        // Then
        assertNotNull(result);
        assertEquals("Test Method", result.getTitle());
        assertEquals("test-123", result.getTestId());
        assertEquals("TestSuite", result.getSuiteTitle());
        assertEquals("TestSuite.java", result.getFile());
        assertEquals(PASSED, result.getStatus());
        assertEquals(reason, result.getMessage());
        assertNull(result.getStack()); // Custom message doesn't include stack
    }

    @Test
    @DisplayName("Should create TestResult with message when message is provided")
    void shouldCreateTestResultWithMessage() {
        // Given
        String message = "Custom test message";
        when(mockWrapper.getReason()).thenReturn(null);
        when(mockWrapper.getMessage()).thenReturn(message);

        // When
        TestResult result = constructor.constructTestRunResult(mockWrapper);

        // Then
        assertNotNull(result);
        assertEquals(message, result.getMessage());
        assertNull(result.getStack()); // Custom message doesn't include stack
    }

    @Test
    @DisplayName("Should prioritize reason over message when both are provided")
    void shouldPrioritizeReasonOverMessage() {
        // Given
        String reason = "Test reason";
        String message = "Test message";
        when(mockWrapper.getReason()).thenReturn(reason);
        when(mockWrapper.getMessage()).thenReturn(message);

        // When
        TestResult result = constructor.constructTestRunResult(mockWrapper);

        // Then
        assertNotNull(result);
        assertEquals(reason, result.getMessage()); // Reason should take priority
        assertNull(result.getStack());
    }

    @Test
    @DisplayName("Should create TestResult with exception details when no custom message")
    void shouldCreateTestResultWithExceptionDetails() {
        // Given
        RuntimeException testException = new RuntimeException("Test failed");

        when(mockWrapper.getReason()).thenReturn(null);
        when(mockWrapper.getMessage()).thenReturn(null);
        when(mockWrapper.getTestResult()).thenReturn(mockTestResult);
        when(mockTestResult.getThrowable()).thenReturn(testException);

        // When
        TestResult result = constructor.constructTestRunResult(mockWrapper);

        // Then
        assertNotNull(result);
        assertEquals("Test failed", result.getMessage());
        assertNotNull(result.getStack());
        assertTrue(result.getStack().contains("RuntimeException"));
        assertTrue(result.getStack().contains("Test failed"));
    }

    @Test
    @DisplayName("Should create TestResult with empty exception details when no exception in test result")
    void shouldCreateTestResultWithEmptyExceptionDetailsWhenNoException() {
        // Given
        when(mockWrapper.getReason()).thenReturn(null);
        when(mockWrapper.getMessage()).thenReturn(null);
        when(mockWrapper.getTestResult()).thenReturn(mockTestResult);
        when(mockTestResult.getThrowable()).thenReturn(null);

        // When
        TestResult result = constructor.constructTestRunResult(mockWrapper);

        // Then
        assertNotNull(result);
        assertNull(result.getMessage());
        assertNull(result.getStack());
    }

    @Test
    @DisplayName("Should create TestResult with empty exception details when no test result")
    void shouldCreateTestResultWithEmptyExceptionDetailsWhenNoTestResult() {
        // Given
        when(mockWrapper.getReason()).thenReturn(null);
        when(mockWrapper.getMessage()).thenReturn(null);
        when(mockWrapper.getTestResult()).thenReturn(null);

        // When
        TestResult result = constructor.constructTestRunResult(mockWrapper);

        // Then
        assertNotNull(result);
        assertEquals("Test Method", result.getTitle());
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
        when(mockWrapper.getReason()).thenReturn(null);
        when(mockWrapper.getMessage()).thenReturn(null);
        when(mockWrapper.getTestResult()).thenReturn(mockTestResult);
        when(mockTestResult.getThrowable()).thenReturn(assertionError);

        // When
        TestResult result = constructor.constructTestRunResult(mockWrapper);

        // Then
        assertNotNull(result);
        assertEquals(FAILED, result.getStatus());
        assertEquals("Expected <true> but was <false>", result.getMessage());
        assertNotNull(result.getStack());
    }

    @Test
    @DisplayName("Should handle skipped test status correctly")
    void shouldHandleSkippedTestStatus() {
        // Given
        when(mockWrapper.getStatus()).thenReturn(SKIPPED);
        when(mockWrapper.getReason()).thenReturn("Test dependencies not met");

        // When
        TestResult result = constructor.constructTestRunResult(mockWrapper);

        // Then
        assertNotNull(result);
        assertEquals(SKIPPED, result.getStatus());
        assertEquals("Test dependencies not met", result.getMessage());
    }

    @Test
    @DisplayName("Should handle metadata with null testId correctly")
    void shouldHandleMetadataWithNullTestId() {
        // Given
        when(mockMetadata.getTestId()).thenReturn(null);
        when(mockWrapper.getMessage()).thenReturn("Test message");

        // When
        TestResult result = constructor.constructTestRunResult(mockWrapper);

        // Then
        assertNotNull(result);
        assertNull(result.getTestId());
        assertEquals("Test Method", result.getTitle());
        assertEquals("Test message", result.getMessage());
    }

    @Test
    @DisplayName("Should preserve all metadata fields in result")
    void shouldPreserveAllMetadataFields() {
        // Given
        when(mockMetadata.getTitle()).thenReturn("Custom Test Title");
        when(mockMetadata.getTestId()).thenReturn("custom-id-456");
        when(mockMetadata.getSuiteTitle()).thenReturn("CustomSuite");
        when(mockMetadata.getFile()).thenReturn("CustomSuite.java");
        when(mockWrapper.getMessage()).thenReturn("Custom message");

        // When
        TestResult result = constructor.constructTestRunResult(mockWrapper);

        // Then
        assertNotNull(result);
        assertEquals("Custom Test Title", result.getTitle());
        assertEquals("custom-id-456", result.getTestId());
        assertEquals("CustomSuite", result.getSuiteTitle());
        assertEquals("CustomSuite.java", result.getFile());
        assertEquals(PASSED, result.getStatus());
        assertEquals("Custom message", result.getMessage());
    }

    @Test
    @DisplayName("Should handle nested exceptions correctly")
    void shouldHandleNestedExceptions() {
        // Given
        RuntimeException cause = new RuntimeException("Root cause");
        RuntimeException wrapper = new RuntimeException("Wrapper exception", cause);

        when(mockWrapper.getReason()).thenReturn(null);
        when(mockWrapper.getMessage()).thenReturn(null);
        when(mockWrapper.getTestResult()).thenReturn(mockTestResult);
        when(mockTestResult.getThrowable()).thenReturn(wrapper);

        // When
        TestResult result = constructor.constructTestRunResult(mockWrapper);

        // Then
        assertNotNull(result);
        assertEquals("Wrapper exception", result.getMessage());
        assertNotNull(result.getStack());
        assertTrue(result.getStack().contains("Wrapper exception"));
        assertTrue(result.getStack().contains("Root cause"));
    }

    @Test
    @DisplayName("Should handle exception without message")
    void shouldHandleExceptionWithoutMessage() {
        // Given
        RuntimeException exceptionWithoutMessage = new RuntimeException();

        when(mockWrapper.getReason()).thenReturn(null);
        when(mockWrapper.getMessage()).thenReturn(null);
        when(mockWrapper.getTestResult()).thenReturn(mockTestResult);
        when(mockTestResult.getThrowable()).thenReturn(exceptionWithoutMessage);

        // When
        TestResult result = constructor.constructTestRunResult(mockWrapper);

        // Then
        assertNotNull(result);
        assertNull(result.getMessage());
        assertNotNull(result.getStack());
        assertTrue(result.getStack().contains("RuntimeException"));
    }

    @Test
    @DisplayName("getStackTrace should convert throwable to string")
    void getStackTraceShouldConvertThrowableToString() {
        // Given
        RuntimeException testException = new RuntimeException("Test exception");

        // When
        String stackTrace = constructor.getStackTrace(testException);

        // Then
        assertNotNull(stackTrace);
        assertTrue(stackTrace.contains("RuntimeException"));
        assertTrue(stackTrace.contains("Test exception"));
        assertTrue(stackTrace.contains("at "));
    }

    @Test
    @DisplayName("getStackTrace should handle nested exceptions")
    void getStackTraceShouldHandleNestedException() {
        // Given
        RuntimeException cause = new RuntimeException("Original cause");
        IllegalStateException wrapper = new IllegalStateException("Wrapper", cause);

        // When
        String stackTrace = constructor.getStackTrace(wrapper);

        // Then
        assertNotNull(stackTrace);
        assertTrue(stackTrace.contains("IllegalStateException"));
        assertTrue(stackTrace.contains("Wrapper"));
        assertTrue(stackTrace.contains("Caused by:"));
        assertTrue(stackTrace.contains("RuntimeException"));
        assertTrue(stackTrace.contains("Original cause"));
    }

    @Test
    @DisplayName("getStackTrace should handle exception without message")
    void getStackTraceShouldHandleExceptionWithoutMessage() {
        // Given
        NullPointerException npe = new NullPointerException();

        // When
        String stackTrace = constructor.getStackTrace(npe);

        // Then
        assertNotNull(stackTrace);
        assertTrue(stackTrace.contains("NullPointerException"));
        assertTrue(stackTrace.contains("at "));
    }

    @Test
    @DisplayName("Should handle empty reason correctly")
    void shouldHandleEmptyReason() {
        // Given
        when(mockWrapper.getReason()).thenReturn("");
        when(mockWrapper.getMessage()).thenReturn("Fallback message");

        // When
        TestResult result = constructor.constructTestRunResult(mockWrapper);

        // Then
        assertNotNull(result);
        assertEquals("", result.getMessage());
    }

    @Test
    @DisplayName("Should handle empty message correctly")
    void shouldHandleEmptyMessage() {
        // Given
        when(mockWrapper.getReason()).thenReturn(null);
        when(mockWrapper.getMessage()).thenReturn("");

        // When
        TestResult result = constructor.constructTestRunResult(mockWrapper);

        // Then
        assertNotNull(result);
        assertEquals("", result.getMessage());
        assertNull(result.getStack());
    }

    @Test
    @DisplayName("Should copy links from metadata")
    void shouldCopyLinksFromMetadata() {
        List<Link> links = Arrays.asList(
            Link.test("T-1"),
            Link.label("Smoke")
        );

        when(mockMetadata.getLinks()).thenReturn(links);
        when(mockWrapper.getMessage()).thenReturn("message");

        TestResult result = constructor.constructTestRunResult(mockWrapper);

        assertNotNull(result.getLinks());
        assertEquals(2, result.getLinks().size());

        assertEquals("T-1", result.getLinks().get(0).getTest());
        assertEquals("Smoke", result.getLinks().get(1).getLabel());
    }

    @Test
    @DisplayName("Should copy example data")
    void shouldCopyExampleData() {
        Object[] example = {"user", 123};

        when(mockWrapper.getExample()).thenReturn(example);
        when(mockWrapper.getMessage()).thenReturn("message");

        TestResult result = constructor.constructTestRunResult(mockWrapper);

        assertEquals(example, result.getExample());
    }

    @Test
    @DisplayName("Should copy rid")
    void shouldCopyRid() {
        when(mockWrapper.getRid()).thenReturn("rid-123");
        when(mockWrapper.getMessage()).thenReturn("message");

        TestResult result = constructor.constructTestRunResult(mockWrapper);

        assertEquals("rid-123", result.getRid());
    }

    @Test
    @DisplayName("Should collect steps from StepStorage")
    void shouldCollectSteps() {
        TestStep step = new TestStep();
        step.setStepTitle("Open page");

        StepStorage.addStep(step);

        when(mockWrapper.getMessage()).thenReturn("message");

        TestResult result = constructor.constructTestRunResult(mockWrapper);

        assertNotNull(result.getSteps());
        assertEquals(1, result.getSteps().size());
        assertEquals("Open page", result.getSteps().get(0).getStepTitle());
    }

    @Test
    @DisplayName("Should clear StepStorage after result construction")
    void shouldClearStepStorage() {
        TestStep step = new TestStep();
        step.setStepTitle("Step");

        StepStorage.addStep(step);

        when(mockWrapper.getMessage()).thenReturn("message");

        constructor.constructTestRunResult(mockWrapper);

        assertTrue(StepStorage.getSteps().isEmpty());
    }
}