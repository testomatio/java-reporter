package io.testomat.core.client.urlbuilder;

import io.testomat.core.exception.InvalidProvidedPropertyException;
import io.testomat.core.exception.UrlBuildingException;

/**
 * Strategy interface for building Testomat.io API endpoint URLs.
 * Handles URL construction with proper parameter encoding and validation.
 */
public interface UrlBuilder {
    
    /**
     * Constructs URL for test run creation endpoint.
     *
     * @return fully qualified URL for creating new test runs
     * @throws InvalidProvidedPropertyException if base URL or API key is invalid
     */
    String buildCreateRunUrl();

    /**
     * Constructs URL for test result reporting endpoint.
     *
     * @param testRunUid unique identifier of the target test run
     * @return fully qualified URL for reporting test results
     * @throws UrlBuildingException if test run UID is null or empty
     * @throws InvalidProvidedPropertyException if base URL or API key is invalid
     */
    String buildReportTestUrl(String testRunUid);

    /**
     * Constructs URL for test run finalization endpoint.
     *
     * @param testRunUid unique identifier of the target test run
     * @return fully qualified URL for finishing test runs
     * @throws UrlBuildingException if test run UID is null or empty
     * @throws InvalidProvidedPropertyException if base URL or API key is invalid
     */
    String buildFinishTestRunUrl(String testRunUid);
}
