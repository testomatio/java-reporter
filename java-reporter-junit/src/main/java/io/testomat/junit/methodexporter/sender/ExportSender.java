package io.testomat.junit.methodexporter.sender;

import static io.testomat.core.constants.PropertyNameConstants.API_KEY_PROPERTY_NAME;
import static io.testomat.core.constants.PropertyNameConstants.HOST_URL_PROPERTY_NAME;

import io.testomat.core.client.http.CustomHttpClient;
import io.testomat.core.client.http.NativeHttpClient;
import io.testomat.core.propertyconfig.impl.PropertyProviderFactoryImpl;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import io.testomat.junit.exception.MethodExporterException;
import io.testomat.junit.methodexporter.ExporterRequestBodyBuilder;
import io.testomat.junit.model.ExporterTestCase;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles sending test case information to the Testomat.io API.
 * This class builds HTTP requests containing test method bodies and metadata,
 * then transmits them to the configured Testomat server endpoint with retry logic.
 */
public class ExportSender {
    private static final Logger log = LoggerFactory.getLogger(ExportSender.class);
    private static final String LOAD_URL_PART = "api/load?api_key=";
    private static final int RETRY_TIMEOUT_MILLISECONDS = 1500;
    private static final int RETRY_MAX_ATTEMPTS = 2;
    private final PropertyProvider provider;
    private String apiUrl;

    public ExportSender() {
        this.provider =
                PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();
        apiUrl = this.provider.getProperty(HOST_URL_PROPERTY_NAME);
    }

    /**
     * Constructor for testing
     *
     * @param provider the property provider
     */
    public ExportSender(PropertyProvider provider) {
        this.provider = provider;
    }

    /**
     * Sends a list of test cases to the Testomat.io API.
     * Builds the request payload and transmits it to the configured endpoint
     * with retry logic for handling temporary failures.
     *
     * @param exporterTestCases the test cases to send to the API
     * @throws MethodExporterException if the export request fails after all retries
     */
    public void sendTestCases(List<ExporterTestCase> exporterTestCases) {
        if (exporterTestCases.isEmpty()) {
            log.debug("No exporter test cases found");
            return;
        }

        ExporterRequestBodyBuilder exporterRequestBodyBuilder = new ExporterRequestBodyBuilder();
        CustomHttpClient client = new NativeHttpClient();

        String requestBody = exporterRequestBodyBuilder.buildRequestBody(exporterTestCases);
        String url = apiUrl + LOAD_URL_PART + provider.getProperty(API_KEY_PROPERTY_NAME);

        log.info("Sending test case export to API: {} test cases", exporterTestCases.size());
        log.info("Export request body: {}", requestBody);
        for (int attempt = 1; attempt <= RETRY_MAX_ATTEMPTS; attempt++) {
            log.debug("Attempt {} of {}", attempt, exporterTestCases.size());
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
                    throw new MethodExporterException("Failed to export test cases", e);
                }
            }
        }
    }
}
