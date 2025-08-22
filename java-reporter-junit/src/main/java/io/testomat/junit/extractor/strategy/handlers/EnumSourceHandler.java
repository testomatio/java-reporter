package io.testomat.junit.extractor.strategy.handlers;

import io.testomat.junit.extractor.strategy.ParameterExtractionContext;
import io.testomat.junit.util.ParameterizedTestSupport;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Parameter extraction handler for @EnumSource annotated parameterized tests.
 * Supports extraction of enum values from @EnumSource annotations with various
 * configurations including names filtering, mode selection, and enum constant parsing.
 */
public class EnumSourceHandler extends AbstractParameterExtractionHandler {

    @Override
    public String getStrategyName() {
        return "EnumSourceExtractionStrategy";
    }

    @Override
    protected Object parseDisplayNameValue(String valueStr, ParameterExtractionContext context) {
        return parseEnumValue(valueStr, context);
    }

    @Override
    protected Object extractFromAnnotation(ParameterExtractionContext context) {
        if (!ParameterizedTestSupport.isAvailable()) {
            return null;
        }

        return ParameterizedTestSupport.loadAnnotationClass("EnumSource")
                .map(enumSourceClass -> {
                    Annotation enumSource = context.getAnnotation(enumSourceClass);
                    if (enumSource == null) {
                        return null;
                    }
                    return extractValueFromEnumSource(enumSource, context);
                })
                .orElse(null);
    }

    private Object extractValueFromEnumSource(Annotation enumSource,
                                              ParameterExtractionContext context) {
        try {
            Class<?> annotationClass = enumSource.annotationType();

            java.lang.reflect.Method valueMethod = annotationClass.getMethod("value");
            Class<? extends Enum<?>> enumClass =
                    (Class<? extends Enum<?>>) valueMethod.invoke(enumSource);

            if (isNullEnum(enumClass)) {
                enumClass = inferEnumClassFromMethod(context);
            }

            if (enumClass == null) {
                return null;
            }

            Enum<?>[] enumConstants = enumClass.getEnumConstants();
            if (enumConstants == null || enumConstants.length == 0) {
                return null;
            }

            java.lang.reflect.Method namesMethod = annotationClass.getMethod("names");
            String[] names = (String[]) namesMethod.invoke(enumSource);

            if (names.length > 0) {
                return findFirstValidEnumConstant(enumClass, names);
            }

            return enumConstants[0];

        } catch (Exception e) {
            logger.debug("Failed to extract enum from annotation", e);
            return null;
        }
    }

    private boolean isNullEnum(Class<? extends Enum<?>> enumClass) {
        return enumClass.getName().equals("org.junit.jupiter.params.provider.NullEnum");
    }

    private Object findFirstValidEnumConstant(Class<? extends Enum<?>> enumClass,
                                             String[] names) {
        for (String name : names) {
            try {
                return Enum.valueOf((Class) enumClass, name);
            } catch (IllegalArgumentException e) {
                logger.warn("Could not find enum constant {}", name);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Enum<?>> inferEnumClassFromMethod(ParameterExtractionContext context) {
        Method method = context.getTestMethod();
        if (method == null) {
            return null;
        }

        Parameter[] parameters = method.getParameters();
        if (parameters.length == 0) {
            return null;
        }

        Class<?> paramType = parameters[0].getType();
        if (paramType.isEnum()) {
            return (Class<? extends Enum<?>>) paramType;
        }

        return null;
    }

    private Object parseEnumValue(String value, ParameterExtractionContext context) {
        if (value == null || value.trim().isEmpty()) {
            return value;
        }

        String trimmed = value.trim();

        if ("null".equals(trimmed)) {
            return null;
        }

        if (!ParameterizedTestSupport.isAvailable()) {
            return value;
        }

        try {
            return ParameterizedTestSupport.loadAnnotationClass("EnumSource")
                    .map(enumSourceClass -> {
                        Annotation enumSource = context.getAnnotation(enumSourceClass);
                        if (enumSource == null) {
                            return value;
                        }
                        return parseEnumWithReflection(enumSource, trimmed, context);
                    })
                    .orElse(value);
        } catch (Exception e) {
            logger.debug("Failed to parse enum value: {}", trimmed, e);
            return value;
        }
    }

    private Object parseEnumWithReflection(Annotation enumSource, String trimmed,
                                           ParameterExtractionContext context) {
        try {
            Method valueMethod = enumSource.annotationType().getMethod("value");
            Class<? extends Enum<?>> enumClass =
                    (Class<? extends Enum<?>>) valueMethod.invoke(enumSource);

            if (isNullEnum(enumClass)) {
                enumClass = inferEnumClassFromMethod(context);
            }

            if (enumClass != null) {
                String enumName = extractEnumName(trimmed);
                return Enum.valueOf((Class) enumClass, enumName);
            }
        } catch (Exception e) {
            logger.debug("Failed to parse enum value with reflection: {}", trimmed, e);
        }

        return trimmed;
    }

    private String extractEnumName(String trimmed) {
        if (trimmed.contains(".")) {
            return trimmed.substring(trimmed.lastIndexOf('.') + 1);
        }
        return trimmed;
    }
}
