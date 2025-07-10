package io.reporter.testng.constructor;

import static io.testomat.core.constants.CommonConstants.FAILED;
import static io.testomat.core.constants.CommonConstants.PASSED;
import static io.testomat.core.constants.CommonConstants.SKIPPED;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

import io.testomat.core.model.TestMetadata;
import io.testomat.core.model.TestResult;
import io.testomat.testng.constructor.TestNgTestResultConstructor;
import io.testomat.testng.constructor.TestResultWrapper;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestNgTestResultConstructorTest {

    private TestNgTestResultConstructor constructor;

    @Mock
    private TestResultWrapper mockWrapper;

    @Mock
    private TestMetadata mockMetadata;

    @Mock
    private ITestResult mockTestResult;

    private AutoCloseable mockitoCloseable;

    @BeforeMethod
    public void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        constructor = new TestNgTestResultConstructor();

        when(mockMetadata.getTitle()).thenReturn("Test Method");
        when(mockMetadata.getTestId()).thenReturn("test-123");
        when(mockMetadata.getSuiteTitle()).thenReturn("TestSuite");
        when(mockMetadata.getFile()).thenReturn("TestSuite.java");

        when(mockWrapper.getTestMetadata()).thenReturn(mockMetadata);
        when(mockWrapper.getStatus()).thenReturn(PASSED);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        if (mockitoCloseable != null) {
            mockitoCloseable.close();
        }
    }

    @Test(description = "Should throw IllegalArgumentException when wrapper is null")
    public void shouldThrowExceptionWhenWrapperIsNull() {
        // When & Then
        IllegalArgumentException exception = expectThrows(
                IllegalArgumentException.class,
                () -> constructor.constructTestRunResult(null)
        );

        assertEquals(exception.getMessage(), "TestRunResultWrapper cannot be null");
    }

    @Test(description = "Should throw IllegalArgumentException when metadata is null")
    public void shouldThrowExceptionWhenMetadataIsNull() {
        // Given
        when(mockWrapper.getTestMetadata()).thenReturn(null);

        // When & Then
        IllegalArgumentException exception = expectThrows(
                IllegalArgumentException.class,
                () -> constructor.constructTestRunResult(mockWrapper)
        );

        assertEquals(exception.getMessage(), "TestMetadata cannot be null");
    }

    @Test(description = "Should create TestResult with reason when reason is provided")
    public void shouldCreateTestResultWithReason() {
        // Given
        String reason = "Test was skipped due to configuration";
        when(mockWrapper.getReason()).thenReturn(reason);
        when(mockWrapper.getMessage()).thenReturn(null);

        // When
        TestResult result = constructor.constructTestRunResult(mockWrapper);

        // Then
        assertNotNull(result);
        assertEquals(result.getTitle(), "Test Method");
        assertEquals(result.getTestId(), "test-123");
        assertEquals(result.getSuiteTitle(), "TestSuite");
        assertEquals(result.getFile(), "TestSuite.java");
        assertEquals(result.getStatus(), PASSED);
        assertEquals(result.getMessage(), reason);
        assertNull(result.getStack()); // Custom message doesn't include stack
    }

    @Test(description = "Should create TestResult with message when message is provided")
    public void shouldCreateTestResultWithMessage() {
        // Given
        String message = "Custom test message";
        when(mockWrapper.getReason()).thenReturn(null);
        when(mockWrapper.getMessage()).thenReturn(message);

        // When
        TestResult result = constructor.constructTestRunResult(mockWrapper);

        // Then
        assertNotNull(result);
        assertEquals(result.getMessage(), message);
        assertNull(result.getStack()); // Custom message doesn't include stack
    }

    @Test(description = "Should prioritize reason over message when both are provided")
    public void shouldPrioritizeReasonOverMessage() {
        // Given
        String reason = "Test reason";
        String message = "Test message";
        when(mockWrapper.getReason()).thenReturn(reason);
        when(mockWrapper.getMessage()).thenReturn(message);

        // When
        TestResult result = constructor.constructTestRunResult(mockWrapper);

        // Then
        assertNotNull(result);
        assertEquals(result.getMessage(), reason); // Reason should take priority
        assertNull(result.getStack());
    }

    @Test(description = "Should create TestResult with exception details when no custom message")
    public void shouldCreateTestResultWithExceptionDetails() {
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
        assertEquals(result.getMessage(), "Test failed");
        assertNotNull(result.getStack());
        assertTrue(result.getStack().contains("RuntimeException"));
        assertTrue(result.getStack().contains("Test failed"));
    }

    @Test(description = "Should create TestResult with empty exception details when no exception in test result")
    public void shouldCreateTestResultWithEmptyExceptionDetailsWhenNoException() {
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

    @Test(description = "Should create TestResult with empty exception details when no test result")
    public void shouldCreateTestResultWithEmptyExceptionDetailsWhenNoTestResult() {
        // Given
        when(mockWrapper.getReason()).thenReturn(null);
        when(mockWrapper.getMessage()).thenReturn(null);
        when(mockWrapper.getTestResult()).thenReturn(null);

        // When
        TestResult result = constructor.constructTestRunResult(mockWrapper);

        // Then
        assertNotNull(result);
        assertEquals(result.getTitle(), "Test Method");
        assertEquals(result.getStatus(), PASSED);
        assertNull(result.getMessage());
        assertNull(result.getStack());
    }

    @Test(description = "Should handle failed test status correctly")
    public void shouldHandleFailedTestStatus() {
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
        assertEquals(result.getStatus(), FAILED);
        assertEquals(result.getMessage(), "Expected <true> but was <false>");
        assertNotNull(result.getStack());
    }

    @Test(description = "Should handle skipped test status correctly")
    public void shouldHandleSkippedTestStatus() {
        // Given
        when(mockWrapper.getStatus()).thenReturn(SKIPPED);
        when(mockWrapper.getReason()).thenReturn("Test dependencies not met");

        // When
        TestResult result = constructor.constructTestRunResult(mockWrapper);

        // Then
        assertNotNull(result);
        assertEquals(result.getStatus(), SKIPPED);
        assertEquals(result.getMessage(), "Test dependencies not met");
    }

    @Test(description = "Should handle metadata with null testId correctly")
    public void shouldHandleMetadataWithNullTestId() {
        // Given
        when(mockMetadata.getTestId()).thenReturn(null);
        when(mockWrapper.getMessage()).thenReturn("Test message");

        // When
        TestResult result = constructor.constructTestRunResult(mockWrapper);

        // Then
        assertNotNull(result);
        assertNull(result.getTestId());
        assertEquals(result.getTitle(), "Test Method");
        assertEquals(result.getMessage(), "Test message");
    }

    @Test(description = "Should preserve all metadata fields in result")
    public void shouldPreserveAllMetadataFields() {
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
        assertEquals(result.getTitle(), "Custom Test Title");
        assertEquals(result.getTestId(), "custom-id-456");
        assertEquals(result.getSuiteTitle(), "CustomSuite");
        assertEquals(result.getFile(), "CustomSuite.java");
        assertEquals(result.getStatus(), PASSED);
        assertEquals(result.getMessage(), "Custom message");
    }

    @Test(description = "Should handle nested exceptions correctly")
    public void shouldHandleNestedExceptions() {
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
        assertEquals(result.getMessage(), "Wrapper exception");
        assertNotNull(result.getStack());
        assertTrue(result.getStack().contains("Wrapper exception"));
        assertTrue(result.getStack().contains("Root cause"));
    }

    @Test(description = "Should handle exception without message")
    public void shouldHandleExceptionWithoutMessage() {
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

    @Test(description = "getStackTrace should convert throwable to string")
    public void getStackTraceShouldConvertThrowableToString() {
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

    @Test(description = "getStackTrace should handle nested exceptions")
    public void getStackTraceShouldHandleNestedException() {
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

    @Test(description = "getStackTrace should handle exception without message")
    public void getStackTraceShouldHandleExceptionWithoutMessage() {
        // Given
        NullPointerException npe = new NullPointerException();

        // When
        String stackTrace = constructor.getStackTrace(npe);

        // Then
        assertNotNull(stackTrace);
        assertTrue(stackTrace.contains("NullPointerException"));
        assertTrue(stackTrace.contains("at "));
    }

    @Test(description = "Should handle empty reason correctly")
    public void shouldHandleEmptyReason() {
        // Given
        when(mockWrapper.getReason()).thenReturn("");
        when(mockWrapper.getMessage()).thenReturn("Fallback message");

        // When
        TestResult result = constructor.constructTestRunResult(mockWrapper);

        // Then
        assertNotNull(result);
        assertEquals(result.getMessage(), "");
    }

    @Test(description = "Should handle empty message correctly")
    public void shouldHandleEmptyMessage() {
        // Given
        when(mockWrapper.getReason()).thenReturn(null);
        when(mockWrapper.getMessage()).thenReturn("");

        // When
        TestResult result = constructor.constructTestRunResult(mockWrapper);

        // Then
        assertNotNull(result);
        assertEquals(result.getMessage(), "");
        assertNull(result.getStack());
    }
}