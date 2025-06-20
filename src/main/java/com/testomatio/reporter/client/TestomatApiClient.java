package com.testomatio.reporter.client;

import com.testomatio.reporter.client.http.HttpClient;
import com.testomatio.reporter.client.http.OkHttpClientImpl;
import com.testomatio.reporter.client.request.RequestBodyBuilder;
import com.testomatio.reporter.client.request.TestomatRequestBodyBuilder;
import com.testomatio.reporter.client.util.RequestUrlBuilderUtil;
import com.testomatio.reporter.exception.FinishReportFailedException;
import com.testomatio.reporter.exception.ReportingFailedException;
import com.testomatio.reporter.exception.RunCreationFailedException;
import com.testomatio.reporter.model.TestRunResult;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.testomatio.reporter.constants.CommonConstants.RESPONSE_UID_KEY;
import static com.testomatio.reporter.logger.LoggerUtils.getLogger;

/**
 * Client for interacting with the Testomat.io API.
 */
public class TestomatApiClient implements ApiInterface {
    private final Logger LOGGER = getLogger(TestomatApiClient.class);

    private final String apiKey;
    private final HttpClient httpClient;
    private final RequestBodyBuilder requestBodyBuilder;

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
    public String createRun(String title) throws IOException {
        LOGGER.fine("Creating run with title: " + title);

        String url = RequestUrlBuilderUtil.buildCreateRunUrl();
        LOGGER.finer("Creating run with url: " + url);
        String requestBody = requestBodyBuilder.buildCreateRunBody(title);

        Map<String, String> responseBody = httpClient.post(url, requestBody, Map.class);

        if (responseBody == null || !responseBody.containsKey(RESPONSE_UID_KEY)) {
            throw new RunCreationFailedException("Invalid response: missing UID in create test run response");
        }
        LOGGER.fine("Created test run with UID: " + responseBody.get(RESPONSE_UID_KEY));

        return responseBody.get(RESPONSE_UID_KEY);
    }

    /**
     * Reports a single test result to an existing test run in Testomat.io.
     *
     * @param uid    the unique identifier of the test run
     * @param result the test result to report
     */
    @Override
    public void reportTest(String uid, TestRunResult result) {
        try {
            LOGGER.fine("Reporting test result for testId: " + result.getTestId());

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
    public void reportTests(String uid, List<TestRunResult> results) {
        try {
            if (results == null || results.isEmpty()) {
                LOGGER.fine("No test results to report");
                return;
            }

            LOGGER.finer("Reporting batch of %d test results" + results.size());

            String url = RequestUrlBuilderUtil.buildReportTestUrl(uid);
            String requestBody = requestBodyBuilder.buildBatchTestReportBody(results, apiKey);

            httpClient.post(url, requestBody, null);
        } catch (Exception e) {
            LOGGER.severe("Failed to report batch test /n" + e.getMessage());
            throw new ReportingFailedException("Failed to report batch /n" + e.getMessage());
        }
    }

    /**
     * Marks a test run as finished in Testomat.io via PUT request.
     *
     * @param uid      the unique identifier of the test run
     * @param duration the duration of the test run in seconds
     */
    @Override
    public void finishTestRun(String uid, float duration) {
        try {
            LOGGER.fine("Finishing test run with uid: " + uid);

            String url = RequestUrlBuilderUtil.buildFinishTestRunUrl(uid);
            String requestBody = requestBodyBuilder.buildFinishRunBody(duration);

            httpClient.put(url, requestBody, null);
        } catch (Exception e) {
            LOGGER.severe("Failed to finish test run with uid: " + uid);
            throw new FinishReportFailedException("Failed to finish test run " + e.getMessage());
        }
    }
}