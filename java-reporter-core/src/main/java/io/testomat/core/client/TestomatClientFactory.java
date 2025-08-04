package io.testomat.core.client;

import static io.testomat.core.constants.PropertyNameConstants.API_KEY_PROPERTY_NAME;

import io.testomat.core.client.http.NativeHttpClient;
import io.testomat.core.client.request.NativeRequestBodyBuilder;
import io.testomat.core.exception.ApiKeyNotFoundException;
import io.testomat.core.propertyconfig.impl.PropertyProviderFactoryImpl;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton factory for creating Testomat.io API client instances.
 * Loads API key from properties and creates configured client.
 */
public class TestomatClientFactory implements ClientFactory {
    private static final PropertyProvider propertyProvider =
            PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();
    private static final Logger log = LoggerFactory.getLogger(TestomatClientFactory.class);
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
            log.error("Api key environment variable not set.");
            throw new ApiKeyNotFoundException(
                    "Api key should be set in properties file or in JVM params.");
        }
        return new NativeApiClient(apiKey,
                new NativeHttpClient(),
                new NativeRequestBodyBuilder());
    }
}
