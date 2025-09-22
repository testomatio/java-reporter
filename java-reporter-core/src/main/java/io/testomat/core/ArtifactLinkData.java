package io.testomat.core;

import java.util.List;

public class ArtifactLinkData {
    private String rid;
    private final String testId;
    private final String testName;

    private final List<String> links;

    public ArtifactLinkData(String testName, String rid, String testId, List<String> links) {
        this.testName = testName;
        this.rid = rid;
        this.testId = testId;
        this.links = links;
    }

    public List<String> getLinks() {
        return links;
    }

    public String getTestId() {
        return testId;
    }

    public String getRid() {
        return rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public String getTestName() {
        return testName;
    }
}
