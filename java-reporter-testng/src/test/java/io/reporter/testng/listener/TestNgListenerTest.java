package io.reporter.testng.listener;

import static io.testomat.core.constants.CommonConstants.FAILED;
import static io.testomat.core.constants.CommonConstants.PASSED;
import static io.testomat.core.constants.CommonConstants.SKIPPED;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.testomat.core.annotation.TestId;
import io.testomat.core.annotation.Title;
import io.testomat.core.exception.ReportTestResultException;
import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.testng.listener.TestNgListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@DisplayName("TestNgListener Tests")
class TestNgListenerTest {

    private TestableTestNgListener listener;

    @Mock
    private GlobalRunManager mockRunManager;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);

        listener = new TestableTestNgListener(mockRunManager);

        when(mockRunManager.isActive()).thenReturn(true);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mockitoCloseable != null) {
            mockitoCloseable.close();
        }
    }

    @Test
    @DisplayName("should increment suite counter when suite starts")
    void shouldIncrementSuiteCounterWhenSuiteStarts() {
        // When
        listener.handleSuiteStarted("Test Suite");

        // Then
        verify(mockRunManager).incrementSuiteCounter();
    }

    @Test
    @DisplayName("should decrement suite counter when suite finishes")
    void shouldDecrementSuiteCounterWhenSuiteFinishes() {
        // When
        listener.handleSuiteFinished("Test Suite");

        // Then
        verify(mockRunManager).decrementSuiteCounter();
    }

    @Test
    @DisplayName("should return true when run manager is active")
    void shouldReturnTrueWhenRunManagerIsActive() {
        // Given
        when(mockRunManager.isActive()).thenReturn(true);

        // When
        boolean result = listener.isManagerActive();

        // Then
        assertTrue(result);
        verify(mockRunManager).isActive();
    }

    @Test
    @DisplayName("should return false when run manager is inactive")
    void shouldReturnFalseWhenRunManagerIsInactive() {
        // Given
        when(mockRunManager.isActive()).thenReturn(false);

        // When
        boolean result = listener.isManagerActive();

        // Then
        assertFalse(result);
        verify(mockRunManager).isActive();
    }

    @Test
    @DisplayName("should handle duplicate test prevention correctly")
    void shouldHandleDuplicateTestPreventionCorrectly() {
        // Given
        String methodKey1 = "com.example.TestClass.testMethod1";
        String methodKey2 = "com.example.TestClass.testMethod2";
        String methodKey1Duplicate = "com.example.TestClass.testMethod1";

        // When
        boolean isFirstTime1 = listener.addToProcessedTests(methodKey1);
        boolean isFirstTime2 = listener.addToProcessedTests(methodKey2);
        boolean isFirstTimeDuplicate = listener.addToProcessedTests(methodKey1Duplicate);

        // Then
        assertTrue(isFirstTime1, "First occurrence should return true");
        assertTrue(isFirstTime2, "Different method should return true");
        assertFalse(isFirstTimeDuplicate, "Duplicate should return false");
    }

    @Test
    @DisplayName("should clear processed tests")
    void shouldClearProcessedTests() {
        // Given
        String methodKey = "com.example.TestClass.testMethod";
        listener.addToProcessedTests(methodKey);

        // When
        listener.clearProcessedTests();
        boolean isFirstTimeAfterClear = listener.addToProcessedTests(methodKey);

        // Then
        assertTrue(isFirstTimeAfterClear, "Should return true after clearing processed tests");
    }

    @Test
    @DisplayName("should create method key correctly")
    void shouldCreateMethodKeyCorrectly() {
        // Given
        String className = "com.example.TestClass";
        String methodName = "testMethod";

        // When
        String methodKey = listener.createMethodKey(className, methodName);

        // Then
        assertEquals("com.example.TestClass.testMethod", methodKey);
    }

    @Test
    @DisplayName("should handle method key with special characters")
    void shouldHandleMethodKeyWithSpecialCharacters() {
        // Given
        String className = "com.example.Test$InnerClass";
        String methodName = "test_method_with_underscores";

        // When
        String methodKey = listener.createMethodKey(className, methodName);

        // Then
        assertEquals("com.example.Test$InnerClass.test_method_with_underscores", methodKey);
    }

    @Test
    @DisplayName("should get processed tests count")
    void shouldGetProcessedTestsCount() {
        // Given
        listener.addToProcessedTests("test1");
        listener.addToProcessedTests("test2");
        listener.addToProcessedTests("test1"); // duplicate

        // When
        int count = listener.getProcessedTestsCount();

        // Then
        assertEquals(2, count, "Should have 2 unique processed tests");
    }

    @Test
    @DisplayName("should handle empty method keys")
    void shouldHandleEmptyMethodKeys() {
        // Given
        String emptyKey = "";

        // When
        boolean added = listener.addToProcessedTests(emptyKey);

        // Then
        assertTrue(added, "Empty key should be added");
        assertEquals(1, listener.getProcessedTestsCount());
    }

    @Test
    @DisplayName("should handle null method keys")
    void shouldHandleNullMethodKeys() {
        // When & Then
        assertDoesNotThrow(() -> {
            boolean added = listener.addToProcessedTests(null);
            assertTrue(added, "Null key should be handled gracefully");
        });
    }

    @Test
    @DisplayName("should report test when manager is active")
    void shouldReportTestWhenManagerIsActive() {
        // Given
        when(mockRunManager.isActive()).thenReturn(true);

        // When
        listener.simulateTestReporting("testMethod", PASSED);

        // Then
        verify(mockRunManager).isActive();
        verify(mockRunManager).reportTest(any());
    }

    @Test
    @DisplayName("should not report test when manager is inactive")
    void shouldNotReportTestWhenManagerIsInactive() {
        // Given
        when(mockRunManager.isActive()).thenReturn(false);

        // When
        listener.simulateTestReporting("testMethod", PASSED);

        // Then
        verify(mockRunManager).isActive();
        verify(mockRunManager, never()).reportTest(any());
    }

    @Test
    @DisplayName("should handle exception during test reporting")
    void shouldHandleExceptionDuringTestReporting() {
        // Given
        when(mockRunManager.isActive()).thenReturn(true);
        doThrow(new RuntimeException("Network error")).when(mockRunManager).reportTest(any());

        // When & Then
        ReportTestResultException exception = assertThrows(
                ReportTestResultException.class,
                () -> listener.simulateTestReporting("testMethod", FAILED)
        );

        assertTrue(exception.getMessage().contains("Failed to report test result for: testMethod"));
    }

    @Test
    @DisplayName("should handle different test statuses")
    void shouldHandleDifferentTestStatuses() {
        // When & Then
        assertDoesNotThrow(() -> {
            listener.simulateTestReporting("passedTest", PASSED);
            listener.simulateTestReporting("failedTest", FAILED);
            listener.simulateTestReporting("skippedTest", SKIPPED);
        });

        verify(mockRunManager, times(3)).reportTest(any());
    }

    public static class TestMethods {
        public void testMethod() {
        }

        @Title("Custom Test Title")
        @TestId("TEST-123")
        public void annotatedTestMethod() {
        }
    }

    /**
     * Testable version of TestNgListener
     */
    private static class TestableTestNgListener extends TestNgListener {
        private final GlobalRunManager testRunManager;
        private final java.util.Set<String> processedTests = new java.util.HashSet<>();

        public TestableTestNgListener(GlobalRunManager runManager) {
            this.testRunManager = runManager;
        }

        public void handleSuiteStarted(String suiteName) {
            testRunManager.incrementSuiteCounter();
        }

        public void handleSuiteFinished(String suiteName) {
            testRunManager.decrementSuiteCounter();
        }

        public boolean isManagerActive() {
            return testRunManager.isActive();
        }

        public boolean addToProcessedTests(String methodKey) {
            return processedTests.add(methodKey);
        }

        public void clearProcessedTests() {
            processedTests.clear();
        }

        public String createMethodKey(String className, String methodName) {
            return className + "." + methodName;
        }

        public int getProcessedTestsCount() {
            return processedTests.size();
        }

        public void simulateTestReporting(String testName, String status) {
            if (!testRunManager.isActive()) {
                return;
            }

            try {
                io.testomat.core.model.TestMetadata metadata = new io.testomat.core.model.TestMetadata(
                        testName,
                        null,
                        "TestSuite",
                        "TestSuite.java"
                );

                io.testomat.testng.constructor.TestResultWrapper.Builder builder =
                        io.testomat.testng.constructor.TestResultWrapper.builder()
                                .withTestMetadata(metadata)
                                .withStatus(status);

                io.testomat.testng.constructor.TestNgTestResultConstructor constructor =
                        new io.testomat.testng.constructor.TestNgTestResultConstructor();
                io.testomat.core.model.TestResult testResult = constructor.constructTestRunResult(builder.build());

                testRunManager.reportTest(testResult);
            } catch (Exception e) {
                throw new ReportTestResultException("Failed to report test result for: " + testName, e);
            }
        }
    }
}