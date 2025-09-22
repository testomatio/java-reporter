package io.testomat.core;

import java.util.List;

/**
 * Data class representing the relationship between test execution and its associated artifact links.
 * Contains test metadata and S3 URLs for uploaded artifacts.
 */
public class ArtifactLinkData {
    private String rid;
    private final String testId;
    private final String testName;

    private final List<String> links;

    /**
     * Creates artifact link data for a test execution.
     *
     * @param testName name of the test
     * @param rid request identifier
     * @param testId unique test identifier
     * @param links list of S3 URLs for uploaded artifacts
     */
    public ArtifactLinkData(String testName, String rid, String testId, List<String> links) {
        this.testName = testName;
        this.rid = rid;
        this.testId = testId;
        this.links = links;
    }

    /**
     * Returns the list of artifact URLs.
     *
     * @return list of S3 URLs for artifacts
     */
    public List<String> getLinks() {
        return links;
    }

    /**
     * Returns the unique test identifier.
     *
     * @return test ID
     */
    public String getTestId() {
        return testId;
    }

    /**
     * Returns the request identifier.
     *
     * @return request ID
     */
    public String getRid() {
        return rid;
    }

    /**
     * Sets the request identifier.
     *
     * @param rid request ID to set
     */
    public void setRid(String rid) {
        this.rid = rid;
    }

    /**
     * Returns the test name.
     *
     * @return name of the test
     */
    public String getTestName() {
        return testName;
    }
}
