package io.testomat.junit.exception;

/**
 * Exception thrown when test metadata extraction fails.
 * This exception is used to indicate problems during the extraction
 * of test information from JUnit test methods or classes.
 */
public class ExtractionException extends MethodExporterException {

    public ExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
