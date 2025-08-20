package io.testomat.junit.extractor.strategy.handlers;

import io.testomat.junit.exception.ParameterExtractionException;
import io.testomat.junit.extractor.strategy.ParameterExtractionContext;
import io.testomat.junit.extractor.strategy.ParameterExtractionHandler;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for parameter extraction handlers that provides common functionality
 * and eliminates code duplication across different annotation-specific handlers.
 * This class implements the Template Method pattern, providing a standardized flow for
 * parameter extraction while allowing concrete implementations to define annotation-specific
 * parsing logic.
 */
public abstract class AbstractParameterExtractionHandler implements ParameterExtractionHandler {

    private static final Pattern DISPLAY_NAME_PATTERN = Pattern.compile("^\\[\\d+\\]\\s*(.*)$");
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected static class ParseResult {
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

    @Override
    public final Object extractParameters(ParameterExtractionContext context) throws ParameterExtractionException {
        if (!supports(context)) {
            return null;
        }

        try {
            ParseResult parseResult = extractFromDisplayNameWithResult(context);
            if (parseResult.isSuccessful()) {
                return formatParameters(parseResult.getValue(), context);
            }

            Object annotationValue = extractFromAnnotation(context);
            return formatParameters(annotationValue, context);

        } catch (Exception e) {
            logger.debug("Failed to extract {} parameters for: {}", 
                        getStrategyName(), context.getDisplayName(), e);
            throw new ParameterExtractionException("Failed to extract " 
                                                 + getStrategyName() + " parameters", e);
        }
    }

    @Override
    public boolean supports(ParameterExtractionContext context) {
        return context.isValid() && context.hasAnnotation(getSupportedAnnotationType());
    }

    /**
     * Extracts parameter values from the test display name using regex pattern matching.
     *
     * @param context the parameter extraction context
     * @return ParseResult containing the extracted values or failure indicator
     */
    protected ParseResult extractFromDisplayNameWithResult(ParameterExtractionContext context) {
        String displayName = context.getDisplayName();
        if (displayName == null || displayName.trim().isEmpty()) {
            return ParseResult.failure();
        }

        Matcher matcher = DISPLAY_NAME_PATTERN.matcher(displayName.trim());
        if (matcher.matches()) {
            String valueStr = matcher.group(1).trim();
            try {
                Object parsedValue = parseDisplayNameValue(valueStr, context);
                return ParseResult.success(parsedValue);
            } catch (Exception e) {
                return ParseResult.failure();
            }
        }

        return ParseResult.failure();
    }

    /**
     * Formats the extracted parameter values into the final result format.
     *
     * @param value the extracted values
     * @param context the parameter extraction context
     * @return formatted parameter map with proper parameter names
     */
    protected Object formatParameters(Object value, ParameterExtractionContext context) {
        Method method = context.getTestMethod();
        if (method == null) {
            return createGenericParameterMap(value);
        }

        if (value instanceof Object[]) {
            return createMultiParameterMap((Object[]) value, context);
        } else {
            return createSingleParameterMap(value, context);
        }
    }

    /**
     * Creates a parameter map for multiple parameter values.
     *
     * @param values the parameter values
     * @param context the parameter extraction context
     * @return parameter map with named parameters
     */
    protected Map<String, Object> createMultiParameterMap(Object[] values, ParameterExtractionContext context) {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i++) {
            String parameterName = context.getParameterName(i);
            parameterMap.put(parameterName, values[i]);
        }
        return parameterMap;
    }

    /**
     * Creates a parameter map for a single parameter value.
     *
     * @param value the parameter value
     * @param context the parameter extraction context
     * @return parameter map with named parameter
     */
    protected Map<String, Object> createSingleParameterMap(Object value, ParameterExtractionContext context) {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        String parameterName = context.getParameterName(0);
        parameterMap.put(parameterName, value);
        return parameterMap;
    }

    /**
     * Creates a generic parameter map when method information is not available.
     *
     * @param value the parameter value or values
     * @return generic parameter map with default parameter names
     */
    protected Map<String, Object> createGenericParameterMap(Object value) {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        if (value instanceof Object[]) {
            Object[] values = (Object[]) value;
            for (int i = 0; i < values.length; i++) {
                parameterMap.put("param" + i, values[i]);
            }
        } else {
            parameterMap.put("param0", value);
        }
        return parameterMap;
    }

    /**
     * Parses a string value into its appropriate type with common type conversions.
     *
     * @param value the string value to parse
     * @return the parsed typed value (Boolean, Integer, Long, Double, or String)
     */
    protected Object parseTypedValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return value;
        }

        String trimmed = value.trim();

        if ("null".equals(trimmed)) {
            return null;
        }

        if ("true".equalsIgnoreCase(trimmed)) {
            return true;
        }
        if ("false".equalsIgnoreCase(trimmed)) {
            return false;
        }

        try {
            if (trimmed.contains(".")) {
                return Double.parseDouble(trimmed);
            } else {
                long longVal = Long.parseLong(trimmed);
                if (longVal >= Integer.MIN_VALUE && longVal <= Integer.MAX_VALUE) {
                    return (int) longVal;
                }
                return longVal;
            }
        } catch (NumberFormatException ignored) {

        }

        return value;
    }

    /**
     * Removes surrounding quotes from a string value.
     *
     * @param value the value to unquote
     * @return the unquoted value
     */
    protected String removeQuotes(String value) {
        if (value == null || value.length() < 2) {
            return value;
        }

        String trimmed = value.trim();
        if ((trimmed.startsWith("\"") && trimmed.endsWith("\"")) 
            || (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
            return trimmed.substring(1, trimmed.length() - 1);
        }

        return value;
    }

    /**
     * Gets the annotation type that this handler supports.
     *
     * @return the supported annotation class
     */
    protected abstract Class<? extends Annotation> getSupportedAnnotationType();

    /**
     * Parses the value from the display name specific to this handler type.
     *
     * @param valueStr the value string from the display name
     * @param context the parameter extraction context
     * @return the parsed values
     * @throws RuntimeException if parsing fails and fallback to annotation is required
     */
    protected abstract Object parseDisplayNameValue(String valueStr, ParameterExtractionContext context);

    /**
     * Extracts parameter values from the annotation as a fallback.
     *
     * @param context the parameter extraction context
     * @return the extracted values from the annotation
     */
    protected abstract Object extractFromAnnotation(ParameterExtractionContext context);
}