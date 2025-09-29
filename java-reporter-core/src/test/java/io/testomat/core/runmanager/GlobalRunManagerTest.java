package io.testomat.core.runmanager;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.testomat.core.batch.BatchResultManager;
import io.testomat.core.client.ApiInterface;
import io.testomat.core.client.ClientFactory;
import io.testomat.core.model.TestResult;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalRunManager Tests")
class GlobalRunManagerTest {

    @Mock
    private PropertyProvider mockPropertyProvider;

    @Mock
    private ClientFactory mockClientFactory;

    @Mock
    private ApiInterface mockApiClient;

    @Mock
    private BatchResultManager mockBatchManager;

    private GlobalRunManager globalRunManager;

    @BeforeEach
    void setUp() throws Exception {
        // Reset singleton instance before each test
        GlobalRunManager.resetInstance();

        // Configure default mock behavior
        lenient().when(mockClientFactory.createClient()).thenReturn(mockApiClient);
        lenient().when(mockApiClient.createRun(any())).thenReturn("test-run-uid-123");
        lenient().when(mockPropertyProvider.getProperty("testomatio.reporting.disable")).thenReturn(null);
        lenient().when(mockPropertyProvider.getProperty("testomatio.run.title")).thenReturn("Test Run");
        lenient().when(mockPropertyProvider.getProperty("testomatio.run.id")).thenThrow(new RuntimeException("Property not found"));

        // Create instance with mocked dependencies
        globalRunManager = new GlobalRunManager(mockPropertyProvider, mockClientFactory);
        GlobalRunManager.setInstance(globalRunManager);
    }

    @AfterEach
    void tearDown() {
        GlobalRunManager.resetInstance();
    }

    @Nested
    @DisplayName("Singleton Pattern Tests")
    class SingletonPatternTests {

        @Test
        @DisplayName("Should return same instance on multiple getInstance calls")
        void shouldReturnSameInstance() {
            // Reset to test lazy initialization
            GlobalRunManager.resetInstance();

            GlobalRunManager instance1 = GlobalRunManager.getInstance();
            GlobalRunManager instance2 = GlobalRunManager.getInstance();

            assertNotNull(instance1);
            assertNotNull(instance2);
            assertSame(instance1, instance2);
        }

        @Test
        @DisplayName("Should be thread-safe during initialization")
        @Timeout(5)
        void shouldBeThreadSafeDuringInitialization() throws InterruptedException {
            GlobalRunManager.resetInstance();

            int threadCount = 10;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(threadCount);
            AtomicReference<GlobalRunManager>[] instances = new AtomicReference[threadCount];

            for (int i = 0; i < threadCount; i++) {
                instances[i] = new AtomicReference<>();
                final int index = i;
                new Thread(() -> {
                    try {
                        startLatch.await();
                        instances[index].set(GlobalRunManager.getInstance());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        endLatch.countDown();
                    }
                }).start();
            }

            startLatch.countDown();
            assertTrue(endLatch.await(3, TimeUnit.SECONDS));

            GlobalRunManager firstInstance = instances[0].get();
            assertNotNull(firstInstance);

            for (int i = 1; i < threadCount; i++) {
                assertSame(firstInstance, instances[i].get(),
                        "All instances should be the same object");
            }
        }
    }

    @Nested
    @DisplayName("Initialization Tests")
    class InitializationTests {

        @Test
        @DisplayName("Should initialize successfully with valid configuration")
        void shouldInitializeSuccessfully() throws IOException {
            globalRunManager.initializeIfNeeded();

            assertTrue(globalRunManager.isActive());
            assertEquals("test-run-uid-123", globalRunManager.getRunUid());

            verify(mockClientFactory).createClient();
            verify(mockApiClient).createRun("Test Run");
        }

        @Test
        @DisplayName("Should not initialize when reporting is disabled")
        void shouldNotInitializeWhenReportingDisabled() {
            when(mockPropertyProvider.getProperty("testomatio.reporting.disable")).thenReturn("1");

            globalRunManager.initializeIfNeeded();

            assertFalse(globalRunManager.isActive());
            assertNull(globalRunManager.getRunUid());

            verify(mockClientFactory, never()).createClient();
        }

        @Test
        @DisplayName("Should not initialize twice")
        void shouldNotInitializeTwice() throws IOException {
            globalRunManager.initializeIfNeeded();
            assertTrue(globalRunManager.isActive());

            // Second initialization should be skipped
            globalRunManager.initializeIfNeeded();

            verify(mockClientFactory, times(1)).createClient();
            verify(mockApiClient, times(1)).createRun(any());
        }

        @Test
        @DisplayName("Should use custom run ID when provided")
        void shouldUseCustomRunId() throws IOException {
            // Reset mocks to avoid stubbing conflicts
            reset(mockPropertyProvider, mockClientFactory, mockApiClient);

            // Configure fresh stubbing for this test
            lenient().when(mockPropertyProvider.getProperty("testomatio.reporting.disable")).thenReturn(null);
            lenient().when(mockPropertyProvider.getProperty("testomatio.run.title")).thenReturn("Test Run");
            when(mockPropertyProvider.getProperty("testomatio.run.id")).thenReturn("custom-run-id");
            when(mockClientFactory.createClient()).thenReturn(mockApiClient);

            globalRunManager.initializeIfNeeded();

            assertTrue(globalRunManager.isActive());
            assertEquals("custom-run-id", globalRunManager.getRunUid());

            verify(mockApiClient, never()).createRun(any());
        }

        @Test
        @DisplayName("Should handle initialization errors gracefully")
        void shouldHandleInitializationErrors() throws IOException {
            when(mockClientFactory.createClient()).thenThrow(new RuntimeException("Client creation failed"));

            globalRunManager.initializeIfNeeded();

            assertFalse(globalRunManager.isActive());
            assertNull(globalRunManager.getRunUid());
        }
    }

    @Nested
    @DisplayName("Suite Counter Tests")
    class SuiteCounterTests {

        @Test
        @DisplayName("Should increment and decrement suite counter correctly")
        void shouldManageSuiteCounterCorrectly() {
            assertEquals(0, globalRunManager.getActiveSuitesCount());

            globalRunManager.incrementSuiteCounter();
            assertEquals(1, globalRunManager.getActiveSuitesCount());

            globalRunManager.incrementSuiteCounter();
            assertEquals(2, globalRunManager.getActiveSuitesCount());

            globalRunManager.decrementSuiteCounter();
            assertEquals(1, globalRunManager.getActiveSuitesCount());

            globalRunManager.decrementSuiteCounter();
            assertEquals(0, globalRunManager.getActiveSuitesCount());
        }

        @Test
        @DisplayName("Should initialize when incrementing suite counter")
        void shouldInitializeWhenIncrementingSuiteCounter() {
            assertFalse(globalRunManager.isActive());

            globalRunManager.incrementSuiteCounter();

            assertTrue(globalRunManager.isActive());
            assertEquals(1, globalRunManager.getActiveSuitesCount());
        }

        @Test
        @DisplayName("Should be thread-safe for concurrent suite operations")
        @Timeout(5)
        void shouldBeThreadSafeForConcurrentSuiteOperations() throws InterruptedException {
            int threadCount = 10;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(threadCount * 2);

            // Start threads that increment
            for (int i = 0; i < threadCount; i++) {
                new Thread(() -> {
                    try {
                        startLatch.await();
                        globalRunManager.incrementSuiteCounter();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        endLatch.countDown();
                    }
                }).start();
            }

            // Start threads that decrement
            for (int i = 0; i < threadCount; i++) {
                new Thread(() -> {
                    try {
                        startLatch.await();
                        Thread.sleep(100); // Wait a bit to ensure some increments happen first
                        globalRunManager.decrementSuiteCounter();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        endLatch.countDown();
                    }
                }).start();
            }

            startLatch.countDown();
            assertTrue(endLatch.await(3, TimeUnit.SECONDS));

            // Final count should be 0 (same number of increments and decrements)
            assertEquals(0, globalRunManager.getActiveSuitesCount());
        }
    }

    @Nested
    @DisplayName("Test Reporting Tests")
    class TestReportingTests {

        @Test
        @DisplayName("Should report test when batch manager is available")
        void shouldReportTestWhenBatchManagerAvailable() throws Exception {
            // Initialize to set up batch manager
            globalRunManager.initializeIfNeeded();

            // Use reflection to inject mock batch manager
            setBatchManager(mockBatchManager);

            TestResult testResult = new TestResult();
            testResult.setTestId("test-123");

            globalRunManager.reportTest(testResult);

            verify(mockBatchManager).addResult(testResult);
        }

        @Test
        @DisplayName("Should handle test reporting when batch manager is null")
        void shouldHandleTestReportingWhenBatchManagerNull() {
            TestResult testResult = new TestResult();
            testResult.setTestId("test-123");

            // Should not throw exception even when not initialized
            assertDoesNotThrow(() -> globalRunManager.reportTest(testResult));
        }

        private void setBatchManager(BatchResultManager batchManager) throws Exception {
            Field batchManagerField = GlobalRunManager.class.getDeclaredField("batchManager");
            batchManagerField.setAccessible(true);
            AtomicReference<BatchResultManager> batchManagerRef =
                    (AtomicReference<BatchResultManager>) batchManagerField.get(globalRunManager);
            batchManagerRef.set(batchManager);
        }
    }

    @Nested
    @DisplayName("State Management Tests")
    class StateManagementTests {

        @Test
        @DisplayName("Should return correct active state")
        void shouldReturnCorrectActiveState() {
            assertFalse(globalRunManager.isActive());
            assertNull(globalRunManager.getRunUid());

            globalRunManager.initializeIfNeeded();

            assertTrue(globalRunManager.isActive());
            assertNotNull(globalRunManager.getRunUid());
        }

        @Test
        @DisplayName("Should handle force finalization")
        void shouldHandleForceFinalization() throws Exception {
            globalRunManager.initializeIfNeeded();
            assertTrue(globalRunManager.isActive());

            // Add active suites
            globalRunManager.incrementSuiteCounter();
            globalRunManager.incrementSuiteCounter();
            assertEquals(2, globalRunManager.getActiveSuitesCount());

            // Force finalization should work even with active suites
            globalRunManager.forceFinalize();

            assertFalse(globalRunManager.isActive());
            assertNull(globalRunManager.getRunUid());
        }
    }

    @Nested
    @DisplayName("Property Provider Tests")
    class PropertyProviderTests {

        @Test
        @DisplayName("Should handle reporting disabled with various values")
        void shouldHandleReportingDisabledWithVariousValues() {
            // Test with "1"
            when(mockPropertyProvider.getProperty("testomatio.reporting.disable")).thenReturn("1");
            globalRunManager.initializeIfNeeded();
            assertFalse(globalRunManager.isActive());

            // Reset and test with "true"
            GlobalRunManager.resetInstance();
            globalRunManager = new GlobalRunManager(mockPropertyProvider, mockClientFactory);
            GlobalRunManager.setInstance(globalRunManager);
            when(mockPropertyProvider.getProperty("testomatio.reporting.disable")).thenReturn("true");
            globalRunManager.initializeIfNeeded();
            assertFalse(globalRunManager.isActive());

            // Reset and test with "0" (should enable)
            GlobalRunManager.resetInstance();
            globalRunManager = new GlobalRunManager(mockPropertyProvider, mockClientFactory);
            GlobalRunManager.setInstance(globalRunManager);
            when(mockPropertyProvider.getProperty("testomatio.reporting.disable")).thenReturn("0");
            globalRunManager.initializeIfNeeded();
            assertTrue(globalRunManager.isActive());
        }

        @Test
        @DisplayName("Should handle missing properties gracefully")
        void shouldHandleMissingPropertiesGracefully() {
            // This test already passes with default stubbing from setUp()
            // The default stubbing simulates the scenario where custom run ID is not found
            globalRunManager.initializeIfNeeded();

            // Should successfully initialize with auto-generated run ID
            assertTrue(globalRunManager.isActive());
            assertEquals("test-run-uid-123", globalRunManager.getRunUid());
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle API client creation failure")
        void shouldHandleApiClientCreationFailure() throws IOException {
            when(mockClientFactory.createClient()).thenThrow(new RuntimeException("Network error"));

            globalRunManager.initializeIfNeeded();

            assertFalse(globalRunManager.isActive());
            assertNull(globalRunManager.getRunUid());
        }

        @Test
        @DisplayName("Should handle run creation failure")
        void shouldHandleRunCreationFailure() throws IOException {
            when(mockApiClient.createRun(any())).thenThrow(new IOException("Server error"));

            globalRunManager.initializeIfNeeded();

            assertFalse(globalRunManager.isActive());
            assertNull(globalRunManager.getRunUid());
        }
    }
}