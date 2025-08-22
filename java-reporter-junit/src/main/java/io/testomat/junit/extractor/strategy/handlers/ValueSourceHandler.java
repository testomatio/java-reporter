package io.testomat.junit.extractor.strategy.handlers;

import io.testomat.junit.extractor.strategy.ParameterExtractionContext;
import io.testomat.junit.util.ParameterizedTestSupport;
import java.lang.annotation.Annotation;

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
        if (!ParameterizedTestSupport.isAvailable()) {
            return null;
        }

        return ParameterizedTestSupport.loadAnnotationClass("ValueSource")
            .map(valueSourceClass -> {
                Annotation valueSource = context.getAnnotation(valueSourceClass);
                if (valueSource == null) {
                    return null;
                }
                return extractValueFromValueSource(valueSource);
            })
            .orElse(null);
    }

    private Object extractValueFromValueSource(Annotation valueSource) {
        try {
            Class<?> annotationClass = valueSource.annotationType();

            String[] methodNames = {"strings", "ints", "longs", "doubles",
                    "floats", "bytes", "shorts", "booleans", "chars", "classes"};
            
            for (String methodName : methodNames) {
                Object result = tryExtractArray(valueSource, annotationClass, methodName);
                if (result != null) {
                    return result;
                }
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private Object tryExtractArray(Annotation annotation, Class<?> annotationClass,
                                   String methodName) {
        try {
            java.lang.reflect.Method method = annotationClass.getMethod(methodName);
            Object array = method.invoke(annotation);

            if (array != null) {
                int length = java.lang.reflect.Array.getLength(array);
                if (length > 0) {
                    return java.lang.reflect.Array.get(array, 0);
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
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
