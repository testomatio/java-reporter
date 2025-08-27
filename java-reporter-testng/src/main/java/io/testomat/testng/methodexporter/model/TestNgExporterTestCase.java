package io.testomat.testng.methodexporter.model;

import java.util.List;

/**
 * Represents a test case to be exported to Testomat.io.
 * Contains test metadata, source code, and associated labels.
 */
public class TestNgExporterTestCase {
    private String name;
    private List<String> suites;
    private String code;
    private String file;
    private boolean skipped;
    private List<String> labels;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getSuites() {
        return suites;
    }

    public void setSuites(List<String> suites) {
        this.suites = suites;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public boolean isSkipped() {
        return skipped;
    }

    public void setSkipped(boolean skipped) {
        this.skipped = skipped;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }
}
