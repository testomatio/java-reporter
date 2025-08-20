package io.testomat.junit.extractor.strategy.handlers;

import io.testomat.junit.extractor.strategy.ParameterExtractionContext;
import io.testomat.junit.exception.ParameterExtractionException;
import io.testomat.junit.extractor.strategy.ParameterExtractionHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Strategy for extracting parameters from @EnumSource annotated parameterized tests.
 * Supports extraction of enum values from @EnumSource annotations with various configurations
 * including names filtering, mode selection, and enum constant parsing.
 */
public class EnumSourceHandler implements ParameterExtractionHandler {

    private static final Logger logger = LoggerFactory.getLogger(EnumSourceHandler.class);
    private static final Pattern DISPLAY_NAME_PATTERN = Pattern.compile("^\\[\\d+\\]\\s*(.*)$");

    @Override
    public boolean supports(ParameterExtractionContext context) {
        return context.isValid() && context.hasAnnotation(EnumSource.class);
    }

    @Override
    public Object extractParameters(ParameterExtractionContext context) throws ParameterExtractionException {
        if (!supports(context)) {
            return null;
        }

        try {
            EnumSource enumSource = context.getAnnotation(EnumSource.class);
            if (enumSource == null) {
                return null;
            }

            // Try to extract from display name first (most reliable for getting actual parameter value)
            ParseResult parseResult = extractFromDisplayNameWithResult(context);
            if (parseResult.isSuccessful()) {
                return formatParameter(parseResult.getValue(), context);
            }

            // Fallback: extract from annotation (won't give us the specific value for this invocation)
            Object annotationValue = extractFromAnnotation(enumSource, context);
            return formatParameter(annotationValue, context);

        } catch (Exception e) {
            logger.debug("Failed to extract EnumSource parameters for: {}", context.getDisplayName(), e);
            throw new ParameterExtractionException("Failed to extract EnumSource parameters", e);
        }
    }

    @Override
    public int getPriority() {
        return 10; // Standard priority for simple source annotations
    }

    @Override
    public String getStrategyName() {
        return "EnumSourceExtractionStrategy";
    }

    private static class ParseResult {
        private final boolean successful;
        private final Object value;

        private ParseResult(boolean successful, Object value) {
            this.successful = successful;
            this.value = value;
        }

        public static ParseResult success(Object value) {
            return new ParseResult(true, value);
        }

        public static ParseResult failure() {
            return new ParseResult(false, null);
        }

        public boolean isSuccessful() {
            return successful;
        }

        public Object getValue() {
            return value;
        }
    }

    private ParseResult extractFromDisplayNameWithResult(ParameterExtractionContext context) {
        String displayName = context.getDisplayName();
        if (displayName == null || displayName.trim().isEmpty()) {
            return ParseResult.failure();
        }

        // JUnit 5 parameterized tests format: "[1] ENUM_VALUE" or "[2] MyEnum.VALUE" etc.
        Matcher matcher = DISPLAY_NAME_PATTERN.matcher(displayName.trim());
        if (matcher.matches()) {
            String valueStr = matcher.group(1).trim();
            Object parsedValue = parseEnumValue(valueStr, context);
            return ParseResult.success(parsedValue);
        }

        return ParseResult.failure();
    }

    private Object extractFromAnnotation(EnumSource enumSource, ParameterExtractionContext context) {
        try {
            Class<? extends Enum<?>> enumClass = enumSource.value();
            
            // If no specific enum class is specified (default is NullEnum.class), try to infer from method parameter
            if (enumClass.getName().equals("org.junit.jupiter.params.provider.NullEnum")) {
                enumClass = inferEnumClassFromMethod(context);
            }
            
            if (enumClass == null) {
                return null;
            }

            // Get enum constants
            Enum<?>[] enumConstants = enumClass.getEnumConstants();
            if (enumConstants == null || enumConstants.length == 0) {
                return null;
            }

            // Apply names filter if specified
            String[] names = enumSource.names();
            if (names.length > 0) {
                for (String name : names) {
                    try {
                        return Enum.valueOf((Class) enumClass, name);
                    } catch (IllegalArgumentException e) {
                        // Continue to next name
                    }
                }
                return null;
            }

            // Return first enum constant as fallback
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

        // Check if first parameter is an enum
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

        // Handle null string
        if ("null".equals(trimmed)) {
            return null;
        }

        // Try to parse as enum constant name
        try {
            // Get enum class from annotation or method parameter
            EnumSource enumSource = context.getAnnotation(EnumSource.class);
            Class<? extends Enum<?>> enumClass = enumSource.value();
            
            if (enumClass.getName().equals("org.junit.jupiter.params.provider.NullEnum")) {
                enumClass = inferEnumClassFromMethod(context);
            }

            if (enumClass != null) {
                // Handle display names like "MyEnum.VALUE" or just "VALUE"
                String enumName = trimmed;
                if (trimmed.contains(".")) {
                    // Extract just the enum constant name after the dot
                    enumName = trimmed.substring(trimmed.lastIndexOf('.') + 1);
                }

                return Enum.valueOf((Class) enumClass, enumName);
            }
        } catch (Exception e) {
            logger.debug("Failed to parse enum value: {}", trimmed, e);
        }

        // Return as string if enum parsing fails
        return value;
    }

    private Object formatParameter(Object value, ParameterExtractionContext context) {
        Method method = context.getTestMethod();
        if (method == null) {
            return value;
        }

        Parameter[] parameters = method.getParameters();
        if (parameters.length == 0) {
            return value;
        }

        // Always create a parameter map for consistent output format (even for null values)
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        String parameterName = context.getParameterName(0);
        parameterMap.put(parameterName, value);

        return parameterMap;
    }

}