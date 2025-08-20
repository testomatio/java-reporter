package io.testomat.junit.extractor.strategy.handlers;

import io.testomat.junit.extractor.strategy.ParameterExtractionContext;

/**
 * Parameter extraction handler for @NullSource annotated parameterized tests.
 * Handles extraction of null parameter values from @NullSource annotations,
 * which always provide a single null value regardless of parameter type.
 */
public class NullSourceHandler extends AbstractParameterExtractionHandler {

    @Override
    public String getStrategyName() {
        return "NullSourceExtractionStrategy";
    }

    @Override
    protected Object parseDisplayNameValue(String valueStr, ParameterExtractionContext context) {
        if ("null".equals(valueStr) || "NULL".equals(valueStr)
                || "<null>".equals(valueStr) || valueStr.isEmpty()) {
            return null;
        }

        throw new RuntimeException("Display name doesn't match null pattern");
    }

    @Override
    protected Object extractFromAnnotation(ParameterExtractionContext context) {
        return null;
    }
}
