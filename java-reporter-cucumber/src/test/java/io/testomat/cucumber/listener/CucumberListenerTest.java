package io.testomat.cucumber.listener;

import static io.testomat.core.constants.CommonConstants.FAILED;
import static io.testomat.core.constants.CommonConstants.PASSED;
import static io.testomat.core.constants.CommonConstants.SKIPPED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.testomat.core.exception.ReportTestResultException;
import io.testomat.core.model.TestMetadata;
import io.testomat.core.model.TestResult;
import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.cucumber.constructor.CucumberTestResultConstructor;
import io.testomat.cucumber.constructor.TestResultWrapper;
import io.testomat.cucumber.extractor.CucumberMetaDataExtractor;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class CucumberListenerTest {

    private GlobalRunManager mockRunManager;
    private CucumberMetaDataExtractor mockMetaDataExtractor;
    private CucumberTestResultConstructor mockResultConstructor;
    private EventPublisher mockEventPublisher;
    private TestCaseFinished mockTestCaseFinished;
    private TestCase mockTestCase;
    private Result mockResult;

    private CucumberListener listener;
    private TestMetadata testMetadata;
    private TestResult testResult;

    @BeforeEach
    void setUp() throws Exception {
        mockRunManager = mock(GlobalRunManager.class);
        mockMetaDataExtractor = mock(CucumberMetaDataExtractor.class);
        mockResultConstructor = mock(CucumberTestResultConstructor.class);
        mockEventPublisher = mock(EventPublisher.class);
        mockTestCaseFinished = mock(TestCaseFinished.class);
        mockTestCase = mock(TestCase.class);
        mockResult = mock(Result.class);

        listener = new CucumberListener();

        injectMockField("runManager", mockRunManager);
        injectMockField("metaDataExtractor", mockMetaDataExtractor);
        injectMockField("resultConstructor", mockResultConstructor);

        testMetadata = new TestMetadata("Test Title", "T12345678", "Suite Title", "test.feature");
        testResult = TestResult.builder()
                .withTitle("Test Title")
                .withTestId("T12345678")
                .withSuiteTitle("Suite Title")
                .withFile("test.feature")
                .withStatus(PASSED)
                .build();

        when(mockTestCaseFinished.getTestCase()).thenReturn(mockTestCase);
        when(mockTestCase.getName()).thenReturn("Test Name");
        when(mockMetaDataExtractor.extractTestMetadata(mockTestCase)).thenReturn(testMetadata);
        when(mockResultConstructor.constructTestRunResult(any())).thenReturn(testResult);
    }

    private void injectMockField(String fieldName, Object mockObject) throws Exception {
        Field field = CucumberListener.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(listener, mockObject);
    }

    @Test
    void shouldRegisterAllEventHandlers() {
        // When
        listener.setEventPublisher(mockEventPublisher);

        // Then
        verify(mockEventPublisher).registerHandlerFor(eq(TestRunStarted.class), any());
        verify(mockEventPublisher).registerHandlerFor(eq(TestRunFinished.class), any());
        verify(mockEventPublisher).registerHandlerFor(eq(TestCaseFinished.class), any());
    }

    @Test
    void shouldReturnEarlyWhenRunManagerNotActive() {
        // Given
        when(mockRunManager.isActive()).thenReturn(false);

        // When
        listener.handleTestCaseFinished(mockTestCaseFinished);

        // Then
        verify(mockRunManager).isActive();
        verify(mockMetaDataExtractor, never()).extractTestMetadata(any());
        verify(mockRunManager, never()).reportTest(any());
    }

    @Test
    void shouldProcessTestCaseSuccessfullyWhenRunManagerActive() {
        // Given
        when(mockRunManager.isActive()).thenReturn(true);
        when(mockTestCaseFinished.getResult()).thenReturn(mockResult);
        when(mockResult.getStatus()).thenReturn(Status.PASSED);

        // When
        listener.handleTestCaseFinished(mockTestCaseFinished);

        // Then
        verify(mockRunManager).isActive();
        verify(mockMetaDataExtractor).extractTestMetadata(mockTestCase);
        verify(mockResultConstructor).constructTestRunResult(any(TestResultWrapper.class));
        verify(mockRunManager).reportTest(testResult);
    }

    @Test
    void shouldHandleNullResultStatus() {
        // Given
        when(mockRunManager.isActive()).thenReturn(true);
        when(mockTestCaseFinished.getResult()).thenReturn(null);

        // When
        listener.handleTestCaseFinished(mockTestCaseFinished);

        // Then
        verify(mockRunManager).reportTest(any());
    }

    @Test
    void shouldThrowExceptionWhenExtractionFails() {
        // Given
        when(mockRunManager.isActive()).thenReturn(true);
        when(mockTestCaseFinished.getResult()).thenReturn(mockResult);
        when(mockResult.getStatus()).thenReturn(Status.PASSED);
        when(mockMetaDataExtractor.extractTestMetadata(mockTestCase))
                .thenThrow(new RuntimeException("Extraction failed"));

        // When & Then
        ReportTestResultException exception = assertThrows(
                ReportTestResultException.class,
                () -> listener.handleTestCaseFinished(mockTestCaseFinished)
        );

        assertEquals("Failed to report test result for: Test Name", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenConstructionFails() {
        // Given
        when(mockRunManager.isActive()).thenReturn(true);
        when(mockTestCaseFinished.getResult()).thenReturn(mockResult);
        when(mockResult.getStatus()).thenReturn(Status.PASSED);
        when(mockResultConstructor.constructTestRunResult(any()))
                .thenThrow(new RuntimeException("Construction failed"));

        // When & Then
        ReportTestResultException exception = assertThrows(
                ReportTestResultException.class,
                () -> listener.handleTestCaseFinished(mockTestCaseFinished)
        );

        assertEquals("Failed to report test result for: Test Name", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenReportingFails() {
        // Given
        when(mockRunManager.isActive()).thenReturn(true);
        when(mockTestCaseFinished.getResult()).thenReturn(mockResult);
        when(mockResult.getStatus()).thenReturn(Status.PASSED);
        doThrow(new RuntimeException("Reporting failed")).when(mockRunManager).reportTest(any());

        // When & Then
        ReportTestResultException exception = assertThrows(
                ReportTestResultException.class,
                () -> listener.handleTestCaseFinished(mockTestCaseFinished)
        );

        assertEquals("Failed to report test result for: Test Name", exception.getMessage());
    }

    @Test
    void shouldReturnFailedForNullStatus() {
        // When
        String result = callNormalizeStatus(null);

        // Then
        assertEquals(FAILED, result);
    }

    @Test
    void shouldReturnPassedForPassedStatuses() {
        assertEquals(PASSED, callNormalizeStatus(createMockStatus("PASSED")));
        assertEquals(PASSED, callNormalizeStatus(createMockStatus("SUCCESS")));
        assertEquals(PASSED, callNormalizeStatus(createMockStatus("SUCCESSFUL")));
        assertEquals(PASSED, callNormalizeStatus(createMockStatus("passed")));
        assertEquals(PASSED, callNormalizeStatus(createMockStatus("success")));
        assertEquals(PASSED, callNormalizeStatus(createMockStatus("successful")));
    }

    @Test
    void shouldReturnSkippedForSkippedStatuses() {
        assertEquals(SKIPPED, callNormalizeStatus(createMockStatus("SKIPPED")));
        assertEquals(SKIPPED, callNormalizeStatus(createMockStatus("PENDING")));
        assertEquals(SKIPPED, callNormalizeStatus(createMockStatus("UNDEFINED")));
        assertEquals(SKIPPED, callNormalizeStatus(createMockStatus("AMBIGUOUS")));
        assertEquals(SKIPPED, callNormalizeStatus(createMockStatus("DISABLED")));
        assertEquals(SKIPPED, callNormalizeStatus(createMockStatus("ABORTED")));
        assertEquals(SKIPPED, callNormalizeStatus(createMockStatus("skipped")));
        assertEquals(SKIPPED, callNormalizeStatus(createMockStatus("pending")));
        assertEquals(SKIPPED, callNormalizeStatus(createMockStatus("undefined")));
        assertEquals(SKIPPED, callNormalizeStatus(createMockStatus("ambiguous")));
        assertEquals(SKIPPED, callNormalizeStatus(createMockStatus("disabled")));
        assertEquals(SKIPPED, callNormalizeStatus(createMockStatus("aborted")));
    }

    @Test
    void shouldReturnFailedForFailedOrUnknownStatuses() {
        assertEquals(FAILED, callNormalizeStatus(createMockStatus("FAILED")));
        assertEquals(FAILED, callNormalizeStatus(createMockStatus("ERROR")));
        assertEquals(FAILED, callNormalizeStatus(createMockStatus("UNKNOWN")));
        assertEquals(FAILED, callNormalizeStatus(createMockStatus("INVALID")));
        assertEquals(FAILED, callNormalizeStatus(createMockStatus("failed")));
        assertEquals(FAILED, callNormalizeStatus(createMockStatus("error")));
        assertEquals(FAILED, callNormalizeStatus(createMockStatus("unknown")));
        assertEquals(FAILED, callNormalizeStatus(createMockStatus("")));
    }

    @Test
    void shouldProcessWithStaticGlobalRunManagerMock() {
        try (MockedStatic<GlobalRunManager> mockedStatic = mockStatic(GlobalRunManager.class)) {
            // Given
            GlobalRunManager staticMockRunManager = mock(GlobalRunManager.class);
            mockedStatic.when(GlobalRunManager::getInstance).thenReturn(staticMockRunManager);
            when(staticMockRunManager.isActive()).thenReturn(true);

            CucumberListener newListener = new CucumberListener();
            when(mockTestCaseFinished.getResult()).thenReturn(mockResult);
            when(mockResult.getStatus()).thenReturn(Status.PASSED);

            // When
            newListener.handleTestCaseFinished(mockTestCaseFinished);

            // Then
            verify(staticMockRunManager).isActive();
        }
    }

    private Object createMockStatus(String statusValue) {
        Object mockStatus = mock(Object.class);
        when(mockStatus.toString()).thenReturn(statusValue);
        return mockStatus;
    }

    private String callNormalizeStatus(Object frameworkStatus) {
        try {
            java.lang.reflect.Method method = CucumberListener.class.getDeclaredMethod("normalizeStatus", Object.class);
            method.setAccessible(true);
            return (String) method.invoke(listener, frameworkStatus);
        } catch (Exception e) {
            throw new RuntimeException("Failed to call normalizeStatus", e);
        }
    }
}