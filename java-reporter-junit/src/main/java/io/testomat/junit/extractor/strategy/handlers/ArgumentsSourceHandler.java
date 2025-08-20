package io.testomat.junit.extractor.strategy.handlers;

import io.testomat.junit.extractor.strategy.ParameterExtractionContext;
import io.testomat.junit.exception.ParameterExtractionException;
import io.testomat.junit.extractor.strategy.ParameterExtractionHandler;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Strategy for extracting parameters from @ArgumentsSource annotated parameterized tests.
 * Supports extraction of parameter values from custom ArgumentsProvider implementations
 * specified in @ArgumentsSource annotations with various configurations.
 */
public class ArgumentsSourceHandler implements ParameterExtractionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ArgumentsSourceHandler.class);
    private static final Pattern DISPLAY_NAME_PATTERN = Pattern.compile("^\\[\\d+\\]\\s*(.*)$");

    @Override
    public boolean supports(ParameterExtractionContext context) {
        return context.isValid() && context.hasAnnotation(ArgumentsSource.class);
    }

    @Override
    public Object extractParameters(ParameterExtractionContext context) 
            throws ParameterExtractionException {
        if (!supports(context)) {
            return null;
        }

        try {
            ArgumentsSource argumentsSource = context.getAnnotation(ArgumentsSource.class);
            if (argumentsSource == null) {
                return null;
            }

            // Try to extract from display name first 
            // (most reliable for getting actual parameter values)
            ParseResult parseResult = extractFromDisplayNameWithResult(context, argumentsSource);
            if (parseResult.isSuccessful()) {
                return formatParameters(parseResult.getValue(), context);
            }

            // Fallback: extract from ArgumentsProvider 
            // (won't give us the specific values for this invocation)
            Object[] providerValues = extractFromArgumentsProvider(argumentsSource, context);
            return formatParameters(providerValues, context);

        } catch (Exception e) {
            logger.debug("Failed to extract ArgumentsSource parameters for: {}", 
                        context.getDisplayName(), e);
            throw new ParameterExtractionException("Failed to extract ArgumentsSource parameters", e);
        }
    }

    @Override
    public int getPriority() {
        return 20; // Highest priority due to complexity of custom providers
    }

    @Override
    public String getStrategyName() {
        return "ArgumentsSourceExtractionStrategy";
    }

    private static class ParseResult {
        private final boolean successful;
        private final Object[] values;

        private ParseResult(boolean successful, Object[] values) {
            this.successful = successful;
            this.values = values;
        }

        public static ParseResult success(Object[] values) {
            return new ParseResult(true, values);
        }

        public static ParseResult failure() {
            return new ParseResult(false, null);
        }

        public boolean isSuccessful() {
            return successful;
        }

        public Object[] getValue() {
            return values;
        }
    }

    private ParseResult extractFromDisplayNameWithResult(ParameterExtractionContext context, 
                                                         ArgumentsSource argumentsSource) {
        String displayName = context.getDisplayName();
        if (displayName == null || displayName.trim().isEmpty()) {
            return ParseResult.failure();
        }

        // JUnit 5 parameterized tests format: "[1] value1, value2, value3" etc.
        // For ArgumentsSource, display name might contain complex objects or custom formats
        Matcher matcher = DISPLAY_NAME_PATTERN.matcher(displayName.trim());
        if (matcher.matches()) {
            String valueStr = matcher.group(1).trim();
            Object[] parsedValues = parseArgumentsSourceDisplayName(valueStr, context);
            return ParseResult.success(parsedValues);
        }

        return ParseResult.failure();
    }

    private Object[] extractFromArgumentsProvider(ArgumentsSource argumentsSource, 
                                                 ParameterExtractionContext context) {
        try {
            Class<? extends ArgumentsProvider> providerClass = argumentsSource.value();
            if (providerClass == null) {
                return new Object[0];
            }

            // Instantiate the ArgumentsProvider
            ArgumentsProvider provider = instantiateProvider(providerClass);
            if (provider == null) {
                return new Object[0];
            }

            // Create a mock ExtensionContext for the provider
            ExtensionContext extensionContext = context.getExtensionContext();
            if (extensionContext == null) {
                return new Object[0];
            }

            // Invoke the provider to get arguments
            Stream<? extends Arguments> argumentsStream = provider.provideArguments(extensionContext);
            if (argumentsStream == null) {
                return new Object[0];
            }

            // Get the first Arguments object as fallback
            Arguments firstArguments = argumentsStream.findFirst().orElse(null);
            if (firstArguments != null) {
                return firstArguments.get();
            }

            return new Object[0];

        } catch (Exception e) {
            logger.debug("Failed to extract parameters from ArgumentsProvider", e);
            return new Object[0];
        }
    }

    private ArgumentsProvider instantiateProvider(Class<? extends ArgumentsProvider> providerClass) {
        try {
            // Try default constructor first
            Constructor<? extends ArgumentsProvider> defaultConstructor = 
                providerClass.getDeclaredConstructor();
            defaultConstructor.setAccessible(true);
            return defaultConstructor.newInstance();
            
        } catch (Exception e) {
            logger.debug("Failed to instantiate ArgumentsProvider with default constructor: {}", 
                        providerClass.getName(), e);
            
            try {
                // Try any available constructor
                Constructor<?>[] constructors = providerClass.getDeclaredConstructors();
                for (Constructor<?> constructor : constructors) {
                    if (constructor.getParameterCount() == 0) {
                        constructor.setAccessible(true);
                        return (ArgumentsProvider) constructor.newInstance();
                    }
                }
                
                // If no parameterless constructor, try first constructor with null parameters
                if (constructors.length > 0) {
                    Constructor<?> firstConstructor = constructors[0];
                    firstConstructor.setAccessible(true);
                    Object[] nullArgs = new Object[firstConstructor.getParameterCount()];
                    return (ArgumentsProvider) firstConstructor.newInstance(nullArgs);
                }
                
            } catch (Exception fallbackException) {
                logger.debug("Failed to instantiate ArgumentsProvider with fallback approaches: {}", 
                            providerClass.getName(), fallbackException);
            }
        }
        
        return null;
    }

    private Object[] parseArgumentsSourceDisplayName(String displayValue, 
                                                     ParameterExtractionContext context) {
        if (displayValue == null || displayValue.trim().isEmpty()) {
            return new Object[0];
        }

        String trimmed = displayValue.trim();

        // Handle Arguments display format: "Arguments{arguments=[value1, value2, value3]}"
        if (trimmed.startsWith("Arguments{arguments=[") && trimmed.endsWith("]}")) {
            String argsContent = trimmed.substring(21, trimmed.length() - 2);
            return parseCommaSeparatedValues(argsContent);
        }

        // Handle array-like display: "[value1, value2, value3]"
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            String arrayContent = trimmed.substring(1, trimmed.length() - 1);
            return parseCommaSeparatedValues(arrayContent);
        }

        // Handle custom object displays - try to extract meaningful values
        if (trimmed.contains("(") && trimmed.contains(")")) {
            // Handle constructor-like display: "CustomObject(value1, value2)"
            int startParen = trimmed.indexOf('(');
            int endParen = trimmed.lastIndexOf(')');
            if (startParen >= 0 && endParen > startParen) {
                String argsContent = trimmed.substring(startParen + 1, endParen);
                return parseCommaSeparatedValues(argsContent);
            }
        }

        // Handle simple comma-separated values
        if (trimmed.contains(",")) {
            return parseCommaSeparatedValues(trimmed);
        }

        // Single value
        return new Object[]{parseTypedValue(trimmed)};
    }

    private Object[] parseCommaSeparatedValues(String content) {
        if (content == null || content.trim().isEmpty()) {
            return new Object[0];
        }

        // Simple comma splitting - could be enhanced for complex nested structures
        String[] parts = content.split(",");
        Object[] result = new Object[parts.length];
        
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            result[i] = parseTypedValue(part);
        }
        
        return result;
    }

    private Object parseTypedValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return value;
        }

        String trimmed = value.trim();

        // Remove quotes if present
        if ((trimmed.startsWith("\"") && trimmed.endsWith("\"")) 
            || (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }

        // Handle null
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
                if (longVal >= Integer.MIN_VALUE 
                    && longVal <= Integer.MAX_VALUE) {
                    return (int) longVal;
                }
                return longVal;
            }
        } catch (NumberFormatException e) {
            // Not a number, continue
        }

        // Return as string
        return trimmed;
    }

    private Object formatParameters(Object[] values, 
                                    ParameterExtractionContext context) {
        if (values == null || values.length == 0) {
            return new LinkedHashMap<String, Object>();
        }

        Method method = context.getTestMethod();
        if (method == null) {
            // If no method info, create generic parameter map
            Map<String, Object> parameterMap = new LinkedHashMap<>();
            for (int i = 0; i < values.length; i++) {
                parameterMap.put("param" + i, values[i]);
            }
            return parameterMap;
        }

        Parameter[] parameters = method.getParameters();
        
        // Create parameter map with proper names
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i++) {
            String parameterName = context.getParameterName(i);
            parameterMap.put(parameterName, values[i]);
        }

        return parameterMap;
    }

}