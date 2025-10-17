package io.testomat.cucumber.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CucumberListenerExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        // Given
        String errorMessage = "Failed to process cucumber event";

        // When
        CucumberListenerException exception = new CucumberListenerException(errorMessage);

        // Then
        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void shouldCreateExceptionWithNullMessage() {
        // When
        CucumberListenerException exception = new CucumberListenerException(null);

        // Then
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    void shouldBeThrowable() {
        // Given
        String errorMessage = "Test error";

        // When & Then
        assertThrows(CucumberListenerException.class, () -> {
            throw new CucumberListenerException(errorMessage);
        });
    }
}
