package io.testomat.core.facade.methods.artifact.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestFile {

    String path;
    String type;

    public TestFile(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public String getType() {
        return type;
    }
}
