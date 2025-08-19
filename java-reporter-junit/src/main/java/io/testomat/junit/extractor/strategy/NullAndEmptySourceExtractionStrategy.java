package io.testomat.junit.extractor.strategy;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NullAndEmptySourceExtractionStrategy implements ParameterExtractionStrategy {

    private static final Logger logger = LoggerFactory.getLogger(NullAndEmptySourceExtractionStrategy.class);
    private static final Pattern DISPLAY_NAME_PATTERN = Pattern.compile("^\\[\\d+\\]\\s*(.*)$");

    @Override
    public boolean supports(ParameterExtractionContext context) {
        return context.isValid() && context.hasAnnotation(NullAndEmptySource.class);
    }

    @Override
    public Object extractParameters(ParameterExtractionContext context) 
            throws ParameterExtractionException {
        if (!supports(context)) {
            return null;
        }

        try {
            NullAndEmptySource nullAndEmptySource = context.getAnnotation(NullAndEmptySource.class);
            if (nullAndEmptySource == null) {
                return null;
            }

            // Try to extract from display name first 
            // (most reliable for getting actual parameter values)
            ParseResult parseResult = extractFromDisplayNameWithResult(context);
            if (parseResult.isSuccessful()) {
                return formatParameter(parseResult.getValue(), context);
            }

            // Fallback: @NullAndEmptySource provides both null and empty values
            // Since we can't determine which invocation this is from context alone,
            // we need to determine based on parameter type (default to null)
            Object fallbackValue = determineFallbackValue(context);
            return formatParameter(fallbackValue, context);

        } catch (Exception e) {
            logger.debug("Failed to extract NullAndEmptySource parameters for: {}", 
                        context.getDisplayName(), e);
            throw new ParameterExtractionException("Failed to extract NullAndEmptySource parameters", e);
        }
    }

    @Override
    public int getPriority() {
        return 4; // Higher priority than individual NullSource/EmptySource since it's more specific
    }

    @Override
    public String getStrategyName() {
        return "NullAndEmptySourceExtractionStrategy";
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

        // JUnit 5 parameterized tests format: "[1] null", "[2] """, "[3] []", "[4] {}" for @NullAndEmptySource
        Matcher matcher = DISPLAY_NAME_PATTERN.matcher(displayName.trim());
        if (matcher.matches()) {
            String valueStr = matcher.group(1).trim();
            
            // Handle null representations
            if ("null".equals(valueStr) || "NULL".equals(valueStr) 
                || "<null>".equals(valueStr)) {
                return ParseResult.success(null);
            }
            
            // Handle empty representations in display names
            if (valueStr.isEmpty() || "\"\"".equals(valueStr) || "''".equals(valueStr)
                || "[]".equals(valueStr) || "{}".equals(valueStr) 
                || "<empty>".equals(valueStr)) {
                // Determine the appropriate empty value based on parameter type
                Object emptyValue = determineEmptyValue(context);
                return ParseResult.success(emptyValue);
            }
            
            // If display name shows something else, it might be from a different source
            // Return failure to fall back to the guaranteed values
            return ParseResult.failure();
        }

        return ParseResult.failure();
    }

    private Object determineFallbackValue(ParameterExtractionContext context) {
        // For @NullAndEmptySource, when we can't determine from display name,
        // default to null (which is always the first value provided)
        return null;
    }

    private Object determineEmptyValue(ParameterExtractionContext context) {
        Method method = context.getTestMethod();
        if (method == null) {
            // Default to empty string if no method info
            return "";
        }

        Parameter[] parameters = method.getParameters();
        if (parameters.length == 0) {
            // Default to empty string if no parameters
            return "";
        }

        // @NullAndEmptySource always provides exactly one parameter per invocation
        Parameter firstParam = parameters[0];
        Class<?> paramType = firstParam.getType();

        // Handle different types that @EmptySource part of @NullAndEmptySource can provide
        if (String.class.isAssignableFrom(paramType)) {
            return "";
        } else if (paramType.isArray()) {
            // Return empty array representation as string (JUnit display format)
            return "[]";
        } else if (java.util.Collection.class.isAssignableFrom(paramType)) {
            // Return empty collection representation as string (JUnit display format)
            return "[]";
        } else if (java.util.Map.class.isAssignableFrom(paramType)) {
            // Return empty map representation as string (JUnit display format)
            return "{}";
        } else {
            // For other types, default to empty string representation
            return "";
        }
    }

    private Object formatParameter(Object value, ParameterExtractionContext context) {
        Method method = context.getTestMethod();
        if (method == null) {
            // If no method info, create generic parameter map
            Map<String, Object> parameterMap = new LinkedHashMap<>();
            parameterMap.put("param0", value);
            return parameterMap;
        }

        Parameter[] parameters = method.getParameters();
        
        // @NullAndEmptySource always provides exactly one parameter per invocation
        // Create parameter map with proper name
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        String parameterName = getParameterName(parameters, 0);
        parameterMap.put(parameterName, value);

        return parameterMap;
    }

    private String getParameterName(Parameter[] parameters, int index) {
        if (index < parameters.length) {
            Parameter param = parameters[index];
            if (param.isNamePresent() && !param.getName().matches("arg\\d+")) {
                return param.getName();
            }
        }
        return "param" + index;
    }
}