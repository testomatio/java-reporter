package io.testomat.junit.extractor.strategy;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.params.provider.NullSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NullSourceExtractionStrategy implements ParameterExtractionStrategy {

    private static final Logger logger = LoggerFactory.getLogger(NullSourceExtractionStrategy.class);
    private static final Pattern DISPLAY_NAME_PATTERN = Pattern.compile("^\\[\\d+\\]\\s*(.*)$");

    @Override
    public boolean supports(ParameterExtractionContext context) {
        return context.isValid() && context.hasAnnotation(NullSource.class);
    }

    @Override
    public Object extractParameters(ParameterExtractionContext context) 
            throws ParameterExtractionException {
        if (!supports(context)) {
            return null;
        }

        try {
            NullSource nullSource = context.getAnnotation(NullSource.class);
            if (nullSource == null) {
                return null;
            }

            // Try to extract from display name first 
            // (most reliable for getting actual parameter values)
            ParseResult parseResult = extractFromDisplayNameWithResult(context);
            if (parseResult.isSuccessful()) {
                return formatParameter(parseResult.getValue(), context);
            }

            // Fallback: @NullSource always provides a single null value
            return formatParameter(null, context);

        } catch (Exception e) {
            logger.debug("Failed to extract NullSource parameters for: {}", 
                        context.getDisplayName(), e);
            throw new ParameterExtractionException("Failed to extract NullSource parameters", e);
        }
    }

    @Override
    public int getPriority() {
        return 5; // Lower priority than complex sources, higher than basic sources
    }

    @Override
    public String getStrategyName() {
        return "NullSourceExtractionStrategy";
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

        // JUnit 5 parameterized tests format: "[1] null" for @NullSource
        Matcher matcher = DISPLAY_NAME_PATTERN.matcher(displayName.trim());
        if (matcher.matches()) {
            String valueStr = matcher.group(1).trim();
            
            // Handle various null representations in display names
            if ("null".equals(valueStr) || "NULL".equals(valueStr) 
                || "<null>".equals(valueStr) || valueStr.isEmpty()) {
                return ParseResult.success(null);
            }
            
            // If display name shows something else, it might be from a different source
            // Return failure to fall back to the guaranteed null value
            return ParseResult.failure();
        }

        return ParseResult.failure();
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
        
        // @NullSource always provides exactly one parameter
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