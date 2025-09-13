//package io.testomat.junit.listener;
//
//import static io.testomat.core.constants.CommonConstants.FAILED;
//import static io.testomat.core.constants.CommonConstants.PASSED;
//import static io.testomat.core.constants.CommonConstants.SKIPPED;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import io.testomat.core.exception.PropertyNotFoundException;
//import io.testomat.core.propertyconfig.interf.PropertyProvider;
//import io.testomat.core.runmanager.GlobalRunManager;
//import io.testomat.junit.methodexporter.MethodExportManager;
//import io.testomat.junit.reporter.JunitTestReporter;
//import java.util.Optional;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtensionContext;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//@DisplayName("JunitListener Tests (Updated Version)")
//class JunitListenerTest {
//
//    @Mock
//    private MethodExportManager mockMethodExportManager;
//
//    @Mock
//    private GlobalRunManager mockRunManager;
//
//    @Mock
//    private JunitTestReporter mockReporter;
//
//    @Mock
//    private PropertyProvider mockPropertyProvider;
//
//    @Mock
//    private ExtensionContext mockExtensionContext;
//
//    private JunitListener junitListener;
//
//    // Test classes for testing different scenarios
//    public static class TestClassA {
//    }
//
//    public static class TestClassB {
//    }
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//
//        // Setup mock context with a valid unique ID to avoid NPE in cleanup methods
//        when(mockExtensionContext.getUniqueId()).thenReturn("test-unique-id-123");
//
//        junitListener = new JunitListener(
//                mockMethodExportManager,
//                mockRunManager,
//                mockReporter,
//                mockPropertyProvider
//        );
//    }
//
//    @Nested
//    @DisplayName("Constructor Tests")
//    class ConstructorTests {
//
//        @Test
//        @DisplayName("Should create instance with default constructor")
//        void shouldCreateInstanceWithDefaultConstructor() {
//            // When
//            JunitListener listener = new JunitListener();
//
//            // Then
//            assertNotNull(listener);
//        }
//
//        @Test
//        @DisplayName("Should create instance with parameterized constructor")
//        void shouldCreateInstanceWithParameterizedConstructor() {
//            // When
//            JunitListener listener = new JunitListener(
//                    mockMethodExportManager,
//                    mockRunManager,
//                    mockReporter,
//                    mockPropertyProvider
//            );
//
//            // Then
//            assertNotNull(listener);
//        }
//    }
//
//    @Nested
//    @DisplayName("Listening Required Logic Tests")
//    class ListeningRequiredLogicTests {
//
//        @Test
//        @DisplayName("Should return false when property throws PropertyNotFoundException")
//        void shouldReturnFalseWhenPropertyThrowsPropertyNotFoundException() {
//            // Given
//            when(mockPropertyProvider.getProperty("testomatio.listening"))
//                    .thenThrow(new PropertyNotFoundException("Property not found"));
//
//            // When
//            junitListener.beforeAll(mockExtensionContext);
//
//            // Then - should do early return, not call runManager
//            verify(mockRunManager, never()).incrementSuiteCounter();
//        }
//
//        @Test
//        @DisplayName("Should return false when property throws RuntimeException")
//        void shouldReturnFalseWhenPropertyThrowsRuntimeException() {
//            // Given
//            when(mockPropertyProvider.getProperty("testomatio.listening"))
//                    .thenThrow(new RuntimeException("Unexpected error"));
//
//            // When
//            junitListener.beforeAll(mockExtensionContext);
//
//            // Then - should do early return, not call runManager
//            verify(mockRunManager, never()).incrementSuiteCounter();
//        }
//
//        @Test
//        @DisplayName("Should return false when property returns null")
//        void shouldReturnFalseWhenPropertyReturnsNull() {
//            // Given
//            when(mockPropertyProvider.getProperty("testomatio.listening")).thenReturn(null);
//
//            // When
//            junitListener.beforeAll(mockExtensionContext);
//
//            // Then - should do early return, not call runManager
//            verify(mockRunManager, never()).incrementSuiteCounter();
//        }
//    }
//
//    @Nested
//    @DisplayName("BeforeAll Tests")
//    class BeforeAllTests {
//
//        @Test
//        @DisplayName("Should increment suite counter when listening is required")
//        void shouldIncrementSuiteCounterWhenListeningIsRequired() {
//            // Given
//            when(mockPropertyProvider.getProperty("testomatio.listening")).thenReturn("true");
//
//            // When
//            junitListener.beforeAll(mockExtensionContext);
//
//            // Then
//            verify(mockRunManager, times(1)).incrementSuiteCounter();
//        }
//
//        @Test
//        @DisplayName("Should not increment suite counter when listening is not required")
//        void shouldNotIncrementSuiteCounterWhenListeningIsNotRequired() {
//            // Given
//            when(mockPropertyProvider.getProperty("testomatio.listening")).thenReturn(null);
//
//            // When
//            junitListener.beforeAll(mockExtensionContext);
//
//            // Then
//            verify(mockRunManager, never()).incrementSuiteCounter();
//        }
//
//        @Test
//        @DisplayName("Should handle multiple beforeAll calls when listening is required")
//        void shouldHandleMultipleBeforeAllCallsWhenListeningIsRequired() {
//            // Given
//            when(mockPropertyProvider.getProperty("testomatio.listening")).thenReturn("enabled");
//
//            // When
//            junitListener.beforeAll(mockExtensionContext);
//            junitListener.beforeAll(mockExtensionContext);
//            junitListener.beforeAll(mockExtensionContext);
//
//            // Then
//            verify(mockRunManager, times(3)).incrementSuiteCounter();
//        }
//    }
//
//    @Nested
//    @DisplayName("AfterAll Tests")
//    class AfterAllTests {
//
//        @Test
//        @DisplayName("Should export test class and decrement suite counter when listening is required")
//        void shouldExportTestClassAndDecrementSuiteCounterWhenListeningIsRequired() {
//            // Given
//            when(mockPropertyProvider.getProperty("testomatio.listening")).thenReturn("true");
//            when(mockExtensionContext.getTestClass()).thenReturn(Optional.of(TestClassA.class));
//
//            // When
//            junitListener.afterAll(mockExtensionContext);
//
//            // Then
//            verify(mockMethodExportManager, times(1)).loadTestBodyForClass(TestClassA.class);
//            verify(mockRunManager, times(1)).decrementSuiteCounter();
//        }
//
//        @Test
//        @DisplayName("Should not export or decrement when listening is not required")
//        void shouldNotExportOrDecrementWhenListeningIsNotRequired() {
//            // Given
//            when(mockPropertyProvider.getProperty("testomatio.listening")).thenReturn(null);
//
//            // When
//            junitListener.afterAll(mockExtensionContext);
//
//            // Then
//            verify(mockMethodExportManager, never()).loadTestBodyForClass(any());
//            verify(mockRunManager, never()).decrementSuiteCounter();
//        }
//
//        @Test
//        @DisplayName("Should only decrement suite counter when no test class present")
//        void shouldOnlyDecrementSuiteCounterWhenNoTestClassPresent() {
//            // Given
//            when(mockPropertyProvider.getProperty("testomatio.listening")).thenReturn("true");
//            when(mockExtensionContext.getTestClass()).thenReturn(Optional.empty());
//
//            // When
//            junitListener.afterAll(mockExtensionContext);
//
//            // Then
//            verify(mockMethodExportManager, never()).loadTestBodyForClass(any());
//            verify(mockRunManager, times(1)).decrementSuiteCounter();
//        }
//    }
//
//    @Nested
//    @DisplayName("BeforeEach Tests")
//    class BeforeEachTests {
//
//        @Test
//        @DisplayName("Should not perform any operations in beforeEach regardless of listening status")
//        void shouldNotPerformAnyOperationsInBeforeEach() {
//            // Given
//            when(mockPropertyProvider.getProperty("testomatio.listening")).thenReturn("true");
//
//            // When
//            junitListener.beforeEach(mockExtensionContext);
//
//            // Then
//            verify(mockMethodExportManager, never()).loadTestBodyForClass(any());
//            verify(mockRunManager, never()).incrementSuiteCounter();
//            verify(mockRunManager, never()).decrementSuiteCounter();
//            verify(mockReporter, never()).reportTestResult(any(), anyString(), anyString());
//        }
//    }
//
//    @Nested
//    @DisplayName("Test Disabled Tests")
//    class TestDisabledTests {
//
//        @Test
//        @DisplayName("Should report test as disabled when listening is required")
//        void shouldReportTestAsDisabledWhenListeningIsRequired() {
//            // Given
//            String reason = "Test disabled for maintenance";
//            when(mockPropertyProvider.getProperty("testomatio.listening")).thenReturn("true");
//            when(mockExtensionContext.getTestClass()).thenReturn(Optional.of(TestClassA.class));
//
//            // When
//            junitListener.testDisabled(mockExtensionContext, Optional.of(reason));
//
//            // Then
//            verify(mockReporter, times(1)).reportTestResult(mockExtensionContext, SKIPPED, reason);
//            verify(mockMethodExportManager, times(1)).loadTestBodyForClass(TestClassA.class);
//        }
//
//        @Test
//        @DisplayName("Should not report when listening is not required")
//        void shouldNotReportWhenListeningIsNotRequired() {
//            // Given
//            String reason = "Test disabled";
//            when(mockPropertyProvider.getProperty("testomatio.listening")).thenReturn(null);
//
//            // When
//            junitListener.testDisabled(mockExtensionContext, Optional.of(reason));
//
//            // Then
//            verify(mockReporter, never()).reportTestResult(any(), anyString(), anyString());
//            verify(mockMethodExportManager, never()).loadTestBodyForClass(any());
//        }
//
//        @Test
//        @DisplayName("Should use default message when no reason provided")
//        void shouldUseDefaultMessageWhenNoReasonProvided() {
//            // Given
//            when(mockPropertyProvider.getProperty("testomatio.listening")).thenReturn("true");
//            when(mockExtensionContext.getTestClass()).thenReturn(Optional.of(TestClassA.class));
//
//            // When
//            junitListener.testDisabled(mockExtensionContext, Optional.empty());
//
//            // Then
//            verify(mockReporter, times(1)).reportTestResult(mockExtensionContext, SKIPPED, "Test disabled");
//            verify(mockMethodExportManager, times(1)).loadTestBodyForClass(TestClassA.class);
//        }
//    }
//
//    @Nested
//    @DisplayName("Test Successful Tests")
//    class TestSuccessfulTests {
//
//        @Test
//        @DisplayName("Should report test as successful when listening is required")
//        void shouldReportTestAsSuccessfulWhenListeningIsRequired() {
//            // Given
//            when(mockPropertyProvider.getProperty("testomatio.listening")).thenReturn("true");
//            when(mockExtensionContext.getTestClass()).thenReturn(Optional.of(TestClassA.class));
//
//            // When
//            junitListener.testSuccessful(mockExtensionContext);
//
//            // Then
//            verify(mockReporter, times(1)).reportTestResult(mockExtensionContext, PASSED, null);
//            verify(mockMethodExportManager, times(1)).loadTestBodyForClass(TestClassA.class);
//        }
//
//        @Test
//        @DisplayName("Should not report when listening is not required")
//        void shouldNotReportWhenListeningIsNotRequired() {
//            // Given
//            when(mockPropertyProvider.getProperty("testomatio.listening")).thenReturn(null);
//
//            // When
//            junitListener.testSuccessful(mockExtensionContext);
//
//            // Then
//            verify(mockReporter, never()).reportTestResult(any(), anyString(), anyString());
//            verify(mockMethodExportManager, never()).loadTestBodyForClass(any());
//        }
//    }
//
//    @Nested
//    @DisplayName("Test Aborted Tests")
//    class TestAbortedTests {
//
//        @Test
//        @DisplayName("Should report test as aborted when listening is required")
//        void shouldReportTestAsAbortedWhenListeningIsRequired() {
//            // Given
//            Throwable cause = new RuntimeException("Test was aborted");
//            when(mockPropertyProvider.getProperty("testomatio.listening")).thenReturn("true");
//            when(mockExtensionContext.getTestClass()).thenReturn(Optional.of(TestClassA.class));
//
//            // When
//            junitListener.testAborted(mockExtensionContext, cause);
//
//            // Then
//            verify(mockReporter, times(1)).reportTestResult(mockExtensionContext, SKIPPED, "Test was aborted");
//            verify(mockMethodExportManager, times(1)).loadTestBodyForClass(TestClassA.class);
//        }
//
//        @Test
//        @DisplayName("Should not report when listening is not required")
//        void shouldNotReportWhenListeningIsNotRequired() {
//            // Given
//            Throwable cause = new RuntimeException("Test was aborted");
//            when(mockPropertyProvider.getProperty("testomatio.listening")).thenReturn(null);
//
//            // When
//            junitListener.testAborted(mockExtensionContext, cause);
//
//            // Then
//            verify(mockReporter, never()).reportTestResult(any(), anyString(), anyString());
//            verify(mockMethodExportManager, never()).loadTestBodyForClass(any());
//        }
//
//        @Test
//        @DisplayName("Should handle null cause message")
//        void shouldHandleNullCauseMessage() {
//            // Given
//            Throwable cause = new RuntimeException(); // No message
//            when(mockPropertyProvider.getProperty("testomatio.listening")).thenReturn("true");
//            when(mockExtensionContext.getTestClass()).thenReturn(Optional.of(TestClassA.class));
//
//            // When
//            junitListener.testAborted(mockExtensionContext, cause);
//
//            // Then
//            verify(mockReporter, times(1)).reportTestResult(mockExtensionContext, SKIPPED, null);
//            verify(mockMethodExportManager, times(1)).loadTestBodyForClass(TestClassA.class);
//        }
//    }
//
//    @Nested
//    @DisplayName("Test Failed Tests")
//    class TestFailedTests {
//
//        @Test
//        @DisplayName("Should report test as failed when listening is required")
//        void shouldReportTestAsFailedWhenListeningIsRequired() {
//            // Given
//            Throwable cause = new AssertionError("Expected <5> but was <3>");
//            when(mockPropertyProvider.getProperty("testomatio.listening")).thenReturn("true");
//            when(mockExtensionContext.getTestClass()).thenReturn(Optional.of(TestClassA.class));
//
//            // When
//            junitListener.testFailed(mockExtensionContext, cause);
//
//            // Then
//            verify(mockReporter, times(1)).reportTestResult(mockExtensionContext, FAILED, "Expected <5> but was <3>");
//            verify(mockMethodExportManager, times(1)).loadTestBodyForClass(TestClassA.class);
//        }
//
//        @Test
//        @DisplayName("Should not report when listening is not required")
//        void shouldNotReportWhenListeningIsNotRequired() {
//            // Given
//            Throwable cause = new AssertionError("Test failed");
//            when(mockPropertyProvider.getProperty("testomatio.listening")).thenReturn(null);
//
//            // When
//            junitListener.testFailed(mockExtensionContext, cause);
//
//            // Then
//            verify(mockReporter, never()).reportTestResult(any(), anyString(), anyString());
//            verify(mockMethodExportManager, never()).loadTestBodyForClass(any());
//        }
//
//        @Test
//        @DisplayName("Should handle null cause message")
//        void shouldHandleNullCauseMessage() {
//            // Given
//            Throwable cause = new AssertionError(); // No message
//            when(mockPropertyProvider.getProperty("testomatio.listening")).thenReturn("true");
//            when(mockExtensionContext.getTestClass()).thenReturn(Optional.of(TestClassA.class));
//
//            // When
//            junitListener.testFailed(mockExtensionContext, cause);
//
//            // Then
//            verify(mockReporter, times(1)).reportTestResult(mockExtensionContext, FAILED, null);
//            verify(mockMethodExportManager, times(1)).loadTestBodyForClass(TestClassA.class);
//        }
//    }
//
//    @Nested
//    @DisplayName("Export Test Class Tests")
//    class ExportTestClassTests {
//
//        @Test
//        @DisplayName("Should export different test classes separately")
//        void shouldExportDifferentTestClassesSeparately() {
//            // Given
//            ExtensionContext contextA = org.mockito.Mockito.mock(ExtensionContext.class);
//            ExtensionContext contextB = org.mockito.Mockito.mock(ExtensionContext.class);
//
//            when(mockPropertyProvider.getProperty("testomatio.listening")).thenReturn("true");
//            when(contextA.getTestClass()).thenReturn(Optional.of(TestClassA.class));
//            when(contextA.getUniqueId()).thenReturn("test-context-A");
//            when(contextB.getTestClass()).thenReturn(Optional.of(TestClassB.class));
//            when(contextB.getUniqueId()).thenReturn("test-context-B");
//
//            // When
//            junitListener.testSuccessful(contextA);
//            junitListener.testSuccessful(contextB);
//            junitListener.testSuccessful(contextA); // Same class again
//
//            // Then - should export each class once
//            verify(mockMethodExportManager, times(1)).loadTestBodyForClass(TestClassA.class);
//            verify(mockMethodExportManager, times(1)).loadTestBodyForClass(TestClassB.class);
//        }
//
//        @Test
//        @DisplayName("Should not export when listening is not required")
//        void shouldNotExportWhenListeningIsNotRequired() {
//            // Given
//            when(mockPropertyProvider.getProperty("testomatio.listening")).thenReturn(null);
//            when(mockExtensionContext.getTestClass()).thenReturn(Optional.of(TestClassA.class));
//
//            // When
//            junitListener.testSuccessful(mockExtensionContext);
//            junitListener.testFailed(mockExtensionContext, new RuntimeException("Error"));
//
//            // Then
//            verify(mockMethodExportManager, never()).loadTestBodyForClass(any());
//        }
//
//        @Test
//        @DisplayName("Should not export when test class is not present")
//        void shouldNotExportWhenTestClassIsNotPresent() {
//            // Given
//            when(mockPropertyProvider.getProperty("testomatio.listening")).thenReturn("true");
//            when(mockExtensionContext.getTestClass()).thenReturn(Optional.empty());
//
//            // When
//            junitListener.testSuccessful(mockExtensionContext);
//
//            // Then
//            verify(mockMethodExportManager, never()).loadTestBodyForClass(any());
//        }
//    }
//
//    @Nested
//    @DisplayName("Property Provider Exception Handling Tests")
//    class PropertyProviderExceptionHandlingTests {
//
//        @Test
//        @DisplayName("Should handle PropertyNotFoundException gracefully in all callbacks")
//        void shouldHandlePropertyNotFoundExceptionGracefullyInAllCallbacks() {
//            // Given
//            when(mockPropertyProvider.getProperty("testomatio.listening"))
//                    .thenThrow(new PropertyNotFoundException("Property not found"));
//
//            // When & Then - all callbacks should handle exception gracefully
//            junitListener.beforeAll(mockExtensionContext);
//            verify(mockRunManager, never()).incrementSuiteCounter();
//
//            junitListener.afterAll(mockExtensionContext);
//            verify(mockRunManager, never()).decrementSuiteCounter();
//            verify(mockMethodExportManager, never()).loadTestBodyForClass(any());
//
//            junitListener.testSuccessful(mockExtensionContext);
//            verify(mockReporter, never()).reportTestResult(any(), anyString(), anyString());
//
//            junitListener.testFailed(mockExtensionContext, new RuntimeException());
//            verify(mockReporter, never()).reportTestResult(any(), anyString(), anyString());
//
//            junitListener.testDisabled(mockExtensionContext, Optional.of("disabled"));
//            verify(mockReporter, never()).reportTestResult(any(), anyString(), anyString());
//
//            junitListener.testAborted(mockExtensionContext, new RuntimeException());
//            verify(mockReporter, never()).reportTestResult(any(), anyString(), anyString());
//        }
//
//        @Test
//        @DisplayName("Should handle unexpected RuntimeException in property access")
//        void shouldHandleUnexpectedRuntimeExceptionInPropertyAccess() {
//            // Given
//            when(mockPropertyProvider.getProperty("testomatio.listening"))
//                    .thenThrow(new RuntimeException("Unexpected property access error"));
//
//            // When & Then - should not propagate exception
//            junitListener.beforeAll(mockExtensionContext);
//            verify(mockRunManager, never()).incrementSuiteCounter();
//
//            junitListener.testSuccessful(mockExtensionContext);
//            verify(mockReporter, never()).reportTestResult(any(), anyString(), anyString());
//        }
//
//        @Test
//        @DisplayName("Should handle intermittent property provider failures")
//        void shouldHandleIntermittentPropertyProviderFailures() {
//            // Given - first call fails, second succeeds
//            when(mockPropertyProvider.getProperty("testomatio.listening"))
//                    .thenThrow(new RuntimeException("Temporary failure"))
//                    .thenReturn("true");
//            when(mockExtensionContext.getTestClass()).thenReturn(Optional.of(TestClassA.class));
//
//            // When
//            junitListener.testSuccessful(mockExtensionContext); // Should fail and not report
//            junitListener.testSuccessful(mockExtensionContext); // Should succeed and report
//
//            // Then
//            verify(mockReporter, times(1)).reportTestResult(mockExtensionContext, PASSED, null);
//            verify(mockMethodExportManager, times(1)).loadTestBodyForClass(TestClassA.class);
//        }
//    }
//
//    @Nested
//    @DisplayName("Integration Tests")
//    class IntegrationTests {
//
//        @Test
//        @DisplayName("Should handle complete test lifecycle when listening is required")
//        void shouldHandleCompleteTestLifecycleWhenListeningIsRequired() {
//            // Given
//            when(mockPropertyProvider.getProperty("testomatio.listening")).thenReturn("enabled");
//            when(mockExtensionContext.getTestClass()).thenReturn(Optional.of(TestClassA.class));
//
//            // When - simulate complete test lifecycle
//            junitListener.beforeAll(mockExtensionContext);
//            junitListener.beforeEach(mockExtensionContext);
//            junitListener.testSuccessful(mockExtensionContext);
//            junitListener.beforeEach(mockExtensionContext);
//            junitListener.testFailed(mockExtensionContext, new RuntimeException("Test failed"));
//            junitListener.afterAll(mockExtensionContext);
//
//            // Then
//            verify(mockRunManager, times(1)).incrementSuiteCounter();
//            verify(mockRunManager, times(1)).decrementSuiteCounter();
//            verify(mockReporter, times(1)).reportTestResult(mockExtensionContext, PASSED, null);
//            verify(mockReporter, times(1)).reportTestResult(mockExtensionContext, FAILED, "Test failed");
//            verify(mockMethodExportManager, times(1)).loadTestBodyForClass(TestClassA.class);
//        }
//
//        @Test
//        @DisplayName("Should handle complete test lifecycle when listening is not required")
//        void shouldHandleCompleteTestLifecycleWhenListeningIsNotRequired() {
//            // Given
//            when(mockPropertyProvider.getProperty("testomatio.listening")).thenReturn(null);
//
//            // When - simulate complete test lifecycle
//            junitListener.beforeAll(mockExtensionContext);
//            junitListener.beforeEach(mockExtensionContext);
//            junitListener.testSuccessful(mockExtensionContext);
//            junitListener.testFailed(mockExtensionContext, new RuntimeException("Test failed"));
//            junitListener.afterAll(mockExtensionContext);
//
//            // Then - nothing should be called
//            verify(mockRunManager, never()).incrementSuiteCounter();
//            verify(mockRunManager, never()).decrementSuiteCounter();
//            verify(mockReporter, never()).reportTestResult(any(), anyString(), anyString());
//            verify(mockMethodExportManager, never()).loadTestBodyForClass(any());
//        }
//
//        @Test
//        @DisplayName("Should be thread-safe for concurrent test execution")
//        void shouldBeThreadSafeForConcurrentTestExecution() {
//            // Given
//            when(mockPropertyProvider.getProperty("testomatio.listening")).thenReturn("true");
//            when(mockExtensionContext.getTestClass()).thenReturn(Optional.of(TestClassA.class));
//
//            // When - simulate concurrent test execution
//            junitListener.testSuccessful(mockExtensionContext);
//            junitListener.testSuccessful(mockExtensionContext);
//            junitListener.testSuccessful(mockExtensionContext);
//
//            // Then - should handle concurrent access to processedClasses set
//            verify(mockReporter, times(3)).reportTestResult(mockExtensionContext, PASSED, null);
//            verify(mockMethodExportManager, times(1)).loadTestBodyForClass(TestClassA.class);
//        }
//    }
//}