package io.testomat.core.client.http;

import java.io.IOException;

/**
 * HTTP client abstraction for Testomat.io API communication.
 * Provides type-safe request/response handling with JSON serialization support.
 */
public interface CustomHttpClient {

    /**
     * Executes HTTP POST request with JSON payload.
     *
     * @param url endpoint URL for the request
     * @param requestBody JSON-formatted request payload
     * @param responseType expected response class for deserialization,
     * or null if no response needed
     * @param <T> response object type
     * @return deserialized response object, or null if responseType is null
     * @throws IOException if network request fails or JSON processing fails
     */
    <T> T post(String url, String requestBody, Class<T> responseType) throws IOException;

    /**
     * Executes HTTP PUT request with JSON payload.
     *
     * @param url endpoint URL for the request
     * @param requestBody JSON-formatted request payload
     * @param responseType expected response class for deserialization,
     * or null if no response needed
     * @param <T> response object type
     * @return deserialized response object, or null if responseType is null
     * @throws IOException if network request fails or JSON processing fails
     */
    <T> T put(String url, String requestBody, Class<T> responseType) throws IOException;
}
