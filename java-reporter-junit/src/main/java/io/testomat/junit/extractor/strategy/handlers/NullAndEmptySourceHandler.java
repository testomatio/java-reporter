package io.testomat.junit.extractor.strategy.handlers;

import io.testomat.junit.extractor.strategy.ParameterExtractionContext;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Parameter extraction handler for @NullAndEmptySource annotated parameterized tests.
 * Handles extraction of both null and empty parameter values from @NullAndEmptySource annotations,
 * providing appropriate null/empty values based on the parameter type.
 */
public class NullAndEmptySourceHandler extends AbstractParameterExtractionHandler {

    @Override
    public String getStrategyName() {
        return "NullAndEmptySourceExtractionStrategy";
    }

    @Override
    protected Object parseDisplayNameValue(String valueStr, ParameterExtractionContext context) {
        if ("null".equals(valueStr) || "NULL".equals(valueStr) || "<null>".equals(valueStr)) {
            return null;
        }

        if (valueStr.isEmpty() || "\"\"".equals(valueStr) || "''".equals(valueStr)
                || "[]".equals(valueStr) || "{}".equals(valueStr)
                || "<empty>".equals(valueStr)) {
            return determineEmptyValue(context);
        }

        throw new RuntimeException("Display name doesn't match null or empty pattern");
    }

    @Override
    protected Object extractFromAnnotation(ParameterExtractionContext context) {
        return null;
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
}
