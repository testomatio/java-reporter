package com.testomatio.reporter.client;

import static com.testomatio.reporter.constants.PropertyNameConstants.API_KEY_PROPERTY_NAME;
import static com.testomatio.reporter.logger.LoggerUtils.getLogger;

import com.testomatio.reporter.client.http.NativeHttpClient;
import com.testomatio.reporter.client.request.DefaultRequestBodyBuilder;
import com.testomatio.reporter.exception.ApiKeyNotFoundException;
import com.testomatio.reporter.propertyconfig.impl.PropertyProviderFactoryImpl;
import com.testomatio.reporter.propertyconfig.interf.PropertyProvider;
import java.util.logging.Logger;

/**
 * Singleton factory for creating Testomat.io API client instances.
 * Loads API key from properties and creates configured client.
 */
public class TestomatClientFactory implements ClientFactory {
    private static final PropertyProvider propertyProvider =
            PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();
    private static final Logger LOGGER = getLogger(TestomatClientFactory.class);
    private static ClientFactory instance;

    private TestomatClientFactory() {
    }

    /**
     * Returns singleton factory instance.
     *
     * @return ClientFactory instance
     */
    public static ClientFactory getClientFactory() {
        if (instance == null) {
            instance = new TestomatClientFactory();
        }
        return instance;
    }

    @Override
    public ApiInterface createClient() {
        String apiKey = propertyProvider.getProperty(API_KEY_PROPERTY_NAME);
        if (apiKey == null) {
            LOGGER.severe("Api key environment variable not set.");
            throw new ApiKeyNotFoundException(
                    "Api key should be set in properties file or in JVM params.");
        }
        return new DefaultApiClient(apiKey,
                new NativeHttpClient(),
                new DefaultRequestBodyBuilder());
    }
}
