package io.testomat.junit.extractor.strategy.handlers;

import io.testomat.junit.extractor.strategy.ParameterExtractionContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import org.junit.jupiter.params.provider.EmptySource;

/**
 * Parameter extraction handler for @EmptySource annotated parameterized tests.
 * Handles extraction of empty parameter values from @EmptySource annotations,
 * providing appropriate empty values based on the parameter type (empty string,
 * empty array, empty collection, etc.).
 */
public class EmptySourceHandler extends AbstractParameterExtractionHandler {


    @Override
    public int getPriority() {
        return 5;
    }

    @Override
    public String getStrategyName() {
        return "EmptySourceExtractionStrategy";
    }

    @Override
    protected Class<? extends Annotation> getSupportedAnnotationType() {
        return EmptySource.class;
    }

    @Override
    protected Object parseDisplayNameValue(String valueStr, ParameterExtractionContext context) {
        if (valueStr.isEmpty() || "\"\"".equals(valueStr) || "''".equals(valueStr)
            || "[]".equals(valueStr) || "{}".equals(valueStr) 
            || "<empty>".equals(valueStr)) {
            return determineEmptyValue(context);
        }
        
        throw new RuntimeException("Display name doesn't match empty pattern");
    }

    private Object determineEmptyValue(ParameterExtractionContext context) {
        Method method = context.getTestMethod();
        if (method == null) {
            return "";
        }

        Parameter[] parameters = method.getParameters();
        if (parameters.length == 0) {
            return "";
        }

        Parameter firstParam = parameters[0];
        Class<?> paramType = firstParam.getType();

        if (String.class.isAssignableFrom(paramType)) {
            return "";
        } else if (paramType.isArray()) {
            return "[]";
        } else if (java.util.Collection.class.isAssignableFrom(paramType)) {
            return "[]";
        } else if (java.util.Map.class.isAssignableFrom(paramType)) {
            return "{}";
        } else {
            return "";
        }
    }

    @Override
    protected Object extractFromAnnotation(ParameterExtractionContext context) {
        return determineEmptyValue(context);
    }
}
