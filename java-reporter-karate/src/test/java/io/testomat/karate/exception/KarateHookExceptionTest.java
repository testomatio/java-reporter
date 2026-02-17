package io.testomat.karate.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KarateHookExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        String message = "Something went wrong";

        KarateHookException exception = new KarateHookException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void shouldBeRuntimeException() {
        KarateHookException exception =
            new KarateHookException("error");

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldBeThrown() {
        String message = "exception";

        KarateHookException thrown = assertThrows(
            KarateHookException.class,
            () -> { throw new KarateHookException(message); }
        );

        assertThat(thrown.getMessage()).isEqualTo(message);
    }
}
