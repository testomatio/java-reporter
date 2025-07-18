package io.testomat.junit.methodloader;

import static io.testomat.core.constants.PropertyNameConstants.API_KEY_PROPERTY_NAME;

import io.testomat.core.client.http.CustomHttpClient;
import io.testomat.core.client.http.NativeHttpClient;
import io.testomat.core.propertyconfig.impl.PropertyProviderFactoryImpl;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import java.util.List;

public class ExportSender {
    private static final String LOAD_URL = "https://app.testomat.io/api/load?api_key=";
    private final PropertyProvider provider =
            PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();
    private final String apiKey = provider.getProperty(API_KEY_PROPERTY_NAME);

    public void sendLoaderTestCases(List<LoaderTestCase> loaderTestCases) {
        RequestBodyBuilder requestBodyBuilder = new RequestBodyBuilder();
        CustomHttpClient client = new NativeHttpClient();
        String requestBody = requestBodyBuilder.buildRequestBody(loaderTestCases);
        String url = LOAD_URL + apiKey;

        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                if (attempt > 1) {
                    Thread.sleep(1500);
                }
                client.post(url, requestBody, null);
                return;
            } catch (Exception e) {
                if (!e.getMessage().contains("422") || attempt >= 2) {
                    break;
                }
            }
        }
    }
}
