package com.testomatio.reporter.model;

public class TestMetadata {
    private String title;
    private String testId;
    private String suiteTitle;
    private String file;

    public TestMetadata(String title, String testId,
                        String suiteTitle, String file) {
        this.title = title;
        this.testId = testId;
        this.suiteTitle = suiteTitle;
        this.file = file;
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
}
