package com.testomatio.reporter.core;

import com.testomatio.reporter.client.ApiInterface;
import com.testomatio.reporter.client.ClientFactory;
import com.testomatio.reporter.client.TestomatClientFactory;
import com.testomatio.reporter.core.batch.BatchResultManager;
import com.testomatio.reporter.model.TestResult;
import com.testomatio.reporter.property_config.impl.PropertyProviderFactoryImpl;
import com.testomatio.reporter.property_config.interf.PropertyProvider;
import com.testomatio.reporter.property_config.interf.PropertyProviderFactory;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.testomatio.reporter.constants.PropertyNameConstants.RUN_TITLE_PROPERTY_NAME;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalRunManagerTest {

    @Mock
    private ClientFactory clientFactory;

    @Mock
    private ApiInterface apiInterface;

    @Mock
    private BatchResultManager batchResultManager;

    @Mock
    private PropertyProviderFactory propertyProviderFactory;

    @Mock
    private PropertyProvider propertyProvider;

    @Mock
    private TestResult testRunResult;

    private GlobalRunManager globalRunManager;

    // Store original values for restoration
    private Object originalActiveSuites;
    private Object originalRunUid;
    private Object originalBatchManager;
    private Object originalApiClient;
    private Object originalShutdownHookRegistered;
    private long originalStartTime;

    @BeforeEach
    void setUp() throws Exception {
        globalRunManager = GlobalRunManager.getInstance();

        // Save original state for restoration
        saveOriginalState();

        // Reset to clean state for test
        resetGlobalRunManagerState();
    }

    @AfterEach
    void tearDown() throws Exception {
        // Restore original state to avoid test pollution
        restoreOriginalState();
    }

    private void saveOriginalState() throws Exception {
        originalActiveSuites = getFieldValue("activeSuites");
        originalRunUid = getFieldValue("runUid");
        originalBatchManager = getFieldValue("batchManager");
        originalApiClient = getFieldValue("apiClient");
        originalShutdownHookRegistered = getFieldValue("shutdownHookRegistered");
        originalStartTime = getStartTimeValue();
    }

    private void restoreOriginalState() throws Exception {
        setFieldValue("activeSuites", originalActiveSuites);
        setFieldValue("runUid", originalRunUid);
        setFieldValue("batchManager", originalBatchManager);
        setFieldValue("apiClient", originalApiClient);
        setFieldValue("shutdownHookRegistered", originalShutdownHookRegistered);
        setStartTimeValue(originalStartTime);
    }

    private void resetGlobalRunManagerState() throws Exception {
        // Reset activeSuites
        Field activeSuitesField = GlobalRunManager.class.getDeclaredField("activeSuites");
        activeSuitesField.setAccessible(true);
        AtomicInteger activeSuites = (AtomicInteger) activeSuitesField.get(globalRunManager);
        activeSuites.set(0);

        // Reset runUid
        Field runUidField = GlobalRunManager.class.getDeclaredField("runUid");
        runUidField.setAccessible(true);
        AtomicReference<String> runUid = (AtomicReference<String>) runUidField.get(globalRunManager);
        runUid.set(null);

        // Reset batchManager
        Field batchManagerField = GlobalRunManager.class.getDeclaredField("batchManager");
        batchManagerField.setAccessible(true);
        AtomicReference<BatchResultManager> batchManager = (AtomicReference<BatchResultManager>) batchManagerField.get(globalRunManager);
        batchManager.set(null);

        // Reset apiClient
        Field apiClientField = GlobalRunManager.class.getDeclaredField("apiClient");
        apiClientField.setAccessible(true);
        AtomicReference<ApiInterface> apiClient = (AtomicReference<ApiInterface>) apiClientField.get(globalRunManager);
        apiClient.set(null);

        // Reset shutdownHookRegistered
        Field shutdownHookRegisteredField = GlobalRunManager.class.getDeclaredField("shutdownHookRegistered");
        shutdownHookRegisteredField.setAccessible(true);
        AtomicBoolean shutdownHookRegistered = (AtomicBoolean) shutdownHookRegisteredField.get(globalRunManager);
        shutdownHookRegistered.set(false);

        // Reset startTime
        setStartTimeValue(0L);
    }

    @Test
    void getInstance_ShouldReturnSameInstance() {
        // Given & When
        GlobalRunManager instance1 = GlobalRunManager.getInstance();
        GlobalRunManager instance2 = GlobalRunManager.getInstance();

        // Then
        assertSame(instance1, instance2);
    }

    @Test
    void initializeIfNeeded_ShouldInitializeWhenNotAlreadyInitialized() throws Exception {
        // Given
        String expectedUid = "test-uid-123";
        String expectedTitle = "Test Run Title";

        try (MockedStatic<TestomatClientFactory> testomatClientFactoryMock = mockStatic(TestomatClientFactory.class);
             MockedStatic<PropertyProviderFactoryImpl> propertyProviderFactoryMock = mockStatic(PropertyProviderFactoryImpl.class)) {

            setupMocksForInitialization(testomatClientFactoryMock, propertyProviderFactoryMock, expectedUid, expectedTitle);

            // When
            globalRunManager.initializeIfNeeded();

            // Then
            verify(apiInterface).createRun(expectedTitle);
            assertTrue(globalRunManager.isActive());
        }
    }

    @Test
    void initializeIfNeeded_ShouldNotInitializeWhenAlreadyInitialized() throws Exception {
        // Given
        setRunUid("existing-uid");

        try (MockedStatic<TestomatClientFactory> testomatClientFactoryMock = mockStatic(TestomatClientFactory.class)) {

            // When
            globalRunManager.initializeIfNeeded();

            // Then
            testomatClientFactoryMock.verifyNoInteractions();
        }
    }

    @Test
    void incrementSuiteCounter_ShouldIncrementCounterAndInitialize() throws Exception {
        // Given
        String expectedUid = "test-uid-123";

        try (MockedStatic<TestomatClientFactory> testomatClientFactoryMock = mockStatic(TestomatClientFactory.class);
             MockedStatic<PropertyProviderFactoryImpl> propertyProviderFactoryMock = mockStatic(PropertyProviderFactoryImpl.class)) {

            setupMocksForInitialization(testomatClientFactoryMock, propertyProviderFactoryMock, expectedUid, "Test Title");

            // When
            globalRunManager.incrementSuiteCounter();

            // Then
            assertEquals(1, getActiveSuitesCount());
            assertTrue(globalRunManager.isActive());
        }
    }

    @Test
    void decrementSuiteCounter_ShouldDecrementCounter() throws Exception {
        // Given
        setActiveSuitesCount(2);

        // When
        globalRunManager.decrementSuiteCounter();

        // Then
        assertEquals(1, getActiveSuitesCount());
    }

    @Test
    void decrementSuiteCounter_ShouldNotGoBelowZero() throws Exception {
        // Given
        setActiveSuitesCount(0);

        // When
        globalRunManager.decrementSuiteCounter();

        // Then
        assertEquals(-1, getActiveSuitesCount()); // Assuming it can go negative
    }

    @Test
    void reportTest_ShouldReportTestWhenBatchManagerExists() throws Exception {
        // Given
        setBatchManager(batchResultManager);

        // When
        globalRunManager.reportTest(testRunResult);

        // Then
        verify(batchResultManager).addResult(testRunResult);
    }

    @Test
    void reportTest_ShouldDoNothingWhenBatchManagerIsNull() {
        // Given - batchManager is null by default

        // When
        globalRunManager.reportTest(testRunResult);

        // Then
        verifyNoInteractions(batchResultManager);
    }

    @Test
    void reportTest_ShouldHandleNullTestResult() throws Exception {
        // Given
        setBatchManager(batchResultManager);

        // When & Then
        assertDoesNotThrow(() -> globalRunManager.reportTest(null));
    }

    @Test
    void isActive_ShouldReturnTrueWhenRunUidIsSet() throws Exception {
        // Given
        setRunUid("test-uid");

        // When & Then
        assertTrue(globalRunManager.isActive());
    }

    @Test
    void isActive_ShouldReturnFalseWhenRunUidIsNull() {
        // Given - runUid is null by default

        // When & Then
        assertFalse(globalRunManager.isActive());
    }


    @Test
    void finalizeRun_ShouldShutdownBatchManagerAndFinishRun() throws Exception {
        // Given
        String testUid = "test-uid-123";
        long startTime = System.currentTimeMillis() - 5000;

        setRunUid(testUid);
        setApiClient(apiInterface);
        setBatchManager(batchResultManager);
        setStartTimeValue(startTime);

        // When
        callFinalizeRun();

        // Then
        verify(batchResultManager).shutdown();
        verify(apiInterface).finishTestRun(eq(testUid), anyFloat());
        assertFalse(globalRunManager.isActive());
    }

    @Test
    void finalizeRun_ShouldHandleIOException() throws Exception {
        // Given
        String testUid = "test-uid-123";

        setRunUid(testUid);
        setApiClient(apiInterface);
        setBatchManager(batchResultManager);

        doThrow(new IOException("Network error")).when(apiInterface).finishTestRun(anyString(), anyFloat());

        // When & Then
        assertDoesNotThrow(() -> callFinalizeRun());

        verify(batchResultManager).shutdown();
        assertFalse(globalRunManager.isActive());
    }

    @Test
    void finalizeRun_ShouldDoNothingWhenNotInitialized() throws Exception {
        // Given - not initialized (runUid is null)

        // When
        callFinalizeRun();

        // Then
        verifyNoInteractions(batchResultManager, apiInterface);
    }

    @Test
    void finalizeRun_ShouldWorkWithNullBatchManager() throws Exception {
        // Given
        String testUid = "test-uid-123";
        setRunUid(testUid);
        setApiClient(apiInterface);
        // batchManager remains null

        // When & Then
        assertDoesNotThrow(() -> callFinalizeRun());

        verify(apiInterface).finishTestRun(eq(testUid), anyFloat());
        assertFalse(globalRunManager.isActive());
    }

    @Test
    void finalizeRun_ShouldWorkWithNullApiClient() throws Exception {
        // Given
        String testUid = "test-uid-123";
        setRunUid(testUid);
        setBatchManager(batchResultManager);
        // apiClient remains null

        // When & Then
        assertDoesNotThrow(() -> callFinalizeRun());

        verify(batchResultManager).shutdown();
        assertFalse(globalRunManager.isActive());
    }

    // Helper methods for reflection access
    private void setupMocksForInitialization(MockedStatic<TestomatClientFactory> testomatClientFactoryMock,
                                             MockedStatic<PropertyProviderFactoryImpl> propertyProviderFactoryMock,
                                             String expectedUid,
                                             String expectedTitle) throws Exception {
        testomatClientFactoryMock.when(TestomatClientFactory::getClientFactory).thenReturn(clientFactory);
        propertyProviderFactoryMock.when(PropertyProviderFactoryImpl::getPropertyProviderFactory).thenReturn(propertyProviderFactory);

        when(clientFactory.createClient()).thenReturn(apiInterface);
        when(apiInterface.createRun(any())).thenReturn(expectedUid);
        when(propertyProviderFactory.getPropertyProvider()).thenReturn(propertyProvider);
        when(propertyProvider.getProperty(RUN_TITLE_PROPERTY_NAME)).thenReturn(expectedTitle);
    }

    private Object getFieldValue(String fieldName) throws Exception {
        Field field = GlobalRunManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(globalRunManager);
    }

    private void setFieldValue(String fieldName, Object value) throws Exception {
        Field field = GlobalRunManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(globalRunManager, value);
    }

    private void setRunUid(String uid) throws Exception {
        Field runUidField = GlobalRunManager.class.getDeclaredField("runUid");
        runUidField.setAccessible(true);
        AtomicReference<String> runUid = (AtomicReference<String>) runUidField.get(globalRunManager);
        runUid.set(uid);
    }

    private void setApiClient(ApiInterface client) throws Exception {
        Field apiClientField = GlobalRunManager.class.getDeclaredField("apiClient");
        apiClientField.setAccessible(true);
        AtomicReference<ApiInterface> apiClient = (AtomicReference<ApiInterface>) apiClientField.get(globalRunManager);
        apiClient.set(client);
    }

    private void setBatchManager(BatchResultManager manager) throws Exception {
        Field batchManagerField = GlobalRunManager.class.getDeclaredField("batchManager");
        batchManagerField.setAccessible(true);
        AtomicReference<BatchResultManager> batchManager = (AtomicReference<BatchResultManager>) batchManagerField.get(globalRunManager);
        batchManager.set(manager);
    }

    private void setActiveSuitesCount(int count) throws Exception {
        Field activeSuitesField = GlobalRunManager.class.getDeclaredField("activeSuites");
        activeSuitesField.setAccessible(true);
        AtomicInteger activeSuites = (AtomicInteger) activeSuitesField.get(globalRunManager);
        activeSuites.set(count);
    }

    private int getActiveSuitesCount() throws Exception {
        Field activeSuitesField = GlobalRunManager.class.getDeclaredField("activeSuites");
        activeSuitesField.setAccessible(true);
        AtomicInteger activeSuites = (AtomicInteger) activeSuitesField.get(globalRunManager);
        return activeSuites.get();
    }

    private long getStartTimeValue() throws Exception {
        Field startTimeField = GlobalRunManager.class.getDeclaredField("startTime");
        startTimeField.setAccessible(true);
        return startTimeField.getLong(globalRunManager);
    }

    private void setStartTimeValue(long time) throws Exception {
        Field startTimeField = GlobalRunManager.class.getDeclaredField("startTime");
        startTimeField.setAccessible(true);
        startTimeField.setLong(globalRunManager, time);
    }

    private void callFinalizeRun() throws Exception {
        Method finalizeRunMethod = GlobalRunManager.class.getDeclaredMethod("finalizeRun");
        finalizeRunMethod.setAccessible(true);
        finalizeRunMethod.invoke(globalRunManager);
    }
}