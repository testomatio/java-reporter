package io.testomat.cucumber.listener;

import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.testomat.core.artifact.client.AwsService;
import io.testomat.core.exception.ReportTestResultException;
import io.testomat.core.model.TestResult;
import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.cucumber.constructor.CucumberTestResultConstructor;
import io.testomat.cucumber.exception.CucumberListenerException;
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
    private AwsService awsService;

    @Mock
    private TestDataExtractor dataExtractor;

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
        listener = new CucumberListener(resultConstructor, runManager, awsService, dataExtractor);
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
//
//    @Test
//    void shouldIncrementSuiteCounterOnTestRunStarted() {
//        // Given
//        when(runManager.isActive()).thenReturn(true);
//        listener.setEventPublisher(eventPublisher);
//
//        // Verify that the handler was registered
//        verify(eventPublisher).registerHandlerFor(eq(TestRunStarted.class), any());
//
//        // We can't easily test the lambda directly, but we can verify the registration
//        verify(runManager, never()).incrementSuiteCounter();
//    }
//
//    @Test
//    void shouldDecrementSuiteCounterOnTestRunFinished() {
//        // Given
//        when(runManager.isActive()).thenReturn(true);
//        listener.setEventPublisher(eventPublisher);
//
//        // Verify that the handler was registered
//        verify(eventPublisher).registerHandlerFor(eq(TestRunFinished.class), any());
//
//        // We can't easily test the lambda directly, but we can verify the registration
//        verify(runManager, never()).decrementSuiteCounter();
//    }
    @Test
    void shouldHandleTestCaseFinishedWhenRunManagerIsActive() {
        // Given
        when(runManager.isActive()).thenReturn(true);
        UUID testCaseId = UUID.randomUUID();
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getName()).thenReturn("Test Scenario");
        when(testCase.getId()).thenReturn(testCaseId);
        when(resultConstructor.constructTestRunResult(testCaseFinished)).thenReturn(testResult);
        when(dataExtractor.extractTitle(testCaseFinished)).thenReturn("Test Title");
        when(dataExtractor.extractTestId(testCaseFinished)).thenReturn("@T12345");

        // When
        listener.handleTestCaseFinished(testCaseFinished);

        // Then
        verify(resultConstructor).constructTestRunResult(testCaseFinished);
        verify(runManager).reportTest(testResult);
        verify(awsService).uploadAllArtifactsForTest("Test Title", testCaseId.toString(), "@T12345");
    }
    @Test
    void shouldWrapExceptionWhenConstructorFails() {
        // Given
        when(runManager.isActive()).thenReturn(true);
        UUID testCaseId = UUID.randomUUID();
        RuntimeException originalException = new RuntimeException("Constructor error");
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getName()).thenReturn("Test Scenario");
        when(testCase.getId()).thenReturn(testCaseId);
        when(dataExtractor.extractTitle(testCaseFinished)).thenReturn("Test Title");
        when(dataExtractor.extractTestId(testCaseFinished)).thenReturn("@T12345");
        when(resultConstructor.constructTestRunResult(testCaseFinished))
                .thenThrow(originalException);

        // When & Then
        ReportTestResultException exception = assertThrows(
                ReportTestResultException.class,
                () -> listener.handleTestCaseFinished(testCaseFinished)
        );

        assertTrue(exception.getMessage().contains("Failed to report test result for: Test Scenario"));
        // Verify that afterEach was still called despite the exception
        verify(awsService).uploadAllArtifactsForTest("Test Title", testCaseId.toString(), "@T12345");
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

    @Test
    void shouldCallAfterEachWithCorrectParameters() {
        // Given
        when(runManager.isActive()).thenReturn(true);
        UUID testCaseId = UUID.randomUUID();
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getName()).thenReturn("Test Scenario");
        when(testCase.getId()).thenReturn(testCaseId);
        when(resultConstructor.constructTestRunResult(testCaseFinished)).thenReturn(testResult);
        when(dataExtractor.extractTitle(testCaseFinished)).thenReturn("Test Title");
        when(dataExtractor.extractTestId(testCaseFinished)).thenReturn("@T12345");

        // When
        listener.handleTestCaseFinished(testCaseFinished);

        // Then
        verify(dataExtractor).extractTitle(testCaseFinished);
        verify(dataExtractor).extractTestId(testCaseFinished);
        verify(awsService).uploadAllArtifactsForTest("Test Title", testCaseId.toString(), "@T12345");
    }

    @Test
    void shouldHandleNullTestIdInAfterEach() {
        // Given
        when(runManager.isActive()).thenReturn(true);
        UUID testCaseId = UUID.randomUUID();
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getName()).thenReturn("Test Scenario");
        when(testCase.getId()).thenReturn(testCaseId);
        when(resultConstructor.constructTestRunResult(testCaseFinished)).thenReturn(testResult);
        when(dataExtractor.extractTitle(testCaseFinished)).thenReturn("Test Title");
        when(dataExtractor.extractTestId(testCaseFinished)).thenReturn(null);

        // When
        listener.handleTestCaseFinished(testCaseFinished);

        // Then
        verify(awsService).uploadAllArtifactsForTest("Test Title", testCaseId.toString(), null);
    }

    @Test
    void shouldHandleNullTitleInAfterEach() {
        // Given
        when(runManager.isActive()).thenReturn(true);
        UUID testCaseId = UUID.randomUUID();
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getName()).thenReturn("Test Scenario");
        when(testCase.getId()).thenReturn(testCaseId);
        when(resultConstructor.constructTestRunResult(testCaseFinished)).thenReturn(testResult);
        when(dataExtractor.extractTitle(testCaseFinished)).thenReturn(null);
        when(dataExtractor.extractTestId(testCaseFinished)).thenReturn("@T12345");

        // When
        listener.handleTestCaseFinished(testCaseFinished);

        // Then
        verify(awsService).uploadAllArtifactsForTest(null, testCaseId.toString(), "@T12345");
    }

    @Test
    void shouldStillCallAfterEachWhenReportTestFails() {
        // Given
        when(runManager.isActive()).thenReturn(true);
        UUID testCaseId = UUID.randomUUID();
        RuntimeException reportException = new RuntimeException("Report failed");
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getName()).thenReturn("Test Scenario");
        when(testCase.getId()).thenReturn(testCaseId);
        when(resultConstructor.constructTestRunResult(testCaseFinished)).thenReturn(testResult);
        when(dataExtractor.extractTitle(testCaseFinished)).thenReturn("Test Title");
        when(dataExtractor.extractTestId(testCaseFinished)).thenReturn("@T12345");
        doThrow(reportException).when(runManager).reportTest(testResult);

        // When & Then
        ReportTestResultException exception = assertThrows(
                ReportTestResultException.class,
                () -> listener.handleTestCaseFinished(testCaseFinished)
        );

        // Verify that afterEach was still called before the exception was thrown
        verify(awsService).uploadAllArtifactsForTest("Test Title", testCaseId.toString(), "@T12345");
        assertTrue(exception.getMessage().contains("Failed to report test result for: Test Scenario"));
    }
}