package io.testomat.junit.exception;

/**
 * Exception thrown when parameter extraction fails during strategy execution.
 */
public class ParameterExtractionException extends RuntimeException {

    public ParameterExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
