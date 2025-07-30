package io.testomat.testng.methodexporter.sender;

import static io.testomat.core.constants.PropertyNameConstants.API_KEY_PROPERTY_NAME;

import io.testomat.core.client.http.CustomHttpClient;
import io.testomat.core.client.http.NativeHttpClient;
import io.testomat.core.propertyconfig.impl.PropertyProviderFactoryImpl;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import io.testomat.testng.methodexporter.TestNgExporterRequestBodyBuilder;
import io.testomat.testng.methodexporter.model.TestNgExporterTestCase;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestNgExportSender {
    private static final Logger log = LoggerFactory.getLogger(TestNgExportSender.class);

    private static final String LOAD_URL = "https://app.testomat.io/api/load?api_key=";
    private static final int RETRY_TIMEOUT_MILLISECONDS = 1500;
    private static final int RETRY_MAX_ATTEMPTS = 2;
    private final PropertyProvider provider;

    public TestNgExportSender() {
        this.provider =
                PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();
    }

    /**
     * Constructor for testing
     */
    public TestNgExportSender(PropertyProvider provider) {
        this.provider = provider;
    }

    /**
     * Sends test cases to testomat.io service with retry logic.
     */
    public void sendTestCases(List<TestNgExporterTestCase> exporterTestCases) {
        log.info("sendTestCases called with {} test cases", exporterTestCases.size());

        if (exporterTestCases.isEmpty()) {
            log.info("No test cases to send, returning early");
            return;
        }

        TestNgExporterRequestBodyBuilder exporterRequestBodyBuilder =
                new TestNgExporterRequestBodyBuilder();
        CustomHttpClient client = new NativeHttpClient();

        String requestBody = exporterRequestBodyBuilder.buildRequestBody(exporterTestCases);
        log.info("Built request body with length: {}", requestBody.length());
        log.debug("Request body: {}", requestBody);

        String apiKey;
        try {
            apiKey = provider.getProperty(API_KEY_PROPERTY_NAME);
            log.info("API key found: {}", apiKey != null ? "YES" : "NO");
            if (apiKey != null) {
                log.info("API key length: {}", apiKey.length());
                log.info("API key starts with: {}", apiKey.length() > 0
                        ? apiKey.substring(0, Math.min(8, apiKey.length())) + "..."
                        : "empty");
            }
        } catch (Exception e) {
            log.error("Error getting API key: {}", e.getMessage(), e);
            return;
        }

        if (apiKey == null) {
            log.error("API key is null - cannot send test cases");
            return;
        }

        String url = LOAD_URL + apiKey;
        log.info("Sending request to URL: {}", LOAD_URL + "***");

        for (int attempt = 1; attempt <= RETRY_MAX_ATTEMPTS; attempt++) {
            log.info("Attempt {} of {}", attempt, RETRY_MAX_ATTEMPTS);

            try {
                if (attempt > 1) {
                    log.info("Waiting {} ms before retry", RETRY_TIMEOUT_MILLISECONDS);
                    Thread.sleep(RETRY_TIMEOUT_MILLISECONDS);
                }

                log.info("Making HTTP POST request...");
                client.post(url, requestBody, null);
                log.info("HTTP POST request completed successfully");
                return;
            } catch (Exception e) {
                log.error("HTTP request failed on attempt {}: {}", attempt, e.getMessage(), e);

                boolean is422Error = e.getMessage().contains("422");
                boolean isLastAttempt = attempt == RETRY_MAX_ATTEMPTS;

                if (!is422Error || isLastAttempt) {
                    log.error("Not retrying - is422Error: {}, isLastAttempt: {}",
                            is422Error, isLastAttempt);
                    e.printStackTrace();
                    break;
                } else {
                    log.info("422 error detected, will retry");
                }
            }
        }

        log.error("All attempts failed, giving up");
    }
}
