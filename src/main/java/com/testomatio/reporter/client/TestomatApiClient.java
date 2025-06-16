package com.testomatio.reporter.client;

import com.testomatio.reporter.client.http.HttpClient;
import com.testomatio.reporter.client.http.OkHttpClientImpl;
import com.testomatio.reporter.client.request.TestomatRequestBodyBuilder;
import com.testomatio.reporter.client.util.RequestUrlBuilderUtil;
import com.testomatio.reporter.exception.FinishReportFailedException;
import com.testomatio.reporter.exception.ReportingFailedException;
import com.testomatio.reporter.exception.TestRunCreationFailedException;
import com.testomatio.reporter.model.TestResult;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enhanced client for interacting with the Testomat.io API.
 * This implementation follows SOLID principles by separating concerns:
 * - HTTP communication is delegated to HttpClient
 * - URL construction is handled by TestomatUrlBuilder
 * - Request body creation is managed by TestomatRequestBodyBuilder
 */
public class TestomatApiClient implements ApiInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestomatApiClient.class);
    private static final String RESPONSE_UID_KEY = "uid";

    private final String apiKey;
    private final HttpClient httpClient;
    private final TestomatRequestBodyBuilder requestBodyBuilder;

    /**
     * Constructs a new TestomatApiClient with the provided API key and default dependencies.
     *
     * @param apiKey the API key for authentication with Testomat.io
     */
    public TestomatApiClient(String apiKey) {
        this(apiKey, new OkHttpClientImpl(), new TestomatRequestBodyBuilder());
    }

    /**
     * Constructs a new TestomatApiClient with custom dependencies for better testability.
     *
     * @param apiKey             the API key for authentication
     * @param httpClient         the HTTP client implementation
     * @param requestBodyBuilder the request body builder for creating JSON payloads
     */
    public TestomatApiClient(String apiKey, HttpClient httpClient, TestomatRequestBodyBuilder requestBodyBuilder) {
        this.apiKey = apiKey;
        this.httpClient = httpClient;
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
        LOGGER.debug("Creating test run with title  {}", title);

        String url = RequestUrlBuilderUtil.buildCreateTestRunUrl();
        LOGGER.debug("Creating test run with url: {}", url);
        String requestBody = requestBodyBuilder.buildCreateTestRunBody(title);

        Map<String, String> responseBody = httpClient.post(url, requestBody, Map.class);

        if (responseBody == null || !responseBody.containsKey(RESPONSE_UID_KEY)) {
            throw new TestRunCreationFailedException("Invalid response: missing UID in create test run response");
        }
        LOGGER.debug("Created test run with UID: {}", responseBody.get(RESPONSE_UID_KEY));

        return responseBody.get(RESPONSE_UID_KEY);
    }

    /**
     * Reports a single test result to an existing test run in Testomat.io.
     *
     * @param uid    the unique identifier of the test run
     * @param result the test result to report
     */
    @Override
    public void reportTest(String uid, TestResult result) {
        try {
            LOGGER.debug("Reporting test result for testId: {}", result.getTestId());

            String url = RequestUrlBuilderUtil.buildReportTestUrl(uid);
            String requestBody = requestBodyBuilder.buildSingleTestReportBody(result);
            httpClient.post(url, requestBody, null);

        } catch (Exception e) {
            throw new ReportingFailedException("Failed to report test /n" + e.getMessage());
        }
    }

    /**
     * Reports multiple test results in a single batch request to Testomat.io.
     * Uses the same endpoint as individual test reporting but with batch structure.
     *
     * @param uid     the unique identifier of the test run
     * @param results the list of test results to report
     */
    @Override
    public void reportTests(String uid, List<TestResult> results) {
        try {
            if (results == null || results.isEmpty()) {
                LOGGER.debug("No test results to report");
                return;
            }

            LOGGER.debug("Reporting batch of {} test results", results.size());

            String url = RequestUrlBuilderUtil.buildReportTestUrl(uid);
            String requestBody = requestBodyBuilder.buildBatchTestReportBody(results, apiKey);

            httpClient.post(url, requestBody, null);
        } catch (Exception e) {
            throw new ReportingFailedException("Failed to report batch /n" + e.getMessage());
        }
    }

    /**
     * Marks a test run as finished in Testomat.io.
     *
     * @param uid      the unique identifier of the test run
     * @param duration the duration of the test run in seconds
     */
    @Override
    public void finishTestRun(String uid, float duration) {
        try {
            LOGGER.debug("Finishing test run with uid: {}", uid);

            String url = RequestUrlBuilderUtil.buildFinishTestRunUrl(uid);
            String requestBody = requestBodyBuilder.buildFinishTestRunBody(duration);

            httpClient.put(url, requestBody, null);
        } catch (Exception e) {
            throw new FinishReportFailedException("Failed to finish test run /n" + e.getMessage());
        }
    }
}