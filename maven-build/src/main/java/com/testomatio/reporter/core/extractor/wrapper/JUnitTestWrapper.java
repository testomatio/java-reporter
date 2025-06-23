package com.testomatio.reporter.core.extractor.wrapper;

import java.lang.reflect.Method;
import org.junit.jupiter.api.extension.ExtensionContext;

public class JUnitTestWrapper {
    private final Method testMethod;
    private final ExtensionContext extensionContext;

    public JUnitTestWrapper(Method testMethod, ExtensionContext extensionContext) {
        this.testMethod = testMethod;
        this.extensionContext = extensionContext;
    }

    public Method getTestMethod() {
        return testMethod;
    }

    public ExtensionContext getExtensionContext() {
        return extensionContext;
    }
}