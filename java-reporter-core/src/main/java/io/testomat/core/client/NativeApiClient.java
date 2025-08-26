package io.testomat.core.client;

import io.testomat.core.client.http.CustomHttpClient;
import io.testomat.core.client.request.NativeRequestBodyBuilder;
import io.testomat.core.client.request.RequestBodyBuilder;
import io.testomat.core.client.urlbuilder.NativeUrlBuilder;
import io.testomat.core.client.urlbuilder.UrlBuilder;
import io.testomat.core.exception.FinishReportFailedException;
import io.testomat.core.exception.ReportingFailedException;
import io.testomat.core.exception.RunCreationFailedException;
import io.testomat.core.model.TestResult;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.testomat.core.constants.CommonConstants.REPORTER_VERSION;
import static io.testomat.core.constants.CommonConstants.RESPONSE_UID_KEY;

/**
 * HTTP client for Testomat.io API operations.
 * Handles test run lifecycle and result reporting with proper error handling.
 */
public class NativeApiClient implements ApiInterface {
    private static final Logger log = LoggerFactory.getLogger(NativeApiClient.class);

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
    public NativeApiClient(String apiKey,
                           CustomHttpClient client,
                           NativeRequestBodyBuilder requestBodyBuilder) {
        this.apiKey = apiKey;
        this.client = client;
        this.requestBodyBuilder = requestBodyBuilder;
    }

    @Override
    public String createRun(String title) throws IOException {
        log.debug("Creating run with title: {}", title);

        String url = urlBuilder.buildCreateRunUrl();
        log.debug("Creating run with request url: {}", url);
        String requestBody = requestBodyBuilder.buildCreateRunBody(title);

        Map<String, String> responseBody = client.post(url, requestBody, Map.class);
        log.debug(responseBody.toString());

        if (responseBody == null || !responseBody.containsKey(RESPONSE_UID_KEY)) {
            throw new RunCreationFailedException(
                    "Invalid response: missing UID in create test run response");
        }
        logAndPrintUrls(responseBody);
        log.debug("Created test run with UID: {}", responseBody.get(RESPONSE_UID_KEY));
        return responseBody.get(RESPONSE_UID_KEY);
    }

    @Override
    public void reportTest(String uid, TestResult result) {
        try {
            log.debug("Reporting test result for testId: {}", result.getTestId());

            String url = urlBuilder.buildReportTestUrl(uid);
            String requestBody = requestBodyBuilder.buildSingleTestReportBody(result);
            log.debug("Request body: {}", requestBody);
            client.post(url, requestBody, null);

        } catch (Exception e) {
            throw new ReportingFailedException("Failed to report test /n" + e.getMessage());
        }
    }

    @Override
    public void reportTests(String uid, List<TestResult> results) {
        try {
            if (results == null || results.isEmpty()) {
                log.debug("No test results to report");
                return;
            }

            log.debug("Reporting batch of {} test results", results.size());

            String url = urlBuilder.buildReportTestUrl(uid);
            String requestBody = requestBodyBuilder.buildBatchTestReportBody(results, apiKey);

            client.post(url, requestBody, null);
        } catch (Exception e) {
            log.error("Failed to report batch test /n{}", e.getMessage());
            throw new ReportingFailedException("Failed to report batch /n" + e.getMessage());
        }
    }

    @Override
    public void finishTestRun(String uid, float duration) {
        try {
            log.debug("Finishing test run with uid: {}", uid);

            String url = urlBuilder.buildFinishTestRunUrl(uid);
            String requestBody = requestBodyBuilder.buildFinishRunBody(duration);

            client.put(url, requestBody, null);
        } catch (Exception e) {
            log.error("Failed to finish test run with uid: {}", uid);
            throw new FinishReportFailedException("Failed to finish test run " + e.getMessage());
        }
    }

    private void logAndPrintUrls(Map<String, String> responseBody) {
        String publicUrl = responseBody.get("public_url");

        log.info("[TESTOMATIO] Testomat.io java core reporter version: [{}]", REPORTER_VERSION);

        if (publicUrl != null) {
            log.info("[TESTOMATIO] Public url: {}", publicUrl);
        }

        log.info("[TESTOMATIO] See run aggregation at: {}", responseBody.get("url"));
    }
}
