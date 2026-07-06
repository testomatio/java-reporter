package io.testomat.core.facade.methods.artifact.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestItem {

    String rid;
    String test_id;
    List<TestFile> files;
    List<Step> steps;

    public TestItem(String rid, String test_id, List<TestFile> files, List<Step> steps) {
        this.test_id = test_id;
        this.rid = rid;
        this.files = files;
        this.steps = steps;
    }

    public void setFiles(List<TestFile> files) {
        this.files = files;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public String getRid() {
        return rid;
    }

    public String getTest_id() {
        return test_id;
    }

    public List<TestFile> getFiles() {
        return files;
    }

    public List<Step> getSteps() {
        return steps;
    }
}
