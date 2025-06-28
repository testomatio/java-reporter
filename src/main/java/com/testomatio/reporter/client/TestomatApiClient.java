package com.testomatio.reporter.client;

import com.testomatio.reporter.client.http.CustomHttpClient;
import com.testomatio.reporter.client.request.RequestBodyBuilder;
import com.testomatio.reporter.client.request.TestomatRequestBodyBuilder;
import com.testomatio.reporter.client.util.RequestUrlBuilderUtil;
import com.testomatio.reporter.exception.FinishReportFailedException;
import com.testomatio.reporter.exception.ReportingFailedException;
import com.testomatio.reporter.exception.RunCreationFailedException;
import com.testomatio.reporter.model.TestResult;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.testomatio.reporter.constants.CommonConstants.REPORTER_VERSION;
import static com.testomatio.reporter.constants.CommonConstants.RESPONSE_UID_KEY;
import static com.testomatio.reporter.logger.LoggerUtils.getLogger;

/**
 * HTTP client for Testomat.io API operations.
 * Handles test run lifecycle and result reporting with proper error handling.
 */
public class TestomatApiClient implements ApiInterface {
    private final Logger LOGGER = getLogger(TestomatApiClient.class);

    private final String apiKey;
    private final CustomHttpClient client;
    private final RequestBodyBuilder requestBodyBuilder;

    /**
     * Creates API client with custom dependencies for testing.
     *
     * @param apiKey API key for authentication
     * @param client HTTP client implementation
     * @param requestBodyBuilder request body builder for JSON payloads
     */
    public TestomatApiClient(String apiKey,
                             CustomHttpClient client,
                             TestomatRequestBodyBuilder requestBodyBuilder) {
        this.apiKey = apiKey;
        this.client = client;
        this.requestBodyBuilder = requestBodyBuilder;
    }

    @Override
    public String createRun(String title) throws IOException {
        LOGGER.fine("Creating run with title: " + title);

        String url = RequestUrlBuilderUtil.buildCreateRunUrl();
        LOGGER.finer("Creating run with request url: " + url);
        String requestBody = requestBodyBuilder.buildCreateRunBody(title);

        Map<String, String> responseBody = client.post(url, requestBody, Map.class);

        if (responseBody == null || !responseBody.containsKey(RESPONSE_UID_KEY)) {
            throw new RunCreationFailedException("Invalid response: missing UID in create test run response");
        }
        LOGGER.fine("Created test run with UID: " + responseBody.get(RESPONSE_UID_KEY));
        LOGGER.info("Testomat.io java reporter version: " + REPORTER_VERSION);
        LOGGER.info("See run aggregation at: " + responseBody.get("url"));

        return responseBody.get(RESPONSE_UID_KEY);
    }

    @Override
    public void reportTest(String uid, TestResult result) {
        try {
            LOGGER.fine("Reporting test result for testId: " + result.getTestId());

            String url = RequestUrlBuilderUtil.buildReportTestUrl(uid);
            String requestBody = requestBodyBuilder.buildSingleTestReportBody(result);
            LOGGER.finest("-----" + requestBody);
            client.post(url, requestBody, null);

        } catch (Exception e) {
            throw new ReportingFailedException("Failed to report test /n" + e.getMessage());
        }
    }

    @Override
    public void reportTests(String uid, List<TestResult> results) {
        try {
            if (results == null || results.isEmpty()) {
                LOGGER.fine("No test results to report");
                return;
            }

            LOGGER.finer("Reporting batch of %d test results" + results.size());

            String url = RequestUrlBuilderUtil.buildReportTestUrl(uid);
            String requestBody = requestBodyBuilder.buildBatchTestReportBody(results, apiKey);

            client.post(url, requestBody, null);
        } catch (Exception e) {
            LOGGER.severe("Failed to report batch test /n" + e.getMessage());
            throw new ReportingFailedException("Failed to report batch /n" + e.getMessage());
        }
    }

    @Override
    public void finishTestRun(String uid, float duration) {
        try {
            LOGGER.fine("Finishing test run with uid: " + uid);

            String url = RequestUrlBuilderUtil.buildFinishTestRunUrl(uid);
            String requestBody = requestBodyBuilder.buildFinishRunBody(duration);

            client.put(url, requestBody, null);
        } catch (Exception e) {
            LOGGER.severe("Failed to finish test run with uid: " + uid);
            throw new FinishReportFailedException("Failed to finish test run " + e.getMessage());
        }
    }
}