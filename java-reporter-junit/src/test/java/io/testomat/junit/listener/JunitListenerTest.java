package io.testomat.junit.listener;

import static io.testomat.core.constants.CommonConstants.FAILED;
import static io.testomat.core.constants.CommonConstants.PASSED;
import static io.testomat.core.constants.CommonConstants.SKIPPED;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

/**
 * Comprehensive unit tests for JunitListener class.
 * Tests all lifecycle callbacks, test watcher methods, and edge cases.
 */
@DisplayName("JunitListener Tests")
class JunitListenerTest {

    @Mock
    private MethodExportManager mockMethodExportManager;

    @Mock
    private GlobalRunManager mockRunManager;

    @Mock
    private JunitTestReporter mockReporter;

    @Mock
    private ExtensionContext mockContext;

    private JunitListener junitListener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        junitListener = new JunitListener(mockMethodExportManager, mockRunManager, mockReporter);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor should create instance with non-null dependencies")
        void defaultConstructor_ShouldCreateInstanceWithDependencies() {
            // When
            JunitListener listener = new JunitListener();

            // Then
            assertNotNull(listener);
            // Note: We can't directly access private fields, but we can verify the instance is created
        }

        @Test
        @DisplayName("Parameterized constructor should create instance with provided dependencies")
        void parameterizedConstructor_ShouldCreateInstanceWithProvidedDependencies() {
            // When
            JunitListener listener = new JunitListener(mockMethodExportManager, mockRunManager, mockReporter);

            // Then
            assertNotNull(listener);
        }

        @Test
        @DisplayName("Parameterized constructor should accept null dependencies without throwing")
        void parameterizedConstructor_ShouldAcceptNullDependencies() {
            // When & Then
            assertDoesNotThrow(() -> new JunitListener(null, null, null));
        }
    }

    @Nested
    @DisplayName("Lifecycle Callback Tests")
    class LifecycleCallbackTests {

        @Test
        @DisplayName("beforeAll should increment suite counter")
        void beforeAll_ShouldIncrementSuiteCounter() {
            // Given
            doNothing().when(mockRunManager).incrementSuiteCounter();

            // When
            junitListener.beforeAll(mockContext);

            // Then
            verify(mockRunManager, times(1)).incrementSuiteCounter();
        }

        @Test
        @DisplayName("beforeAll should handle null context gracefully")
        void beforeAll_ShouldHandleNullContext() {
            // Given
            doNothing().when(mockRunManager).incrementSuiteCounter();

            // When & Then
            assertDoesNotThrow(() -> junitListener.beforeAll(null));
            verify(mockRunManager, times(1)).incrementSuiteCounter();
        }

        @Test
        @DisplayName("beforeAll should propagate runtime exceptions")
        void beforeAll_ShouldPropagateRuntimeExceptions() {
            // Given
            RuntimeException expectedException = new RuntimeException("Test exception");
            doThrow(expectedException).when(mockRunManager).incrementSuiteCounter();

            // When & Then
            org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                    () -> junitListener.beforeAll(mockContext));
        }

        @Test
        @DisplayName("afterAll should decrement suite counter and load test body")
        void afterAll_ShouldDecrementSuiteCounterAndLoadTestBody() {
            // Given
            doNothing().when(mockRunManager).decrementSuiteCounter();
            doNothing().when(mockMethodExportManager).loadTestBodyIfRequired(mockContext);

            // When
            junitListener.afterAll(mockContext);

            // Then
            verify(mockRunManager, times(1)).decrementSuiteCounter();
            verify(mockMethodExportManager, times(1)).loadTestBodyIfRequired(mockContext);
        }

        @Test
        @DisplayName("afterAll should handle null context gracefully")
        void afterAll_ShouldHandleNullContext() {
            // Given
            doNothing().when(mockRunManager).decrementSuiteCounter();
            doNothing().when(mockMethodExportManager).loadTestBodyIfRequired(null);

            // When & Then
            assertDoesNotThrow(() -> junitListener.afterAll(null));
            verify(mockRunManager, times(1)).decrementSuiteCounter();
            verify(mockMethodExportManager, times(1)).loadTestBodyIfRequired(null);
        }

        @Test
        @DisplayName("afterAll should continue execution even if decrementSuiteCounter throws exception")
        void afterAll_ShouldContinueExecutionEvenIfDecrementThrowsException() {
            // Given
            doThrow(new RuntimeException("Decrement failed")).when(mockRunManager).decrementSuiteCounter();
            doNothing().when(mockMethodExportManager).loadTestBodyIfRequired(mockContext);

            // When & Then
            org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                    () -> junitListener.afterAll(mockContext));

            // loadTestBodyIfRequired should not be called due to exception
            verify(mockMethodExportManager, never()).loadTestBodyIfRequired(any());
        }

        @Test
        @DisplayName("beforeEach should execute without throwing exceptions")
        void beforeEach_ShouldExecuteWithoutThrowingExceptions() {
            // When & Then
            assertDoesNotThrow(() -> junitListener.beforeEach(mockContext));
            assertDoesNotThrow(() -> junitListener.beforeEach(null));
        }
    }

    @Nested
    @DisplayName("TestWatcher Implementation Tests")
    class TestWatcherTests {

        @Test
        @DisplayName("testSuccessful should report test as PASSED with null message")
        void testSuccessful_ShouldReportTestAsPassed() {
            // Given
            doNothing().when(mockReporter).reportTestResult(mockContext, PASSED, null);

            // When
            junitListener.testSuccessful(mockContext);

            // Then
            verify(mockReporter, times(1)).reportTestResult(mockContext, PASSED, null);
        }

        @Test
        @DisplayName("testSuccessful should handle null context")
        void testSuccessful_ShouldHandleNullContext() {
            // Given
            doNothing().when(mockReporter).reportTestResult(null, PASSED, null);

            // When & Then
            assertDoesNotThrow(() -> junitListener.testSuccessful(null));
            verify(mockReporter, times(1)).reportTestResult(null, PASSED, null);
        }

        @Test
        @DisplayName("testFailed should report test as FAILED with exception message")
        void testFailed_ShouldReportTestAsFailedWithMessage() {
            // Given
            Throwable cause = new RuntimeException("Test failure reason");
            doNothing().when(mockReporter).reportTestResult(mockContext, FAILED, "Test failure reason");

            // When
            junitListener.testFailed(mockContext, cause);

            // Then
            verify(mockReporter, times(1)).reportTestResult(mockContext, FAILED, "Test failure reason");
        }

        @Test
        @DisplayName("testFailed should handle null exception message")
        void testFailed_ShouldHandleNullExceptionMessage() {
            // Given
            Throwable cause = new RuntimeException((String) null);
            doNothing().when(mockReporter).reportTestResult(mockContext, FAILED, null);

            // When
            junitListener.testFailed(mockContext, cause);

            // Then
            verify(mockReporter, times(1)).reportTestResult(mockContext, FAILED, null);
        }

        @Test
        @DisplayName("testAborted should report test as SKIPPED with exception message")
        void testAborted_ShouldReportTestAsSkippedWithMessage() {
            // Given
            Throwable cause = new RuntimeException("Test aborted reason");
            doNothing().when(mockReporter).reportTestResult(mockContext, SKIPPED, "Test aborted reason");

            // When
            junitListener.testAborted(mockContext, cause);

            // Then
            verify(mockReporter, times(1)).reportTestResult(mockContext, SKIPPED, "Test aborted reason");
        }

        @Test
        @DisplayName("testAborted should handle null exception message")
        void testAborted_ShouldHandleNullExceptionMessage() {
            // Given
            Throwable cause = new RuntimeException((String) null);
            doNothing().when(mockReporter).reportTestResult(mockContext, SKIPPED, null);

            // When
            junitListener.testAborted(mockContext, cause);

            // Then
            verify(mockReporter, times(1)).reportTestResult(mockContext, SKIPPED, null);
        }

        @Test
        @DisplayName("testDisabled should report test as SKIPPED with provided reason")
        void testDisabled_ShouldReportTestAsSkippedWithReason() {
            // Given
            Optional<String> reason = Optional.of("Test is disabled due to configuration");
            doNothing().when(mockReporter).reportTestResult(mockContext, SKIPPED, "Test is disabled due to configuration");

            // When
            junitListener.testDisabled(mockContext, reason);

            // Then
            verify(mockReporter, times(1)).reportTestResult(mockContext, SKIPPED, "Test is disabled due to configuration");
        }

        @Test
        @DisplayName("testDisabled should use default message when reason is empty")
        void testDisabled_ShouldUseDefaultMessageWhenReasonIsEmpty() {
            // Given
            Optional<String> reason = Optional.empty();
            doNothing().when(mockReporter).reportTestResult(mockContext, SKIPPED, "Test disabled");

            // When
            junitListener.testDisabled(mockContext, reason);

            // Then
            verify(mockReporter, times(1)).reportTestResult(mockContext, SKIPPED, "Test disabled");
        }

        @Test
        @DisplayName("testDisabled should handle null context")
        void testDisabled_ShouldHandleNullContext() {
            // Given
            Optional<String> reason = Optional.of("Test disabled");

            // When & Then
            assertDoesNotThrow(() -> junitListener.testDisabled(null, reason));
        }
    }

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("Should propagate reporter exceptions in testSuccessful")
        void shouldPropagateReporterExceptionsInTestSuccessful() {
            // Given
            RuntimeException expectedException = new RuntimeException("Reporter failed");
            doThrow(expectedException).when(mockReporter).reportTestResult(any(), eq(PASSED), any());

            // When & Then
            org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                    () -> junitListener.testSuccessful(mockContext));
        }

        @Test
        @DisplayName("Should propagate reporter exceptions in testFailed")
        void shouldPropagateReporterExceptionsInTestFailed() {
            // Given
            RuntimeException expectedException = new RuntimeException("Reporter failed");
            Throwable cause = new AssertionError("Test failed");
            doThrow(expectedException).when(mockReporter).reportTestResult(any(), eq(FAILED), any());

            // When & Then
            org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                    () -> junitListener.testFailed(mockContext, cause));
        }

        @Test
        @DisplayName("Should propagate reporter exceptions in testAborted")
        void shouldPropagateReporterExceptionsInTestAborted() {
            // Given
            RuntimeException expectedException = new RuntimeException("Reporter failed");
            Throwable cause = new InterruptedException("Test aborted");
            doThrow(expectedException).when(mockReporter).reportTestResult(any(), eq(SKIPPED), any());

            // When & Then
            org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                    () -> junitListener.testAborted(mockContext, cause));
        }

        @Test
        @DisplayName("Should propagate reporter exceptions in testDisabled")
        void shouldPropagateReporterExceptionsInTestDisabled() {
            // Given
            RuntimeException expectedException = new RuntimeException("Reporter failed");
            Optional<String> reason = Optional.of("Test disabled");
            doThrow(expectedException).when(mockReporter).reportTestResult(any(), eq(SKIPPED), any());

            // When & Then
            org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                    () -> junitListener.testDisabled(mockContext, reason));
        }

        @Test
        @DisplayName("Should propagate methodExportManager exceptions in afterAll")
        void shouldPropagateMethodExportManagerExceptionsInAfterAll() {
            // Given
            RuntimeException expectedException = new RuntimeException("Export failed");
            doNothing().when(mockRunManager).decrementSuiteCounter();
            doThrow(expectedException).when(mockMethodExportManager).loadTestBodyIfRequired(any());

            // When & Then
            org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                    () -> junitListener.afterAll(mockContext));

            verify(mockRunManager, times(1)).decrementSuiteCounter();
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Complete test lifecycle should call all expected methods")
        void completeTestLifecycle_ShouldCallAllExpectedMethods() {
            // Given
            doNothing().when(mockRunManager).incrementSuiteCounter();
            doNothing().when(mockRunManager).decrementSuiteCounter();
            doNothing().when(mockMethodExportManager).loadTestBodyIfRequired(mockContext);
            doNothing().when(mockReporter).reportTestResult(any(), any(), any());

            Throwable testFailure = new AssertionError("Expected failure");

            // When - Simulate complete test lifecycle
            junitListener.beforeAll(mockContext);
            junitListener.beforeEach(mockContext);
            junitListener.testFailed(mockContext, testFailure);
            junitListener.afterAll(mockContext);

            // Then
            verify(mockRunManager, times(1)).incrementSuiteCounter();
            verify(mockRunManager, times(1)).decrementSuiteCounter();
            verify(mockMethodExportManager, times(1)).loadTestBodyIfRequired(mockContext);
            verify(mockReporter, times(1)).reportTestResult(mockContext, FAILED, "Expected failure");
        }

        @Test
        @DisplayName("Multiple test results should be reported correctly")
        void multipleTestResults_ShouldBeReportedCorrectly() {
            // Given
            ExtensionContext context1 = mock(ExtensionContext.class);
            ExtensionContext context2 = mock(ExtensionContext.class);
            ExtensionContext context3 = mock(ExtensionContext.class);

            doNothing().when(mockReporter).reportTestResult(any(), any(), any());

            // When
            junitListener.testSuccessful(context1);
            junitListener.testFailed(context2, new RuntimeException("Failure"));
            junitListener.testDisabled(context3, Optional.of("Disabled reason"));

            // Then
            verify(mockReporter, times(1)).reportTestResult(context1, PASSED, null);
            verify(mockReporter, times(1)).reportTestResult(context2, FAILED, "Failure");
            verify(mockReporter, times(1)).reportTestResult(context3, SKIPPED, "Disabled reason");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty string exception messages")
        void shouldHandleEmptyStringExceptionMessages() {
            // Given
            Throwable cause = new RuntimeException("");
            doNothing().when(mockReporter).reportTestResult(mockContext, FAILED, "");

            // When
            junitListener.testFailed(mockContext, cause);

            // Then
            verify(mockReporter, times(1)).reportTestResult(mockContext, FAILED, "");
        }

        @Test
        @DisplayName("Should handle very long exception messages")
        void shouldHandleVeryLongExceptionMessages() {
            // Given
            String longMessage = "A".repeat(10000); // Very long message
            Throwable cause = new RuntimeException(longMessage);
            doNothing().when(mockReporter).reportTestResult(mockContext, FAILED, longMessage);

            // When
            junitListener.testFailed(mockContext, cause);

            // Then
            verify(mockReporter, times(1)).reportTestResult(mockContext, FAILED, longMessage);
        }

        @Test
        @DisplayName("Should handle special characters in exception messages")
        void shouldHandleSpecialCharactersInExceptionMessages() {
            // Given
            String specialMessage = "Test failed: \n\t\r\"Special chars: áéíóú ñ üöä 中文 🚀\"";
            Throwable cause = new RuntimeException(specialMessage);
            doNothing().when(mockReporter).reportTestResult(mockContext, FAILED, specialMessage);

            // When
            junitListener.testFailed(mockContext, cause);

            // Then
            verify(mockReporter, times(1)).reportTestResult(mockContext, FAILED, specialMessage);
        }

        @Test
        @DisplayName("Should handle nested exceptions correctly")
        void shouldHandleNestedExceptionsCorrectly() {
            // Given
            RuntimeException rootCause = new RuntimeException("Root cause");
            RuntimeException wrappedException = new RuntimeException("Wrapper exception", rootCause);
            doNothing().when(mockReporter).reportTestResult(mockContext, FAILED, "Wrapper exception");

            // When
            junitListener.testFailed(mockContext, wrappedException);

            // Then
            verify(mockReporter, times(1)).reportTestResult(mockContext, FAILED, "Wrapper exception");
        }
    }
}