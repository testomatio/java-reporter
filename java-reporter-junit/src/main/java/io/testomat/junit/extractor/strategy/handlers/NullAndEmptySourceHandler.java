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
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NullAndEmptySourceHandler implements ParameterExtractionHandler {

    private static final Logger logger = LoggerFactory.getLogger(NullAndEmptySourceHandler.class);
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

            ParseResult parseResult = extractFromDisplayNameWithResult(context);
            if (parseResult.isSuccessful()) {
                return formatParameter(parseResult.getValue(), context);
            }
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
        return 4;
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

        Matcher matcher = DISPLAY_NAME_PATTERN.matcher(displayName.trim());
        if (matcher.matches()) {
            String valueStr = matcher.group(1).trim();
            
            if ("null".equals(valueStr) || "NULL".equals(valueStr)
                || "<null>".equals(valueStr)) {
                return ParseResult.success(null);
            }
            
            if (valueStr.isEmpty() || "\"\"".equals(valueStr) || "''".equals(valueStr)
                || "[]".equals(valueStr) || "{}".equals(valueStr) 
                || "<empty>".equals(valueStr)) {
                Object emptyValue = determineEmptyValue(context);
                return ParseResult.success(emptyValue);
            }
            
            return ParseResult.failure();
        }

        return ParseResult.failure();
    }

    private Object determineFallbackValue(ParameterExtractionContext context) {
        return null;
    }

    private Object determineEmptyValue(ParameterExtractionContext context) {
        Method method = context.getTestMethod();
        if (method == null) {
            return "";
        }

        Parameter[] parameters = method.getParameters();
        if (parameters.length == 0) {
            return "";
        }

        Parameter firstParam = parameters[0];
        Class<?> paramType = firstParam.getType();

        if (String.class.isAssignableFrom(paramType)) {
            return "";
        } else if (paramType.isArray()) {
            return "[]";
        } else if (java.util.Collection.class.isAssignableFrom(paramType)) {
            return "[]";
        } else if (java.util.Map.class.isAssignableFrom(paramType)) {
            return "{}";
        } else {
            return "";
        }
    }

    private Object formatParameter(Object value, ParameterExtractionContext context) {
        Method method = context.getTestMethod();
        if (method == null) {
            Map<String, Object> parameterMap = new LinkedHashMap<>();
            parameterMap.put("param0", value);
            return parameterMap;
        }


        Map<String, Object> parameterMap = new LinkedHashMap<>();
        String parameterName = context.getParameterName(0);
        parameterMap.put(parameterName, value);

        return parameterMap;
    }

}