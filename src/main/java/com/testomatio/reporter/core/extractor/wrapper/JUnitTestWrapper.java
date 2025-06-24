package com.testomatio.reporter.core.extractor.wrapper;

import java.lang.reflect.Method;
import lombok.Getter;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Wrapper for JUnit 5 test method and extension context.
 */
@Getter
public class JUnitTestWrapper {
    private final Method testMethod;
    private final ExtensionContext extensionContext;

    public JUnitTestWrapper(Method testMethod, ExtensionContext extensionContext) {
        this.testMethod = testMethod;
        this.extensionContext = extensionContext;
    }
}