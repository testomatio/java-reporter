package com.testomatio.reporter.client.util;

import static com.testomatio.reporter.constants.CommonConstants.API_KEY_STRING;
import static com.testomatio.reporter.constants.PropertyNameConstants.API_KEY_PROPERTY_NAME;
import static com.testomatio.reporter.constants.PropertyNameConstants.HOST_URL_PROPERTY_NAME;
import static com.testomatio.reporter.logger.LoggerUtils.getLogger;

import com.testomatio.reporter.propertyconfig.impl.PropertyProviderFactoryImpl;
import com.testomatio.reporter.propertyconfig.interf.PropertyProvider;
import java.util.Objects;
import okhttp3.HttpUrl;

/**
 * Utility for building Testomat.io API URLs.
 * Constructs URLs for test run creation, reporting, and completion.
 */
public class RequestUrlBuilderUtil {
    private static final String API_PATH = "api";
    private static final String REPORTER_PATH = "reporter";
    private static final String TEST_RUN_PATH = "testrun";

    private RequestUrlBuilderUtil() {
    }

    /**
     * Builds URL for creating new test run.
     *
     * @return complete URL for test run creation
     * @throws IllegalStateException if required properties are not configured
     */
    public static String buildCreateRunUrl() {
        String baseUrl = getBaseUrl();
        String apiKey = getApiKey();

        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(baseUrl), "Invalid base URL: "
                        + baseUrl)
                .newBuilder()
                .addPathSegment(API_PATH)
                .addPathSegment(REPORTER_PATH)
                .addQueryParameter(API_KEY_STRING, apiKey)
                .build();

        String urlString = url.toString();
        getLogger(RequestUrlBuilderUtil.class).finer("Built create test run URL: "
                + urlString);
        return urlString;
    }

    /**
     * Builds URL for reporting test results.
     *
     * @param testRunUid test run identifier
     * @return complete URL for test result reporting
     * @throws IllegalArgumentException if testRunUid is null or empty
     * @throws IllegalStateException    if required properties are not configured
     */
    public static String buildReportTestUrl(String testRunUid) {
        if (testRunUid == null || testRunUid.trim().isEmpty()) {
            throw new IllegalArgumentException("Test run UID cannot be null or empty");
        }

        String baseUrl = getBaseUrl();
        String apiKey = getApiKey();

        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(baseUrl), "Invalid base URL: "
                        + baseUrl)
                .newBuilder()
                .addPathSegment(API_PATH)
                .addPathSegment(REPORTER_PATH)
                .addPathSegment(testRunUid.trim())
                .addPathSegment(TEST_RUN_PATH)
                .addQueryParameter(API_KEY_STRING, apiKey)
                .build();

        String urlString = url.toString();
        getLogger(RequestUrlBuilderUtil.class).finer("Built report test URL: " + urlString);
        return urlString;
    }

    /**
     * Builds URL for finishing test run.
     *
     * @param testRunUid test run identifier
     * @return complete URL for finishing test run
     * @throws IllegalArgumentException if testRunUid is null or empty
     * @throws IllegalStateException    if required properties are not configured
     */
    public static String buildFinishTestRunUrl(String testRunUid) {
        if (testRunUid == null || testRunUid.trim().isEmpty()) {
            throw new IllegalArgumentException("Test run UID cannot be null or empty");
        }

        String baseUrl = getBaseUrl();
        String apiKey = getApiKey();

        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(baseUrl), "Invalid base URL: "
                        + baseUrl)
                .newBuilder()
                .addPathSegment(API_PATH)
                .addPathSegment(REPORTER_PATH)
                .addPathSegment(testRunUid.trim())
                .addQueryParameter(API_KEY_STRING, apiKey)
                .build();

        String urlString = url.toString();
        getLogger(RequestUrlBuilderUtil.class).finer("Built finish test run URL: "
                + urlString);
        return urlString;
    }

    /**
     * Gets base URL from properties with validation.
     */
    private static String getBaseUrl() {
        try {
            PropertyProvider propertyProvider = getPropertyProvider();
            String baseUrl = propertyProvider.getProperty(HOST_URL_PROPERTY_NAME);

            if (baseUrl == null || baseUrl.trim().isEmpty()) {
                throw new IllegalStateException(
                        "Base URL not configured. Please set property: " + HOST_URL_PROPERTY_NAME);
            }

            baseUrl = baseUrl.trim();
            if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
                throw new IllegalStateException(
                        "Invalid base URL format. Must start with http:// or https://. Got: "
                                + baseUrl);
            }

            return baseUrl;
        } catch (Exception e) {
            getLogger(RequestUrlBuilderUtil.class).severe(
                    "Failed to get base URL from properties" + e.getCause());
            throw new IllegalStateException("Failed to load base URL configuration", e);
        }
    }

    /**
     * Gets API key from properties with validation.
     */
    private static String getApiKey() {
        try {
            PropertyProvider propertyProvider = getPropertyProvider();
            String apiKey = propertyProvider.getProperty(API_KEY_PROPERTY_NAME);

            if (apiKey == null || apiKey.trim().isEmpty()) {
                throw new IllegalStateException(
                        "API key not configured. Please set property: " + API_KEY_PROPERTY_NAME);
            }

            return apiKey.trim();
        } catch (Exception e) {
            getLogger(RequestUrlBuilderUtil.class).severe(
                    "Failed to get API key from properties" + e.getCause());
            throw new IllegalStateException("Failed to load API key configuration", e);
        }
    }

    /**
     * Gets PropertyProvider instance with error handling.
     */
    private static PropertyProvider getPropertyProvider() {
        try {
            return PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();
        } catch (Exception e) {
            getLogger(RequestUrlBuilderUtil.class).severe(
                    "Failed to create PropertyProvider" + e.getCause());
            throw new IllegalStateException("Failed to initialize property provider", e);
        }
    }
}
