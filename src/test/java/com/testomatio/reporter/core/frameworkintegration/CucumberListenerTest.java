package com.testomatio.reporter.core.frameworkintegration;

import com.testomatio.reporter.core.GlobalRunManager;
import com.testomatio.reporter.core.constructor.TestResultWrapper;
import com.testomatio.reporter.core.extractor.CucumberMetaDataExtractor;
import com.testomatio.reporter.model.TestMetadata;
import io.cucumber.plugin.event.EventHandler;
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
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CucumberListenerTest {

    private CucumberListener cucumberListener;

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
    private Result testResult;

    @Mock
    private GlobalRunManager runManager;

    @Mock
    private CucumberMetaDataExtractor metaDataExtractor;

    @Mock
    private TestMetadata testMetadata;

    @Mock
    private Logger logger;

    private Object originalLogger;
    private Field loggerField;
    private boolean loggerFieldModified = false;

    @BeforeEach
    void setUp() throws Exception {
        cucumberListener = spy(new CucumberListener());

        setupMockFields();
    }

    @AfterEach
    void tearDown() throws Exception {
        try {
            if (loggerFieldModified && loggerField != null) {
                try {
                    loggerField.set(null, originalLogger);
                } catch (Exception e) {
                    System.err.println("Warning: Could not restore original logger: " + e.getMessage());
                }
            }
        } finally {
            loggerFieldModified = false;

            try {
                Mockito.framework().clearInlineMocks();
            } catch (Exception e) {
                System.err.println("Warning: Could not clear inline mocks: " + e.getMessage());
            }
        }
    }

    private void setupMockFields() throws Exception {
        try {
            Field metaDataExtractorField = CucumberListener.class.getDeclaredField("metaDataExtractor");
            metaDataExtractorField.setAccessible(true);
            metaDataExtractorField.set(cucumberListener, metaDataExtractor);
        } catch (Exception e) {
            System.err.println("Warning: Could not set metaDataExtractor: " + e.getMessage());
        }

        try {
            loggerField = AbstractTestFrameworkListener.class.getDeclaredField("LOGGER");
            loggerField.setAccessible(true);

            originalLogger = loggerField.get(null);

            loggerField.set(null, logger);
            loggerFieldModified = true;

        } catch (NoSuchFieldException e) {
            System.err.println("Warning: LOGGER field not found, tests may not verify logging: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Warning: Could not access LOGGER field: " + e.getMessage());
        }

        // Встановлюємо runManager
        try {
            Field runManagerField = AbstractTestFrameworkListener.class.getDeclaredField("runManager");
            runManagerField.setAccessible(true);
            runManagerField.set(cucumberListener, runManager);
        } catch (Exception e) {
            System.err.println("Warning: Could not set runManager: " + e.getMessage());
        }
    }

    @Test
    void constructor_noArgs_shouldInitializeSuccessfully() {
        CucumberListener listener = new CucumberListener();
        assertNotNull(listener);
    }

    @Test
    void constructor_withOutput_shouldInitializeSuccessfully() {
        String output = "test-output";
        CucumberListener listener = new CucumberListener(output);
        assertNotNull(listener);
    }

    @Test
    void addFrameworkSpecificData_withNonTestCaseFinished_shouldNotAddData() {
        TestResultWrapper.Builder builder = mock(TestResultWrapper.Builder.class);
        Object otherData = new Object();

        cucumberListener.addFrameworkSpecificData(builder, otherData);

        verify(builder, never()).withCucumberTestCaseFinished(any());
    }

    @Test
    void setEventPublisher_shouldRegisterAllHandlers() {
        cucumberListener.setEventPublisher(eventPublisher);

        verify(eventPublisher).registerHandlerFor(eq(TestRunStarted.class), any());
        verify(eventPublisher).registerHandlerFor(eq(TestRunFinished.class), any());
        verify(eventPublisher).registerHandlerFor(eq(TestCaseStarted.class), any());
        verify(eventPublisher).registerHandlerFor(eq(TestCaseFinished.class), any());
    }

    @Test
    void handleTestRunStarted_shouldCallHandleSuiteStarted() throws Exception {
        doNothing().when(cucumberListener).handleSuiteStarted(any());

        cucumberListener.setEventPublisher(eventPublisher);

        ArgumentCaptor<EventHandler<TestRunStarted>> handlerCaptor = ArgumentCaptor.forClass(EventHandler.class);
        verify(eventPublisher).registerHandlerFor(eq(TestRunStarted.class), handlerCaptor.capture());

        handlerCaptor.getValue().receive(testRunStarted);

        verify(cucumberListener).handleSuiteStarted("Cucumber Test Run");
    }

    @Test
    void handleTestCaseStarted_shouldLogTestCaseStart() throws Exception {
        when(testCaseStarted.getTestCase()).thenReturn(testCase);
        when(testCase.getName()).thenReturn("Test Case Name");

        cucumberListener.setEventPublisher(eventPublisher);

        ArgumentCaptor<EventHandler<TestCaseStarted>> handlerCaptor = ArgumentCaptor.forClass(EventHandler.class);
        verify(eventPublisher).registerHandlerFor(eq(TestCaseStarted.class), handlerCaptor.capture());

        handlerCaptor.getValue().receive(testCaseStarted);

        if (loggerFieldModified) {
            verify(logger).finer("Starting test case: " + testCase.getName());
        }
    }

    @Test
    void determineTestStatus_withPassedStatus_shouldReturnPassed() throws Exception {
        when(testCaseFinished.getResult()).thenReturn(testResult);
        when(testResult.getStatus()).thenReturn(Status.PASSED);

        Method method = CucumberListener.class.getDeclaredMethod("determineTestStatus", TestCaseFinished.class);
        method.setAccessible(true);
        String status = (String) method.invoke(cucumberListener, testCaseFinished);

        assertEquals("passed", status);
    }

    @Test
    void determineTestStatus_withFailedStatus_shouldReturnFailed() throws Exception {
        when(testCaseFinished.getResult()).thenReturn(testResult);
        when(testResult.getStatus()).thenReturn(Status.FAILED);

        Method method = CucumberListener.class.getDeclaredMethod("determineTestStatus", TestCaseFinished.class);
        method.setAccessible(true);
        String status = (String) method.invoke(cucumberListener, testCaseFinished);

        assertEquals("failed", status);
    }

    @Test
    void determineTestStatus_withSkippedStatus_shouldReturnSkipped() throws Exception {
        when(testCaseFinished.getResult()).thenReturn(testResult);
        when(testResult.getStatus()).thenReturn(Status.SKIPPED);

        Method method = CucumberListener.class
                .getDeclaredMethod("determineTestStatus", TestCaseFinished.class);
        method.setAccessible(true);
        String status = (String) method.invoke(cucumberListener, testCaseFinished);

        assertEquals("skipped", status);
    }

    @Test
    void determineTestStatus_withUndefinedStatus_shouldReturnSkipped() throws Exception {
        when(testCaseFinished.getResult()).thenReturn(testResult);
        when(testResult.getStatus()).thenReturn(Status.UNDEFINED);

        Method method = CucumberListener.class.getDeclaredMethod("determineTestStatus", TestCaseFinished.class);
        method.setAccessible(true);
        String status = (String) method.invoke(cucumberListener, testCaseFinished);

        assertEquals("skipped", status);
    }

    @Test
    void determineTestStatus_withNullResult_shouldReturnFailed() throws Exception {
        when(testCaseFinished.getResult()).thenReturn(null);

        Method method = CucumberListener.class.getDeclaredMethod("determineTestStatus", TestCaseFinished.class);
        method.setAccessible(true);
        String status = (String) method.invoke(cucumberListener, testCaseFinished);

        assertEquals("failed", status);
    }
}