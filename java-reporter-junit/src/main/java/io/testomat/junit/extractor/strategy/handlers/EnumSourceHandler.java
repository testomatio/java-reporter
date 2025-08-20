package io.testomat.junit.extractor.strategy.handlers;

import io.testomat.junit.extractor.strategy.ParameterExtractionContext;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import org.junit.jupiter.params.provider.EnumSource;

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
        EnumSource enumSource = context.getAnnotation(EnumSource.class);
        if (enumSource == null) {
            return null;
        }
        try {
            Class<? extends Enum<?>> enumClass = enumSource.value();
            
            if (enumClass.getName().equals("org.junit.jupiter.params.provider.NullEnum")) {
                enumClass = inferEnumClassFromMethod(context);
            }
            
            if (enumClass == null) {
                return null;
            }

            Enum<?>[] enumConstants = enumClass.getEnumConstants();
            if (enumConstants == null || enumConstants.length == 0) {
                return null;
            }

            String[] names = enumSource.names();
            if (names.length > 0) {
                for (String name : names) {
                    try {
                        return Enum.valueOf((Class) enumClass, name);
                    } catch (IllegalArgumentException e) {

                    }
                }
                return null;
            }

            return enumConstants[0];

        } catch (Exception e) {
            logger.debug("Failed to extract enum from annotation", e);
            return null;
        }
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

        try {
            EnumSource enumSource = context.getAnnotation(EnumSource.class);
            Class<? extends Enum<?>> enumClass = enumSource.value();
            
            if (enumClass.getName().equals("org.junit.jupiter.params.provider.NullEnum")) {
                enumClass = inferEnumClassFromMethod(context);
            }

            if (enumClass != null) {
                String enumName = trimmed;
                if (trimmed.contains(".")) {
                    enumName = trimmed.substring(trimmed.lastIndexOf('.') + 1);
                }

                return Enum.valueOf((Class) enumClass, enumName);
            }
        } catch (Exception e) {
            logger.debug("Failed to parse enum value: {}", trimmed, e);
        }

        return value;
    }
}
