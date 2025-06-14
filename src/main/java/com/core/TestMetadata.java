package com.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TestMetadata {
    private String title;
    private String testId;
    private String suiteTitle;
    private String file;
}
