package io.testomat.junit.listener;

import static io.testomat.core.constants.CommonConstants.FAILED;
import static io.testomat.core.constants.CommonConstants.PASSED;
import static io.testomat.core.constants.CommonConstants.SKIPPED;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.junit.methodexporter.MethodExportManager;
import io.testomat.junit.reporter.JunitTestReporter;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@DisplayName("JunitListener Tests")
class JunitListenerTest {

    @Mock
    private MethodExportManager mockMethodExportManager;

    @Mock
    private GlobalRunManager mockRunManager;

    @Mock
    private JunitTestReporter mockReporter;

    @Mock
    private ExtensionContext mockExtensionContext;

    private JunitListener junitListener;

    private static class TestClassA {
    }

    private static class TestClassB {
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        junitListener = new JunitListener(mockMethodExportManager, mockRunManager, mockReporter);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create instance with default constructor")
        void shouldCreateInstanceWithDefaultConstructor() {
            // When
            JunitListener listener = new JunitListener();

            // Then
            assertNotNull(listener);
        }

        @Test
        @DisplayName("Should create instance with parameterized constructor")
        void shouldCreateInstanceWithParameterizedConstructor() {
            // When
            JunitListener listener = new JunitListener(mockMethodExportManager, mockRunManager, mockReporter);

            // Then
            assertNotNull(listener);
        }

        @Test
        @DisplayName("Should initialize processedClasses as thread-safe set")
        void shouldInitializeProcessedClassesAsThreadSafeSet() {
            // Given
            Class<?> testClass = TestClassA.class;
            when(mockExtensionContext.getTestClass()).thenReturn(Optional.of(testClass));

            // When - simulate concurrent access
            junitListener.testSuccessful(mockExtensionContext);
            junitListener.testSuccessful(mockExtensionContext);

            // Then - should only process class once due to thread-safe set
            verify(mockMethodExportManager, times(1)).loadTestBodyForClass(testClass);
        }
    }

    @Nested
    @DisplayName("BeforeAll Tests")
    class BeforeAllTests {

        @Test
        @DisplayName("Should increment suite counter when beforeAll is called")
        void shouldIncrementSuiteCounterWhenBeforeAllIsCalled() {
            // When
            junitListener.beforeAll(mockExtensionContext);

            // Then
            verify(mockRunManager, times(1)).incrementSuiteCounter();
            verifyNoMoreInteractions(mockRunManager, mockMethodExportManager, mockReporter);
        }

        @Test
        @DisplayName("Should increment suite counter multiple times for multiple suites")
        void shouldIncrementSuiteCounterMultipleTimesForMultipleSuites() {
            // When
            junitListener.beforeAll(mockExtensionContext);
            junitListener.beforeAll(mockExtensionContext);
            junitListener.beforeAll(mockExtensionContext);

            // Then
            verify(mockRunManager, times(3)).incrementSuiteCounter();
        }
    }

    @Nested
    @DisplayName("AfterAll Tests")
    class AfterAllTests {

        @Test
        @DisplayName("Should export test class and decrement suite counter when afterAll is called")
        void shouldExportTestClassAndDecrementSuiteCounterWhenAfterAllIsCalled() {
            // Given
            Class<?> testClass = TestClassA.class;
            when(mockExtensionContext.getTestClass()).thenReturn(Optional.of(testClass));

            // When
            junitListener.afterAll(mockExtensionContext);

            // Then
            verify(mockMethodExportManager, times(1)).loadTestBodyForClass(testClass);
            verify(mockRunManager, times(1)).decrementSuiteCounter();
        }

        @Test
        @DisplayName("Should only decrement suite counter when no test class present")
        void shouldOnlyDecrementSuiteCounterWhenNoTestClassPresent() {
            // Given
            when(mockExtensionContext.getTestClass()).thenReturn(Optional.empty());

            // When
            junitListener.afterAll(mockExtensionContext);

            // Then
            verify(mockMethodExportManager, never()).loadTestBodyForClass(any());
            verify(mockRunManager, times(1)).decrementSuiteCounter();
        }

        @Test
        @DisplayName("Should handle multiple afterAll calls for same class")
        void shouldHandleMultipleAfterAllCallsForSameClass() {
            // Given
            Class<?> testClass = TestClassA.class;
            when(mockExtensionContext.getTestClass()).thenReturn(Optional.of(testClass));

            // When
            junitListener.afterAll(mockExtensionContext);
            junitListener.afterAll(mockExtensionContext);

            // Then - should only export once but decrement counter twice
            verify(mockMethodExportManager, times(1)).loadTestBodyForClass(testClass);
            verify(mockRunManager, times(2)).decrementSuiteCounter();
        }
    }

    @Nested
    @DisplayName("BeforeEach Tests")
    class BeforeEachTests {

        @Test
        @DisplayName("Should not perform any operations in beforeEach")
        void shouldNotPerformAnyOperationsInBeforeEach() {
            // When
            junitListener.beforeEach(mockExtensionContext);

            // Then
            verifyNoInteractions(mockMethodExportManager, mockRunManager, mockReporter);
        }

        @Test
        @DisplayName("Should not perform any operations even with multiple beforeEach calls")
        void shouldNotPerformAnyOperationsEvenWithMultipleBeforeEachCalls() {
            // When
            junitListener.beforeEach(mockExtensionContext);
            junitListener.beforeEach(mockExtensionContext);
            junitListener.beforeEach(mockExtensionContext);

            // Then
            verifyNoInteractions(mockMethodExportManager, mockRunManager, mockReporter);
        }
    }

    @Nested
    @DisplayName("Test Disabled Tests")
    class TestDisabledTests {

        @Test
        @DisplayName("Should report test as disabled with provided reason")
        void shouldReportTestAsDisabledWithProvidedReason() {
            // Given
            String reason = "Test disabled for maintenance";
            Class<?> testClass = TestClassA.class;
            when(mockExtensionContext.getTestClass()).thenReturn(Optional.of(testClass));

            // When
            junitListener.testDisabled(mockExtensionContext, Optional.of(reason));

            // Then
            verify(mockReporter, times(1)).reportTestResult(mockExtensionContext, SKIPPED, reason);
            verify(mockMethodExportManager, times(1)).loadTestBodyForClass(testClass);
        }

        @Test
        @DisplayName("Should report test as disabled with default message when no reason provided")
        void shouldReportTestAsDisabledWithDefaultMessageWhenNoReasonProvided() {
            // Given
            Class<?> testClass = TestClassA.class;
            when(mockExtensionContext.getTestClass()).thenReturn(Optional.of(testClass));

            // When
            junitListener.testDisabled(mockExtensionContext, Optional.empty());

            // Then
            verify(mockReporter, times(1)).reportTestResult(mockExtensionContext, SKIPPED, "Test disabled");
            verify(mockMethodExportManager, times(1)).loadTestBodyForClass(testClass);
        }

        @Test
        @DisplayName("Should handle empty reason string")
        void shouldHandleEmptyReasonString() {
            // Given
            String emptyReason = "";
            Class<?> testClass = TestClassA.class;
            when(mockExtensionContext.getTestClass()).thenReturn(Optional.of(testClass));

            // When
            junitListener.testDisabled(mockExtensionContext, Optional.of(emptyReason));

            // Then
            verify(mockReporter, times(1)).reportTestResult(mockExtensionContext, SKIPPED, emptyReason);
            verify(mockMethodExportManager, times(1)).loadTestBodyForClass(testClass);
        }

        @Test
        @DisplayName("Should report test as disabled when no test class present")
        void shouldReportTestAsDisabledWhenNoTestClassPresent() {
            // Given
            String reason = "Test disabled";
            when(mockExtensionContext.getTestClass()).thenReturn(Optional.empty());

            // When
            junitListener.testDisabled(mockExtensionContext, Optional.of(reason));

            // Then
            verify(mockReporter, times(1)).reportTestResult(mockExtensionContext, SKIPPED, reason);
            verify(mockMethodExportManager, never()).loadTestBodyForClass(any());
        }
    }

    @Nested
    @DisplayName("Test Successful Tests")
    class TestSuccessfulTests {

        @Test
        @DisplayName("Should report test as successful when testSuccessful is called")
        void shouldReportTestAsSuccessfulWhenTestSuccessfulIsCalled() {
            // Given
            Class<?> testClass = TestClassA.class;
            when(mockExtensionContext.getTestClass()).thenReturn(Optional.of(testClass));

            // When
            junitListener.testSuccessful(mockExtensionContext);

            // Then
            verify(mockReporter, times(1)).reportTestResult(mockExtensionContext, PASSED, null);
            verify(mockMethodExportManager, times(1)).loadTestBodyForClass(testClass);
        }

        @Test
        @DisplayName("Should report test as successful when no test class present")
        void shouldReportTestAsSuccessfulWhenNoTestClassPresent() {
            // Given
            when(mockExtensionContext.getTestClass()).thenReturn(Optional.empty());

            // When
            junitListener.testSuccessful(mockExtensionContext);

            // Then
            verify(mockReporter, times(1)).reportTestResult(mockExtensionContext, PASSED, null);
            verify(mockMethodExportManager, never()).loadTestBodyForClass(any());
        }

        @Test
        @DisplayName("Should handle multiple successful test calls for same class")
        void shouldHandleMultipleSuccessfulTestCallsForSameClass() {
            // Given
            Class<?> testClass = TestClassA.class;
            when(mockExtensionContext.getTestClass()).thenReturn(Optional.of(testClass));

            // When
            junitListener.testSuccessful(mockExtensionContext);
            junitListener.testSuccessful(mockExtensionContext);

            // Then - should report twice but export class only once
            verify(mockReporter, times(2)).reportTestResult(mockExtensionContext, PASSED, null);
            verify(mockMethodExportManager, times(1)).loadTestBodyForClass(testClass);
        }
    }

    @Nested
    @DisplayName("Test Aborted Tests")
    class TestAbortedTests {

        @Test
        @DisplayName("Should report test as aborted with cause message")
        void shouldReportTestAsAbortedWithCauseMessage() {
            // Given
            Throwable cause = new RuntimeException("Test was aborted due to timeout");
            Class<?> testClass = TestClassA.class;
            when(mockExtensionContext.getTestClass()).thenReturn(Optional.of(testClass));

            // When
            junitListener.testAborted(mockExtensionContext, cause);

            // Then
            verify(mockReporter, times(1)).reportTestResult(mockExtensionContext, SKIPPED, "Test was aborted due to timeout");
            verify(mockMethodExportManager, times(1)).loadTestBodyForClass(testClass);
        }

        @Test
        @DisplayName("Should handle aborted test with null message")
        void shouldHandleAbortedTestWithNullMessage() {
            // Given
            Throwable cause = new RuntimeException(); // No message
            Class<?> testClass = TestClassA.class;
            when(mockExtensionContext.getTestClass()).thenReturn(Optional.of(testClass));

            // When
            junitListener.testAborted(mockExtensionContext, cause);

            // Then
            verify(mockReporter, times(1)).reportTestResult(mockExtensionContext, SKIPPED, null);
            verify(mockMethodExportManager, times(1)).loadTestBodyForClass(testClass);
        }

        @Test
        @DisplayName("Should report test as aborted when no test class present")
        void shouldReportTestAsAbortedWhenNoTestClassPresent() {
            // Given
            Throwable cause = new InterruptedException("Test interrupted");
            when(mockExtensionContext.getTestClass()).thenReturn(Optional.empty());

            // When
            junitListener.testAborted(mockExtensionContext, cause);

            // Then
            verify(mockReporter, times(1)).reportTestResult(mockExtensionContext, SKIPPED, "Test interrupted");
            verify(mockMethodExportManager, never()).loadTestBodyForClass(any());
        }
    }

    @Nested
    @DisplayName("Test Failed Tests")
    class TestFailedTests {

        @Test
        @DisplayName("Should report test as failed with cause message")
        void shouldReportTestAsFailedWithCauseMessage() {
            // Given
            Throwable cause = new AssertionError("Expected <5> but was <3>");
            Class<?> testClass = TestClassA.class;
            when(mockExtensionContext.getTestClass()).thenReturn(Optional.of(testClass));

            // When
            junitListener.testFailed(mockExtensionContext, cause);

            // Then
            verify(mockReporter, times(1)).reportTestResult(mockExtensionContext, FAILED, "Expected <5> but was <3>");
            verify(mockMethodExportManager, times(1)).loadTestBodyForClass(testClass);
        }

        @Test
        @DisplayName("Should handle failed test with null message")
        void shouldHandleFailedTestWithNullMessage() {
            // Given
            Throwable cause = new AssertionError(); // No message
            Class<?> testClass = TestClassA.class;
            when(mockExtensionContext.getTestClass()).thenReturn(Optional.of(testClass));

            // When
            junitListener.testFailed(mockExtensionContext, cause);

            // Then
            verify(mockReporter, times(1)).reportTestResult(mockExtensionContext, FAILED, null);
            verify(mockMethodExportManager, times(1)).loadTestBodyForClass(testClass);
        }

        @Test
        @DisplayName("Should report test as failed when no test class present")
        void shouldReportTestAsFailedWhenNoTestClassPresent() {
            // Given
            Throwable cause = new RuntimeException("Unexpected error occurred");
            when(mockExtensionContext.getTestClass()).thenReturn(Optional.empty());

            // When
            junitListener.testFailed(mockExtensionContext, cause);

            // Then
            verify(mockReporter, times(1)).reportTestResult(mockExtensionContext, FAILED, "Unexpected error occurred");
            verify(mockMethodExportManager, never()).loadTestBodyForClass(any());
        }

        @Test
        @DisplayName("Should handle different types of exceptions")
        void shouldHandleDifferentTypesOfExceptions() {
            // Given
            Class<?> testClass = TestClassA.class;
            when(mockExtensionContext.getTestClass()).thenReturn(Optional.of(testClass));

            Throwable[] exceptions = {
                    new AssertionError("Assertion failed"),
                    new RuntimeException("Runtime error"),
                    new IllegalArgumentException("Invalid argument"),
                    new NullPointerException("Null pointer")
            };

            // When & Then
            for (Throwable exception : exceptions) {
                junitListener.testFailed(mockExtensionContext, exception);
                verify(mockReporter).reportTestResult(mockExtensionContext, FAILED, exception.getMessage());
            }

            // Should only export class once despite multiple test failures
            verify(mockMethodExportManager, times(1)).loadTestBodyForClass(testClass);
        }
    }

    @Nested
    @DisplayName("Export Test Class Tests")
    class ExportTestClassTests {

        @Test
        @DisplayName("Should export test class only once for multiple test results from same class")
        void shouldExportTestClassOnlyOnceForMultipleTestResultsFromSameClass() {
            // Given
            Class<?> testClass = TestClassA.class;
            when(mockExtensionContext.getTestClass()).thenReturn(Optional.of(testClass));

            // When - simulate multiple test results from same class
            junitListener.testSuccessful(mockExtensionContext);
            junitListener.testFailed(mockExtensionContext, new RuntimeException("Error"));
            junitListener.testDisabled(mockExtensionContext, Optional.of("Disabled"));
            junitListener.testAborted(mockExtensionContext, new InterruptedException("Aborted"));

            // Then - should export class only once
            verify(mockMethodExportManager, times(1)).loadTestBodyForClass(testClass);
        }

        @Test
        @DisplayName("Should export different test classes separately")
        void shouldExportDifferentTestClassesSeparately() {
            // Given
            ExtensionContext contextA = mock(ExtensionContext.class);
            ExtensionContext contextB = mock(ExtensionContext.class);

            when(contextA.getTestClass()).thenReturn(Optional.of(TestClassA.class));
            when(contextB.getTestClass()).thenReturn(Optional.of(TestClassB.class));

            // When
            junitListener.testSuccessful(contextA);
            junitListener.testSuccessful(contextB);
            junitListener.testSuccessful(contextA); // Same class again

            // Then - should export each class once
            verify(mockMethodExportManager, times(1)).loadTestBodyForClass(TestClassA.class);
            verify(mockMethodExportManager, times(1)).loadTestBodyForClass(TestClassB.class);
        }

        @Test
        @DisplayName("Should not export when test class is not present")
        void shouldNotExportWhenTestClassIsNotPresent() {
            // Given
            when(mockExtensionContext.getTestClass()).thenReturn(Optional.empty());

            // When
            junitListener.testSuccessful(mockExtensionContext);
            junitListener.testFailed(mockExtensionContext, new RuntimeException("Error"));

            // Then
            verify(mockMethodExportManager, never()).loadTestBodyForClass(any());
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle complete test lifecycle correctly")
        void shouldHandleCompleteTestLifecycleCorrectly() {
            // Given
            Class<?> testClass = TestClassA.class;
            when(mockExtensionContext.getTestClass()).thenReturn(Optional.of(testClass));

            // When - simulate complete test lifecycle
            junitListener.beforeAll(mockExtensionContext);
            junitListener.beforeEach(mockExtensionContext);
            junitListener.testSuccessful(mockExtensionContext);
            junitListener.beforeEach(mockExtensionContext);
            junitListener.testFailed(mockExtensionContext, new RuntimeException("Test failed"));
            junitListener.afterAll(mockExtensionContext);

            // Then
            verify(mockRunManager, times(1)).incrementSuiteCounter();
            verify(mockRunManager, times(1)).decrementSuiteCounter();
            verify(mockReporter, times(1)).reportTestResult(mockExtensionContext, PASSED, null);
            verify(mockReporter, times(1)).reportTestResult(mockExtensionContext, FAILED, "Test failed");
            verify(mockMethodExportManager, times(1)).loadTestBodyForClass(testClass);
        }

        @Test
        @DisplayName("Should be thread-safe for concurrent test execution")
        void shouldBeThreadSafeForConcurrentTestExecution() {
            // Given
            Class<?> testClass = TestClassA.class;
            when(mockExtensionContext.getTestClass()).thenReturn(Optional.of(testClass));

            // When - simulate concurrent test execution
            junitListener.testSuccessful(mockExtensionContext);
            junitListener.testSuccessful(mockExtensionContext);
            junitListener.testSuccessful(mockExtensionContext);

            // Then - should handle concurrent access to processedClasses set
            verify(mockReporter, times(3)).reportTestResult(mockExtensionContext, PASSED, null);
            verify(mockMethodExportManager, times(1)).loadTestBodyForClass(testClass);
        }
    }
}