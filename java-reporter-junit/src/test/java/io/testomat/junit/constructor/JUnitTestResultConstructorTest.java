package io.testomat.junit.constructor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.testomat.core.model.TestMetadata;
import io.testomat.core.model.TestResult;
import io.testomat.junit.extractor.JunitMetaDataExtractor;
import java.lang.reflect.Method;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opentest4j.TestAbortedException;

class JUnitTestResultConstructorTest {

    @Mock
    private JunitMetaDataExtractor mockExtractor;
    @Mock
    private ExtensionContext mockContext;
    @Mock
    private TestMetadata mockMetadata;

    private JUnitTestResultConstructor constructor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        constructor = new JUnitTestResultConstructor(mockExtractor);
    }

    @Test
    @DisplayName("Constructor should reject null extractor")
    void constructorShouldRejectNullExtractor() {
        assertThatThrownBy(() -> new JUnitTestResultConstructor(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("metaDataExtractor cannot be null");
    }

    @Test
    @DisplayName("Should reject null metadata")
    void shouldRejectNullMetadata() {
        assertThatThrownBy(() -> constructor.constructTestRunResult(null, "msg", "passed", mockContext))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("metadata cannot be null");
    }

    @Test
    @DisplayName("Should reject null status")
    void shouldRejectNullStatus() {
        assertThatThrownBy(() -> constructor.constructTestRunResult(mockMetadata, "msg", null, mockContext))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("status cannot be null");
    }

    @Test
    @DisplayName("Should reject null context")
    void shouldRejectNullContext() {
        assertThatThrownBy(() -> constructor.constructTestRunResult(mockMetadata, "msg", "passed", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("context cannot be null");
    }

    @Test
    @DisplayName("Should build basic test result from metadata")
    void shouldBuildBasicTestResultFromMetadata() {
        // Given
        when(mockMetadata.getSuiteTitle()).thenReturn("MySuite");
        when(mockMetadata.getTestId()).thenReturn("test-1");
        when(mockMetadata.getTitle()).thenReturn("MyTest");
        when(mockMetadata.getFile()).thenReturn("MyTest.java");
        when(mockExtractor.isParameterizedTest(mockContext)).thenReturn(false);
        when(mockContext.getExecutionException()).thenReturn(Optional.empty());

        // When
        TestResult result = constructor.constructTestRunResult(mockMetadata, "custom message", "passed", mockContext);

        // Then
        assertThat(result.getSuiteTitle()).isEqualTo("MySuite");
        assertThat(result.getTestId()).isEqualTo("test-1");
        assertThat(result.getTitle()).isEqualTo("MyTest");
        assertThat(result.getFile()).isEqualTo("MyTest.java");
        assertThat(result.getMessage()).isEqualTo("custom message");
        assertThat(result.getStatus()).isEqualTo("passed");
        assertThat(result.getStack()).isNull();
        assertThat(result.getExample()).isNull();
        assertThat(result.getRid()).isNull();
    }

    @Test
    @DisplayName("Should extract stack trace when message provided but exception exists")
    void shouldExtractStackTraceWhenMessageProvidedButExceptionExists() {
        // Given
        RuntimeException testException = new RuntimeException("Test error");
        when(mockExtractor.isParameterizedTest(mockContext)).thenReturn(false);
        when(mockContext.getExecutionException()).thenReturn(Optional.of(testException));
        setupBasicMetadata();

        // When
        TestResult result = constructor.constructTestRunResult(mockMetadata, "custom message", "failed", mockContext);

        // Then
        assertThat(result.getMessage()).isEqualTo("custom message");
        assertThat(result.getStack()).contains("RuntimeException").contains("Test error");
    }

    @Test
    @DisplayName("Should extract message and stack when no message provided")
    void shouldExtractMessageAndStackWhenNoMessageProvided() {
        // Given
        RuntimeException testException = new RuntimeException("Extracted error");
        when(mockExtractor.isParameterizedTest(mockContext)).thenReturn(false);
        when(mockContext.getExecutionException()).thenReturn(Optional.of(testException));
        setupBasicMetadata();

        // When
        TestResult result = constructor.constructTestRunResult(mockMetadata, null, "failed", mockContext);

        // Then
        assertThat(result.getMessage()).isEqualTo("Extracted error");
        assertThat(result.getStack()).contains("RuntimeException").contains("Extracted error");
    }

    @Test
    @DisplayName("Should handle parameterized test with parameters")
    void shouldHandleParameterizedTestWithParameters() {
        // Given
        Object[] params = {"param1", 42, true};
        when(mockExtractor.isParameterizedTest(mockContext)).thenReturn(true);
        when(mockExtractor.extractTestParameters(mockContext)).thenReturn(params);
        when(mockContext.getUniqueId()).thenReturn("unique-123");
        when(mockContext.getExecutionException()).thenReturn(Optional.empty());
        setupBasicMetadata();

        // When
        TestResult result = constructor.constructTestRunResult(mockMetadata, "test", "passed", mockContext);

        // Then
        assertThat(result.getExample()).isEqualTo(params);
        assertThat(result.getRid()).isEqualTo("unique-123");
    }

    @Test
    @DisplayName("Should filter out TestAbortedException")
    void shouldFilterOutTestAbortedException() {
        // Given
        TestAbortedException abortedException = new TestAbortedException("Test skipped");
        when(mockExtractor.isParameterizedTest(mockContext)).thenReturn(false);
        when(mockContext.getExecutionException()).thenReturn(Optional.of(abortedException));
        setupBasicMetadata();

        // When
        TestResult result = constructor.constructTestRunResult(mockMetadata, null, "skipped", mockContext);

        // Then
        assertThat(result.getMessage()).isNull();
        assertThat(result.getStack()).isNull();
    }

    @Test
    @DisplayName("Should allow other exceptions through filter")
    void shouldAllowOtherExceptionsThroughFilter() {
        // Given
        IllegalArgumentException allowedException = new IllegalArgumentException("Valid error");
        when(mockExtractor.isParameterizedTest(mockContext)).thenReturn(false);
        when(mockContext.getExecutionException()).thenReturn(Optional.of(allowedException));
        setupBasicMetadata();

        // When
        TestResult result = constructor.constructTestRunResult(mockMetadata, null, "failed", mockContext);

        // Then
        assertThat(result.getMessage()).isEqualTo("Valid error");
        assertThat(result.getStack()).contains("IllegalArgumentException");
    }

    @Test
    @DisplayName("Should sanitize Testomat API keys in exception messages")
    void shouldSanitizeTestomatApiKeysInExceptionMessages() {
        // Given
        RuntimeException exception = new RuntimeException("Error with key tstmt_secret123");
        when(mockExtractor.isParameterizedTest(mockContext)).thenReturn(false);
        when(mockContext.getExecutionException()).thenReturn(Optional.of(exception));
        setupBasicMetadata();

        // When
        TestResult result = constructor.constructTestRunResult(mockMetadata, null, "failed", mockContext);

        // Then
        assertThat(result.getMessage()).isEqualTo("Error with key tstmt_***");
    }

    @Test
    @DisplayName("Should sanitize Windows paths in stack traces")
    void shouldSanitizeWindowsPathsInStackTraces() {
        // Given
        RuntimeException exception = new RuntimeException("Error in C:\\Users\\test\\MyClass.java");
        when(mockExtractor.isParameterizedTest(mockContext)).thenReturn(false);
        when(mockContext.getExecutionException()).thenReturn(Optional.of(exception));
        setupBasicMetadata();

        // When
        TestResult result = constructor.constructTestRunResult(mockMetadata, null, "failed", mockContext);

        // Then
        assertThat(result.getMessage()).isEqualTo("Error in MyClass.java");
    }

    @Test
    @DisplayName("Should sanitize Unix paths in stack traces")
    void shouldSanitizeUnixPathsInStackTraces() {
        // Given
        RuntimeException exception = new RuntimeException("Error in /home/user/project/MyClass.java");
        when(mockExtractor.isParameterizedTest(mockContext)).thenReturn(false);
        when(mockContext.getExecutionException()).thenReturn(Optional.of(exception));
        setupBasicMetadata();

        // When
        TestResult result = constructor.constructTestRunResult(mockMetadata, null, "failed", mockContext);

        // Then
        assertThat(result.getMessage()).isEqualTo("Error in MyClass.java");
    }

    @Test
    @DisplayName("Should preserve original text when no sensitive content")
    void shouldPreserveOriginalTextWhenNoSensitiveContent() {
        // Given
        RuntimeException exception = new RuntimeException("Normal error message");
        when(mockExtractor.isParameterizedTest(mockContext)).thenReturn(false);
        when(mockContext.getExecutionException()).thenReturn(Optional.of(exception));
        setupBasicMetadata();

        // When
        TestResult result = constructor.constructTestRunResult(mockMetadata, null, "failed", mockContext);

        // Then
        assertThat(result.getMessage()).isEqualTo("Normal error message");
    }

    @Test
    @DisplayName("Should handle multiple sanitization patterns in same text")
    void shouldHandleMultipleSanitizationPatternsInSameText() {
        // Given
        String complexMessage = "Failed with tstmt_key123 in C:\\Users\\test\\App.java";
        RuntimeException exception = new RuntimeException(complexMessage);
        when(mockExtractor.isParameterizedTest(mockContext)).thenReturn(false);
        when(mockContext.getExecutionException()).thenReturn(Optional.of(exception));
        setupBasicMetadata();

        // When
        TestResult result = constructor.constructTestRunResult(mockMetadata, null, "failed", mockContext);

        // Then
        assertThat(result.getMessage()).isEqualTo("Failed with tstmt_*** in App.java");
    }

    @Test
    @DisplayName("Should handle null exception message gracefully")
    void shouldHandleNullExceptionMessageGracefully() {
        // Given
        RuntimeException exception = new RuntimeException((String) null);
        when(mockExtractor.isParameterizedTest(mockContext)).thenReturn(false);
        when(mockContext.getExecutionException()).thenReturn(Optional.of(exception));
        setupBasicMetadata();

        // When
        TestResult result = constructor.constructTestRunResult(mockMetadata, null, "failed", mockContext);

        // Then
        assertThat(result.getMessage()).isNull();
        assertThat(result.getStack()).isNotNull(); // Stack trace should still be present
    }

    @Test
    @DisplayName("Should verify extractor is called for parameterized test detection")
    void shouldVerifyExtractorIsCalledForParameterizedTestDetection() {
        // Given
        when(mockExtractor.isParameterizedTest(mockContext)).thenReturn(false);
        when(mockContext.getExecutionException()).thenReturn(Optional.empty());
        setupBasicMetadata();

        // When
        constructor.constructTestRunResult(mockMetadata, "test", "passed", mockContext);

        // Then
        verify(mockExtractor).isParameterizedTest(mockContext);
    }

    @Test
    @DisplayName("Should not extract parameters when test is not parameterized")
    void shouldNotExtractParametersWhenTestIsNotParameterized() {
        // Given
        when(mockExtractor.isParameterizedTest(mockContext)).thenReturn(false);
        when(mockContext.getExecutionException()).thenReturn(Optional.empty());
        setupBasicMetadata();

        // When
        constructor.constructTestRunResult(mockMetadata, "test", "passed", mockContext);

        // Then
        verify(mockExtractor, never()).extractTestParameters(mockContext);
    }

    @Test
    @DisplayName("Should extract parameters only when test is parameterized")
    void shouldExtractParametersOnlyWhenTestIsParameterized() {
        // Given
        when(mockExtractor.isParameterizedTest(mockContext)).thenReturn(true);
        when(mockExtractor.extractTestParameters(mockContext)).thenReturn(null);
        when(mockContext.getDisplayName()).thenReturn("testMethod");
        when(mockContext.getExecutionException()).thenReturn(Optional.empty());
        setupBasicMetadata();

        // When
        constructor.constructTestRunResult(mockMetadata, "test", "passed", mockContext);

        // Then
        verify(mockExtractor).extractTestParameters(mockContext);
    }

    private void setupBasicMetadata() {
        when(mockMetadata.getSuiteTitle()).thenReturn("TestSuite");
        when(mockMetadata.getTestId()).thenReturn("test-1");
        when(mockMetadata.getTitle()).thenReturn("TestMethod");
        when(mockMetadata.getFile()).thenReturn("TestClass.java");
    }
}