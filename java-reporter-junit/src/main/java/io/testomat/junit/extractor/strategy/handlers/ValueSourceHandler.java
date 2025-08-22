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

            Object result = tryExtractArray(valueSource, annotationClass, "strings");
            if (result != null) {
                return result;
            }

            result = tryExtractArray(valueSource, annotationClass, "ints");
            if (result != null) {
                return result;
            }

            result = tryExtractArray(valueSource, annotationClass, "longs");
            if (result != null) {
                return result;
            }

            result = tryExtractArray(valueSource, annotationClass, "doubles");
            if (result != null) {
                return result;
            }

            result = tryExtractArray(valueSource, annotationClass, "floats");
            if (result != null) {
                return result;
            }

            result = tryExtractArray(valueSource, annotationClass, "bytes");
            if (result != null) {
                return result;
            }

            result = tryExtractArray(valueSource, annotationClass, "shorts");
            if (result != null) {
                return result;
            }

            result = tryExtractArray(valueSource, annotationClass, "booleans");
            if (result != null) {
                return result;
            }

            result = tryExtractArray(valueSource, annotationClass, "chars");
            if (result != null) {
                return result;
            }

            result = tryExtractArray(valueSource, annotationClass, "classes");
            if (result != null) {
                return result;
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
