package com.testomatio.reporter.client;

import static com.testomatio.reporter.constants.CommonConstants.REPORTER_VERSION;
import static com.testomatio.reporter.constants.CommonConstants.RESPONSE_UID_KEY;
import static com.testomatio.reporter.logger.LoggerUtils.getLogger;

import com.testomatio.reporter.client.http.CustomHttpClient;
import com.testomatio.reporter.client.request.DefaultRequestBodyBuilder;
import com.testomatio.reporter.client.request.RequestBodyBuilder;
import com.testomatio.reporter.client.urlbuilder.NativeUrlBuilder;
import com.testomatio.reporter.client.urlbuilder.UrlBuilder;
import com.testomatio.reporter.exception.FinishReportFailedException;
import com.testomatio.reporter.exception.ReportingFailedException;
import com.testomatio.reporter.exception.RunCreationFailedException;
import com.testomatio.reporter.model.TestResult;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * HTTP client for Testomat.io API operations.
 * Handles test run lifecycle and result reporting with proper error handling.
 */
public class DefaultApiClient implements ApiInterface {
    private static final Logger LOGGER = getLogger(DefaultApiClient.class);

    private final UrlBuilder urlBuilder = new NativeUrlBuilder();

    private final String apiKey;
    private final CustomHttpClient client;
    private final RequestBodyBuilder requestBodyBuilder;

    /**
     * Creates API client with custom dependencies for testing.
     *
     * @param apiKey             API key for authentication
     * @param client             HTTP client implementation
     * @param requestBodyBuilder request body builder for JSON payloads
     */
    public DefaultApiClient(String apiKey,
                            CustomHttpClient client,
                            DefaultRequestBodyBuilder requestBodyBuilder) {
        this.apiKey = apiKey;
        this.client = client;
        this.requestBodyBuilder = requestBodyBuilder;
    }

    @Override
    public String createRun(String title) throws IOException {
        LOGGER.fine("Creating run with title: " + title);

        String url = urlBuilder.buildCreateRunUrl();
        LOGGER.finer("Creating run with request url: " + url);
        String requestBody = requestBodyBuilder.buildCreateRunBody(title);

        Map<String, String> responseBody = client.post(url, requestBody, Map.class);
        LOGGER.fine(responseBody.toString());

        if (responseBody == null || !responseBody.containsKey(RESPONSE_UID_KEY)) {
            throw new RunCreationFailedException(
                    "Invalid response: missing UID in create test run response");
        }
        LOGGER.info("[TESTOMATIO] Testomat.io java reporter version: "
                + "[" + REPORTER_VERSION + "]");
        LOGGER.info("[TESTOMATIO] Public url: " + responseBody.get("public_url"));
        LOGGER.fine("Created test run with UID: " + responseBody.get(RESPONSE_UID_KEY));
        LOGGER.info("[TESTOMATIO] See run aggregation at: " + responseBody.get("url"));

        return responseBody.get(RESPONSE_UID_KEY);
    }

    @Override
    public void reportTest(String uid, TestResult result) {
        try {
            LOGGER.fine("Reporting test result for testId: " + result.getTestId());

            String url = urlBuilder.buildReportTestUrl(uid);
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

            String url = urlBuilder.buildReportTestUrl(uid);
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

            String url = urlBuilder.buildFinishTestRunUrl(uid);
            String requestBody = requestBodyBuilder.buildFinishRunBody(duration);

            client.put(url, requestBody, null);
        } catch (Exception e) {
            LOGGER.severe("Failed to finish test run with uid: " + uid);
            throw new FinishReportFailedException("Failed to finish test run " + e.getMessage());
        }
    }
}
