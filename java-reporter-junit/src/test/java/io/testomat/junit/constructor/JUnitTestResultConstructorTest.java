//package io.testomat.junit.constructor;
//
//import static io.testomat.core.constants.CommonConstants.FAILED;
//import static io.testomat.core.constants.CommonConstants.PASSED;
//import static io.testomat.core.constants.CommonConstants.SKIPPED;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertNull;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.Mockito.when;
//
//import io.testomat.core.model.TestMetadata;
//import io.testomat.core.model.TestResult;
//import io.testomat.junit.model.TestResultWrapper;
//import java.util.Optional;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtensionContext;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.opentest4j.TestAbortedException;
//
//@DisplayName("JUnitTestResultConstructor Tests")
//class JUnitTestResultConstructorTest {
//
//    private JUnitTestResultConstructor constructor;
//
//    @Mock
//    private TestResultWrapper mockWrapper;
//
//    @Mock
//    private TestMetadata mockMetadata;
//
//    @Mock
//    private ExtensionContext mockContext;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        constructor = new JUnitTestResultConstructor();
//
//        // Default setup for metadata
//        when(mockMetadata.getTitle()).thenReturn("Test Method");
//        when(mockMetadata.getTestId()).thenReturn("test-123");
//        when(mockMetadata.getSuiteTitle()).thenReturn("TestSuite");
//        when(mockMetadata.getFile()).thenReturn("TestSuite.java");
//
//        // Default setup for wrapper
//        when(mockWrapper.getTestMetadata()).thenReturn(mockMetadata);
//        when(mockWrapper.getStatus()).thenReturn(PASSED);
//    }
//
//    @Test
//    @DisplayName("Should throw IllegalArgumentException when wrapper is null")
//    void shouldThrowExceptionWhenWrapperIsNull() {
//        // When & Then
//        IllegalArgumentException exception = assertThrows(
//                IllegalArgumentException.class,
//                () -> constructor.constructTestRunResult(null)
//        );
//
//        assertEquals("TestRunResultWrapper cannot be null", exception.getMessage());
//    }
//
//    @Test
//    @DisplayName("Should throw IllegalArgumentException when metadata is null")
//    void shouldThrowExceptionWhenMetadataIsNull() {
//        // Given
//        when(mockWrapper.getTestMetadata()).thenReturn(null);
//
//        // When & Then
//        IllegalArgumentException exception = assertThrows(
//                IllegalArgumentException.class,
//                () -> constructor.constructTestRunResult(mockWrapper)
//        );
//
//        assertEquals("TestMetadata cannot be null", exception.getMessage());
//    }
//
//    @Test
//    @DisplayName("Should create TestResult with custom message when message is provided")
//    void shouldCreateTestResultWithCustomMessage() {
//        // Given
//        String customMessage = "Custom test message";
//        when(mockWrapper.getMessage()).thenReturn(customMessage);
//        when(mockWrapper.getJunitExtensionContext()).thenReturn(mockContext);
//        when(mockContext.getExecutionException()).thenReturn(Optional.empty());
//
//        // When
//        TestResult result = constructor.constructTestRunResult(mockWrapper);
//
//        // Then
//        assertNotNull(result);
//        assertEquals("Test Method", result.getTitle());
//        assertEquals("test-123", result.getTestId());
//        assertEquals("TestSuite", result.getSuiteTitle());
//        assertEquals("TestSuite.java", result.getFile());
//        assertEquals(PASSED, result.getStatus());
//        assertEquals(customMessage, result.getMessage());
//        assertNull(result.getStack()); // No exception in context
//    }
//
//    @Test
//    @DisplayName("Should create TestResult with custom message and stack trace when exception exists")
//    void shouldCreateTestResultWithCustomMessageAndStackTrace() {
//        // Given
//        String customMessage = "Custom failure message";
//        RuntimeException testException = new RuntimeException("Test exception");
//
//        when(mockWrapper.getMessage()).thenReturn(customMessage);
//        when(mockWrapper.getJunitExtensionContext()).thenReturn(mockContext);
//        when(mockContext.getExecutionException()).thenReturn(Optional.of(testException));
//
//        // When
//        TestResult result = constructor.constructTestRunResult(mockWrapper);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(customMessage, result.getMessage());
//        assertNotNull(result.getStack());
//        assertTrue(result.getStack().contains("RuntimeException"));
//        assertTrue(result.getStack().contains("Test exception"));
//    }
//
//    @Test
//    @DisplayName("Should create TestResult with exception details when no custom message")
//    void shouldCreateTestResultWithExceptionDetails() {
//        // Given
//        RuntimeException testException = new RuntimeException("Exception message");
//
//        when(mockWrapper.getMessage()).thenReturn(null);
//        when(mockWrapper.getJunitExtensionContext()).thenReturn(mockContext);
//        when(mockContext.getExecutionException()).thenReturn(Optional.of(testException));
//
//        // When
//        TestResult result = constructor.constructTestRunResult(mockWrapper);
//
//        // Then
//        assertNotNull(result);
//        assertEquals("Exception message", result.getMessage());
//        assertNotNull(result.getStack());
//        assertTrue(result.getStack().contains("RuntimeException"));
//        assertTrue(result.getStack().contains("Exception message"));
//    }
//
//    @Test
//    @DisplayName("Should ignore TestAbortedException and create empty exception details")
//    void shouldIgnoreTestAbortedException() {
//        // Given
//        TestAbortedException abortedException = new TestAbortedException("Test was aborted");
//
//        when(mockWrapper.getMessage()).thenReturn(null);
//        when(mockWrapper.getJunitExtensionContext()).thenReturn(mockContext);
//        when(mockContext.getExecutionException()).thenReturn(Optional.of(abortedException));
//
//        // When
//        TestResult result = constructor.constructTestRunResult(mockWrapper);
//
//        // Then
//        assertNotNull(result);
//        assertNull(result.getMessage());
//        assertNull(result.getStack());
//    }
//
//    @Test
//    @DisplayName("Should create TestResult with empty exception details when no exception in context")
//    void shouldCreateTestResultWithEmptyExceptionDetailsWhenNoException() {
//        // Given
//        when(mockWrapper.getMessage()).thenReturn(null);
//        when(mockWrapper.getJunitExtensionContext()).thenReturn(mockContext);
//        when(mockContext.getExecutionException()).thenReturn(Optional.empty());
//
//        // When
//        TestResult result = constructor.constructTestRunResult(mockWrapper);
//
//        // Then
//        assertNotNull(result);
//        assertEquals("Test Method", result.getTitle());
//        assertEquals("test-123", result.getTestId());
//        assertEquals("TestSuite", result.getSuiteTitle());
//        assertEquals("TestSuite.java", result.getFile());
//        assertEquals(PASSED, result.getStatus());
//        assertNull(result.getMessage());
//        assertNull(result.getStack());
//    }
//
//    @Test
//    @DisplayName("Should create TestResult with empty exception details when ExtensionContext is null")
//    void shouldCreateTestResultWithEmptyExceptionDetailsWhenContextIsNull() {
//        // Given
//        when(mockWrapper.getMessage()).thenReturn(null);
//        when(mockWrapper.getJunitExtensionContext()).thenReturn(null);
//
//        // When
//        TestResult result = constructor.constructTestRunResult(mockWrapper);
//
//        // Then
//        assertNotNull(result);
//        assertEquals("Test Method", result.getTitle());
//        assertEquals(PASSED, result.getStatus());
//        assertNull(result.getMessage());
//        assertNull(result.getStack());
//    }
//
//    @Test
//    @DisplayName("Should handle failed test status correctly")
//    void shouldHandleFailedTestStatus() {
//        // Given
//        RuntimeException testException = new RuntimeException("Assertion failed");
//
//        when(mockWrapper.getStatus()).thenReturn(FAILED);
//        when(mockWrapper.getMessage()).thenReturn(null);
//        when(mockWrapper.getJunitExtensionContext()).thenReturn(mockContext);
//        when(mockContext.getExecutionException()).thenReturn(Optional.of(testException));
//
//        // When
//        TestResult result = constructor.constructTestRunResult(mockWrapper);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(FAILED, result.getStatus());
//        assertEquals("Assertion failed", result.getMessage());
//        assertNotNull(result.getStack());
//    }
//
//    @Test
//    @DisplayName("Should handle skipped test status correctly")
//    void shouldHandleSkippedTestStatus() {
//        // Given
//        when(mockWrapper.getStatus()).thenReturn(SKIPPED);
//        when(mockWrapper.getMessage()).thenReturn("Test skipped");
//
//        // When
//        TestResult result = constructor.constructTestRunResult(mockWrapper);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(SKIPPED, result.getStatus());
//        assertEquals("Test skipped", result.getMessage());
//    }
//
//    @Test
//    @DisplayName("Should handle metadata with null testId correctly")
//    void shouldHandleMetadataWithNullTestId() {
//        // Given
//        when(mockMetadata.getTestId()).thenReturn(null);
//        when(mockWrapper.getMessage()).thenReturn("Test message");
//
//        // When
//        TestResult result = constructor.constructTestRunResult(mockWrapper);
//
//        // Then
//        assertNotNull(result);
//        assertNull(result.getTestId());
//        assertEquals("Test Method", result.getTitle());
//        assertEquals("Test message", result.getMessage());
//    }
//
//    @Test
//    @DisplayName("Should preserve all metadata fields in result")
//    void shouldPreserveAllMetadataFields() {
//        // Given
//        when(mockMetadata.getTitle()).thenReturn("Custom Test Title");
//        when(mockMetadata.getTestId()).thenReturn("custom-id-456");
//        when(mockMetadata.getSuiteTitle()).thenReturn("CustomSuite");
//        when(mockMetadata.getFile()).thenReturn("CustomSuite.java");
//        when(mockWrapper.getMessage()).thenReturn("Custom message");
//
//        // When
//        TestResult result = constructor.constructTestRunResult(mockWrapper);
//
//        // Then
//        assertNotNull(result);
//        assertEquals("Custom Test Title", result.getTitle());
//        assertEquals("custom-id-456", result.getTestId());
//        assertEquals("CustomSuite", result.getSuiteTitle());
//        assertEquals("CustomSuite.java", result.getFile());
//        assertEquals(PASSED, result.getStatus());
//        assertEquals("Custom message", result.getMessage());
//    }
//
//    @Test
//    @DisplayName("Should handle nested exceptions correctly")
//    void shouldHandleNestedExceptions() {
//        // Given
//        RuntimeException cause = new RuntimeException("Root cause");
//        RuntimeException wrapper = new RuntimeException("Wrapper exception", cause);
//
//        when(mockWrapper.getMessage()).thenReturn(null);
//        when(mockWrapper.getJunitExtensionContext()).thenReturn(mockContext);
//        when(mockContext.getExecutionException()).thenReturn(Optional.of(wrapper));
//
//        // When
//        TestResult result = constructor.constructTestRunResult(mockWrapper);
//
//        // Then
//        assertNotNull(result);
//        assertEquals("Wrapper exception", result.getMessage());
//        assertNotNull(result.getStack());
//        assertTrue(result.getStack().contains("Wrapper exception"));
//        assertTrue(result.getStack().contains("Root cause"));
//    }
//}