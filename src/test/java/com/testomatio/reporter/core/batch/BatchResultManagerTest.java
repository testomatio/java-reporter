package com.testomatio.reporter.core.batch;

import com.testomatio.reporter.client.ApiInterface;
import com.testomatio.reporter.model.TestResult;
import com.testomatio.reporter.property_config.impl.PropertyProviderFactoryImpl;
import com.testomatio.reporter.property_config.interf.PropertyProvider;
import com.testomatio.reporter.property_config.interf.PropertyProviderFactory;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.testomatio.reporter.constants.PropertyNameConstants.BATCH_FLUSH_INTERVAL_PROPERTY_NAME;
import static com.testomatio.reporter.constants.PropertyNameConstants.BATCH_SIZE_PROPERTY_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchResultManagerTest {

    private BatchResultManager batchResultManager;
    private AutoCloseable mockitoCloseable;

    @Mock
    private ApiInterface apiClient;

    @Mock
    private PropertyProviderFactory propertyProviderFactory;

    @Mock
    private PropertyProvider propertyProvider;

    @Mock
    private TestResult testRunResult1;

    @Mock
    private TestResult testRunResult2;

    @Mock
    private TestResult testRunResult3;

    private static final String TEST_RUN_UID = "test-run-uid-123";

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        resetAllMocks();
    }

    @AfterEach
    void tearDown() throws Exception {
        resetAllMocks();

        if (batchResultManager != null) {
            try {
                batchResultManager.shutdown();
            } catch (Exception e) {
            }
        }

        if (mockitoCloseable != null) {
            mockitoCloseable.close();
        }
    }

    private void resetAllMocks() {
        reset(apiClient, propertyProviderFactory, propertyProvider,
                testRunResult1, testRunResult2, testRunResult3);
    }

    @Test
    void constructor_WithDefaultProperties_ShouldUseDefaultValues() throws Exception {
        // Given
        try (MockedStatic<PropertyProviderFactoryImpl> factoryMock = mockStatic(PropertyProviderFactoryImpl.class)) {
            factoryMock.when(PropertyProviderFactoryImpl::getPropertyProviderFactory).thenReturn(propertyProviderFactory);
            when(propertyProviderFactory.getPropertyProvider()).thenReturn(propertyProvider);
            when(propertyProvider.getProperty(BATCH_SIZE_PROPERTY_NAME)).thenReturn(null);
            when(propertyProvider.getProperty(BATCH_FLUSH_INTERVAL_PROPERTY_NAME)).thenReturn(null);

            // When
            batchResultManager = new BatchResultManager(apiClient, TEST_RUN_UID);

            // Then
            assertEquals(10, getBatchSize(batchResultManager)); // Default batch size
            assertNotNull(getScheduler(batchResultManager));
            assertTrue(isActive(batchResultManager));
        }
    }

    @Test
    void constructor_WithCustomProperties_ShouldUseCustomValues() throws Exception {
        // Given
        try (MockedStatic<PropertyProviderFactoryImpl> factoryMock = mockStatic(PropertyProviderFactoryImpl.class)) {
            factoryMock.when(PropertyProviderFactoryImpl::getPropertyProviderFactory).thenReturn(propertyProviderFactory);
            when(propertyProviderFactory.getPropertyProvider()).thenReturn(propertyProvider);
            when(propertyProvider.getProperty(BATCH_SIZE_PROPERTY_NAME)).thenReturn("5");
            when(propertyProvider.getProperty(BATCH_FLUSH_INTERVAL_PROPERTY_NAME)).thenReturn("10");

            // When
            batchResultManager = new BatchResultManager(apiClient, TEST_RUN_UID);

            // Then
            assertEquals(5, getBatchSize(batchResultManager));
            assertNotNull(getScheduler(batchResultManager));
            assertTrue(isActive(batchResultManager));
        }
    }

    @Test
    void addResult_SingleResult_ShouldAddToPendingList() throws Exception {
        // Given
        setupManagerWithDefaults();
        when(testRunResult1.getTitle()).thenReturn("Test 1");

        // When
        batchResultManager.addResult(testRunResult1);

        // Then
        assertEquals(1, getPendingResults(batchResultManager).size());
        verify(apiClient, never()).reportTest(anyString(), any(TestResult.class));
    }

    @Test
    void addResult_WhenBatchSizeReached_ShouldFlushAutomatically() throws Exception {
        // Given
        setupManagerWithBatchSize(2);
        when(testRunResult1.getTitle()).thenReturn("Test 1");
        when(testRunResult2.getTitle()).thenReturn("Test 2");

        // When
        batchResultManager.addResult(testRunResult1);
        batchResultManager.addResult(testRunResult2);

        // Then
        assertEquals(0, getPendingResults(batchResultManager).size()); // Should be flushed
        verify(apiClient).reportTests(eq(TEST_RUN_UID), anyList());
    }

    @Test
    void addResult_WhenInactive_ShouldSkipResult() throws Exception {
        // Given
        setupManagerWithDefaults();
        setInactive(batchResultManager);
        when(testRunResult1.getTitle()).thenReturn("Test 1");

        // When
        batchResultManager.addResult(testRunResult1);

        // Then
        assertEquals(0, getPendingResults(batchResultManager).size());
        verify(apiClient, never()).reportTest(anyString(), any(TestResult.class));
    }

    @Test
    void flushPendingResults_WithPendingResults_ShouldSendBatch() throws Exception {
        // Given
        setupManagerWithDefaults();
        when(testRunResult1.getTitle()).thenReturn("Test 1");
        batchResultManager.addResult(testRunResult1);

        // When
        batchResultManager.flushPendingResults();

        // Then
        assertEquals(0, getPendingResults(batchResultManager).size());
        verify(apiClient).reportTest(TEST_RUN_UID, testRunResult1);
    }

    @Test
    void flushPendingResults_WithEmptyPendingList_ShouldDoNothing() throws Exception {
        // Given
        setupManagerWithDefaults();

        // When
        batchResultManager.flushPendingResults();

        // Then
        verify(apiClient, never()).reportTest(anyString(), any(TestResult.class));
        verify(apiClient, never()).reportTests(anyString(), anyList());
    }

    @Test
    void sendBatch_SingleResult_ShouldCallReportTest() throws Exception {
        // Given
        setupManagerWithDefaults();
        when(testRunResult1.getTitle()).thenReturn("Test 1");

        // When
        invokeSendBatch(batchResultManager, List.of(testRunResult1), 1);

        // Then
        verify(apiClient).reportTest(TEST_RUN_UID, testRunResult1);
        verify(apiClient, never()).reportTests(anyString(), anyList());
    }

    @Test
    void sendBatch_MultipleResults_ShouldCallReportTests() throws Exception {
        // Given
        setupManagerWithDefaults();
        when(testRunResult1.getTitle()).thenReturn("Test 1");
        when(testRunResult2.getTitle()).thenReturn("Test 2");

        // When
        invokeSendBatch(batchResultManager, List.of(testRunResult1, testRunResult2), 1);

        // Then
        verify(apiClient).reportTests(eq(TEST_RUN_UID), anyList());
        verify(apiClient, never()).reportTest(anyString(), any(TestResult.class));
    }

    @Test
    void sendBatch_WithIOException_ShouldRetry() throws Exception {
        // Given
        setupManagerWithDefaults();
        when(testRunResult1.getTitle()).thenReturn("Test 1");
        doThrow(new IOException("Network error")).when(apiClient).reportTest(anyString(), any(TestResult.class));

        // When
        invokeSendBatch(batchResultManager, List.of(testRunResult1), 1);

        Thread.sleep(100);

        // Then
        verify(apiClient).reportTest(TEST_RUN_UID, testRunResult1);
    }

    @Test
    void sendBatch_WithMaxRetries_ShouldAddToFailedResults() throws Exception {
        // Given
        setupManagerWithDefaults();
        when(testRunResult1.getTitle()).thenReturn("Test 1");
        doThrow(new IOException("Network error")).when(apiClient).reportTest(anyString(), any(TestResult.class));

        // When
        invokeSendBatch(batchResultManager, List.of(testRunResult1), 3); // Max retry attempt

        // Then
        verify(apiClient).reportTest(TEST_RUN_UID, testRunResult1);
        assertEquals(1, getFailedResults(batchResultManager).size());
    }

    @Test
    void shutdown_ShouldSetInactiveAndFlush() throws Exception {
        // Given
        setupManagerWithDefaults();
        when(testRunResult1.getTitle()).thenReturn("Test 1");
        batchResultManager.addResult(testRunResult1);

        // When
        batchResultManager.shutdown();

        // Then
        assertFalse(isActive(batchResultManager));
        assertEquals(0, getPendingResults(batchResultManager).size()); // Should be flushed
        assertTrue(getScheduler(batchResultManager).isShutdown());
    }

    @Test
    void shutdown_WithFailedResults_ShouldLogWarning() throws Exception {
        // Given
        setupManagerWithDefaults();
        when(testRunResult1.getTitle()).thenReturn("Test 1");

        getFailedResults(batchResultManager).add(testRunResult1);

        // When
        batchResultManager.shutdown();

        // Then
        assertFalse(isActive(batchResultManager));
        assertEquals(1, getFailedResults(batchResultManager).size());
    }

    @Test
    void addResult_MultipleCalls_ShouldMaintainOrder() throws Exception {
        // Given
        setupManagerWithDefaults();
        when(testRunResult1.getTitle()).thenReturn("Test 1");
        when(testRunResult2.getTitle()).thenReturn("Test 2");
        when(testRunResult3.getTitle()).thenReturn("Test 3");

        // When
        batchResultManager.addResult(testRunResult1);
        batchResultManager.addResult(testRunResult2);
        batchResultManager.addResult(testRunResult3);

        // Then
        List<TestResult> pending = getPendingResults(batchResultManager);
        assertEquals(3, pending.size());
        assertEquals(testRunResult1, pending.get(0));
        assertEquals(testRunResult2, pending.get(1));
        assertEquals(testRunResult3, pending.get(2));
    }

    // Helper methods for reflection access
    private void setupManagerWithDefaults() {
        try (MockedStatic<PropertyProviderFactoryImpl> factoryMock = mockStatic(PropertyProviderFactoryImpl.class)) {
            factoryMock.when(PropertyProviderFactoryImpl::getPropertyProviderFactory).thenReturn(propertyProviderFactory);
            when(propertyProviderFactory.getPropertyProvider()).thenReturn(propertyProvider);
            when(propertyProvider.getProperty(BATCH_SIZE_PROPERTY_NAME)).thenReturn(null);
            when(propertyProvider.getProperty(BATCH_FLUSH_INTERVAL_PROPERTY_NAME)).thenReturn(null);

            batchResultManager = new BatchResultManager(apiClient, TEST_RUN_UID);
        }
    }

    private void setupManagerWithBatchSize(int batchSize) {
        try (MockedStatic<PropertyProviderFactoryImpl> factoryMock = mockStatic(PropertyProviderFactoryImpl.class)) {
            factoryMock.when(PropertyProviderFactoryImpl::getPropertyProviderFactory).thenReturn(propertyProviderFactory);
            when(propertyProviderFactory.getPropertyProvider()).thenReturn(propertyProvider);
            when(propertyProvider.getProperty(BATCH_SIZE_PROPERTY_NAME)).thenReturn(String.valueOf(batchSize));
            when(propertyProvider.getProperty(BATCH_FLUSH_INTERVAL_PROPERTY_NAME)).thenReturn("1");

            batchResultManager = new BatchResultManager(apiClient, TEST_RUN_UID);
        }
    }

    private int getBatchSize(BatchResultManager manager) throws Exception {
        Field field = BatchResultManager.class.getDeclaredField("batchSize");
        field.setAccessible(true);
        return field.getInt(manager);
    }

    private ScheduledExecutorService getScheduler(BatchResultManager manager) throws Exception {
        Field field = BatchResultManager.class.getDeclaredField("scheduler");
        field.setAccessible(true);
        return (ScheduledExecutorService) field.get(manager);
    }

    private boolean isActive(BatchResultManager manager) throws Exception {
        Field field = BatchResultManager.class.getDeclaredField("isActive");
        field.setAccessible(true);
        AtomicBoolean isActive = (AtomicBoolean) field.get(manager);
        return isActive.get();
    }

    private void setInactive(BatchResultManager manager) throws Exception {
        Field field = BatchResultManager.class.getDeclaredField("isActive");
        field.setAccessible(true);
        AtomicBoolean isActive = (AtomicBoolean) field.get(manager);
        isActive.set(false);
    }

    @SuppressWarnings("unchecked")
    private List<TestResult> getPendingResults(BatchResultManager manager) throws Exception {
        Field field = BatchResultManager.class.getDeclaredField("pendingResults");
        field.setAccessible(true);
        return (List<TestResult>) field.get(manager);
    }

    @SuppressWarnings("unchecked")
    private List<TestResult> getFailedResults(BatchResultManager manager) throws Exception {
        Field field = BatchResultManager.class.getDeclaredField("failedResults");
        field.setAccessible(true);
        return (List<TestResult>) field.get(manager);
    }

    private void invokeSendBatch(BatchResultManager manager, List<TestResult> results, int attempt) throws Exception {
        Method method = BatchResultManager.class.getDeclaredMethod("sendBatch", List.class, int.class);
        method.setAccessible(true);
        method.invoke(manager, results, attempt);
    }
}