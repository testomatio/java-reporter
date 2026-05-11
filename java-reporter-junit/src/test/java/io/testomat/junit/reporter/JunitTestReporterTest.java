package io.testomat.junit.reporter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.testomat.core.exception.ReportTestResultException;
import io.testomat.core.model.TestMetadata;
import io.testomat.core.model.TestResult;
import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.junit.constructor.JUnitTestResultConstructor;
import io.testomat.junit.extractor.JunitMetaDataExtractor;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class JunitTestReporterTest {

    @Mock
    private JUnitTestResultConstructor resultConstructor;

    @Mock
    private JunitMetaDataExtractor metaDataExtractor;

    @Mock
    private GlobalRunManager runManager;

    @Mock
    private ExtensionContext context;

    @Mock
    private Method testMethod;

    private JunitTestReporter reporter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reporter = new JunitTestReporter(resultConstructor, metaDataExtractor, runManager);
    }

    @Test
    void reportTestResult_WhenRunManagerNotActive_ShouldSkipReporting() {
        // Given
        when(runManager.isActive()).thenReturn(false);

        // When
        reporter.reportTestResult(context, "passed", "Test message");

        // Then
        verify(runManager).isActive();
        verifyNoInteractions(metaDataExtractor);
        verifyNoInteractions(resultConstructor);
        verify(runManager, never()).reportTest(any());
    }

    @Test
    void reportTestResult_WhenRunManagerActive_ShouldReportTestSuccessfully() {
        // Given
        String status = "passed";
        String message = "Test passed successfully";
        String uniqueId = "test-unique-id-123";

        TestMetadata metadata = new TestMetadata("Test Title", "test-id", "TestSuite", "test/file.java", null);
        TestResult expectedResult = TestResult.builder()
                .withTitle("Test Title")
                .withStatus(status)
                .withMessage(message)
                .build();

        when(runManager.isActive()).thenReturn(true);
        when(context.getUniqueId()).thenReturn(uniqueId);
        when(metaDataExtractor.extractTestMetadata(context)).thenReturn(metadata);
        when(resultConstructor.constructTestRunResult(metadata, message, status, context))
                .thenReturn(expectedResult);

        // When
        reporter.reportTestResult(context, status, message);

        // Then
        verify(runManager).isActive();
        verify(metaDataExtractor).extractTestMetadata(context);
        verify(resultConstructor).constructTestRunResult(metadata, message, status, context);
        verify(runManager).reportTest(expectedResult);
    }

    @Test
    void reportTestResult_WhenResultConstructionFails_ShouldThrowException() {
        // Given
        TestMetadata metadata = new TestMetadata("Test Title", "test-id", "TestSuite", "test/file.java", null);

        when(runManager.isActive()).thenReturn(true);
        when(context.getUniqueId()).thenReturn("test-id");
        when(metaDataExtractor.extractTestMetadata(context)).thenReturn(metadata);
        when(resultConstructor.constructTestRunResult(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Construction failed"));

        // When & Then
        ReportTestResultException exception = assertThrows(
                ReportTestResultException.class,
                () -> reporter.reportTestResult(context, "failed", "message")
        );

        assertTrue(exception.getMessage().contains("Failed to report test result for: Test Title"));
        verify(runManager, never()).reportTest(any());
    }

    @Test
    void reportTestResult_WhenReportingFails_ShouldThrowException() {
        // Given
        TestMetadata metadata = new TestMetadata("Test Title", "test-id", "TestSuite", "test/file.java", null);
        TestResult result = TestResult.builder().withTitle("Test Title").build();

        when(runManager.isActive()).thenReturn(true);
        when(context.getUniqueId()).thenReturn("test-id");
        when(metaDataExtractor.extractTestMetadata(context)).thenReturn(metadata);
        when(resultConstructor.constructTestRunResult(any(), any(), any(), any())).thenReturn(result);
        doThrow(new RuntimeException("Reporting failed")).when(runManager).reportTest(any());

        // When & Then
        ReportTestResultException exception = assertThrows(
                ReportTestResultException.class,
                () -> reporter.reportTestResult(context, "failed", "message")
        );

        assertTrue(exception.getMessage().contains("Failed to report test result for: Test Title"));
    }

    @Test
    void reportTestResult_ConcurrentExecution_ShouldHandleThreadSafety() throws InterruptedException {
        // Given
        int numberOfThreads = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numberOfThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);

        TestMetadata metadata = new TestMetadata("Test Title", "test-id", "TestSuite", "test/file.java", null);
        TestResult result = TestResult.builder().withTitle("Test Title").build();

        when(runManager.isActive()).thenReturn(true);
        when(context.getUniqueId()).thenReturn("same-test-id"); // Same ID for all threads
        when(metaDataExtractor.extractTestMetadata(context)).thenReturn(metadata);
        when(resultConstructor.constructTestRunResult(any(), any(), any(), any())).thenReturn(result);

        // When
        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    reporter.reportTestResult(context, "passed", "message");
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Start all threads simultaneously
        assertTrue(endLatch.await(5, TimeUnit.SECONDS));
        executor.shutdown();

        // Then
        assertEquals(numberOfThreads, successCount.get());
        verify(runManager, times(numberOfThreads)).reportTest(result);
    }

    @Test
    void reportTestResult_MultipleDifferentTests_ShouldUseDifferentLocks() throws InterruptedException {
        // Given
        ExtensionContext context1 = mock(ExtensionContext.class);
        ExtensionContext context2 = mock(ExtensionContext.class);

        TestMetadata metadata1 = new TestMetadata("Test 1", "id-1", "Suite1", "file1.java", null);
        TestMetadata metadata2 = new TestMetadata("Test 2", "id-2", "Suite2", "file2.java", null);

        TestResult result1 = TestResult.builder().withTitle("Test 1").build();
        TestResult result2 = TestResult.builder().withTitle("Test 2").build();

        when(runManager.isActive()).thenReturn(true);

        when(context1.getUniqueId()).thenReturn("unique-id-1");
        when(context2.getUniqueId()).thenReturn("unique-id-2");

        when(metaDataExtractor.extractTestMetadata(context1)).thenReturn(metadata1);
        when(metaDataExtractor.extractTestMetadata(context2)).thenReturn(metadata2);

        when(resultConstructor.constructTestRunResult(eq(metadata1), any(), any(), eq(context1)))
                .thenReturn(result1);
        when(resultConstructor.constructTestRunResult(eq(metadata2), any(), any(), eq(context2)))
                .thenReturn(result2);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // When
        executor.submit(() -> {
            try {
                startLatch.await();
                reporter.reportTestResult(context1, "passed", "message1");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                endLatch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                startLatch.await();
                reporter.reportTestResult(context2, "failed", "message2");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                endLatch.countDown();
            }
        });

        startLatch.countDown();
        assertTrue(endLatch.await(5, TimeUnit.SECONDS));
        executor.shutdown();

        // Then
        verify(runManager).reportTest(result1);
        verify(runManager).reportTest(result2);
    }

    @Test
    void reportTestResult_VerifyCorrectParametersPassedToConstructor() {
        // Given
        String status = "failed";
        String message = "Custom failure message";
        TestMetadata metadata = new TestMetadata("Test Title", "test-id", "TestSuite", "test/file.java", null);
        TestResult result = TestResult.builder().withTitle("Test Title").build();

        when(runManager.isActive()).thenReturn(true);
        when(context.getUniqueId()).thenReturn("test-id");
        when(metaDataExtractor.extractTestMetadata(context)).thenReturn(metadata);
        when(resultConstructor.constructTestRunResult(any(), any(), any(), any())).thenReturn(result);

        ArgumentCaptor<TestMetadata> metadataCaptor = ArgumentCaptor.forClass(TestMetadata.class);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> statusCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ExtensionContext> contextCaptor = ArgumentCaptor.forClass(ExtensionContext.class);

        // When
        reporter.reportTestResult(context, status, message);

        // Then
        verify(resultConstructor).constructTestRunResult(
                metadataCaptor.capture(),
                messageCaptor.capture(),
                statusCaptor.capture(),
                contextCaptor.capture()
        );

        assertEquals(metadata, metadataCaptor.getValue());
        assertEquals(message, messageCaptor.getValue());
        assertEquals(status, statusCaptor.getValue());
        assertEquals(context, contextCaptor.getValue());
    }
}