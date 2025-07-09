package io.testomat.core.exception;

import java.net.URISyntaxException;

public class RequestUriBuildingException extends RuntimeException {
    public RequestUriBuildingException(String message, URISyntaxException e) {
        super(message);
    }
}
