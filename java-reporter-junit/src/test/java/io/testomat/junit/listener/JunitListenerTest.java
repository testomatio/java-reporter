package io.testomat.junit.listener;

import io.testomat.core.annotation.TestId;
import io.testomat.core.annotation.Title;
import io.testomat.core.exception.ReportTestResultException;
import io.testomat.core.runmanager.GlobalRunManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;
import java.util.Optional;

import static io.testomat.core.constants.CommonConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("JunitListener Tests")
class JunitListenerTest {

    private TestableJunitListener listener;

    @Mock
    private GlobalRunManager mockRunManager;

    @Mock
    private ExtensionContext mockContext;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() {
        // Встановлюємо тестові properties перед ініціалізацією моків
        System.setProperty("testomatio.api.key", "test-key-12345");
        System.setProperty("testomatio.url", "https://app.testomat.io");
        System.setProperty("testomatio.create", "false");
        System.setProperty("testomatio.publish", "false");

        mockitoCloseable = MockitoAnnotations.openMocks(this);

        listener = new TestableJunitListener(mockRunManager);

        // Default active state
        doReturn(true).when(mockRunManager).isActive();
    }

    @AfterEach
    void tearDown() throws Exception {
        // Очищаємо тестові properties
        System.clearProperty("testomatio.api.key");
        System.clearProperty("testomatio.url");
        System.clearProperty("testomatio.create");
        System.clearProperty("testomatio.publish");

        if (mockitoCloseable != null) {
            mockitoCloseable.close();
        }
    }

    @Test
    @DisplayName("beforeAll should increment suite counter")
    void beforeAllShouldIncrementSuiteCounter() {
        // When
        listener.beforeAll(mockContext);

        // Then
        verify(mockRunManager).incrementSuiteCounter();
    }

    @Test
    @DisplayName("afterAll should decrement suite counter")
    void afterAllShouldDecrementSuiteCounter() {
        // When
        listener.afterAll(mockContext);

        // Then
        verify(mockRunManager).decrementSuiteCounter();
    }

    @Test
    @DisplayName("beforeEach should not perform any operations")
    void beforeEachShouldNotPerformAnyOperations() {
        // When
        listener.beforeEach(mockContext);

        // Then
        verifyNoInteractions(mockRunManager);
    }

    @Test
    @DisplayName("testSuccessful should report PASSED result")
    void testSuccessfulShouldReportPassedResult() throws NoSuchMethodException {
        // Given
        setupMockContext("testMethod", "Test Method Display Name");

        // When
        listener.testSuccessful(mockContext);

        // Then
        verify(mockRunManager).reportTest(argThat(result ->
                PASSED.equals(result.getStatus()) &&
                        "Test Method Display Name".equals(result.getTitle()) &&
                        result.getMessage() == null
        ));
    }

    @Test
    @DisplayName("testFailed should report FAILED result with exception message")
    void testFailedShouldReportFailedResult() throws NoSuchMethodException {
        // Given
        setupMockContext("testMethod", "Test Method Display Name");
        RuntimeException cause = new RuntimeException("Test assertion failed");

        // When
        listener.testFailed(mockContext, cause);

        // Then
        verify(mockRunManager).reportTest(argThat(result ->
                FAILED.equals(result.getStatus()) &&
                        "Test Method Display Name".equals(result.getTitle()) &&
                        "Test assertion failed".equals(result.getMessage())
        ));
    }

    @Test
    @DisplayName("testAborted should report SKIPPED result with cause message")
    void testAbortedShouldReportSkippedResult() throws NoSuchMethodException {
        // Given
        setupMockContext("testMethod", "Test Method Display Name");
        RuntimeException cause = new RuntimeException("Test was aborted");

        // When
        listener.testAborted(mockContext, cause);

        // Then
        verify(mockRunManager).reportTest(argThat(result ->
                SKIPPED.equals(result.getStatus()) &&
                        "Test Method Display Name".equals(result.getTitle()) &&
                        "Test was aborted".equals(result.getMessage())
        ));
    }

    @Test
    @DisplayName("testDisabled should report SKIPPED result with reason")
    void testDisabledShouldReportSkippedResultWithReason() throws NoSuchMethodException {
        // Given
        setupMockContext("testMethod", "Test Method Display Name");
        String reason = "Test is disabled for maintenance";

        // When
        listener.testDisabled(mockContext, Optional.of(reason));

        // Then
        verify(mockRunManager).reportTest(argThat(result ->
                SKIPPED.equals(result.getStatus()) &&
                        "Test Method Display Name".equals(result.getTitle()) &&
                        reason.equals(result.getMessage())
        ));
    }

    @Test
    @DisplayName("testDisabled should report SKIPPED result with default reason when reason is empty")
    void testDisabledShouldReportSkippedResultWithDefaultReason() throws NoSuchMethodException {
        // Given
        setupMockContext("testMethod", "Test Method Display Name");

        // When
        listener.testDisabled(mockContext, Optional.empty());

        // Then
        verify(mockRunManager).reportTest(argThat(result ->
                SKIPPED.equals(result.getStatus()) &&
                        "Test Method Display Name".equals(result.getTitle()) &&
                        "Test disabled".equals(result.getMessage())
        ));
    }

    @Test
    @DisplayName("should not report results when run manager is inactive")
    void shouldNotReportResultsWhenRunManagerInactive() throws NoSuchMethodException {
        // Given
        setupMockContext("testMethod", "Test Method Display Name");
        doReturn(false).when(mockRunManager).isActive();

        // When
        listener.testSuccessful(mockContext);

        // Then
        verify(mockRunManager, never()).reportTest(any());
        verify(mockRunManager).isActive();
    }

    @Test
    @DisplayName("should handle test with annotations correctly")
    void shouldHandleTestWithAnnotationsCorrectly() throws NoSuchMethodException {
        // Given
        setupMockContextWithAnnotations("annotatedTestMethod", "Custom Display Name");

        // When
        listener.testSuccessful(mockContext);

        // Then
        verify(mockRunManager).reportTest(argThat(result ->
                PASSED.equals(result.getStatus()) &&
                        "Custom Test Title".equals(result.getTitle()) &&
                        "TEST-123".equals(result.getTestId()) &&
                        "TestMethods".equals(result.getSuiteTitle()) &&
                        "TestMethods.java".equals(result.getFile())
        ));
    }

    @Test
    @DisplayName("should throw ReportTestResultException when run manager throws exception")
    void shouldThrowReportTestResultExceptionWhenRunManagerThrows() throws NoSuchMethodException {
        // Given
        setupMockContext("testMethod", "Test Method Display Name");
        doThrow(new RuntimeException("Network error")).when(mockRunManager).reportTest(any());

        // When & Then
        ReportTestResultException exception = assertThrows(
                ReportTestResultException.class,
                () -> listener.testSuccessful(mockContext)
        );

        assertTrue(exception.getMessage().contains("Failed to report test result for: Test Method Display Name"));
    }

    @Test
    @DisplayName("should handle test without test class correctly")
    void shouldHandleTestWithoutTestClassCorrectly() throws NoSuchMethodException {
        // Given
        Method testMethod = TestMethods.class.getMethod("testMethod");
        doReturn(Optional.of(testMethod)).when(mockContext).getTestMethod();
        doReturn(Optional.empty()).when(mockContext).getTestClass();
        doReturn("Unknown Class Test").when(mockContext).getDisplayName();

        // When
        listener.testSuccessful(mockContext);

        // Then
        verify(mockRunManager).reportTest(argThat(result ->
                PASSED.equals(result.getStatus()) &&
                        "Unknown Class Test".equals(result.getTitle()) &&
                        "Unknown".equals(result.getSuiteTitle()) &&
                        "Unknown.java".equals(result.getFile())
        ));
    }

    // Helper methods
    private void setupMockContext(String methodName, String displayName) throws NoSuchMethodException {
        Method testMethod = TestMethods.class.getMethod(methodName);
        doReturn(Optional.of(testMethod)).when(mockContext).getTestMethod();
        doReturn(Optional.of(TestMethods.class)).when(mockContext).getTestClass();
        doReturn(displayName).when(mockContext).getDisplayName();
    }

    private void setupMockContextWithAnnotations(String methodName, String displayName) throws NoSuchMethodException {
        Method testMethod = TestMethods.class.getMethod(methodName);
        doReturn(Optional.of(testMethod)).when(mockContext).getTestMethod();
        doReturn(Optional.of(TestMethods.class)).when(mockContext).getTestClass();
        doReturn(displayName).when(mockContext).getDisplayName();
    }

    // Test helper classes
    private static class TestMethods {
        public void testMethod() {}

        @Title("Custom Test Title")
        @TestId("TEST-123")
        public void annotatedTestMethod() {}
    }

    /**
     * Testable version of JunitListener that accepts GlobalRunManager in constructor
     * This allows us to inject mock without reflection or static mocking
     */
    private static class TestableJunitListener extends JunitListener {
        private final GlobalRunManager testRunManager;
        private final io.testomat.junit.constructor.JUnitTestResultConstructor resultConstructor = new io.testomat.junit.constructor.JUnitTestResultConstructor();
        private final io.testomat.junit.extractor.JunitMetaDataExtractor metaDataExtractor = new io.testomat.junit.extractor.JunitMetaDataExtractor();

        public TestableJunitListener(GlobalRunManager runManager) {
            this.testRunManager = runManager;
        }

        @Override
        public void beforeAll(ExtensionContext context) {
            testRunManager.incrementSuiteCounter();
        }

        @Override
        public void afterAll(ExtensionContext context) {
            testRunManager.decrementSuiteCounter();
        }

        @Override
        public void testSuccessful(ExtensionContext context) {
            handleTestResult(context, PASSED, null);
        }

        @Override
        public void testFailed(ExtensionContext context, Throwable cause) {
            handleTestResult(context, FAILED, cause.getMessage());
        }

        @Override
        public void testAborted(ExtensionContext context, Throwable cause) {
            handleTestResult(context, SKIPPED, cause.getMessage());
        }

        @Override
        public void testDisabled(ExtensionContext context, Optional<String> reason) {
            String reasonText = reason.orElse("Test disabled");
            handleTestResult(context, SKIPPED, reasonText);
        }

        // Copy the logic from original JunitListener but use injected runManager
        private void handleTestResult(ExtensionContext context, String status, String message) {
            if (!testRunManager.isActive()) {
                return;
            }

            try {
                io.testomat.core.model.TestMetadata metadata = metaDataExtractor.extractTestMetadata(context);
                reportTestResult(metadata, status, message, context);
            } catch (Exception e) {
                String testName = "Unknown Test";
                try {
                    testName = context.getDisplayName();
                } catch (Exception ignored) {}
                throw new ReportTestResultException("Failed to report test result for: " + testName, e);
            }
        }

        private void reportTestResult(io.testomat.core.model.TestMetadata metadata,
                                      String status,
                                      String message,
                                      Object frameworkSpecificData) {
            io.testomat.core.model.TestResult result = createTestResult(metadata, status, message, frameworkSpecificData);
            testRunManager.reportTest(result);
        }

        private io.testomat.core.model.TestResult createTestResult(io.testomat.core.model.TestMetadata metadata,
                                                                   String status,
                                                                   String message,
                                                                   Object frameworkSpecificData) {
            io.testomat.junit.model.TestResultWrapper.Builder builder = io.testomat.junit.model.TestResultWrapper.builder()
                    .withTestMetadata(metadata)
                    .withStatus(status);

            if (message != null) {
                builder.withMessage(message);
            }

            if (frameworkSpecificData instanceof ExtensionContext) {
                builder.withJunitExtensionContext((ExtensionContext) frameworkSpecificData);
            }

            return resultConstructor.constructTestRunResult(builder.build());
        }
    }
}