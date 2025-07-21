package io.testomat.junit.constructor;

import static io.testomat.core.constants.CommonConstants.FAILED;
import static io.testomat.core.constants.CommonConstants.PASSED;
import static io.testomat.core.constants.CommonConstants.SKIPPED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.testomat.core.model.TestMetadata;
import io.testomat.core.model.TestResult;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.opentest4j.TestAbortedException;

/**
 * Comprehensive unit tests for JUnitTestResultConstructor class.
 * Tests the public constructTestRunResult method with various scenarios.
 */
@DisplayName("JUnitTestResultConstructor Tests")
class JUnitTestResultConstructorTest {

    private JUnitTestResultConstructor constructor;
    private TestMetadata testMetadata;
    private ExtensionContext mockContext;

    @BeforeEach
    void setUp() {
        constructor = new JUnitTestResultConstructor();
        testMetadata = new TestMetadata(
                "Test Method Name",
                "TEST-001",
                "Test Suite Name",
                "TestClass.java"
        );
        mockContext = mock(ExtensionContext.class);
    }

    @Nested
    @DisplayName("Custom Message Scenarios")
    class CustomMessageTests {

        @Test
        @DisplayName("Should create TestResult with custom message when message is provided")
        void shouldCreateTestResultWithCustomMessage() {
            // Given
            String customMessage = "Custom test message";
            String status = PASSED;

            // When
            TestResult result = constructor.constructTestRunResult(
                    testMetadata, customMessage, status, mockContext);

            // Then
            assertNotNull(result);
            assertEquals(testMetadata.getTitle(), result.getTitle());
            assertEquals(testMetadata.getTestId(), result.getTestId());
            assertEquals(testMetadata.getSuiteTitle(), result.getSuiteTitle());
            assertEquals(testMetadata.getFile(), result.getFile());
            assertEquals(customMessage, result.getMessage());
            assertEquals(status, result.getStatus());
        }

        @Test
        @DisplayName("Should use custom message and extract stack trace when context has exception")
        void shouldUseCustomMessageAndExtractStackTrace() {
            // Given
            String customMessage = "Custom failure message";
            String status = FAILED;
            RuntimeException exception = new RuntimeException("Original exception");
            when(mockContext.getExecutionException()).thenReturn(Optional.of(exception));

            // When
            TestResult result = constructor.constructTestRunResult(
                    testMetadata, customMessage, status, mockContext);

            // Then
            assertEquals(customMessage, result.getMessage());
            assertEquals(status, result.getStatus());
            assertNotNull(result.getStack());
            assertTrue(result.getStack().contains("RuntimeException"));
            assertTrue(result.getStack().contains("Original exception"));
        }

        @Test
        @DisplayName("Should use custom message with null stack when context has no exception")
        void shouldUseCustomMessageWithNullStackWhenNoException() {
            // Given
            String customMessage = "Custom message";
            String status = PASSED;
            when(mockContext.getExecutionException()).thenReturn(Optional.empty());

            // When
            TestResult result = constructor.constructTestRunResult(
                    testMetadata, customMessage, status, mockContext);

            // Then
            assertEquals(customMessage, result.getMessage());
            assertEquals(status, result.getStatus());
            assertNull(result.getStack());
        }

        @Test
        @DisplayName("Should use custom message with null stack when context is null")
        void shouldUseCustomMessageWithNullStackWhenContextIsNull() {
            // Given
            String customMessage = "Custom message";
            String status = PASSED;

            // When
            TestResult result = constructor.constructTestRunResult(
                    testMetadata, customMessage, status, null);

            // Then
            assertEquals(customMessage, result.getMessage());
            assertEquals(status, result.getStatus());
            assertNull(result.getStack());
        }

        @Test
        @DisplayName("Should filter TestAbortedException when using custom message")
        void shouldFilterTestAbortedExceptionWhenUsingCustomMessage() {
            // Given
            String customMessage = "Custom message";
            String status = SKIPPED;
            TestAbortedException abortedException = new TestAbortedException("Test aborted");
            when(mockContext.getExecutionException()).thenReturn(Optional.of(abortedException));

            // When
            TestResult result = constructor.constructTestRunResult(
                    testMetadata, customMessage, status, mockContext);

            // Then
            assertEquals(customMessage, result.getMessage());
            assertEquals(status, result.getStatus());
            assertNull(result.getStack());
        }
    }

    @Nested
    @DisplayName("Exception Details Extraction Scenarios")
    class ExceptionDetailsTests {

        @Test
        @DisplayName("Should extract exception message and stack when message is null")
        void shouldExtractExceptionDetailsWhenMessageIsNull() {
            // Given
            String status = FAILED;
            RuntimeException exception = new RuntimeException("Test failed with exception");
            when(mockContext.getExecutionException()).thenReturn(Optional.of(exception));

            // When
            TestResult result = constructor.constructTestRunResult(
                    testMetadata, null, status, mockContext);

            // Then
            assertEquals("Test failed with exception", result.getMessage());
            assertEquals(status, result.getStatus());
            assertNotNull(result.getStack());
            assertTrue(result.getStack().contains("RuntimeException"));
            assertTrue(result.getStack().contains("Test failed with exception"));
        }

        @Test
        @DisplayName("Should handle exception with null message")
        void shouldHandleExceptionWithNullMessage() {
            // Given
            String status = FAILED;
            RuntimeException exception = new RuntimeException((String) null);
            when(mockContext.getExecutionException()).thenReturn(Optional.of(exception));

            // When
            TestResult result = constructor.constructTestRunResult(
                    testMetadata, null, status, mockContext);

            // Then
            assertNull(result.getMessage());
            assertEquals(status, result.getStatus());
            assertNotNull(result.getStack());
            assertTrue(result.getStack().contains("RuntimeException"));
        }

        @Test
        @DisplayName("Should handle exception with empty message")
        void shouldHandleExceptionWithEmptyMessage() {
            // Given
            String status = FAILED;
            RuntimeException exception = new RuntimeException("");
            when(mockContext.getExecutionException()).thenReturn(Optional.of(exception));

            // When
            TestResult result = constructor.constructTestRunResult(
                    testMetadata, null, status, mockContext);

            // Then
            assertEquals("", result.getMessage());
            assertEquals(status, result.getStatus());
            assertNotNull(result.getStack());
        }

        @Test
        @DisplayName("Should filter TestAbortedException and return empty details")
        void shouldFilterTestAbortedExceptionAndReturnEmptyDetails() {
            // Given
            String status = SKIPPED;
            TestAbortedException abortedException = new TestAbortedException("Test was aborted");
            when(mockContext.getExecutionException()).thenReturn(Optional.of(abortedException));

            // When
            TestResult result = constructor.constructTestRunResult(
                    testMetadata, null, status, mockContext);

            // Then
            assertNull(result.getMessage());
            assertEquals(status, result.getStatus());
            assertNull(result.getStack());
        }

        @Test
        @DisplayName("Should return empty details when context has no exception")
        void shouldReturnEmptyDetailsWhenContextHasNoException() {
            // Given
            String status = PASSED;
            when(mockContext.getExecutionException()).thenReturn(Optional.empty());

            // When
            TestResult result = constructor.constructTestRunResult(
                    testMetadata, null, status, mockContext);

            // Then
            assertNull(result.getMessage());
            assertEquals(status, result.getStatus());
            assertNull(result.getStack());
        }

        @Test
        @DisplayName("Should return empty details when context is null")
        void shouldReturnEmptyDetailsWhenContextIsNull() {
            // Given
            String status = PASSED;

            // When
            TestResult result = constructor.constructTestRunResult(
                    testMetadata, null, status, null);

            // Then
            assertNull(result.getMessage());
            assertEquals(status, result.getStatus());
            assertNull(result.getStack());
        }
    }

    @Nested
    @DisplayName("Different Exception Types")
    class DifferentExceptionTypesTests {

        @Test
        @DisplayName("Should handle AssertionError correctly")
        void shouldHandleAssertionError() {
            // Given
            String status = FAILED;
            AssertionError assertionError = new AssertionError("Assertion failed: expected <true> but was <false>");
            when(mockContext.getExecutionException()).thenReturn(Optional.of(assertionError));

            // When
            TestResult result = constructor.constructTestRunResult(
                    testMetadata, null, status, mockContext);

            // Then
            assertEquals("Assertion failed: expected <true> but was <false>", result.getMessage());
            assertNotNull(result.getStack());
            assertTrue(result.getStack().contains("AssertionError"));
        }

        @Test
        @DisplayName("Should handle IllegalArgumentException correctly")
        void shouldHandleIllegalArgumentException() {
            // Given
            String status = FAILED;
            IllegalArgumentException exception = new IllegalArgumentException("Invalid argument provided");
            when(mockContext.getExecutionException()).thenReturn(Optional.of(exception));

            // When
            TestResult result = constructor.constructTestRunResult(
                    testMetadata, null, status, mockContext);

            // Then
            assertEquals("Invalid argument provided", result.getMessage());
            assertNotNull(result.getStack());
            assertTrue(result.getStack().contains("IllegalArgumentException"));
        }

        @Test
        @DisplayName("Should handle nested exceptions correctly")
        void shouldHandleNestedExceptions() {
            // Given
            String status = FAILED;
            RuntimeException rootCause = new RuntimeException("Root cause");
            IllegalStateException wrappedException = new IllegalStateException("Wrapper exception", rootCause);
            when(mockContext.getExecutionException()).thenReturn(Optional.of(wrappedException));

            // When
            TestResult result = constructor.constructTestRunResult(
                    testMetadata, null, status, mockContext);

            // Then
            assertEquals("Wrapper exception", result.getMessage());
            assertNotNull(result.getStack());
            assertTrue(result.getStack().contains("IllegalStateException"));
            assertTrue(result.getStack().contains("Wrapper exception"));
            assertTrue(result.getStack().contains("Caused by"));
            assertTrue(result.getStack().contains("Root cause"));
        }

        @Test
        @DisplayName("Should handle custom exceptions correctly")
        void shouldHandleCustomExceptions() {
            // Given
            String status = FAILED;
            CustomTestException customException = new CustomTestException("Custom test failure");
            when(mockContext.getExecutionException()).thenReturn(Optional.of(customException));

            // When
            TestResult result = constructor.constructTestRunResult(
                    testMetadata, null, status, mockContext);

            // Then
            assertEquals("Custom test failure", result.getMessage());
            assertNotNull(result.getStack());
            assertTrue(result.getStack().contains("CustomTestException"));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null metadata gracefully")
        void shouldHandleNullMetadata() {
            // Given
            String customMessage = "Test message";
            String status = PASSED;

            assertThrows(IllegalArgumentException.class,
                    () -> constructor.constructTestRunResult(null, customMessage, status, mockContext));
        }

        @Test
        @DisplayName("Should handle null status")
        void shouldHandleNullStatus() {
            // Given
            String customMessage = "Test message";

            // When
            TestResult result = constructor.constructTestRunResult(
                    testMetadata, customMessage, null, mockContext);

            // Then
            assertNotNull(result);
            assertEquals(customMessage, result.getMessage());
            assertNull(result.getStatus());
        }

        @Test
        @DisplayName("Should handle very long exception messages")
        void shouldHandleVeryLongExceptionMessages() {
            // Given
            String status = FAILED;
            String longMessage = "A".repeat(10000);
            RuntimeException exception = new RuntimeException(longMessage);
            when(mockContext.getExecutionException()).thenReturn(Optional.of(exception));

            // When
            TestResult result = constructor.constructTestRunResult(
                    testMetadata, null, status, mockContext);

            // Then
            assertEquals(longMessage, result.getMessage());
            assertNotNull(result.getStack());
        }

        @Test
        @DisplayName("Should handle special characters in exception messages")
        void shouldHandleSpecialCharactersInExceptionMessages() {
            // Given
            String status = FAILED;
            String specialMessage = "Test failed: \n\t\r\"Special chars: áéíóú ñ üöä 中文 🚀\"";
            RuntimeException exception = new RuntimeException(specialMessage);
            when(mockContext.getExecutionException()).thenReturn(Optional.of(exception));

            // When
            TestResult result = constructor.constructTestRunResult(
                    testMetadata, null, status, mockContext);

            // Then
            assertEquals(specialMessage, result.getMessage());
            assertNotNull(result.getStack());
            assertTrue(result.getStack().contains(specialMessage));
        }

        @Test
        @DisplayName("Should preserve metadata fields correctly")
        void shouldPreserveMetadataFieldsCorrectly() {
            // Given
            TestMetadata customMetadata = new TestMetadata(
                    "Custom Test Title",
                    "CUSTOM-TEST-ID-123",
                    "Custom Suite Title",
                    "com/example/CustomTestClass.java"
            );
            String customMessage = "Custom message";
            String status = PASSED;

            // When
            TestResult result = constructor.constructTestRunResult(
                    customMetadata, customMessage, status, mockContext);

            // Then
            assertEquals("Custom Test Title", result.getTitle());
            assertEquals("CUSTOM-TEST-ID-123", result.getTestId());
            assertEquals("Custom Suite Title", result.getSuiteTitle());
            assertEquals("com/example/CustomTestClass.java", result.getFile());
            assertEquals(customMessage, result.getMessage());
            assertEquals(status, result.getStatus());
        }

        @Test
        @DisplayName("Should handle metadata with null fields")
        void shouldHandleMetadataWithNullFields() {
            // Given
            TestMetadata nullFieldsMetadata = new TestMetadata(null, null, null, null);
            String customMessage = "Test message";
            String status = PASSED;

            // When
            TestResult result = constructor.constructTestRunResult(
                    nullFieldsMetadata, customMessage, status, mockContext);

            // Then
            assertNotNull(result);
            assertNull(result.getTitle());
            assertNull(result.getTestId());
            assertNull(result.getSuiteTitle());
            assertNull(result.getFile());
            assertEquals(customMessage, result.getMessage());
            assertEquals(status, result.getStatus());
        }
    }

    @Nested
    @DisplayName("Stack Trace Handling Tests")
    class StackTraceTests {

        @Test
        @DisplayName("Should generate complete stack trace")
        void shouldGenerateCompleteStackTrace() {
            // Given
            String status = FAILED;
            RuntimeException exception = new RuntimeException("Test exception");
            when(mockContext.getExecutionException()).thenReturn(Optional.of(exception));

            // When
            TestResult result = constructor.constructTestRunResult(
                    testMetadata, null, status, mockContext);

            // Then
            assertNotNull(result.getStack());
            assertTrue(result.getStack().contains("java.lang.RuntimeException: Test exception"));
            assertTrue(result.getStack().contains("at "));
            assertTrue(result.getStack().contains(this.getClass().getName()));
        }

        @Test
        @DisplayName("Should include method names in stack trace")
        void shouldIncludeMethodNamesInStackTrace() {
            // Given
            RuntimeException exception = createExceptionInNestedMethod();
            when(mockContext.getExecutionException()).thenReturn(Optional.of(exception));

            // When
            TestResult result = constructor.constructTestRunResult(
                    testMetadata, null, FAILED, mockContext);

            // Then
            assertNotNull(result.getStack());
            assertTrue(result.getStack().contains("createExceptionInNestedMethod"));
        }

        private RuntimeException createExceptionInNestedMethod() {
            return new RuntimeException("Exception from nested method");
        }
    }

    /**
     * Custom exception class for testing
     */
    private static class CustomTestException extends Exception {
        public CustomTestException(String message) {
            super(message);
        }
    }
}