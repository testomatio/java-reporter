package io.testomat.methodloader.junit;

import static io.testomat.core.constants.PropertyNameConstants.API_KEY_PROPERTY_NAME;

import io.testomat.core.client.http.CustomHttpClient;
import io.testomat.core.client.http.NativeHttpClient;
import io.testomat.core.propertyconfig.impl.PropertyProviderFactoryImpl;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import java.io.IOException;

public class MethodSender {
    private final PathFinder pathFinder = new PathFinder();
    private final CustomHttpClient client = new NativeHttpClient();
    private final PropertyProvider provider =
            PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();

    private String apiKey = provider.getProperty(API_KEY_PROPERTY_NAME);

    private String url = "https://app/testomat.io/api/load?api_key=" + apiKey;

    void sendMethodPayload(String requestBody) {
        try {
            client.post(url,requestBody, null);
        } catch (IOException e) {
            throw new RuntimeException("Failed to send method payload", e);
        }
    }
}
