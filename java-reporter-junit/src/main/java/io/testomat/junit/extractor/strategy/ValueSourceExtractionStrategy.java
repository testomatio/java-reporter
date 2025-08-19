package io.testomat.junit.extractor.strategy;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Strategy for extracting parameters from @ValueSource annotated parameterized tests.
 * Supports extraction of strings, ints, longs, doubles, floats, bytes, shorts, booleans, 
 * chars, and classes from @ValueSource annotations.
 */
public class ValueSourceExtractionStrategy implements ParameterExtractionStrategy {

    private static final Logger logger = LoggerFactory.getLogger(ValueSourceExtractionStrategy.class);
    private static final Pattern DISPLAY_NAME_PATTERN = Pattern.compile("^\\[\\d+\\]\\s*(.*)$");

    @Override
    public boolean supports(ParameterExtractionContext context) {
        return context.isValid() && context.hasAnnotation(ValueSource.class);
    }

    @Override
    public Object extractParameters(ParameterExtractionContext context) throws ParameterExtractionException {
        if (!supports(context)) {
            return null;
        }

        try {
            ValueSource valueSource = context.getAnnotation(ValueSource.class);
            if (valueSource == null) {
                return null;
            }

            // Try to extract from display name first (most reliable for getting actual parameter value)
            ParseResult parseResult = extractFromDisplayNameWithResult(context);
            if (parseResult.isSuccessful()) {
                return formatParameter(parseResult.getValue(), context);
            }

            // Fallback: extract from annotation (won't give us the specific value for this invocation)
            Object annotationValue = extractFromAnnotation(valueSource, context);
            return formatParameter(annotationValue, context);

        } catch (Exception e) {
            logger.debug("Failed to extract ValueSource parameters for: {}", context.getDisplayName(), e);
            throw new ParameterExtractionException("Failed to extract ValueSource parameters", e);
        }
    }

    @Override
    public int getPriority() {
        return 10; // Standard priority for simple source annotations
    }

    @Override
    public String getStrategyName() {
        return "ValueSourceExtractionStrategy";
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

        // JUnit 5 parameterized tests format: "[1] value" or "[2] 42" etc.
        Matcher matcher = DISPLAY_NAME_PATTERN.matcher(displayName.trim());
        if (matcher.matches()) {
            String valueStr = matcher.group(1).trim();
            Object parsedValue = parseValue(valueStr);
            return ParseResult.success(parsedValue);
        }

        return ParseResult.failure();
    }

    private Object extractFromDisplayName(ParameterExtractionContext context) {
        String displayName = context.getDisplayName();
        if (displayName == null || displayName.trim().isEmpty()) {
            return null;
        }

        // JUnit 5 parameterized tests format: "[1] value" or "[2] 42" etc.
        Matcher matcher = DISPLAY_NAME_PATTERN.matcher(displayName.trim());
        if (matcher.matches()) {
            String valueStr = matcher.group(1).trim();
            return parseValue(valueStr);
        }

        return null;
    }

    private Object extractFromAnnotation(ValueSource valueSource, ParameterExtractionContext context) {
        // Return the first value from the annotation as a fallback
        // Note: This won't give us the specific value for this test invocation,
        // but provides a representative value from the source

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

        // Handle null string
        if ("null".equals(trimmed)) {
            return null;
        }

        // Try parsing as boolean
        if ("true".equalsIgnoreCase(trimmed)) {
            return true;
        }
        if ("false".equalsIgnoreCase(trimmed)) {
            return false;
        }

        // Try parsing as number
        try {
            if (trimmed.contains(".")) {
                return Double.parseDouble(trimmed);
            } else {
                long longVal = Long.parseLong(trimmed);
                // Return as int if it fits, otherwise as long
                if (longVal >= Integer.MIN_VALUE && longVal <= Integer.MAX_VALUE) {
                    return (int) longVal;
                }
                return longVal;
            }
        } catch (NumberFormatException e) {
            // Not a number, continue
        }

        // Try parsing as char (single character)
        if (trimmed.length() == 1) {
            return trimmed.charAt(0);
        }

        // Return as string
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


    private boolean isSimpleType(Object value) {
        if (value == null) {
            return true;
        }

        Class<?> type = value.getClass();
        return type.isPrimitive() ||
               Number.class.isAssignableFrom(type) ||
               String.class.equals(type) ||
               Boolean.class.equals(type) ||
               Character.class.equals(type);
    }
}