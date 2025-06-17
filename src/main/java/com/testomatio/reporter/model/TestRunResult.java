package com.testomatio.reporter.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TestRunResult {
    private String title;
    private String testId;
    private String suiteTitle;
    private String file;
    private String status;
    private String message;
    private String stack;
}