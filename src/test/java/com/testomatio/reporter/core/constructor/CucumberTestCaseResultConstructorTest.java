package com.testomatio.reporter.core.constructor;

import com.testomatio.reporter.model.TestResult;
import com.testomatio.reporter.model.TestMetadata;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.TestCaseFinished;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CucumberTestCaseResultConstructorTest {

    private CucumberTestResultConstructor constructor;

    @Mock
    private TestCaseResultWrapper holder;

    @Mock
    private TestMetadata testMetadata;

    @Mock
    private TestCaseFinished testCaseFinished;

    @Mock
    private Result cucumberResult;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        constructor = new CucumberTestResultConstructor();
        
        when(testMetadata.getTitle()).thenReturn("Cucumber Test Title");
        when(testMetadata.getTestId()).thenReturn("cucumber-test-id-123");
        when(testMetadata.getSuiteTitle()).thenReturn("Cucumber Test Suite");
        when(testMetadata.getFile()).thenReturn("cucumber.feature");
        when(holder.getTestMetadata()).thenReturn(testMetadata);
        when(holder.getStatus()).thenReturn("FAILED");
    }

    @Test
    void shouldReturnCucumberAsFrameworkName() {
        assertEquals("Cucumber", constructor.getFrameworkName());
    }

    @Test
    void hasCustomMessage_shouldAlwaysReturnFalse() {
        assertFalse(constructor.hasCustomMessage(holder));
        
        when(holder.getMessage()).thenReturn("Some message");
        assertFalse(constructor.hasCustomMessage(holder));
        
        when(holder.getReason()).thenReturn("Some reason");
        assertFalse(constructor.hasCustomMessage(holder));
    }

    @Test
    void getCustomMessage_shouldAlwaysReturnNull() {
        assertNull(constructor.getCustomMessage(holder));
        
        when(holder.getMessage()).thenReturn("Some message");
        assertNull(constructor.getCustomMessage(holder));
        
        when(holder.getReason()).thenReturn("Some reason");
        assertNull(constructor.getCustomMessage(holder));
    }

    @Test
    void createWithCustomMessage_shouldCreateResultWithNullMessageAndStack() {
        TestResult result = constructor.createWithCustomMessage(holder);
        
        assertNotNull(result);
        assertEquals("Cucumber Test Title", result.getTitle());
        assertEquals("cucumber-test-id-123", result.getTestId());
        assertEquals("Cucumber Test Suite", result.getSuiteTitle());
        assertEquals("cucumber.feature", result.getFile());
        assertEquals("FAILED", result.getStatus());
        assertNull(result.getMessage());
        assertNull(result.getStack());
    }

    @Test
    void createWithExceptionDetails_shouldCreateResultWithException() {
        RuntimeException testException = new RuntimeException("Cucumber step failed");
        
        when(holder.getCucumberTestCaseFinished()).thenReturn(testCaseFinished);
        when(testCaseFinished.getResult()).thenReturn(cucumberResult);
        when(cucumberResult.getError()).thenReturn(testException);
        
        TestResult result = constructor.createWithExceptionDetails(holder);
        
        assertNotNull(result);
        assertEquals("Cucumber Test Title", result.getTitle());
        assertEquals("Cucumber step failed", result.getMessage());
        assertNotNull(result.getStack());
        assertTrue(result.getStack().contains("Cucumber step failed"));
    }

    @Test
    void createWithExceptionDetails_shouldCreateResultWithEmptyDetails_whenNoException() {
        when(holder.getCucumberTestCaseFinished()).thenReturn(testCaseFinished);
        when(testCaseFinished.getResult()).thenReturn(cucumberResult);
        when(cucumberResult.getError()).thenReturn(null);
        
        TestResult result = constructor.createWithExceptionDetails(holder);
        
        assertNotNull(result);
        assertNull(result.getMessage());
        assertNull(result.getStack());
    }

    @Test
    void createWithExceptionDetails_shouldCreateResultWithEmptyDetails_whenNoTestCaseFinished() {
        when(holder.getCucumberTestCaseFinished()).thenReturn(null);
        
        TestResult result = constructor.createWithExceptionDetails(holder);
        
        assertNotNull(result);
        assertNull(result.getMessage());
        assertNull(result.getStack());
    }

    @Test
    void createWithExceptionDetails_shouldCreateResultWithEmptyDetails_whenTestAbortedException() {
        org.opentest4j.TestAbortedException abortedException = 
            new org.opentest4j.TestAbortedException("Test was aborted");
        
        when(holder.getCucumberTestCaseFinished()).thenReturn(testCaseFinished);
        when(testCaseFinished.getResult()).thenReturn(cucumberResult);
        when(cucumberResult.getError()).thenReturn(abortedException);
        
        TestResult result = constructor.createWithExceptionDetails(holder);
        
        assertNotNull(result);
        assertNull(result.getMessage());
        assertNull(result.getStack());
    }

    @Test
    void constructTestRunResult_shouldAlwaysUseExceptionDetails() {
        RuntimeException testException = new RuntimeException("Cucumber error");
        
        when(holder.getCucumberTestCaseFinished()).thenReturn(testCaseFinished);
        when(testCaseFinished.getResult()).thenReturn(cucumberResult);
        when(cucumberResult.getError()).thenReturn(testException);
        
        TestResult result = constructor.constructTestRunResult(holder);
        
        assertNotNull(result);
        assertEquals("Cucumber error", result.getMessage());
        assertNotNull(result.getStack());
        assertTrue(result.getStack().contains("Cucumber error"));
    }

    @Test
    void constructTestRunResult_shouldCreatePassedResult_whenNoException() {
        when(holder.getStatus()).thenReturn("PASSED");
        when(holder.getCucumberTestCaseFinished()).thenReturn(null);
        
        TestResult result = constructor.constructTestRunResult(holder);
        
        assertNotNull(result);
        assertEquals("Cucumber Test Title", result.getTitle());
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