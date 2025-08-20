package io.testomat.junit.extractor.strategy.handlers;

import io.testomat.junit.extractor.strategy.ParameterExtractionContext;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Parameter extraction handler for @ValueSource annotated parameterized tests.
 * Supports extraction of various value types including strings, numeric types,
 * booleans, characters, and classes from @ValueSource annotations.

 */
public class ValueSourceHandler extends AbstractParameterExtractionHandler {



    @Override
    public String getStrategyName() {
        return "ValueSourceExtractionStrategy";
    }


    @Override
    protected Object parseDisplayNameValue(String valueStr, ParameterExtractionContext context) {
        return parseValue(valueStr);
    }

    @Override
    protected Object extractFromAnnotation(ParameterExtractionContext context) {
        ValueSource valueSource = context.getAnnotation(ValueSource.class);
        if (valueSource == null) {
            return null;
        }

        if (valueSource.strings().length > 0) {
            return valueSource.strings()[0];
        }
        if (valueSource.ints().length > 0) {
            return valueSource.ints()[0];
        }
        if (valueSource.longs().length > 0) {
            return valueSource.longs()[0];
        }
        if (valueSource.doubles().length > 0) {
            return valueSource.doubles()[0];
        }
        if (valueSource.floats().length > 0) {
            return valueSource.floats()[0];
        }
        if (valueSource.bytes().length > 0) {
            return valueSource.bytes()[0];
        }
        if (valueSource.shorts().length > 0) {
            return valueSource.shorts()[0];
        }
        if (valueSource.booleans().length > 0) {
            return valueSource.booleans()[0];
        }
        if (valueSource.chars().length > 0) {
            return valueSource.chars()[0];
        }
        if (valueSource.classes().length > 0) {
            return valueSource.classes()[0];
        }

        return null;
    }

    private Object parseValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return value;
        }

        String trimmed = value.trim();

        if (trimmed.length() == 1) {
            return trimmed.charAt(0);
        }

        return parseTypedValue(value);
    }

}