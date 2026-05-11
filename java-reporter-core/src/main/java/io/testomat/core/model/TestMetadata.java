package io.testomat.core.model;

import java.util.List;

/**
 * Basic test identification metadata without execution results.
 * Contains essential information for test discovery and organization.
 * Lighter alternative to {@link TestResult} when only test identity is needed.
 */
public class TestMetadata {
    private String title;
    private String testId;
    private String suiteTitle;
    private String file;
    private List<Link> links;

    /**
     * Creates test metadata with identification information.
     * 
     * @param title human-readable test name
     * @param testId unique test identifier from test management system  
     * @param suiteTitle test suite or class name
     * @param file source file path where test is located
     */
    public TestMetadata(String title, String testId,
                        String suiteTitle, String file, List<Link> links) {
        this.title = title;
        this.testId = testId;
        this.suiteTitle = suiteTitle;
        this.file = file;
        this.links = links;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public String getSuiteTitle() {
        return suiteTitle;
    }

    public void setSuiteTitle(String suiteTitle) {
        this.suiteTitle = suiteTitle;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }
}
