package com.testomatio.reporter.core.constructor;

import com.testomatio.reporter.model.TestCaseResult;
import com.testomatio.reporter.model.TestMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JUnitTestCaseResultConstructorTest {

    private JUnitTestCaseResultConstructor constructor;

    @Mock
    private TestCaseResultWrapper holder;

    @Mock
    private TestMetadata testMetadata;

    @Mock
    private ExtensionContext extensionContext;

    @BeforeEach
    void setUp() {
        constructor = new JUnitTestCaseResultConstructor();
    }

    @Test
    void shouldReturnJUnitAsFrameworkName() {
        assertEquals("JUnit", constructor.getFrameworkName());
    }

    @Test
    void hasCustomMessage_shouldReturnTrue_whenMessageExists() {
        when(holder.getMessage()).thenReturn("Custom test message");

        assertTrue(constructor.hasCustomMessage(holder));
    }

    @Test
    void hasCustomMessage_shouldReturnFalse_whenMessageIsNull() {
        when(holder.getMessage()).thenReturn(null);

        assertFalse(constructor.hasCustomMessage(holder));
    }

    @Test
    void getCustomMessage_shouldReturnMessage_whenExists() {
        String expectedMessage = "Custom test message";
        when(holder.getMessage()).thenReturn(expectedMessage);

        assertEquals(expectedMessage, constructor.getCustomMessage(holder));
    }

    @Test
    void getCustomMessage_shouldReturnNull_whenNoMessage() {
        when(holder.getMessage()).thenReturn(null);

        assertNull(constructor.getCustomMessage(holder));
    }

    @Test
    void createWithCustomMessage_shouldCreateResultWithNullStack_whenNoException() {
        String customMessage = "Custom message";

        setupBasicTestMetadata();
        when(holder.getMessage()).thenReturn(customMessage);
        when(holder.getStatus()).thenReturn("PASSED");
        when(holder.getJUnitExtensionContext()).thenReturn(null);

        TestCaseResult result = constructor.createWithCustomMessage(holder);

        assertNotNull(result);
        assertEquals(customMessage, result.getMessage());
        assertNull(result.getStack());
    }

    @Test
    void createWithExceptionDetails_shouldCreateResultWithException() {
        RuntimeException testException = new RuntimeException("Test exception");

        setupBasicTestMetadata();
        when(holder.getStatus()).thenReturn("FAILED");
        when(holder.getJUnitExtensionContext()).thenReturn(extensionContext);
        when(extensionContext.getExecutionException()).thenReturn(Optional.of(testException));

        TestCaseResult result = constructor.createWithExceptionDetails(holder);

        assertNotNull(result);
        assertEquals("Test Title", result.getTitle());
        assertEquals("Test exception", result.getMessage());
        assertNotNull(result.getStack());
        assertTrue(result.getStack().contains("Test exception"));
    }

    @Test
    void createWithExceptionDetails_shouldCreateResultWithEmptyDetails_whenNoException() {
        setupBasicTestMetadata();
        when(holder.getStatus()).thenReturn("PASSED");
        when(holder.getJUnitExtensionContext()).thenReturn(extensionContext);
        when(extensionContext.getExecutionException()).thenReturn(Optional.empty());

        TestCaseResult result = constructor.createWithExceptionDetails(holder);

        assertNotNull(result);
        assertNull(result.getMessage());
        assertNull(result.getStack());
    }

    @Test
    void createWithExceptionDetails_shouldCreateResultWithEmptyDetails_whenNoExtensionContext() {
        setupBasicTestMetadata();
        when(holder.getStatus()).thenReturn("PASSED");
        when(holder.getJUnitExtensionContext()).thenReturn(null);

        TestCaseResult result = constructor.createWithExceptionDetails(holder);

        assertNotNull(result);
        assertNull(result.getMessage());
        assertNull(result.getStack());
    }

    @Test
    void constructTestRunResult_shouldUseCustomMessage_whenMessageExists() {
        String customMessage = "Custom test message";

        setupBasicTestMetadata();
        when(holder.getMessage()).thenReturn(customMessage);
        when(holder.getStatus()).thenReturn("PASSED");

        TestCaseResult result = constructor.constructTestRunResult(holder);

        assertNotNull(result);
        assertEquals(customMessage, result.getMessage());
    }

    @Test
    void constructTestRunResult_shouldUseExceptionDetails_whenNoCustomMessage() {
        RuntimeException testException = new RuntimeException("Exception message");

        setupBasicTestMetadata();
        when(holder.getMessage()).thenReturn(null);
        when(holder.getStatus()).thenReturn("FAILED");
        when(holder.getJUnitExtensionContext()).thenReturn(extensionContext);
        when(extensionContext.getExecutionException()).thenReturn(Optional.of(testException));

        TestCaseResult result = constructor.constructTestRunResult(holder);

        assertNotNull(result);
        assertEquals("Exception message", result.getMessage());
        assertNotNull(result.getStack());
    }

    @Test
    void constructTestRunResult_shouldThrowException_whenTestMetadataIsNull() {
        when(holder.getTestMetadata()).thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> constructor.constructTestRunResult(holder)
        );

        assertEquals("TestMetadata cannot be null", exception.getMessage());
    }

    /**
     * Допоміжний метод для налаштування базових даних TestMetadata
     * Використовується тільки тестами, які потребують повних даних
     */
    private void setupBasicTestMetadata() {
        when(holder.getTestMetadata()).thenReturn(testMetadata);
        when(testMetadata.getTitle()).thenReturn("Test Title");
        when(testMetadata.getTestId()).thenReturn("test-id-123");
        when(testMetadata.getSuiteTitle()).thenReturn("Test Suite");
        when(testMetadata.getFile()).thenReturn("TestFile.java");
    }
}