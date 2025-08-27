package io.testomat.core.client;

/**
 * Abstract factory for creating {@link ApiInterface} implementations.
 * Provides a pluggable mechanism for different API client configurations.
 */
public interface ClientFactory {

    /**
     * Creates a fully configured API client instance.
     * Implementation should handle all necessary initialization including
     * authentication, HTTP client setup, and request/response handling.
     *
     * @return ready-to-use API client implementation
     */
    ApiInterface createClient();
}
