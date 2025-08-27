package io.testomat.testng.methodexporter.sender;

import static io.testomat.core.constants.PropertyNameConstants.API_KEY_PROPERTY_NAME;
import static io.testomat.core.constants.PropertyNameConstants.HOST_URL_PROPERTY_NAME;

import io.testomat.core.client.http.CustomHttpClient;
import io.testomat.core.client.http.NativeHttpClient;
import io.testomat.core.propertyconfig.impl.PropertyProviderFactoryImpl;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import io.testomat.testng.methodexporter.TestNgExporterRequestBodyBuilder;
import io.testomat.testng.methodexporter.model.TestNgExporterTestCase;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sends extracted TestNG test cases to Testomat.io API.
 * Handles HTTP communication with retry logic and error handling.
 */
public class TestNgExportSender {
    private static final Logger log = LoggerFactory.getLogger(TestNgExportSender.class);

    private static final String LOAD_URL_PART = "api/load?api_key=";

    private static final int RETRY_TIMEOUT_MILLISECONDS = 1500;
    private static final int RETRY_MAX_ATTEMPTS = 2;
    private final PropertyProvider provider;
    private String apiUrl;

    public TestNgExportSender() {
        this.provider =
                PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();
        apiUrl = this.provider.getProperty(HOST_URL_PROPERTY_NAME);
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
        log.debug("sendTestCases called with {} test cases", exporterTestCases.size());

        if (exporterTestCases.isEmpty()) {
            log.debug("No test cases to send, returning early");
            return;
        }

        TestNgExporterRequestBodyBuilder exporterRequestBodyBuilder =
                new TestNgExporterRequestBodyBuilder();
        CustomHttpClient client = new NativeHttpClient();

        String requestBody = exporterRequestBodyBuilder.buildRequestBody(exporterTestCases);
        log.debug("Request body: {}", requestBody);

        String apiKey;
        try {
            apiKey = provider.getProperty(API_KEY_PROPERTY_NAME);
        } catch (Exception e) {
            return;
        }

        if (apiKey == null) {
            return;
        }

        String url = apiUrl + LOAD_URL_PART + provider.getProperty(API_KEY_PROPERTY_NAME);

        for (int attempt = 1; attempt <= RETRY_MAX_ATTEMPTS; attempt++) {
            log.debug("Attempt {} of {}", attempt, RETRY_MAX_ATTEMPTS);

            try {
                if (attempt > 1) {
                    log.debug("Waiting {} ms before retry", RETRY_TIMEOUT_MILLISECONDS);
                    Thread.sleep(RETRY_TIMEOUT_MILLISECONDS);
                }

                log.debug("Making HTTP POST request...");
                client.post(url, requestBody, null);
                log.debug("HTTP POST request completed successfully");
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
                    log.debug("422 error detected, will retry");
                }
            }
        }
        log.error("All attempts failed, giving up");
    }
}
