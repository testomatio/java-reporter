package com.testomatio.reporter.core.constructor;

import com.testomatio.reporter.model.TestCaseResult;
import com.testomatio.reporter.model.TestMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.ITestResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestNGTestCaseResultConstructorTest {

    private TestNGTestCaseResultConstructor constructor;

    @Mock
    private TestCaseResultWrapper holder;

    @Mock
    private TestMetadata testMetadata;

    @Mock
    private ITestResult testResult;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        constructor = new TestNGTestCaseResultConstructor();
        
        when(testMetadata.getTitle()).thenReturn("TestNG Test Title");
        when(testMetadata.getTestId()).thenReturn("testng-test-id-123");
        when(testMetadata.getSuiteTitle()).thenReturn("TestNG Test Suite");
        when(testMetadata.getFile()).thenReturn("TestNGTest.java");
        when(holder.getTestMetadata()).thenReturn(testMetadata);
        when(holder.getStatus()).thenReturn("FAILED");
    }

    @Test
    void shouldReturnTestNGAsFrameworkName() {
        assertEquals("TestNG", constructor.getFrameworkName());
    }

    @Test
    void hasCustomMessage_shouldReturnTrue_whenReasonExists() {
        when(holder.getReason()).thenReturn("Test was skipped");
        when(holder.getMessage()).thenReturn(null);
        
        assertTrue(constructor.hasCustomMessage(holder));
    }

    @Test
    void hasCustomMessage_shouldReturnTrue_whenMessageExists() {
        when(holder.getReason()).thenReturn(null);
        when(holder.getMessage()).thenReturn("Custom message");
        
        assertTrue(constructor.hasCustomMessage(holder));
    }

    @Test
    void hasCustomMessage_shouldReturnTrue_whenBothReasonAndMessageExist() {
        when(holder.getReason()).thenReturn("Test was skipped");
        when(holder.getMessage()).thenReturn("Custom message");
        
        assertTrue(constructor.hasCustomMessage(holder));
    }

    @Test
    void hasCustomMessage_shouldReturnFalse_whenBothAreNull() {
        when(holder.getReason()).thenReturn(null);
        when(holder.getMessage()).thenReturn(null);
        
        assertFalse(constructor.hasCustomMessage(holder));
    }

    @Test
    void getCustomMessage_shouldReturnReason_whenReasonExists() {
        String reason = "Test was skipped";
        String message = "Custom message";
        
        when(holder.getReason()).thenReturn(reason);
        when(holder.getMessage()).thenReturn(message);
        
        assertEquals(reason, constructor.getCustomMessage(holder));
    }

    @Test
    void getCustomMessage_shouldReturnMessage_whenReasonIsNull() {
        String message = "Custom message";
        
        when(holder.getReason()).thenReturn(null);
        when(holder.getMessage()).thenReturn(message);
        
        assertEquals(message, constructor.getCustomMessage(holder));
    }

    @Test
    void getCustomMessage_shouldReturnNull_whenBothAreNull() {
        when(holder.getReason()).thenReturn(null);
        when(holder.getMessage()).thenReturn(null);
        
        assertNull(constructor.getCustomMessage(holder));
    }

    @Test
    void createWithCustomMessage_shouldCreateResultWithReasonMessage() {
        String reason = "Test was skipped due to condition";
        
        when(holder.getReason()).thenReturn(reason);
        when(holder.getMessage()).thenReturn("Other message");
        
        TestCaseResult result = constructor.createWithCustomMessage(holder);
        
        assertNotNull(result);
        assertEquals("TestNG Test Title", result.getTitle());
        assertEquals("testng-test-id-123", result.getTestId());
        assertEquals("TestNG Test Suite", result.getSuiteTitle());
        assertEquals("TestNGTest.java", result.getFile());
        assertEquals("FAILED", result.getStatus());
        assertEquals(reason, result.getMessage());
        assertNull(result.getStack());
    }

    @Test
    void createWithCustomMessage_shouldCreateResultWithCustomMessage() {
        String message = "Custom test message";
        
        when(holder.getReason()).thenReturn(null);
        when(holder.getMessage()).thenReturn(message);
        
        TestCaseResult result = constructor.createWithCustomMessage(holder);
        
        assertNotNull(result);
        assertEquals(message, result.getMessage());
        assertNull(result.getStack());
    }

    @Test
    void createWithExceptionDetails_shouldCreateResultWithException() {
        RuntimeException testException = new RuntimeException("TestNG assertion failed");
        
        when(holder.getTestResult()).thenReturn(testResult);
        when(testResult.getThrowable()).thenReturn(testException);
        
        TestCaseResult result = constructor.createWithExceptionDetails(holder);
        
        assertNotNull(result);
        assertEquals("TestNG Test Title", result.getTitle());
        assertEquals("TestNG assertion failed", result.getMessage());
        assertNotNull(result.getStack());
        assertTrue(result.getStack().contains("TestNG assertion failed"));
    }

    @Test
    void createWithExceptionDetails_shouldCreateResultWithEmptyDetails_whenNoException() {
        when(holder.getTestResult()).thenReturn(testResult);
        when(testResult.getThrowable()).thenReturn(null);
        
        TestCaseResult result = constructor.createWithExceptionDetails(holder);
        
        assertNotNull(result);
        assertNull(result.getMessage());
        assertNull(result.getStack());
    }

    @Test
    void createWithExceptionDetails_shouldCreateResultWithEmptyDetails_whenNoTestResult() {
        when(holder.getTestResult()).thenReturn(null);
        
        TestCaseResult result = constructor.createWithExceptionDetails(holder);
        
        assertNotNull(result);
        assertNull(result.getMessage());
        assertNull(result.getStack());
    }

    @Test
    void createWithExceptionDetails_shouldCreateResultWithEmptyDetails_whenTestAbortedException() {
        org.opentest4j.TestAbortedException abortedException = 
            new org.opentest4j.TestAbortedException("Test was aborted");
        
        when(holder.getTestResult()).thenReturn(testResult);
        when(testResult.getThrowable()).thenReturn(abortedException);
        
        TestCaseResult result = constructor.createWithExceptionDetails(holder);
        
        assertNotNull(result);
        assertNull(result.getMessage());
        assertNull(result.getStack());
    }

    @Test
    void constructTestRunResult_shouldUseCustomMessage_whenReasonExists() {
        String reason = "Test was skipped";
        
        when(holder.getReason()).thenReturn(reason);
        when(holder.getMessage()).thenReturn(null);
        
        TestCaseResult result = constructor.constructTestRunResult(holder);
        
        assertNotNull(result);
        assertEquals(reason, result.getMessage());
        assertNull(result.getStack());
    }

    @Test
    void constructTestRunResult_shouldUseCustomMessage_whenMessageExists() {
        String message = "Custom test message";
        
        when(holder.getReason()).thenReturn(null);
        when(holder.getMessage()).thenReturn(message);
        
        TestCaseResult result = constructor.constructTestRunResult(holder);
        
        assertNotNull(result);
        assertEquals(message, result.getMessage());
        assertNull(result.getStack());
    }

    @Test
    void constructTestRunResult_shouldUseExceptionDetails_whenNoCustomMessage() {
        RuntimeException testException = new RuntimeException("TestNG exception");
        
        when(holder.getReason()).thenReturn(null);
        when(holder.getMessage()).thenReturn(null);
        when(holder.getTestResult()).thenReturn(testResult);
        when(testResult.getThrowable()).thenReturn(testException);
        
        TestCaseResult result = constructor.constructTestRunResult(holder);
        
        assertNotNull(result);
        assertEquals("TestNG exception", result.getMessage());
        assertNotNull(result.getStack());
    }

    @Test
    void constructTestRunResult_shouldCreatePassedResult_whenNoCustomMessageAndNoException() {
        when(holder.getStatus()).thenReturn("PASSED");
        when(holder.getReason()).thenReturn(null);
        when(holder.getMessage()).thenReturn(null);
        when(holder.getTestResult()).thenReturn(null);
        
        TestCaseResult result = constructor.constructTestRunResult(holder);
        
        assertNotNull(result);
        assertEquals("TestNG Test Title", result.getTitle());
        assertEquals("PASSED", result.getStatus());
        assertNull(result.getMessage());
        assertNull(result.getStack());
    }

    @Test
    void constructTestRunResult_shouldThrowException_whenHolderIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> constructor.constructTestRunResult(null)
        );
        
        assertEquals("TestRunResultWrapper cannot be null", exception.getMessage());
    }

    @Test
    void constructTestRunResult_shouldThrowException_whenTestMetadataIsNull() {
        when(holder.getTestMetadata()).thenReturn(null);
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> constructor.constructTestRunResult(holder)
        );
        
        assertEquals("TestMetadata cannot be null", exception.getMessage());
    }
}