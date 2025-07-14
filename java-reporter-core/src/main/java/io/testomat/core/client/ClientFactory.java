package io.testomat.core.client;

/**
 * Factory for creating API client instances.
 */
public interface ClientFactory {

    /**
     * Creates configured API client instance.
     *
     * @return API client implementation
     */
    ApiInterface createClient();
}
