package io.testomat.junit.exception;

/**
 * Exception thrown when parameter extraction fails during strategy execution.
 */
public class ParameterExtractionException extends Exception {

    public ParameterExtractionException(String message) {
        super(message);
    }

    public ParameterExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}