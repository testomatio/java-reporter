package com.model;

public class TestResult {
    String title;
    String testId;
    String suiteTitle;
    String file;
    String status;
    String message;
    String stack;

    public TestResult(String title, String testId, String suiteTitle, String file, String status, String message, String stack) {
        this.title = title;
        this.testId = testId;
        this.suiteTitle = suiteTitle;
        this.file = file;
        this.status = status;
        this.message = message;
        this.stack = stack;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStack() {
        return stack;
    }

    public void setStack(String stack) {
        this.stack = stack;
    }
}