package io.testomat.cucumber.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatusNormalizerExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        // Given
        String errorMessage = "Failed to normalize test status";

        // When
        StatusNormalizerException exception = new StatusNormalizerException(errorMessage);

        // Then
        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void shouldCreateExceptionWithNullMessage() {
        // When
        StatusNormalizerException exception = new StatusNormalizerException(null);

        // Then
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    void shouldBeThrowable() {
        // Given
        String errorMessage = "Status normalization error";

        // When & Then
        assertThrows(StatusNormalizerException.class, () -> {
            throw new StatusNormalizerException(errorMessage);
        });
    }
}
