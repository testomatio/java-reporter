package com.testomatio.reporter.core.framework_integration;

import com.testomatio.reporter.core.extractor.TestMetadataExtractor;
import com.testomatio.reporter.model.TestMetadata;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.testomatio.reporter.constants.CommonConstants.FAILED;
import static com.testomatio.reporter.constants.CommonConstants.PASSED;
import static com.testomatio.reporter.constants.CommonConstants.SKIPPED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CucumberListenerTest {

    private CucumberListener cucumberListener;
    private AutoCloseable mockitoCloseable;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private TestRunStarted testRunStarted;

    @Mock
    private TestRunFinished testRunFinished;

    @Mock
    private TestCaseStarted testCaseStarted;

    @Mock
    private TestCaseFinished testCaseFinished;

    @Mock
    private TestCase testCase;

    @Mock
    private Result result;

    @Mock
    private TestMetadataExtractor metadataExtractor;

    @Mock
    private TestMetadata testMetadata;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);

        // Створюємо spy для можливості мокання protected методів з BaseTestReporter
        cucumberListener = spy(new CucumberListener());

        // Встановлюємо мок metadataExtractor через рефлексію
        setMetadataExtractor(cucumberListener, metadataExtractor);

        // Скидаємо всі моки до чистого стану
        resetAllMocks();
    }

    @AfterEach
    void tearDown() throws Exception {
        resetAllMocks();

        if (mockitoCloseable != null) {
            mockitoCloseable.close();
        }
    }

    private void resetAllMocks() {
        reset(eventPublisher, testRunStarted, testRunFinished, testCaseStarted,
                testCaseFinished, testCase, result, metadataExtractor, testMetadata);
    }

    private void setMetadataExtractor(CucumberListener listener, TestMetadataExtractor extractor) throws Exception {
        Field field = BaseTestReporter.class.getDeclaredField("metadataExtractor");
        field.setAccessible(true);
        field.set(listener, extractor);
    }

    @Test
    void constructor_DefaultConstructor_ShouldCreateInstance() {
        // When
        CucumberListener listener = new CucumberListener();

        // Then
        assertNotNull(listener);
    }

    @Test
    void constructor_WithOutputParameter_ShouldCreateInstance() {
        // Given
        String output = "test-output";

        // When
        CucumberListener listener = new CucumberListener(output);

        // Then
        assertNotNull(listener);
    }

    @Test
    void handleTestRunStarted_ShouldCallHandleSuiteStart() throws Exception {
        // Given
        doNothing().when(cucumberListener).handleSuiteStart(any(String.class));
        Method method = CucumberListener.class.getDeclaredMethod("handleTestRunStarted", TestRunStarted.class);
        method.setAccessible(true);

        // When
        method.invoke(cucumberListener, testRunStarted);

        // Then
        verify(cucumberListener).handleSuiteStart("Cucumber test run");
    }

    @Test
    void handleTestRunFinished_ShouldCallHandleSuiteFinish() throws Exception {
        // Given
        doNothing().when(cucumberListener).handleSuiteFinish(any(String.class));
        Method method = CucumberListener.class.getDeclaredMethod("handleTestRunFinished", TestRunFinished.class);
        method.setAccessible(true);

        // When
        method.invoke(cucumberListener, testRunFinished);

        // Then
        verify(cucumberListener).handleSuiteFinish("Cucumber test run");
    }

    @Test
    void handleTestCaseStarted_ShouldLogTestCaseName() throws Exception {
        // Given
        String testCaseName = "Test Scenario";
        when(testCaseStarted.getTestCase()).thenReturn(testCase);
        when(testCase.getName()).thenReturn(testCaseName);

        Method method = CucumberListener.class.getDeclaredMethod("handleTestCaseStarted", TestCaseStarted.class);
        method.setAccessible(true);

        // When
        method.invoke(cucumberListener, testCaseStarted);

        // Then
        verify(testCaseStarted).getTestCase();
        verify(testCase).getName();
    }

    @Test
    void handleTestCaseFinished_WithFailedStatus_ShouldReportFailedTest() throws Exception {
        // Given
        doNothing().when(cucumberListener).reportTest(any(String.class), any(TestMetadata.class), any(String.class), any(Throwable.class));

        String testCaseName = "Test Scenario";
        RuntimeException error = new RuntimeException("Test failed");

        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getName()).thenReturn(testCaseName);
        when(testCaseFinished.getResult()).thenReturn(result);
        when(result.getStatus()).thenReturn(Status.FAILED);
        when(result.getError()).thenReturn(error);
        when(metadataExtractor.extractFromCucumber(testCase)).thenReturn(testMetadata);

        Method method = CucumberListener.class.getDeclaredMethod("handleTestCaseFinished", TestCaseFinished.class);
        method.setAccessible(true);

        // When
        method.invoke(cucumberListener, testCaseFinished);

        // Then
        verify(metadataExtractor).extractFromCucumber(testCase);
        verify(cucumberListener).reportTest(testCaseName, testMetadata, FAILED, error);
    }

    @Test
    void determineTestStatus_WithPassedStatus_ShouldReturnPassed() throws Exception {
        // Given
        when(testCaseFinished.getResult()).thenReturn(result);
        when(result.getStatus()).thenReturn(Status.PASSED);

        Method method = CucumberListener.class.getDeclaredMethod("determineTestStatus", TestCaseFinished.class);
        method.setAccessible(true);

        // When
        String status = (String) method.invoke(cucumberListener, testCaseFinished);

        // Then
        assertEquals(PASSED, status);
    }

    @Test
    void determineTestStatus_WithFailedStatus_ShouldReturnFailed() throws Exception {
        // Given
        when(testCaseFinished.getResult()).thenReturn(result);
        when(result.getStatus()).thenReturn(Status.FAILED);

        Method method = CucumberListener.class.getDeclaredMethod("determineTestStatus", TestCaseFinished.class);
        method.setAccessible(true);

        // When
        String status = (String) method.invoke(cucumberListener, testCaseFinished);

        // Then
        assertEquals(FAILED, status);
    }

    @Test
    void determineTestStatus_WithSkippedStatus_ShouldReturnSkipped() throws Exception {
        // Given
        when(testCaseFinished.getResult()).thenReturn(result);
        when(result.getStatus()).thenReturn(Status.SKIPPED);

        Method method = CucumberListener.class.getDeclaredMethod("determineTestStatus", TestCaseFinished.class);
        method.setAccessible(true);

        // When
        String status = (String) method.invoke(cucumberListener, testCaseFinished);

        // Then
        assertEquals(SKIPPED, status);
    }

    @Test
    void determineTestStatus_WithPendingStatus_ShouldReturnSkipped() throws Exception {
        // Given
        when(testCaseFinished.getResult()).thenReturn(result);
        when(result.getStatus()).thenReturn(Status.PENDING);

        Method method = CucumberListener.class.getDeclaredMethod("determineTestStatus", TestCaseFinished.class);
        method.setAccessible(true);

        // When
        String status = (String) method.invoke(cucumberListener, testCaseFinished);

        // Then
        assertEquals(SKIPPED, status);
    }

    @Test
    void determineTestStatus_WithUndefinedStatus_ShouldReturnSkipped() throws Exception {
        // Given
        when(testCaseFinished.getResult()).thenReturn(result);
        when(result.getStatus()).thenReturn(Status.UNDEFINED);

        Method method = CucumberListener.class.getDeclaredMethod("determineTestStatus", TestCaseFinished.class);
        method.setAccessible(true);

        // When
        String status = (String) method.invoke(cucumberListener, testCaseFinished);

        // Then
        assertEquals(SKIPPED, status);
    }

    @Test
    void determineTestStatus_WithAmbiguousStatus_ShouldReturnSkipped() throws Exception {
        // Given
        when(testCaseFinished.getResult()).thenReturn(result);
        when(result.getStatus()).thenReturn(Status.AMBIGUOUS);

        Method method = CucumberListener.class.getDeclaredMethod("determineTestStatus", TestCaseFinished.class);
        method.setAccessible(true);

        // When
        String status = (String) method.invoke(cucumberListener, testCaseFinished);

        // Then
        assertEquals(SKIPPED, status);
    }

    @Test
    void determineTestStatus_WithNullEvent_ShouldReturnFailed() throws Exception {
        // Given
        Method method = CucumberListener.class.getDeclaredMethod("determineTestStatus", TestCaseFinished.class);
        method.setAccessible(true);

        // When
        String status = (String) method.invoke(cucumberListener, (TestCaseFinished) null);

        // Then
        assertEquals(FAILED, status);
    }

    @Test
    void determineTestStatus_WithNullResult_ShouldReturnFailed() throws Exception {
        // Given
        when(testCaseFinished.getResult()).thenReturn(null);

        Method method = CucumberListener.class.getDeclaredMethod("determineTestStatus", TestCaseFinished.class);
        method.setAccessible(true);

        // When
        String status = (String) method.invoke(cucumberListener, testCaseFinished);

        // Then
        assertEquals(FAILED, status);
    }

    @Test
    void determineTestStatus_WithNullStatus_ShouldReturnFailed() throws Exception {
        // Given
        when(testCaseFinished.getResult()).thenReturn(result);
        when(result.getStatus()).thenReturn(null);

        Method method = CucumberListener.class.getDeclaredMethod("determineTestStatus", TestCaseFinished.class);
        method.setAccessible(true);

        // When
        String status = (String) method.invoke(cucumberListener, testCaseFinished);

        // Then
        assertEquals(FAILED, status);
    }

    @Test
    void setEventPublisher_ShouldRegisterAllEventHandlers() {
        // When
        cucumberListener.setEventPublisher(eventPublisher);

        // Then
        verify(eventPublisher).registerHandlerFor(eq(TestRunStarted.class), any());
        verify(eventPublisher).registerHandlerFor(eq(TestRunFinished.class), any());
        verify(eventPublisher).registerHandlerFor(eq(TestCaseStarted.class), any());
        verify(eventPublisher).registerHandlerFor(eq(TestCaseFinished.class), any());
    }
}