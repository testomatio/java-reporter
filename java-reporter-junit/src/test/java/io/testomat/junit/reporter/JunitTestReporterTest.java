package io.testomat.junit.reporter;

import static io.testomat.core.constants.CommonConstants.FAILED;
import static io.testomat.core.constants.CommonConstants.PASSED;
import static io.testomat.core.constants.CommonConstants.SKIPPED;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Comprehensive unit tests for JunitTestReporter class.
 * Tests the public reportTestResult method with detailed failure diagnostics.
 */
@DisplayName("JunitTestReporter Tests")
class JunitTestReporterTest {

    @Mock
    private JUnitTestResultConstructor mockResultConstructor;

    @Mock
    private JunitMetaDataExtractor mockMetaDataExtractor;

    @Mock
    private GlobalRunManager mockRunManager;

    @Mock
    private ExtensionContext mockContext;

    @Mock
    private TestMetadata mockMetadata;

    @Mock
    private TestResult mockTestResult;

    private JunitTestReporter reporter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reporter = new JunitTestReporter(mockResultConstructor, mockMetaDataExtractor, mockRunManager);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor should create instance with non-null dependencies")
        void defaultConstructor_ShouldCreateInstanceWithDependencies() {
            assertDoesNotThrow(() -> {
                JunitTestReporter defaultReporter = new JunitTestReporter();
                assertNotNull(defaultReporter, "Default constructor should create non-null instance");
            });
        }

        @Test
        @DisplayName("Parameterized constructor should create instance with provided dependencies")
        void parameterizedConstructor_ShouldCreateInstanceWithProvidedDependencies() {
            JunitTestReporter paramReporter = new JunitTestReporter(
                    mockResultConstructor, mockMetaDataExtractor, mockRunManager);

            assertNotNull(paramReporter, "Parameterized constructor should create non-null instance");
        }

        @Test
        @DisplayName("Parameterized constructor should accept null dependencies without throwing")
        void parameterizedConstructor_ShouldAcceptNullDependencies() {
            assertDoesNotThrow(() -> {
                JunitTestReporter nullDepsReporter = new JunitTestReporter(null, null, null);
                assertNotNull(nullDepsReporter, "Constructor should accept null dependencies");
            });
        }
    }

    @Nested
    @DisplayName("Successful Reporting Scenarios")
    class SuccessfulReportingTests {

        @Test
        @DisplayName("Should report test result successfully when runManager is active")
        void shouldReportTestResultSuccessfullyWhenRunManagerIsActive() {
            String testStatus = PASSED;
            String testMessage = "Test passed successfully";
            String testTitle = "Sample Test";

            when(mockRunManager.isActive()).thenReturn(true);
            when(mockMetadata.getTitle()).thenReturn(testTitle);
            when(mockMetaDataExtractor.extractTestMetadata(mockContext)).thenReturn(mockMetadata);
            when(mockResultConstructor.constructTestRunResult(mockMetadata, testMessage, testStatus, mockContext))
                    .thenReturn(mockTestResult);
            doNothing().when(mockRunManager).reportTest(mockTestResult);

            System.out.println("Testing successful reporting scenario:");
            System.out.println("Status: " + testStatus);
            System.out.println("Message: " + testMessage);
            System.out.println("RunManager active: true");

            assertDoesNotThrow(() -> {
                reporter.reportTestResult(mockContext, testStatus, testMessage);
            }, "Should not throw exception during successful reporting");

            assertAll("Verify correct interaction sequence for successful reporting",
                    () -> verify(mockRunManager, times(1)).isActive(),
                    () -> verify(mockMetaDataExtractor, times(1)).extractTestMetadata(mockContext),
                    () -> verify(mockResultConstructor, times(1)).constructTestRunResult(
                            mockMetadata, testMessage, testStatus, mockContext),
                    () -> verify(mockRunManager, times(1)).reportTest(mockTestResult)
            );

            System.out.println("All interactions verified successfully");
        }

        @Test
        @DisplayName("Should handle null message parameter correctly")
        void shouldHandleNullMessageParameterCorrectly() {
            String testStatus = FAILED;
            String testTitle = "Failed Test";

            when(mockRunManager.isActive()).thenReturn(true);
            when(mockMetadata.getTitle()).thenReturn(testTitle);
            when(mockMetaDataExtractor.extractTestMetadata(mockContext)).thenReturn(mockMetadata);
            when(mockResultConstructor.constructTestRunResult(mockMetadata, null, testStatus, mockContext))
                    .thenReturn(mockTestResult);
            doNothing().when(mockRunManager).reportTest(mockTestResult);

            System.out.println("Testing null message handling:");
            System.out.println("Status: " + testStatus);
            System.out.println("Message: null");

            assertDoesNotThrow(() -> {
                reporter.reportTestResult(mockContext, testStatus, null);
            }, "Should handle null message without throwing exception");

            verify(mockResultConstructor, times(1)).constructTestRunResult(
                    mockMetadata, null, testStatus, mockContext);

            System.out.println("Null message handled correctly");
        }

        @Test
        @DisplayName("Should handle different test statuses correctly")
        void shouldHandleDifferentTestStatusesCorrectly() {
            String[] testStatuses = {PASSED, FAILED, SKIPPED};
            String testMessage = "Test result message";
            String testTitle = "Multi-status Test";

            when(mockRunManager.isActive()).thenReturn(true);
            when(mockMetadata.getTitle()).thenReturn(testTitle);
            when(mockMetaDataExtractor.extractTestMetadata(mockContext)).thenReturn(mockMetadata);
            when(mockResultConstructor.constructTestRunResult(any(), eq(testMessage), any(), eq(mockContext)))
                    .thenReturn(mockTestResult);
            doNothing().when(mockRunManager).reportTest(mockTestResult);

            for (String status : testStatuses) {
                System.out.println("Testing status: " + status);

                assertDoesNotThrow(() -> {
                    reporter.reportTestResult(mockContext, status, testMessage);
                }, "Should handle status '" + status + "' without throwing exception");
            }

            verify(mockResultConstructor, times(testStatuses.length)).constructTestRunResult(
                    any(), eq(testMessage), any(), eq(mockContext));

            System.out.println("All statuses handled correctly: " + java.util.Arrays.toString(testStatuses));
        }
    }

    @Nested
    @DisplayName("Early Return Scenarios")
    class EarlyReturnTests {

        @Test
        @DisplayName("Should return early when runManager is not active")
        void shouldReturnEarlyWhenRunManagerIsNotActive() {
            String testStatus = PASSED;
            String testMessage = "Test message";

            when(mockRunManager.isActive()).thenReturn(false);

            System.out.println("Testing early return scenario:");
            System.out.println("RunManager active: false");
            System.out.println("Expected: method should return without further processing");

            assertDoesNotThrow(() -> {
                reporter.reportTestResult(mockContext, testStatus, testMessage);
            }, "Should not throw exception when runManager is inactive");

            assertAll("Verify only isActive() is called when runManager is inactive",
                    () -> verify(mockRunManager, times(1)).isActive(),
                    () -> verifyNoInteractions(mockMetaDataExtractor),
                    () -> verifyNoInteractions(mockResultConstructor),
                    () -> verify(mockRunManager, never()).reportTest(any())
            );

            System.out.println("Early return verified - no further processing occurred");
        }

        @Test
        @DisplayName("Should handle multiple calls with inactive runManager efficiently")
        void shouldHandleMultipleCallsWithInactiveRunManagerEfficiently() {
            when(mockRunManager.isActive()).thenReturn(false);

            System.out.println("Testing multiple calls with inactive runManager");

            for (int i = 0; i < 5; i++) {
                reporter.reportTestResult(mockContext, PASSED, "Message " + i);
            }

            verify(mockRunManager, times(5)).isActive();
            verifyNoInteractions(mockMetaDataExtractor);
            verifyNoInteractions(mockResultConstructor);

            System.out.println("Multiple calls handled efficiently");
        }
    }

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("Should throw ReportTestResultException when metaDataExtractor fails")
        void shouldThrowReportTestResultExceptionWhenMetaDataExtractorFails() {
            String testStatus = FAILED;
            String testMessage = "Test message";
            RuntimeException extractorException = new RuntimeException("Metadata extraction failed");

            when(mockRunManager.isActive()).thenReturn(true);
            when(mockMetaDataExtractor.extractTestMetadata(mockContext)).thenThrow(extractorException);

            System.out.println("Testing exception handling for metadata extractor failure");
            System.out.println("Expected exception: ReportTestResultException");

            ReportTestResultException exception = assertThrows(
                    ReportTestResultException.class,
                    () -> reporter.reportTestResult(mockContext, testStatus, testMessage),
                    "Should throw ReportTestResultException when metadata extraction fails"
            );

            assertEquals("Failed to report test result for: Unknown Test", exception.getMessage(),
                    String.format("Exception message should indicate unknown test. Expected: 'Failed to report test result for: Unknown Test', " +
                            "but was: '%s'", exception.getMessage()));

            assertAll("Verify interaction pattern when metadata extraction fails",
                    () -> verify(mockRunManager, times(1)).isActive(),
                    () -> verify(mockMetaDataExtractor, times(1)).extractTestMetadata(mockContext),
                    () -> verify(mockResultConstructor, never()).constructTestRunResult(any(), any(), any(), any()),
                    () -> verify(mockRunManager, never()).reportTest(any())
            );

            System.out.println("Metadata extractor failure handled correctly");
        }

        @Test
        @DisplayName("Should throw ReportTestResultException when resultConstructor fails")
        void shouldThrowReportTestResultExceptionWhenResultConstructorFails() {
            String testStatus = FAILED;
            String testMessage = "Test message";
            String testTitle = "Failed Construction Test";
            RuntimeException constructorException = new RuntimeException("Result construction failed");

            when(mockRunManager.isActive()).thenReturn(true);
            when(mockMetadata.getTitle()).thenReturn(testTitle);
            when(mockMetaDataExtractor.extractTestMetadata(mockContext)).thenReturn(mockMetadata);
            when(mockResultConstructor.constructTestRunResult(mockMetadata, testMessage, testStatus, mockContext))
                    .thenThrow(constructorException);

            System.out.println("Testing exception handling for result constructor failure");
            System.out.println("Test title: " + testTitle);

            ReportTestResultException exception = assertThrows(
                    ReportTestResultException.class,
                    () -> reporter.reportTestResult(mockContext, testStatus, testMessage),
                    "Should throw ReportTestResultException when result construction fails"
            );

            assertEquals("Failed to report test result for: " + testTitle, exception.getMessage(),
                    String.format("Exception message should include test title. Expected: 'Failed to report test result for: %s', " +
                            "but was: '%s'", testTitle, exception.getMessage()));

            assertAll("Verify interaction pattern when result construction fails",
                    () -> verify(mockRunManager, times(1)).isActive(),
                    () -> verify(mockMetaDataExtractor, times(1)).extractTestMetadata(mockContext),
                    () -> verify(mockResultConstructor, times(1)).constructTestRunResult(
                            mockMetadata, testMessage, testStatus, mockContext),
                    () -> verify(mockRunManager, never()).reportTest(any())
            );

            System.out.println("Result constructor failure handled correctly");
        }

        @Test
        @DisplayName("Should throw ReportTestResultException when runManager.reportTest fails")
        void shouldThrowReportTestResultExceptionWhenRunManagerReportTestFails() {
            String testStatus = PASSED;
            String testMessage = "Test message";
            String testTitle = "Report Failure Test";
            RuntimeException reportException = new RuntimeException("Report test failed");

            when(mockRunManager.isActive()).thenReturn(true);
            when(mockMetadata.getTitle()).thenReturn(testTitle);
            when(mockMetaDataExtractor.extractTestMetadata(mockContext)).thenReturn(mockMetadata);
            when(mockResultConstructor.constructTestRunResult(mockMetadata, testMessage, testStatus, mockContext))
                    .thenReturn(mockTestResult);
            doThrow(reportException).when(mockRunManager).reportTest(mockTestResult);

            System.out.println("Testing exception handling for runManager.reportTest failure");
            System.out.println("Test title: " + testTitle);

            ReportTestResultException exception = assertThrows(
                    ReportTestResultException.class,
                    () -> reporter.reportTestResult(mockContext, testStatus, testMessage),
                    "Should throw ReportTestResultException when reportTest fails"
            );

            assertEquals("Failed to report test result for: " + testTitle, exception.getMessage(),
                    String.format("Exception message should include test title. Expected: 'Failed to report test result for: %s', " +
                            "but was: '%s'", testTitle, exception.getMessage()));

            verify(mockRunManager, times(1)).reportTest(mockTestResult);

            System.out.println("runManager.reportTest failure handled correctly");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null ExtensionContext")
        void shouldHandleNullExtensionContext() {
            String testStatus = PASSED;
            String testMessage = "Test message";

            when(mockRunManager.isActive()).thenReturn(true);
            when(mockMetaDataExtractor.extractTestMetadata(null)).thenReturn(mockMetadata);
            when(mockMetadata.getTitle()).thenReturn("Null Context Test");
            when(mockResultConstructor.constructTestRunResult(mockMetadata, testMessage, testStatus, null))
                    .thenReturn(mockTestResult);
            doNothing().when(mockRunManager).reportTest(mockTestResult);

            System.out.println("Testing null ExtensionContext handling");

            assertDoesNotThrow(() -> {
                reporter.reportTestResult(null, testStatus, testMessage);
            }, "Should handle null ExtensionContext without throwing exception");

            verify(mockMetaDataExtractor, times(1)).extractTestMetadata(null);
            verify(mockResultConstructor, times(1)).constructTestRunResult(mockMetadata, testMessage, testStatus, null);

            System.out.println("Null ExtensionContext handled correctly");
        }

        @Test
        @DisplayName("Should handle null status parameter")
        void shouldHandleNullStatusParameter() {
            String testMessage = "Test message";
            String testTitle = "Null Status Test";

            when(mockRunManager.isActive()).thenReturn(true);
            when(mockMetadata.getTitle()).thenReturn(testTitle);
            when(mockMetaDataExtractor.extractTestMetadata(mockContext)).thenReturn(mockMetadata);
            when(mockResultConstructor.constructTestRunResult(mockMetadata, testMessage, null, mockContext))
                    .thenReturn(mockTestResult);
            doNothing().when(mockRunManager).reportTest(mockTestResult);

            System.out.println("Testing null status parameter handling");

            assertDoesNotThrow(() -> {
                reporter.reportTestResult(mockContext, null, testMessage);
            }, "Should handle null status without throwing exception");

            verify(mockResultConstructor, times(1)).constructTestRunResult(mockMetadata, testMessage, null, mockContext);

            System.out.println("Null status parameter handled correctly");
        }

        @Test
        @DisplayName("Should handle empty string parameters")
        void shouldHandleEmptyStringParameters() {
            String emptyStatus = "";
            String emptyMessage = "";
            String testTitle = "Empty Strings Test";

            when(mockRunManager.isActive()).thenReturn(true);
            when(mockMetadata.getTitle()).thenReturn(testTitle);
            when(mockMetaDataExtractor.extractTestMetadata(mockContext)).thenReturn(mockMetadata);
            when(mockResultConstructor.constructTestRunResult(mockMetadata, emptyMessage, emptyStatus, mockContext))
                    .thenReturn(mockTestResult);
            doNothing().when(mockRunManager).reportTest(mockTestResult);

            System.out.println("Testing empty string parameters:");
            System.out.println("Status: '" + emptyStatus + "'");
            System.out.println("Message: '" + emptyMessage + "'");

            assertDoesNotThrow(() -> {
                reporter.reportTestResult(mockContext, emptyStatus, emptyMessage);
            }, "Should handle empty string parameters without throwing exception");

            verify(mockResultConstructor, times(1)).constructTestRunResult(
                    mockMetadata, emptyMessage, emptyStatus, mockContext);

            System.out.println("Empty string parameters handled correctly");
        }

        @Test
        @DisplayName("Should handle very long message parameter")
        void shouldHandleVeryLongMessageParameter() {
            String testStatus = FAILED;
            String longMessage = "A".repeat(10000);
            String testTitle = "Long Message Test";

            when(mockRunManager.isActive()).thenReturn(true);
            when(mockMetadata.getTitle()).thenReturn(testTitle);
            when(mockMetaDataExtractor.extractTestMetadata(mockContext)).thenReturn(mockMetadata);
            when(mockResultConstructor.constructTestRunResult(mockMetadata, longMessage, testStatus, mockContext))
                    .thenReturn(mockTestResult);
            doNothing().when(mockRunManager).reportTest(mockTestResult);

            System.out.println("Testing very long message parameter:");
            System.out.println("Message length: " + longMessage.length());

            assertDoesNotThrow(() -> {
                reporter.reportTestResult(mockContext, testStatus, longMessage);
            }, "Should handle very long message without throwing exception");

            verify(mockResultConstructor, times(1)).constructTestRunResult(
                    mockMetadata, longMessage, testStatus, mockContext);

            System.out.println("Long message parameter handled correctly");
        }

        @Test
        @DisplayName("Should handle special characters in message and status")
        void shouldHandleSpecialCharactersInMessageAndStatus() {
            String specialStatus = "FAILED_🚫";
            String specialMessage = "Test failed: \n\t\r\"Special chars: áéíóú ñ üöä 中文 🚀\"";
            String testTitle = "Special Characters Test";

            when(mockRunManager.isActive()).thenReturn(true);
            when(mockMetadata.getTitle()).thenReturn(testTitle);
            when(mockMetaDataExtractor.extractTestMetadata(mockContext)).thenReturn(mockMetadata);
            when(mockResultConstructor.constructTestRunResult(mockMetadata, specialMessage, specialStatus, mockContext))
                    .thenReturn(mockTestResult);
            doNothing().when(mockRunManager).reportTest(mockTestResult);

            System.out.println("Testing special characters:");
            System.out.println("Status: '" + specialStatus + "'");
            System.out.println("Message (escaped): '" + specialMessage.replace("\n", "\\n").replace("\t", "\\t").replace("\r", "\\r") + "'");

            assertDoesNotThrow(() -> {
                reporter.reportTestResult(mockContext, specialStatus, specialMessage);
            }, "Should handle special characters without throwing exception");

            verify(mockResultConstructor, times(1)).constructTestRunResult(
                    mockMetadata, specialMessage, specialStatus, mockContext);

            System.out.println("Special characters handled correctly");
        }
    }

    @Nested
    @DisplayName("Integration and Performance Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle complete workflow successfully")
        void shouldHandleCompleteWorkflowSuccessfully() {
            String testStatus = PASSED;
            String testMessage = "Integration test message";
            String testTitle = "Integration Test";

            when(mockRunManager.isActive()).thenReturn(true);
            when(mockMetadata.getTitle()).thenReturn(testTitle);
            when(mockMetaDataExtractor.extractTestMetadata(mockContext)).thenReturn(mockMetadata);
            when(mockResultConstructor.constructTestRunResult(mockMetadata, testMessage, testStatus, mockContext))
                    .thenReturn(mockTestResult);
            doNothing().when(mockRunManager).reportTest(mockTestResult);

            System.out.println("Testing complete integration workflow");
            System.out.println("Expected sequence: isActive -> extractMetadata -> constructResult -> reportTest");

            long startTime = System.nanoTime();

            assertDoesNotThrow(() -> {
                reporter.reportTestResult(mockContext, testStatus, testMessage);
            });

            long endTime = System.nanoTime();
            long durationNanos = endTime - startTime;
            long durationMillis = durationNanos / 1_000_000;

            System.out.println("Workflow completed in: " + durationMillis + "ms (" + durationNanos + "ns)");

            verify(mockRunManager, times(1)).isActive();
            verify(mockMetaDataExtractor, times(1)).extractTestMetadata(mockContext);
            verify(mockResultConstructor, times(1)).constructTestRunResult(
                    mockMetadata, testMessage, testStatus, mockContext);
            verify(mockRunManager, times(1)).reportTest(mockTestResult);

            System.out.println("Complete workflow verified successfully");
        }

        @Test
        @DisplayName("Should handle multiple consecutive reports efficiently")
        void shouldHandleMultipleConsecutiveReportsEfficiently() {
            when(mockRunManager.isActive()).thenReturn(true);
            when(mockMetadata.getTitle()).thenReturn("Multiple Reports Test");
            when(mockMetaDataExtractor.extractTestMetadata(mockContext)).thenReturn(mockMetadata);
            when(mockResultConstructor.constructTestRunResult(any(), any(), any(), eq(mockContext)))
                    .thenReturn(mockTestResult);
            doNothing().when(mockRunManager).reportTest(mockTestResult);

            System.out.println("Testing multiple consecutive reports");

            int reportCount = 10;
            for (int i = 0; i < reportCount; i++) {
                reporter.reportTestResult(mockContext, PASSED, "Message " + i);
            }

            verify(mockRunManager, times(reportCount)).isActive();
            verify(mockMetaDataExtractor, times(reportCount)).extractTestMetadata(mockContext);
            verify(mockResultConstructor, times(reportCount)).constructTestRunResult(any(), any(), any(), eq(mockContext));
            verify(mockRunManager, times(reportCount)).reportTest(mockTestResult);

            System.out.println("Multiple reports (" + reportCount + ") handled efficiently");
        }
    }
}