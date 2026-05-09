package io.testomat.core.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Link {

    private final String label;
    private final String test;

    private Link(String label, String test) {
        this.label = label;
        this.test = test;
    }

    public static Link test(String test) {
        return new Link(null, test);
    }

    public static Link label(String label) {
        return new Link(label, null);
    }

    public String getLabel() { return label; }
    public String getTest() { return test; }
}
