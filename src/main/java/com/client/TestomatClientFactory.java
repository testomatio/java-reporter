package com.client;

import com.exception.ApiKeyNotFoundException;
import com.property_config.impl.PropertyProviderFactoryImpl;
import com.property_config.interf.PropertyProvider;
import com.property_config.interf.PropertyProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.constants.CommonConstants.API_KEY_PROPERTY_NAME;

public class TestomatClientFactory implements ClientFactory {
    private static final PropertyProviderFactory propertyProviderFactory =
            PropertyProviderFactoryImpl.getPropertyProviderFactory();
    private static final Logger LOGGER = LoggerFactory.getLogger(TestomatClientFactory.class);
    private static ClientFactory instance;

    private TestomatClientFactory() {
    }

    public static ClientFactory getClientFactory() {
        if (instance == null) {
            instance = new TestomatClientFactory();
        }
        return instance;
    }

    @Override
    public ApiInterface createClient() {
        PropertyProvider propertyProvider = propertyProviderFactory.getPropertyProvider();
        String apiKey = propertyProvider.getProperty(API_KEY_PROPERTY_NAME);
        if (apiKey == null) {
            LOGGER.warn("TESTOMATIO environment variable not set. Skipping reporting.");
            throw new ApiKeyNotFoundException("TESTOMATIO environment variable not set.");
        }

        return new TestomatApiClient(apiKey);
    }

}
