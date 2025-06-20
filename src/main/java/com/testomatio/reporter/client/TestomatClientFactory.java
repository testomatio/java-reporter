package com.testomatio.reporter.client;

import com.testomatio.reporter.exception.ApiKeyNotFoundException;
import com.testomatio.reporter.property_config.impl.PropertyProviderFactoryImpl;
import com.testomatio.reporter.property_config.interf.PropertyProvider;
import java.util.logging.Logger;

import static com.testomatio.reporter.constants.PropertyNameConstants.API_KEY_PROPERTY_NAME;
import static com.testomatio.reporter.logger.LoggerUtils.getLogger;

/**
 * Singleton factory for creating Testomat.io API client instances.
 */
public class TestomatClientFactory implements ClientFactory {
    private static final PropertyProvider propertyProvider =
            PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();
    private static final Logger LOGGER = getLogger(TestomatClientFactory.class);
    private static ClientFactory instance;

    private TestomatClientFactory() {
    }

    /**
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
     *
     * @throws ApiKeyNotFoundException if the API key is not configured or is null/empty
     * @see TestomatApiClient
     * @see ApiInterface
     */
    @Override
    public ApiInterface createClient() {
        String apiKey = propertyProvider.getProperty(API_KEY_PROPERTY_NAME);
        if (apiKey == null) {
            LOGGER.severe("Api key environment variable not set.");
            throw new ApiKeyNotFoundException("Api key should be set in properties file or in JVM params.");
        }

        return new TestomatApiClient(apiKey);
    }
}