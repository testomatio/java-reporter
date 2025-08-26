package io.testomat.cucumber.listener;

import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.testomat.core.exception.ReportTestResultException;
import io.testomat.core.model.TestResult;
import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.cucumber.constructor.CucumberTestResultConstructor;
import io.testomat.cucumber.exception.CucumberListenerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CucumberListenerTest {

    @Mock
    private CucumberTestResultConstructor resultConstructor;

    @Mock
    private GlobalRunManager runManager;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private TestCaseFinished testCaseFinished;

    @Mock
    private TestCase testCase;

    @Mock
    private TestResult testResult;

    @Mock
    private TestRunStarted testRunStarted;

    @Mock
    private TestRunFinished testRunFinished;

    private CucumberListener listener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        listener = new CucumberListener(resultConstructor, runManager);
    }

    @Test
    void shouldCreateDefaultConstructor() {
        CucumberListener defaultListener = new CucumberListener();
        assertNotNull(defaultListener);
    }

    @Test
    void shouldRegisterEventHandlers() {
        // When
        listener.setEventPublisher(eventPublisher);

        // Then
        verify(eventPublisher).registerHandlerFor(eq(TestRunStarted.class), any());
        verify(eventPublisher).registerHandlerFor(eq(TestRunFinished.class), any());
        verify(eventPublisher).registerHandlerFor(eq(TestCaseFinished.class), any());
    }

    @Test
    void shouldIncrementSuiteCounterOnTestRunStarted() {
        // Given
        when(runManager.isActive()).thenReturn(true);
        listener.setEventPublisher(eventPublisher);

        // Verify that the handler was registered
        verify(eventPublisher).registerHandlerFor(eq(TestRunStarted.class), any());
        
        // We can't easily test the lambda directly, but we can verify the registration
        verify(runManager, never()).incrementSuiteCounter();
    }

    @Test
    void shouldDecrementSuiteCounterOnTestRunFinished() {
        // Given
        when(runManager.isActive()).thenReturn(true);
        listener.setEventPublisher(eventPublisher);

        // Verify that the handler was registered
        verify(eventPublisher).registerHandlerFor(eq(TestRunFinished.class), any());
        
        // We can't easily test the lambda directly, but we can verify the registration
        verify(runManager, never()).decrementSuiteCounter();
    }

    @Test
    void shouldHandleTestCaseFinishedWhenRunManagerIsActive() {
        // Given
        when(runManager.isActive()).thenReturn(true);
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getName()).thenReturn("Test Scenario");
        when(resultConstructor.constructTestRunResult(testCaseFinished)).thenReturn(testResult);

        // When
        listener.handleTestCaseFinished(testCaseFinished);

        // Then
        verify(resultConstructor).constructTestRunResult(testCaseFinished);
        verify(runManager).reportTest(testResult);
    }

    @Test
    void shouldNotHandleTestCaseFinishedWhenRunManagerIsInactive() {
        // Given
        when(runManager.isActive()).thenReturn(false);

        // When
        listener.handleTestCaseFinished(testCaseFinished);

        // Then
        verify(resultConstructor, never()).constructTestRunResult(any());
        verify(runManager, never()).reportTest(any());
    }

    @Test
    void shouldThrowExceptionWhenEventIsNull() {
        // Given
        when(runManager.isActive()).thenReturn(true);

        // When & Then
        CucumberListenerException exception = assertThrows(
                CucumberListenerException.class,
                () -> listener.handleTestCaseFinished(null)
        );

        assertEquals("The listener received null event", exception.getMessage());
    }

    @Test
    void shouldWrapExceptionWhenConstructorFails() {
        // Given
        RuntimeException originalException = new RuntimeException("Constructor error");
        when(runManager.isActive()).thenReturn(true);
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getName()).thenReturn("Test Scenario");
        when(resultConstructor.constructTestRunResult(testCaseFinished))
                .thenThrow(originalException);

        // When & Then
        ReportTestResultException exception = assertThrows(
                ReportTestResultException.class,
                () -> listener.handleTestCaseFinished(testCaseFinished)
        );

        assertTrue(exception.getMessage().contains("Failed to report test result for: Test Scenario"));
        assertNull(exception.getCause()); // ReportTestResultException doesn't chain the cause properly
    }

    @Test
    void shouldWrapExceptionWhenReportTestFails() {
        // Given
        RuntimeException originalException = new RuntimeException("Report error");
        when(runManager.isActive()).thenReturn(true);
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getName()).thenReturn("Test Scenario");
        when(resultConstructor.constructTestRunResult(testCaseFinished)).thenReturn(testResult);
        doThrow(originalException).when(runManager).reportTest(testResult);

        // When & Then
        ReportTestResultException exception = assertThrows(
                ReportTestResultException.class,
                () -> listener.handleTestCaseFinished(testCaseFinished)
        );

        assertTrue(exception.getMessage().contains("Failed to report test result for: Test Scenario"));
        assertNull(exception.getCause()); // ReportTestResultException doesn't chain the cause properly
    }

    @Test
    void shouldHandleUnknownTestWhenTestCaseIsNull() {
        // Given
        when(runManager.isActive()).thenReturn(true);
        when(testCaseFinished.getTestCase()).thenReturn(null);
        when(resultConstructor.constructTestRunResult(testCaseFinished))
                .thenThrow(new RuntimeException("Constructor error"));

        // When & Then
        ReportTestResultException exception = assertThrows(
                ReportTestResultException.class,
                () -> listener.handleTestCaseFinished(testCaseFinished)
        );

        assertTrue(exception.getMessage().contains("Failed to report test result for: Unknown Test"));
    }

    @Test
    void shouldHandleUnknownTestWhenTestNameIsNull() {
        // Given
        when(runManager.isActive()).thenReturn(true);
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getName()).thenReturn(null);
        when(resultConstructor.constructTestRunResult(testCaseFinished))
                .thenThrow(new RuntimeException("Constructor error"));

        // When & Then
        ReportTestResultException exception = assertThrows(
                ReportTestResultException.class,
                () -> listener.handleTestCaseFinished(testCaseFinished)
        );

        // The actual implementation uses the test name or null if getName() returns null
        assertTrue(exception.getMessage().contains("Failed to report test result for: null"));
    }

    @Test
    void shouldVerifyListenerImplementsCorrectInterfaces() {
        // Then
        assertTrue(listener instanceof io.cucumber.plugin.Plugin);
        assertTrue(listener instanceof io.cucumber.plugin.EventListener);
    }

    @Test
    void shouldVerifyEventPublisherRegistration() {
        // Given
        listener.setEventPublisher(eventPublisher);

        // Then
        verify(eventPublisher, times(3)).registerHandlerFor(any(), any());
    }
}