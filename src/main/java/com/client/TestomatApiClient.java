package com.client;

import com.client.http.HttpClient;
import com.client.http.OkHttpClientImpl;
import com.client.request.TestomatRequestBodyBuilder;
import com.client.url.TestomatUrlBuilder;
import com.model.TestResult;
import com.property_config.impl.PropertyProviderFactoryImpl;
import com.property_config.interf.PropertyProviderFactory;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.constants.CommonConstants.HOST_URL_PROPERTY_NAME;

/**
 * Enhanced client for interacting with the Testomat.io API.
 * This implementation follows SOLID principles by separating concerns:
 * - HTTP communication is delegated to HttpClient
 * - URL construction is handled by TestomatUrlBuilder
 * - Request body creation is managed by TestomatRequestBodyBuilder
 */
public class TestomatApiClient implements ApiInterface {

    private static final PropertyProviderFactory propertyProviderFactory =
            PropertyProviderFactoryImpl.getPropertyProviderFactory();
    private static final String RESPONSE_UID_KEY = "uid";
    private static final Logger LOGGER = LoggerFactory.getLogger(TestomatApiClient.class);

    private final String apiKey;
    private final HttpClient httpClient;
    private final TestomatUrlBuilder urlBuilder;
    private final TestomatRequestBodyBuilder requestBodyBuilder;

    /**
     * Constructs a new TestomatApiClient with the provided API key and default dependencies.
     *
     * @param apiKey the API key for authentication with Testomat.io
     */
    public TestomatApiClient(String apiKey) {
        this(apiKey, new OkHttpClientImpl(), createUrlBuilder(apiKey), new TestomatRequestBodyBuilder());
    }

    /**
     * Constructs a new TestomatApiClient with custom dependencies for better testability.
     *
     * @param apiKey             the API key for authentication
     * @param httpClient         the HTTP client implementation
     * @param urlBuilder         the URL builder for constructing API endpoints
     * @param requestBodyBuilder the request body builder for creating JSON payloads
     */
    public TestomatApiClient(String apiKey, HttpClient httpClient,
                             TestomatUrlBuilder urlBuilder, TestomatRequestBodyBuilder requestBodyBuilder) {
        this.apiKey = apiKey;
        this.httpClient = httpClient;
        this.urlBuilder = urlBuilder;
        this.requestBodyBuilder = requestBodyBuilder;
    }

    /**
     * Creates a new test run in Testomat.io.
     *
     * @param title the title of the test run
     * @return the unique identifier of the created test run
     * @throws IOException if the API request fails or response cannot be processed
     */
    @Override
    public String createTestRun(String title) throws IOException {
        LOGGER.debug("Creating test run with title: {}", title);

        String url = urlBuilder.buildCreateTestRunUrl();
        String requestBody = requestBodyBuilder.buildCreateTestRunBody(title);

        Map<String, String> responseBody = httpClient.post(url, requestBody, Map.class);

        if (responseBody == null || !responseBody.containsKey(RESPONSE_UID_KEY)) {
            throw new IOException("Invalid response: missing UID in create test run response");
        }

        return responseBody.get(RESPONSE_UID_KEY);
    }

    /**
     * Reports a single test result to an existing test run in Testomat.io.
     *
     * @param uid    the unique identifier of the test run
     * @param result the test result to report
     * @throws IOException if the API request fails
     */
    @Override
    public void reportTest(String uid, TestResult result) throws IOException {
        LOGGER.debug("Reporting test result for testId: {}", result.getTestId());

        String url = urlBuilder.buildReportTestUrl(uid);
        String requestBody = requestBodyBuilder.buildSingleTestReportBody(result);

        httpClient.post(url, requestBody, null);
    }

    /**
     * Reports multiple test results in a single batch request to Testomat.io.
     * Uses the same endpoint as individual test reporting but with batch structure.
     *
     * @param uid     the unique identifier of the test run
     * @param results the list of test results to report
     * @throws IOException if the API request fails
     */
    @Override
    public void reportTests(String uid, List<TestResult> results) throws IOException {
        if (results == null || results.isEmpty()) {
            LOGGER.debug("No test results to report");
            return;
        }

        LOGGER.debug("Reporting batch of {} test results", results.size());

        String url = urlBuilder.buildReportTestUrl(uid);
        String requestBody = requestBodyBuilder.buildBatchTestReportBody(results, apiKey);

        httpClient.post(url, requestBody, null);
    }

    /**
     * Marks a test run as finished in Testomat.io.
     *
     * @param uid      the unique identifier of the test run
     * @param duration the duration of the test run in seconds
     * @throws IOException if the API request fails
     */
    @Override
    public void finishTestRun(String uid, float duration) throws IOException {
        LOGGER.debug("Finishing test run with uid: {}", uid);

        String url = urlBuilder.buildFinishTestRunUrl(uid);
        String requestBody = requestBodyBuilder.buildFinishTestRunBody(duration);

        httpClient.put(url, requestBody, null);
    }

    /**
     * Creates a URL builder instance with the host URL from properties.
     *
     * @param apiKey the API key for URL construction
     * @return configured TestomatUrlBuilder instance
     */
    private static TestomatUrlBuilder createUrlBuilder(String apiKey) {
        String hostUrl = propertyProviderFactory.getPropertyProvider().getProperty(HOST_URL_PROPERTY_NAME);
        return new TestomatUrlBuilder(hostUrl, apiKey);
    }
}