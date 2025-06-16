package com.testomatio.reporter.client;

import com.testomatio.reporter.exception.ApiKeyNotFoundException;
import com.testomatio.reporter.property_config.impl.PropertyProviderFactoryImpl;
import com.testomatio.reporter.property_config.interf.PropertyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.testomatio.reporter.constants.PropertyNameConstants.API_KEY_PROPERTY_NAME;

/**
 * Singleton factory for creating Testomat.io API client instances.
 * This factory handles the creation and configuration of API clients with proper
 * API key validation and error handling.
 *
 * The factory follows the Singleton pattern to ensure consistent client creation
 * throughout the application lifecycle. It automatically loads the API key from
 * the configured property provider and validates its presence before creating clients.
 *
 * Key features:
 * - Singleton pattern implementation for consistent factory access
 * - Automatic API key loading and validation
 * - Proper error handling for missing configuration
 * - Logging for configuration issues and client creation
 *
 * Usage:
 * <pre>
 * ClientFactory factory = TestomatClientFactory.getClientFactory();
 * ApiInterface client = factory.createClient();
 * </pre>
 */
public class TestomatClientFactory implements ClientFactory {
    private static final PropertyProvider propertyProvider =
            PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();
    private static final Logger LOGGER = LoggerFactory.getLogger(TestomatClientFactory.class);
    private static ClientFactory instance;

    /**
     * Private constructor to prevent external instantiation.
     * This constructor is part of the Singleton pattern implementation,
     * ensuring that only one instance of the factory can exist.
     */
    private TestomatClientFactory() {
    }

    /**
     * Returns the singleton instance of the TestomatClientFactory.
     * Creates a new instance if one doesn't exist, otherwise returns the existing instance.
     * This method is thread-safe for the initial creation, though subsequent calls
     * may not be perfectly synchronized (acceptable for this use case).
     *
     * @return the singleton ClientFactory instance
     */
    public static ClientFactory getClientFactory() {
        if (instance == null) {
            instance = new TestomatClientFactory();
        }
        return instance;
    }

    /**
     * Creates and returns a new Testomat.io API client instance.
     * Loads the API key from the configured property provider and validates its presence.
     * If the API key is missing or null, logs a warning and throws an exception.
     *
     * The created client is fully configured and ready for use with the Testomat.io API.
     * Each call to this method creates a new client instance with the same configuration.
     *
     * @return a configured ApiInterface implementation ready for API communication
     * @throws ApiKeyNotFoundException if the API key is not configured or is null/empty
     * @see TestomatApiClient
     * @see ApiInterface
     */
    @Override
    public ApiInterface createClient() {
        String apiKey = propertyProvider.getProperty(API_KEY_PROPERTY_NAME);
        if (apiKey == null) {
            LOGGER.warn("TESTOMATIO environment variable not set. Skipping reporting.");
            throw new ApiKeyNotFoundException("TESTOMATIO environment variable not set.");
        }

        return new TestomatApiClient(apiKey);
    }
}