package io.testomat.cucumber.listener;

import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.testomat.core.exception.ReportTestResultException;
import io.testomat.core.model.TestResult;
import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.cucumber.constructor.CucumberTestResultConstructor;
import io.testomat.cucumber.extractor.TestDataExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

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
    private TestDataExtractor dataExtractor;

    @Mock
    private FacadeFunctionsHandler functionsHandler;

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
        CucumberTestRegistry.reset();
        listener = new CucumberListener(resultConstructor, runManager, null, dataExtractor, functionsHandler);
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
        verify(eventPublisher).registerHandlerFor(eq(TestCaseStarted.class), any());
        verify(eventPublisher).registerHandlerFor(eq(TestCaseFinished.class), any());
    }

    @Test
    void shouldHandleTestCaseFinishedWhenRunManagerIsActive() {
        // Given
        when(runManager.isActive()).thenReturn(true);
        UUID testCaseId = UUID.randomUUID();
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getName()).thenReturn("Test Scenario");
        when(testCase.getId()).thenReturn(testCaseId);
        when(resultConstructor.constructTestRunResult(testCaseFinished)).thenReturn(testResult);

        // When
        listener.handleTestCaseFinished(testCaseFinished);

        // Then
        verify(resultConstructor).constructTestRunResult(testCaseFinished);
        verify(runManager).reportTest(testResult);
        verify(functionsHandler).handleFacadeFunctions(testCaseFinished);
    }
    @Test
    void shouldWrapExceptionWhenConstructorFails() {
        // Given
        when(runManager.isActive()).thenReturn(true);
        RuntimeException originalException = new RuntimeException("Constructor error");
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
        // Verify that functionsHandler was still called despite the exception
        verify(functionsHandler).handleFacadeFunctions(testCaseFinished);
    }
}