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
import org.junit.jupiter.params.provider.EmptySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmptySourceHandler implements ParameterExtractionHandler {

    private static final Logger logger = LoggerFactory.getLogger(EmptySourceHandler.class);
    private static final Pattern DISPLAY_NAME_PATTERN = Pattern.compile("^\\[\\d+\\]\\s*(.*)$");

    @Override
    public boolean supports(ParameterExtractionContext context) {
        return context.isValid() && context.hasAnnotation(EmptySource.class);
    }

    @Override
    public Object extractParameters(ParameterExtractionContext context) 
            throws ParameterExtractionException {
        if (!supports(context)) {
            return null;
        }

        try {
            EmptySource emptySource = context.getAnnotation(EmptySource.class);
            if (emptySource == null) {
                return null;
            }

            // Try to extract from display name first 
            // (most reliable for getting actual parameter values)
            ParseResult parseResult = extractFromDisplayNameWithResult(context);
            if (parseResult.isSuccessful()) {
                return formatParameter(parseResult.getValue(), context);
            }

            // Fallback: @EmptySource provides empty value based on parameter type
            Object emptyValue = determineEmptyValue(context);
            return formatParameter(emptyValue, context);

        } catch (Exception e) {
            logger.debug("Failed to extract EmptySource parameters for: {}", 
                        context.getDisplayName(), e);
            throw new ParameterExtractionException("Failed to extract EmptySource parameters", e);
        }
    }

    @Override
    public int getPriority() {
        return 5; // Same priority as NullSource - simple source annotations
    }

    @Override
    public String getStrategyName() {
        return "EmptySourceExtractionStrategy";
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

        // JUnit 5 parameterized tests format: "[1] " or "[1] []" or "[1] {}" for @EmptySource
        Matcher matcher = DISPLAY_NAME_PATTERN.matcher(displayName.trim());
        if (matcher.matches()) {
            String valueStr = matcher.group(1).trim();
            
            // Handle various empty representations in display names
            if (valueStr.isEmpty() || "\"\"".equals(valueStr) || "''".equals(valueStr)
                || "[]".equals(valueStr) || "{}".equals(valueStr) 
                || "<empty>".equals(valueStr)) {
                // Determine the appropriate empty value based on parameter type
                Object emptyValue = determineEmptyValue(context);
                return ParseResult.success(emptyValue);
            }
            
            // If display name shows something else, it might be from a different source
            // Return failure to fall back to the guaranteed empty value
            return ParseResult.failure();
        }

        return ParseResult.failure();
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

        // @EmptySource always provides exactly one parameter
        Parameter firstParam = parameters[0];
        Class<?> paramType = firstParam.getType();

        // Handle different types that @EmptySource can provide
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
        
        // @EmptySource always provides exactly one parameter
        // Create parameter map with proper name
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        String parameterName = context.getParameterName(0);
        parameterMap.put(parameterName, value);

        return parameterMap;
    }

}