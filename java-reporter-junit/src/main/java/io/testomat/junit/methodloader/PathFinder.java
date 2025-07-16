package io.testomat.junit.methodloader;

import org.junit.jupiter.api.extension.ExtensionContext;

public class PathFinder {

    public String getPath(ExtensionContext context) {
        Class<?> testClass = context.getTestClass().orElseThrow(
                () -> new ParsingException("No test class found"));

        return testClass.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getPath();
    }
}
