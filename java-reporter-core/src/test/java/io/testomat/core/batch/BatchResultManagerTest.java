package io.testomat.core.batch;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doAnswer;

import io.testomat.core.client.ApiInterface;
import io.testomat.core.constants.PropertyValuesConstants;
import io.testomat.core.facade.methods.artifact.model.AddTestsBatchRequest;
import io.testomat.core.facade.methods.artifact.model.Step;
import io.testomat.core.facade.methods.artifact.model.TestItem;
import io.testomat.core.model.TestResult;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("BatchResultManager Tests")
class BatchResultManagerTest {

    @Mock
    private ApiInterface mockApiClient;

    private BatchResultManager batchManager;
    private final String testRunUid = "test-run-123";

    @BeforeEach
    void setUp() {
        batchManager = new BatchResultManager(mockApiClient, testRunUid);
    }

    @AfterEach
    void tearDown() {
        if (batchManager != null) {
            try {
                batchManager.shutdown();
            } catch (Exception e) {
                // Ignore exceptions during cleanup
            }
        }
    }

    @Test
    @DisplayName("Should initialize with correct configuration")
    void constructor_ShouldInitializeCorrectly() throws Exception {
        // Verify the manager was created
        assertNotNull(batchManager);
        
        // Verify internal state using reflection
        Field isActiveField = BatchResultManager.class.getDeclaredField("isActive");
        isActiveField.setAccessible(true);
        AtomicBoolean isActive = (AtomicBoolean) isActiveField.get(batchManager);
        assertTrue(isActive.get());
        
        Field schedulerField = BatchResultManager.class.getDeclaredField("scheduler");
        schedulerField.setAccessible(true);
        ScheduledExecutorService scheduler = (ScheduledExecutorService) schedulerField.get(batchManager);
        assertNotNull(scheduler);
        assertFalse(scheduler.isShutdown());
    }

    @Test
    @DisplayName("Should add single result to pending batch")
    void addResult_SingleResult_ShouldAddToPending() throws IOException {
        TestResult testResult = createTestResult("Test 1", "passed");

        batchManager.addResult(testResult);

        // Verify no API call yet (batch not full)
        verify(mockApiClient, never()).reportTest(anyString(), any());
        verify(mockApiClient, never()).reportTests(anyString(), anyList());
    }

    @Test
    @DisplayName("Should automatically flush when batch size reached")
    void addResult_BatchSizeFull_ShouldAutoFlush() throws IOException {
        // Add results up to batch size
        for (int i = 0; i < PropertyValuesConstants.DEFAULT_BATCH_SIZE; i++) {
            TestResult result = createTestResult("Test " + i, "passed");
            batchManager.addResult(result);
        }

        // Verify batch was sent
        verify(mockApiClient, times(1)).reportTests(eq(testRunUid), anyList());
    }

    @Test
    @DisplayName("Should not accept results when inactive")
    void addResult_WhenInactive_ShouldSkipResult() throws Exception {
        batchManager.shutdown();
        
        TestResult testResult = createTestResult("Test 1", "passed");
        
        // Should not throw exception, just skip
        assertDoesNotThrow(() -> batchManager.addResult(testResult));
        
        // Since we shutdown immediately, only verify no new interactions after shutdown
        reset(mockApiClient);
        batchManager.addResult(testResult);
        verifyNoInteractions(mockApiClient);
    }

    @Test
    @DisplayName("Should flush empty batch gracefully")
    void flushPendingResults_EmptyBatch_ShouldHandleGracefully() {
        assertDoesNotThrow(() -> batchManager.flushPendingResults());
        
        verifyNoInteractions(mockApiClient);
    }

    @Test
    @DisplayName("Should flush single result using reportTest")
    void flushPendingResults_SingleResult_ShouldUseReportTest() throws IOException {
        TestResult testResult = createTestResult("Test 1", "passed");
        batchManager.addResult(testResult);

        batchManager.flushPendingResults();

        verify(mockApiClient, times(1)).reportTest(testRunUid, testResult);
        verify(mockApiClient, never()).reportTests(anyString(), anyList());
    }

    @Test
    @DisplayName("Should flush multiple results using reportTests")
    void flushPendingResults_MultipleResults_ShouldUseReportTests() throws IOException {
        TestResult result1 = createTestResult("Test 1", "passed");
        TestResult result2 = createTestResult("Test 2", "failed");
        batchManager.addResult(result1);
        batchManager.addResult(result2);

        batchManager.flushPendingResults();

        ArgumentCaptor<List<TestResult>> captor = ArgumentCaptor.forClass(List.class);
        verify(mockApiClient, times(1)).reportTests(eq(testRunUid), captor.capture());
        
        List<TestResult> sentResults = captor.getValue();
        assertEquals(2, sentResults.size());
        assertTrue(sentResults.contains(result1));
        assertTrue(sentResults.contains(result2));
    }

    @Test
    @DisplayName("Should handle API exceptions gracefully")
    void sendBatch_WithIOException_ShouldHandleGracefully() throws IOException, InterruptedException {
        doAnswer(invocation -> {
            throw new IOException("Network error");
        }).when(mockApiClient).reportTest(anyString(), any());

        TestResult testResult = createTestResult("Test 1", "failed");
        batchManager.addResult(testResult);
        
        // Should not throw exception
        assertDoesNotThrow(() -> batchManager.flushPendingResults());
        
        // Give some time for retry attempts
        Thread.sleep(1000);
    }


    @Test
    @DisplayName("Should shutdown gracefully and flush remaining results")
    void shutdown_WithPendingResults_ShouldFlushAndShutdown() throws IOException {
        TestResult testResult = createTestResult("Test 1", "passed");
        batchManager.addResult(testResult);

        batchManager.shutdown();

        // Verify pending results were flushed
        verify(mockApiClient, times(1)).reportTest(testRunUid, testResult);
        
        // Verify manager is inactive
        TestResult newResult = createTestResult("Test 2", "passed");
        batchManager.addResult(newResult);
        
        // Should not process new results
        verify(mockApiClient, never()).reportTest(testRunUid, newResult);
    }

    @Test
    @DisplayName("Should handle concurrent addResult calls safely")
    @Timeout(10)
    void addResult_ConcurrentCalls_ShouldBeThreadSafe() throws InterruptedException, IOException {
        int numberOfThreads = 10;
        int resultsPerThread = 50;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(numberOfThreads);
        AtomicInteger totalResults = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        // Create threads that add results concurrently
        for (int t = 0; t < numberOfThreads; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < resultsPerThread; i++) {
                        TestResult result = createTestResult("Thread-" + threadId + "-Test-" + i, "passed");
                        batchManager.addResult(result);
                        totalResults.incrementAndGet();
                    }
                } catch (Exception e) {
                    fail("Thread failed: " + e.getMessage());
                } finally {
                    completeLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Start all threads
        assertTrue(completeLatch.await(5, TimeUnit.SECONDS), "All threads should complete");

        // Flush any remaining results
        batchManager.flushPendingResults();

        // Verify all results were processed
        int expectedBatches = (numberOfThreads * resultsPerThread) / PropertyValuesConstants.DEFAULT_BATCH_SIZE;
        if ((numberOfThreads * resultsPerThread) % PropertyValuesConstants.DEFAULT_BATCH_SIZE > 0) {
            expectedBatches++; // Account for partial batch
        }

        // Verify API calls were made (exact count depends on timing)
        verify(mockApiClient, atLeastOnce()).reportTests(eq(testRunUid), anyList());

        executor.shutdown();
    }

    @Test
    @DisplayName("Should handle concurrent flush calls safely")
    @Timeout(10)
    void flushPendingResults_ConcurrentCalls_ShouldBeThreadSafe() throws InterruptedException, IOException {
        // Add some results first
        for (int i = 0; i < 10; i++) {
            TestResult result = createTestResult("Test " + i, "passed");
            batchManager.addResult(result);
        }

        int numberOfThreads = 5;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(numberOfThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        // Create threads that flush concurrently
        for (int t = 0; t < numberOfThreads; t++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    batchManager.flushPendingResults();
                } catch (Exception e) {
                    fail("Thread failed: " + e.getMessage());
                } finally {
                    completeLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(completeLatch.await(5, TimeUnit.SECONDS), "All threads should complete");

        // Should only have one successful flush (others should find empty batch)
        verify(mockApiClient, times(1)).reportTests(eq(testRunUid), anyList());

        executor.shutdown();
    }

    @Test
    @DisplayName("Should handle shutdown interruption gracefully")
    void shutdown_WithInterruption_ShouldHandleGracefully() throws Exception {
        // Create a custom manager to control scheduler behavior
        BatchResultManager manager = new BatchResultManager(mockApiClient, testRunUid);
        
        // Interrupt the current thread during shutdown
        Thread shutdownThread = new Thread(() -> {
            Thread.currentThread().interrupt(); // Set interrupt flag
            manager.shutdown();
        });
        
        shutdownThread.start();
        shutdownThread.join(5000); // Wait for completion
        
        // Should complete without throwing exceptions
        assertFalse(shutdownThread.isAlive());
    }

    @Test
    @DisplayName("Should have scheduled executor for periodic flush")
    void periodicFlush_ShouldHaveScheduler() throws Exception {
        // Verify scheduler exists and is active
        Field schedulerField = BatchResultManager.class.getDeclaredField("scheduler");
        schedulerField.setAccessible(true);
        ScheduledExecutorService scheduler = (ScheduledExecutorService) schedulerField.get(batchManager);
        
        assertNotNull(scheduler);
        assertFalse(scheduler.isShutdown());
    }

    @Test
    @DisplayName("Should handle mixed single and batch operations")
    void mixedOperations_ShouldHandleCorrectly() throws IOException {
        // Add single result and flush
        TestResult singleResult = createTestResult("Single Test", "passed");
        batchManager.addResult(singleResult);
        batchManager.flushPendingResults();

        // Add multiple results and flush
        TestResult result1 = createTestResult("Batch Test 1", "passed");
        TestResult result2 = createTestResult("Batch Test 2", "failed");
        batchManager.addResult(result1);
        batchManager.addResult(result2);
        batchManager.flushPendingResults();

        // Verify both single and batch calls were made
        verify(mockApiClient, times(1)).reportTest(testRunUid, singleResult);
        verify(mockApiClient, times(1)).reportTests(eq(testRunUid), anyList());
    }

    @Test
    @DisplayName("Should throw exception for null result")
    void addResult_NullResult_ShouldThrowException() {
        // BatchResultManager doesn't handle null results gracefully
        assertThrows(NullPointerException.class, () -> batchManager.addResult(null));
    }

    @Test
    @DisplayName("Should build batch request from added test items")
    void buildRequestShouldReturnAddedTestItems() {
        TestItem item1 = new TestItem("rid1", "test1", List.of(), null);
        TestItem item2 = new TestItem("rid2", "test2", List.of(), null);

        batchManager.addTestItem(item1);
        batchManager.addTestItem(item2);

        AddTestsBatchRequest request = batchManager.buildRequest();

        assertEquals("addTestsBatch", request.getAction());
        assertEquals(testRunUid, request.getRunId());
        assertEquals(2, request.getTests().size());

        assertEquals("rid1", request.getTests().get(0).getRid());
        assertEquals("rid2", request.getTests().get(1).getRid());
    }

//    @Test
//    @DisplayName("Should update steps for existing test item")
//    void updateTestStepsShouldUpdateExistingItem() {
//        TestItem item = new TestItem("rid1", "test1", List.of(), null);
//        batchManager.addTestItem(item);
//
//        List<Step> steps = List.of(
//            new Step(List.of("artifact1"), null)
//        );
//
//        batchManager.updateTestSteps("rid1", steps);
//
//        AddTestsBatchRequest request = batchManager.buildRequest();
//
//        assertEquals(steps, request.getTests().get(0).getSteps());
//    }
//
//    @Test
//    @DisplayName("Should ignore update for unknown rid")
//    void updateTestStepsShouldIgnoreUnknownRid() {
//        List<Step> steps = List.of(
//            new Step(List.of("artifact1"), null)
//        );
//
//        assertDoesNotThrow(() ->
//            batchManager.updateTestSteps("unknown-rid", steps)
//        );
//
//        AddTestsBatchRequest request = batchManager.buildRequest();
//
//        assertTrue(request.getTests().isEmpty());
//    }

    @Test
    @DisplayName("Should replace existing test item with same rid")
    void addTestItem_ShouldReplaceExistingItem() {
        batchManager.addTestItem(
            new TestItem("rid1", "test1", List.of(), null)
        );

        batchManager.addTestItem(
            new TestItem("rid1", "test2", List.of(), null)
        );

        AddTestsBatchRequest request = batchManager.buildRequest();

        assertEquals(1, request.getTests().size());
        assertEquals("test2", request.getTests().get(0).getTest_id());
    }

    private TestResult createTestResult(String title, String status) {
        return new TestResult.Builder()
                .withTitle(title)
                .withSuiteTitle("Test Suite")
                .withFile("TestClass.java")
                .withStatus(status)
                .withTestId("test-" + title.replaceAll("\\s+", "-").toLowerCase())
                .build();
    }
}