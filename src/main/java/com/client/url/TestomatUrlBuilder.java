package com.client.url;

import okhttp3.HttpUrl;

/**
 * Builder class for constructing Testomat.io API URLs.
 * Encapsulates URL construction logic and provides fluent interface.
 */
public class TestomatUrlBuilder {

    private static final String API_KEY_PARAM = "api_key";
    private static final String API_PATH = "api";
    private static final String REPORTER_PATH = "reporter";
    private static final String TEST_RUN_PATH = "testrun";

    private final String baseUrl;
    private final String apiKey;

    /**
     * Constructs a new TestomatUrlBuilder with base URL and API key.
     *
     * @param baseUrl the base URL of the Testomat.io API
     * @param apiKey  the API key for authentication
     */
    public TestomatUrlBuilder(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    /**
     * Builds URL for creating a new test run.
     *
     * @return the complete URL string for test run creation
     */
    public String buildCreateTestRunUrl() {
        return HttpUrl.parse(baseUrl).newBuilder()
                .addPathSegment(API_PATH)
                .addPathSegment(REPORTER_PATH)
                .addQueryParameter(API_KEY_PARAM, apiKey)
                .build()
                .toString();
    }

    /**
     * Builds URL for reporting test results to a specific test run.
     *
     * @param testRunUid the unique identifier of the test run
     * @return the complete URL string for test result reporting
     */
    public String buildReportTestUrl(String testRunUid) {
        return HttpUrl.parse(baseUrl).newBuilder()
                .addPathSegment(API_PATH)
                .addPathSegment(REPORTER_PATH)
                .addPathSegment(testRunUid)
                .addPathSegment(TEST_RUN_PATH)
                .addQueryParameter(API_KEY_PARAM, apiKey)
                .build()
                .toString();
    }

    /**
     * Builds URL for finishing a test run.
     *
     * @param testRunUid the unique identifier of the test run
     * @return the complete URL string for finishing test run
     */
    public String buildFinishTestRunUrl(String testRunUid) {
        return HttpUrl.parse(baseUrl).newBuilder()
                .addPathSegment(API_PATH)
                .addPathSegment(REPORTER_PATH)
                .addPathSegment(testRunUid)
                .addQueryParameter(API_KEY_PARAM, apiKey)
                .build()
                .toString();
    }
}