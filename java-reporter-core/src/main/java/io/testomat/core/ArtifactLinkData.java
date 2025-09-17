package io.testomat.core;

import java.util.List;

public class ArtifactLinkData {
    private String rid;
    private String testId;
    private String testName;

    private List<String> links;

    public ArtifactLinkData(String testName, String rid, String testId, List<String> links) {
        this.testName = testName;
        this.rid = rid;
        this.testId = testId;
        this.links = links;
    }

    public List<String> getLinks() {
        return links;
    }

    public void setLinks(List<String> links) {
        this.links = links;
    }

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
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

    public void setTestName(String testName) {
        this.testName = testName;
    }
}
