package io.testomat.junit.methodexporter;

import static io.testomat.core.constants.PropertyNameConstants.API_KEY_PROPERTY_NAME;

import io.testomat.core.client.http.CustomHttpClient;
import io.testomat.core.client.http.NativeHttpClient;
import io.testomat.core.propertyconfig.impl.PropertyProviderFactoryImpl;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import java.util.List;

public class ExportSender {
    private static final String LOAD_URL = "https://app.testomat.io/api/load?api_key=";
    private static final int RETRY_TIMEOUT_MILLISECONDS = 1500;
    private static final int RETRY_MAX_ATTEMPTS = 2;
    private final PropertyProvider provider =
            PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();
    private final String apiKey = provider.getProperty(API_KEY_PROPERTY_NAME);

    public void sendTestCases(List<ExporterTestCase> exporterTestCases) {
        if (exporterTestCases.isEmpty()) {
            return;
        }

        ExporterRequestBodyBuilder exporterRequestBodyBuilder = new ExporterRequestBodyBuilder();
        CustomHttpClient client = new NativeHttpClient();
        
        String requestBody = exporterRequestBodyBuilder.buildRequestBody(exporterTestCases);
        String url = LOAD_URL + apiKey;

        for (int attempt = 1; attempt <= RETRY_MAX_ATTEMPTS; attempt++) {
            try {
                if (attempt > 1) {
                    Thread.sleep(RETRY_TIMEOUT_MILLISECONDS);
                }
                
                client.post(url, requestBody, null);
                return;
            } catch (Exception e) {
                boolean is422Error = e.getMessage().contains("422");
                boolean isLastAttempt = attempt == RETRY_MAX_ATTEMPTS;
                
                if (!is422Error || isLastAttempt) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }
}
