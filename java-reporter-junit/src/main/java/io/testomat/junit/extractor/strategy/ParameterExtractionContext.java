package io.testomat.junit.extractor.strategy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Context object containing all information needed for parameter extraction
 * from parameterized tests.
 * This immutable value object is passed to parameter extraction strategies.
 * Includes centralized parameter name resolution using source code parsing.
 */
public class ParameterExtractionContext {

    private final ExtensionContext extensionContext;
    private final Method testMethod;
    private final Annotation[] annotations;
    private final String displayName;
    private final String uniqueId;
    private final SourceCodeParameterNameResolver parameterNameResolver;

    public ParameterExtractionContext(ExtensionContext extensionContext) {
        this(extensionContext, SourceCodeParameterNameResolver.getInstance());
    }

    /**
     * Constructor for testing with custom parameter name resolver.
     */
    ParameterExtractionContext(ExtensionContext extensionContext,
                               SourceCodeParameterNameResolver parameterNameResolver) {
        this.extensionContext = extensionContext;
        this.testMethod = extensionContext.getTestMethod().orElse(null);
        this.annotations = testMethod != null ? testMethod.getAnnotations() : new Annotation[0];
        this.displayName = extensionContext.getDisplayName();
        this.uniqueId = extensionContext.getUniqueId();
        this.parameterNameResolver = parameterNameResolver;
    }

    /**
     * Gets the JUnit extension context.
     */
    public ExtensionContext getExtensionContext() {
        return extensionContext;
    }

    /**
     * Gets the test method being executed.
     */
    public Method getTestMethod() {
        return testMethod;
    }

    /**
     * Gets all annotations present on the test method.
     */
    public Annotation[] getAnnotations() {
        return annotations.clone();
    }

    /**
     * Gets the display name of the test execution.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the unique identifier for this test execution.
     */
    public String getUniqueId() {
        return uniqueId;
    }

    /**
     * Checks if the test method has a specific annotation.
     */
    public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
        if (testMethod == null) {
            return false;
        }
        return testMethod.isAnnotationPresent(annotationType);
    }

    /**
     * Gets a specific annotation from the test method.
     */
    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        if (testMethod == null) {
            return null;
        }
        return testMethod.getAnnotation(annotationType);
    }

    /**
     * Checks if this context represents a valid parameterized test.
     */
    public boolean isValid() {
        return testMethod != null && extensionContext != null;
    }

    /**
     * Gets a specific parameter name by index.
     *
     * @param parameterIndex the index of the parameter (0-based)
     * @return the parameter name, or "param{index}" as fallback
     */
    public String getParameterName(int parameterIndex) {
        if (testMethod == null) {
            return "param" + parameterIndex;
        }
        return parameterNameResolver.getParameterName(testMethod, parameterIndex);
    }
}
