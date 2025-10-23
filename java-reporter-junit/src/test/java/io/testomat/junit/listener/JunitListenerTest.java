package io.testomat.junit.listener;

import static io.testomat.core.constants.ArtifactPropertyNames.ARTIFACT_DISABLE_PROPERTY_NAME;
import static io.testomat.core.constants.CommonConstants.FAILED;
import static io.testomat.core.constants.CommonConstants.PASSED;
import static io.testomat.core.constants.CommonConstants.SKIPPED;
import static io.testomat.core.constants.PropertyNameConstants.API_KEY_PROPERTY_NAME;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.testomat.core.facade.methods.artifact.client.AwsService;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.junit.methodexporter.MethodExportManager;
import io.testomat.junit.reporter.JunitTestReporter;
import java.lang.reflect.Method;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class JunitListenerTest {

    @Mock
    private MethodExportManager methodExportManager;

    @Mock
    private GlobalRunManager runManager;

    @Mock
    private JunitTestReporter reporter;

    @Mock
    private PropertyProvider propertyProvider;

    @Mock
    private AwsService awsService;

    @Mock
    private ExtensionContext context;

    @Mock
    private Method testMethod;

    private JunitListener listener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        listener = new JunitListener(methodExportManager, runManager, reporter, propertyProvider, awsService);
    }

    @Test
    void beforeAll_WhenListeningNotRequired_ShouldSkipIncrement() {
        when(propertyProvider.getProperty(API_KEY_PROPERTY_NAME)).thenReturn(null);

        listener.beforeAll(context);

        verify(runManager, never()).incrementSuiteCounter();
    }

    @Test
    void beforeAll_WhenListeningRequired_ShouldIncrementSuiteCounter() {
        when(propertyProvider.getProperty(API_KEY_PROPERTY_NAME)).thenReturn("test-api-key");

        listener.beforeAll(context);

        verify(runManager).incrementSuiteCounter();
    }

    @Test
    void beforeAll_WhenPropertyProviderThrowsException_ShouldSkipIncrement() {
        when(propertyProvider.getProperty(API_KEY_PROPERTY_NAME)).thenThrow(new RuntimeException("Property error"));

        listener.beforeAll(context);

        verify(runManager, never()).incrementSuiteCounter();
    }

    @Test
    void afterAll_WhenListeningNotRequired_ShouldSkipProcessing() {
        when(propertyProvider.getProperty(API_KEY_PROPERTY_NAME)).thenReturn(null);

        listener.afterAll(context);

        verify(runManager, never()).decrementSuiteCounter();
        verifyNoInteractions(methodExportManager);
    }

    @Test
    void afterAll_WhenListeningRequired_ShouldDecrementCounterAndExportClass() {
        when(propertyProvider.getProperty(API_KEY_PROPERTY_NAME)).thenReturn("test-api-key");
        when(context.getTestClass()).thenReturn(Optional.of(String.class));

        listener.afterAll(context);

        verify(runManager).decrementSuiteCounter();
        verify(methodExportManager).loadTestBodyForClass(String.class);
    }

    @Test
    void afterAll_WhenTestClassEmpty_ShouldDecrementCounterOnly() {
        when(propertyProvider.getProperty(API_KEY_PROPERTY_NAME)).thenReturn("test-api-key");
        when(context.getTestClass()).thenReturn(Optional.empty());

        listener.afterAll(context);

        verify(runManager).decrementSuiteCounter();
        verify(methodExportManager, never()).loadTestBodyForClass(any());
    }

    @Test
    void beforeEach_ShouldNotPerformAnyActions() {
        assertDoesNotThrow(() -> listener.beforeEach(context));
        verifyNoInteractions(runManager, reporter, methodExportManager, awsService);
    }

    @Test
    void testDisabled_WhenListeningNotRequired_ShouldSkipReporting() {
        when(propertyProvider.getProperty(API_KEY_PROPERTY_NAME)).thenReturn(null);

        listener.testDisabled(context, Optional.of("Test disabled"));

        verifyNoInteractions(reporter, methodExportManager);
    }

    @Test
    void testDisabled_WhenListeningRequired_ShouldReportSkippedWithReason() {
        when(propertyProvider.getProperty(API_KEY_PROPERTY_NAME)).thenReturn("test-api-key");
        when(context.getTestClass()).thenReturn(Optional.of(String.class));

        listener.testDisabled(context, Optional.of("Test disabled"));

        verify(reporter).reportTestResult(context, SKIPPED, "Test disabled");
        verify(methodExportManager).loadTestBodyForClass(String.class);
    }

    @Test
    void testDisabled_WhenReasonEmpty_ShouldReportSkippedWithDefaultMessage() {
        when(propertyProvider.getProperty(API_KEY_PROPERTY_NAME)).thenReturn("test-api-key");
        when(context.getTestClass()).thenReturn(Optional.of(String.class));

        listener.testDisabled(context, Optional.empty());

        verify(reporter).reportTestResult(context, SKIPPED, "Test disabled");
        verify(methodExportManager).loadTestBodyForClass(String.class);
    }

    @Test
    void testSuccessful_WhenListeningNotRequired_ShouldSkipReporting() {
        when(propertyProvider.getProperty(API_KEY_PROPERTY_NAME)).thenReturn(null);

        listener.testSuccessful(context);

        verifyNoInteractions(reporter, methodExportManager);
    }

    @Test
    void testSuccessful_WhenListeningRequired_ShouldReportPassed() {
        when(propertyProvider.getProperty(API_KEY_PROPERTY_NAME)).thenReturn("test-api-key");
        when(context.getTestClass()).thenReturn(Optional.of(String.class));

        listener.testSuccessful(context);

        verify(reporter).reportTestResult(context, PASSED, null);
        verify(methodExportManager).loadTestBodyForClass(String.class);
    }

    @Test
    void testAborted_WhenListeningNotRequired_ShouldSkipReporting() {
        when(propertyProvider.getProperty(API_KEY_PROPERTY_NAME)).thenReturn(null);
        Throwable cause = new RuntimeException("Test aborted");

        listener.testAborted(context, cause);

        verifyNoInteractions(reporter, methodExportManager);
    }

    @Test
    void testAborted_WhenListeningRequired_ShouldReportSkippedWithCauseMessage() {
        when(propertyProvider.getProperty(API_KEY_PROPERTY_NAME)).thenReturn("test-api-key");
        when(context.getTestClass()).thenReturn(Optional.of(String.class));
        Throwable cause = new RuntimeException("Test aborted");

        listener.testAborted(context, cause);

        verify(reporter).reportTestResult(context, SKIPPED, "Test aborted");
        verify(methodExportManager).loadTestBodyForClass(String.class);
    }

    @Test
    void testFailed_WhenListeningNotRequired_ShouldSkipReporting() {
        when(propertyProvider.getProperty(API_KEY_PROPERTY_NAME)).thenReturn(null);
        Throwable cause = new AssertionError("Test failed");

        listener.testFailed(context, cause);

        verifyNoInteractions(reporter, methodExportManager);
    }

    @Test
    void testFailed_WhenListeningRequired_ShouldReportFailedWithCauseMessage() {
        when(propertyProvider.getProperty(API_KEY_PROPERTY_NAME)).thenReturn("test-api-key");
        when(context.getTestClass()).thenReturn(Optional.of(String.class));
        Throwable cause = new AssertionError("Test failed");

        listener.testFailed(context, cause);

        verify(reporter).reportTestResult(context, FAILED, "Test failed");
        verify(methodExportManager).loadTestBodyForClass(String.class);
    }

    @Test
    void exportTestClassIfNotProcessed_WhenSameClassCalledMultipleTimes_ShouldExportOnlyOnce() {
        when(propertyProvider.getProperty(API_KEY_PROPERTY_NAME)).thenReturn("test-api-key");
        when(context.getTestClass()).thenReturn(Optional.of(String.class));

        listener.testSuccessful(context);
        listener.testSuccessful(context);
        listener.testFailed(context, new Exception("Failed"));

        verify(methodExportManager, times(1)).loadTestBodyForClass(String.class);
    }

    @Test
    void afterEach_WhenArtifactsEnabled_ShouldUploadArtifacts() {
        when(context.getDisplayName()).thenReturn("testName");
        when(context.getUniqueId()).thenReturn("uniqueId");
        when(context.getTestMethod()).thenReturn(Optional.of(testMethod));

        listener.afterEach(context);

        verify(awsService).uploadAllArtifactsForTest(eq("testName"), eq("uniqueId"), any());
    }

    @Test
    void defineArtifactsDisabled_WhenPropertyIsNull_ShouldReturnFalse() {
        when(propertyProvider.getProperty(ARTIFACT_DISABLE_PROPERTY_NAME)).thenReturn(null);

        JunitListener testListener = new JunitListener(methodExportManager, runManager, reporter, propertyProvider, awsService);

        when(context.getDisplayName()).thenReturn("testName");
        when(context.getUniqueId()).thenReturn("uniqueId");
        when(context.getTestMethod()).thenReturn(Optional.of(testMethod));

        testListener.afterEach(context);

        verify(awsService).uploadAllArtifactsForTest(anyString(), anyString(), any());
    }

    @Test
    void defineArtifactsDisabled_WhenPropertyIsEmpty_ShouldReturnFalse() {
        when(propertyProvider.getProperty(ARTIFACT_DISABLE_PROPERTY_NAME)).thenReturn("");

        JunitListener testListener = new JunitListener(methodExportManager, runManager, reporter, propertyProvider, awsService);

        when(context.getDisplayName()).thenReturn("testName");
        when(context.getUniqueId()).thenReturn("uniqueId");
        when(context.getTestMethod()).thenReturn(Optional.of(testMethod));

        testListener.afterEach(context);

        verify(awsService).uploadAllArtifactsForTest(anyString(), anyString(), any());
    }

    @Test
    void defineArtifactsDisabled_WhenPropertyIsZero_ShouldReturnFalse() {
        when(propertyProvider.getProperty(ARTIFACT_DISABLE_PROPERTY_NAME)).thenReturn("0");

        JunitListener testListener = new JunitListener(methodExportManager, runManager, reporter, propertyProvider, awsService);

        when(context.getDisplayName()).thenReturn("testName");
        when(context.getUniqueId()).thenReturn("uniqueId");
        when(context.getTestMethod()).thenReturn(Optional.of(testMethod));

        testListener.afterEach(context);

        verify(awsService).uploadAllArtifactsForTest(anyString(), anyString(), any());
    }

    @Test
    void defineArtifactsDisabled_WhenPropertyIsTrue_ShouldReturnTrue() {
        when(propertyProvider.getProperty(ARTIFACT_DISABLE_PROPERTY_NAME)).thenReturn("true");

        JunitListener testListener = new JunitListener(methodExportManager, runManager, reporter, propertyProvider, awsService);

        when(context.getDisplayName()).thenReturn("testName");
        when(context.getUniqueId()).thenReturn("uniqueId");
        when(context.getTestMethod()).thenReturn(Optional.of(testMethod));

        testListener.afterEach(context);

        verify(awsService).uploadAllArtifactsForTest(anyString(), anyString(), any());
    }

    @Test
    void defineArtifactsDisabled_WhenPropertyIsAnyNonZeroValue_ShouldReturnTrue() {
        when(propertyProvider.getProperty(ARTIFACT_DISABLE_PROPERTY_NAME)).thenReturn("disable");

        JunitListener testListener = new JunitListener(methodExportManager, runManager, reporter, propertyProvider, awsService);

        when(context.getDisplayName()).thenReturn("testName");
        when(context.getUniqueId()).thenReturn("uniqueId");
        when(context.getTestMethod()).thenReturn(Optional.of(testMethod));

        testListener.afterEach(context);

        verify(awsService).uploadAllArtifactsForTest(anyString(), anyString(), any());
    }

    @Test
    void defineArtifactsDisabled_WhenPropertyProviderThrowsException_ShouldDefaultToFalse() {
        when(propertyProvider.getProperty(ARTIFACT_DISABLE_PROPERTY_NAME)).thenThrow(new RuntimeException("Property error"));

        JunitListener testListener = new JunitListener(methodExportManager, runManager, reporter, propertyProvider, awsService);

        when(context.getDisplayName()).thenReturn("testName");
        when(context.getUniqueId()).thenReturn("uniqueId");
        when(context.getTestMethod()).thenReturn(Optional.of(testMethod));

        testListener.afterEach(context);

        verify(awsService).uploadAllArtifactsForTest(anyString(), anyString(), any());
    }

    @Test
    void defaultConstructor_ShouldInitializeAllDependencies() {
        JunitListener defaultListener = new JunitListener();

        assertDoesNotThrow(() -> {
            when(context.getTestClass()).thenReturn(Optional.of(String.class));
            defaultListener.testSuccessful(context);
        });
    }

    @Test
    void testMethodReporting_WhenReporterThrowsException_ShouldPropagateException() {
        when(propertyProvider.getProperty(API_KEY_PROPERTY_NAME)).thenReturn("test-api-key");
        when(context.getTestClass()).thenReturn(Optional.of(String.class));
        doThrow(new RuntimeException("Reporter error")).when(reporter).reportTestResult(any(), any(), any());

        RuntimeException exception = org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
            () -> listener.testSuccessful(context));

        org.junit.jupiter.api.Assertions.assertEquals("Reporter error", exception.getMessage());
    }

    @Test
    void testMethodExport_WhenMethodExportManagerThrowsException_ShouldPropagateException() {
        when(propertyProvider.getProperty(API_KEY_PROPERTY_NAME)).thenReturn("test-api-key");
        when(context.getTestClass()).thenReturn(Optional.of(String.class));
        doThrow(new RuntimeException("Export error")).when(methodExportManager).loadTestBodyForClass(any());

        RuntimeException exception = org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
            () -> listener.testSuccessful(context));

        org.junit.jupiter.api.Assertions.assertEquals("Export error", exception.getMessage());
        verify(reporter).reportTestResult(context, PASSED, null);
    }
}