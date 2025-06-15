package com.client.util;

import com.property_config.impl.PropertyProviderFactoryImpl;
import com.property_config.interf.PropertyProvider;
import java.util.Objects;
import okhttp3.HttpUrl;

import static com.constants.CommonConstants.API_KEY_STRING;
import static com.constants.PropertyNameConstants.API_KEY_PROPERTY_NAME;
import static com.constants.PropertyNameConstants.HOST_URL_PROPERTY_NAME;

/**
 * Builder class for constructing Testomat.io API URLs.
 * Encapsulates URL construction logic and provides fluent interface.
 */
public class RequestUrlBuilderUtil {

    private static final String API_PATH = "api";
    private static final String REPORTER_PATH = "reporter";
    private static final String TEST_RUN_PATH = "testrun";

    private static final String baseUrl;
    private static final String apiKey;

    static {
        PropertyProvider propertyProvider = PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();

        baseUrl = propertyProvider.getProperty(HOST_URL_PROPERTY_NAME);
        apiKey = propertyProvider.getProperty(API_KEY_PROPERTY_NAME);
    }

    private RequestUrlBuilderUtil() {
    }

    /**
     * Builds URL for creating a new test run.
     *
     * @return the complete URL string for test run creation
     */
    public static String buildCreateTestRunUrl() {
        return Objects.requireNonNull(
                        HttpUrl.parse(baseUrl)
                )
                .newBuilder()
                .addPathSegment(API_PATH)
                .addPathSegment(REPORTER_PATH)
                .addQueryParameter(API_KEY_STRING, apiKey)
                .build()
                .toString();
    }

    /**
     * Builds URL for reporting test results to a specific test run.
     *
     * @param testRunUid the unique identifier of the test run
     * @return the complete URL string for test result reporting
     */
    public static String buildReportTestUrl(String testRunUid) {
        return Objects.requireNonNull(
                        HttpUrl.parse(baseUrl)
                )
                .newBuilder()
                .addPathSegment(API_PATH)
                .addPathSegment(REPORTER_PATH)
                .addPathSegment(testRunUid)
                .addPathSegment(TEST_RUN_PATH)
                .addQueryParameter(API_KEY_STRING, apiKey)
                .build()
                .toString();
    }

    /**
     * Builds URL for finishing a test run.
     *
     * @param testRunUid the unique identifier of the test run
     * @return the complete URL string for finishing test run
     */
    public static String buildFinishTestRunUrl(String testRunUid) {
        return Objects.requireNonNull(
                        HttpUrl.parse(baseUrl)
                )
                .newBuilder()
                .addPathSegment(API_PATH)
                .addPathSegment(REPORTER_PATH)
                .addPathSegment(testRunUid)
                .addQueryParameter(API_KEY_STRING, apiKey)
                .build()
                .toString();
    }
}