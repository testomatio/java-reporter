package io.testomat.core.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Link {

    private final String label;
    private final String jira;
    private final String test;

    private Link(String label, String jira, String test) {
        this.label = label;
        this.jira = jira;
        this.test = test;
    }

    public static Link test(String test) {
        return new Link(null, null, test);
    }

    public static Link label(String label) {
        return new Link(label, null, null);
    }

    public static Link jira(String jira) {
        return new Link(null, jira, null);
    }

    public String getLabel() {
        return label;
    }

    public String getJira() {
        return jira;
    }

    public String getTest() {
        return test;
    }
}
