package io.testomat.core.exception;

public class ArtifactManagementException extends RuntimeException {
    public ArtifactManagementException(String message) {
        super(message);
    }

    public ArtifactManagementException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
