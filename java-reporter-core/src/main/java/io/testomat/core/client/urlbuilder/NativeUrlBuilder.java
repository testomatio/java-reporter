package io.testomat.core.client.urlbuilder;

import static io.testomat.core.constants.PropertyNameConstants.API_KEY_PROPERTY_NAME;
import static io.testomat.core.constants.PropertyNameConstants.HOST_URL_PROPERTY_NAME;

import io.testomat.core.exception.InvalidProvidedPropertyException;
import io.testomat.core.exception.UrlBuildingException;
import io.testomat.core.propertyconfig.impl.PropertyProviderFactoryImpl;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import io.testomat.core.propertyconfig.interf.PropertyProviderFactory;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Native implementation of URL builder for Testomat.io API endpoints.
 * Constructs URLs for test run operations using standard Java libraries.
 */
public class NativeUrlBuilder implements UrlBuilder {
    private static final Logger log = LoggerFactory.getLogger(NativeUrlBuilder.class);
    private static final String API_REPORTER_PATH = "/api/reporter";
    private static final String TEST_RUN_PATH = "/testrun";
    private static final String API_KEY_PARAM = "api_key=";
    private static final String PARAMS_START_MARK = "?";

    private final PropertyProviderFactory factory =
            PropertyProviderFactoryImpl.getPropertyProviderFactory();
    private final PropertyProvider provider = factory.getPropertyProvider();

    /**
     * Builds URL for creating a new test run.
     *
     * @return complete URL for test run creation
     */
    @Override
    public String buildCreateRunUrl() {
        String baseUrl = getBaseUrl();
        String apiKey = getApiKey();
        String encodedApiKey = URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
        String urlString = baseUrl
                + API_REPORTER_PATH
                + PARAMS_START_MARK
                + API_KEY_PARAM
                + encodedApiKey;
        validateBuiltUrl(urlString);
        return urlString;
    }

    /**
     * Builds URL for reporting test results.
     *
     * @param testRunUid unique identifier of the test run
     * @return complete URL for test result reporting
     */
    @Override
    public String buildReportTestUrl(String testRunUid) {
        validateTestRunId(testRunUid);
        String apiKey = getApiKey();
        String encodedApiKey = URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
        String baseUrl = getBaseUrl();
        String urlString = baseUrl
                + API_REPORTER_PATH
                + "/" + testRunUid.trim()
                + TEST_RUN_PATH
                + PARAMS_START_MARK
                + API_KEY_PARAM
                + encodedApiKey;
        validateBuiltUrl(urlString);
        return urlString;
    }

    /**
     * Builds URL for finishing a test run.
     *
     * @param testRunUid unique identifier of the test run
     * @return complete URL for test run completion
     */
    @Override
    public String buildFinishTestRunUrl(String testRunUid) {
        validateTestRunId(testRunUid);
        String apiKey = getApiKey();
        String encodedApiKey = URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
        String baseUrl = getBaseUrl();
        String urlString = baseUrl
                + API_REPORTER_PATH
                + "/" + testRunUid.trim()
                + PARAMS_START_MARK
                + API_KEY_PARAM
                + encodedApiKey;
        validateBuiltUrl(urlString);
        return urlString;
    }

    /**
     * Gets base URL from properties with validation.
     */
    private String getBaseUrl() {
        String baseUrl = provider.getProperty(HOST_URL_PROPERTY_NAME);
        validateProvidedUrl(baseUrl);
        return normalizeUrl(baseUrl);
    }

    private void validateProvidedUrl(String providedUrl) {
        if (providedUrl == null || providedUrl.trim().isEmpty()) {
            String message = "Base URL is required. Please set property: " + HOST_URL_PROPERTY_NAME;
            log.warn(message);
            throw new InvalidProvidedPropertyException(message);
        }

        String trimmedProvidedUrl = providedUrl.trim();

        if (!trimmedProvidedUrl.startsWith("http://")
                && !trimmedProvidedUrl.startsWith("https://")) {
            String message = "Base URL must start with http:// or https://, got: '"
                    + trimmedProvidedUrl
                    + "'. Please set property: "
                    + HOST_URL_PROPERTY_NAME;
            log.warn(message);
            throw new InvalidProvidedPropertyException(message);
        }

        checkProvidedUrlContainsHost(trimmedProvidedUrl);
    }

    private String normalizeUrl(String url) {
        String normalizedUrl = url.trim();
        return normalizedUrl.endsWith("/")
                ? normalizedUrl.substring(0, normalizedUrl.length() - 1)
                : normalizedUrl;
    }

    private void checkProvidedUrlContainsHost(String providedUrl) {
        try {
            URI uri = URI.create(providedUrl);
            if (uri.getHost() == null || uri.getHost().isEmpty()) {
                String message = "Base URL must contain valid hostname, got: '" + providedUrl
                        + "'. Please set property: " + HOST_URL_PROPERTY_NAME;
                log.warn(message);
                throw new InvalidProvidedPropertyException(message);
            }
        } catch (IllegalArgumentException e) {
            String message = "Malformed base URL: '" + providedUrl
                    + "'. Please set property: " + HOST_URL_PROPERTY_NAME;
            log.warn(message);
            throw new InvalidProvidedPropertyException(message);
        }
    }

    private void validateBuiltUrl(String builtUrl) {
        try {
            new URL(builtUrl);
        } catch (MalformedURLException e) {
            throw new UrlBuildingException("Malformed URL result: " + builtUrl, e);
        }
    }

    private void validateTestRunId(String testRunUid) {
        if (testRunUid == null || testRunUid.trim().isEmpty()) {
            throw new UrlBuildingException(
                    "Failed to build URL. Test run id is null or empty: " + testRunUid);
        }
    }

    private String getApiKey() {
        String apiKey = provider.getProperty(API_KEY_PROPERTY_NAME);
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new InvalidProvidedPropertyException("API key is required. Set property: "
                    + API_KEY_PROPERTY_NAME);
        }
        return apiKey.trim();
    }
}
