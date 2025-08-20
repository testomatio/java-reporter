package io.testomat.junit.extractor.strategy.handlers;

import io.testomat.junit.extractor.strategy.ParameterExtractionContext;
import io.testomat.junit.exception.ParameterExtractionException;
import io.testomat.junit.extractor.strategy.ParameterExtractionHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Strategy for extracting parameters from @MethodSource annotated parameterized tests.
 * Supports extraction of parameter values from methods specified in @MethodSource annotations
 * with various configurations including method name resolution and argument handling.
 */
public class MethodSourceHandler implements ParameterExtractionHandler {

    private static final Logger logger = LoggerFactory.getLogger(MethodSourceHandler.class);
    private static final Pattern DISPLAY_NAME_PATTERN = Pattern.compile("^\\[\\d+\\]\\s*(.*)$");

    @Override
    public boolean supports(ParameterExtractionContext context) {
        return context.isValid() && context.hasAnnotation(MethodSource.class);
    }

    @Override
    public Object extractParameters(ParameterExtractionContext context) 
            throws ParameterExtractionException {
        if (!supports(context)) {
            return null;
        }

        try {
            MethodSource methodSource = context.getAnnotation(MethodSource.class);
            if (methodSource == null) {
                return null;
            }

            // Try to extract from display name first 
            // (most reliable for getting actual parameter values)
            ParseResult parseResult = extractFromDisplayNameWithResult(context, methodSource);
            if (parseResult.isSuccessful()) {
                return formatParameters(parseResult.getValue(), context);
            }

            // Fallback: extract from method source 
            // (won't give us the specific values for this invocation)
            Object[] methodValues = extractFromMethodSource(methodSource, context);
            return formatParameters(methodValues, context);

        } catch (Exception e) {
            logger.debug("Failed to extract MethodSource parameters for: {}", 
                        context.getDisplayName(), e);
            throw new ParameterExtractionException("Failed to extract MethodSource parameters", e);
        }
    }

    @Override
    public int getPriority() {
        return 15; // Higher priority than simple sources due to complexity
    }

    @Override
    public String getStrategyName() {
        return "MethodSourceExtractionStrategy";
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
                                                         MethodSource methodSource) {
        String displayName = context.getDisplayName();
        if (displayName == null || displayName.trim().isEmpty()) {
            return ParseResult.failure();
        }

        // JUnit 5 parameterized tests format: "[1] value1, value2, value3" etc.
        // For MethodSource, the display name might contain complex objects or arrays
        Matcher matcher = DISPLAY_NAME_PATTERN.matcher(displayName.trim());
        if (matcher.matches()) {
            String valueStr = matcher.group(1).trim();
            Object[] parsedValues = parseMethodSourceDisplayName(valueStr, context);
            return ParseResult.success(parsedValues);
        }

        return ParseResult.failure();
    }

    private Object[] extractFromMethodSource(MethodSource methodSource, 
                                            ParameterExtractionContext context) {
        try {
            String[] methodNames = methodSource.value();
            
            // If no method names specified, try to infer from test method name
            if (methodNames.length == 0) {
                Method testMethod = context.getTestMethod();
                if (testMethod != null) {
                    methodNames = new String[]{inferMethodName(testMethod.getName())};
                }
            }
            
            if (methodNames.length == 0) {
                return new Object[0];
            }

            // Try to resolve and invoke the first method
            String methodName = methodNames[0];
            Method sourceMethod = resolveSourceMethod(methodName, context);
            if (sourceMethod != null) {
                Object result = invokeSourceMethod(sourceMethod);
                return extractArgumentsFromResult(result);
            }

            return new Object[0];

        } catch (Exception e) {
            logger.debug("Failed to extract parameters from method source", e);
            return new Object[0];
        }
    }

    private String inferMethodName(String testMethodName) {
        // Common patterns: testSomething -> provideSomething or somethingProvider
        if (testMethodName.startsWith("test")) {
            String baseName = testMethodName.substring(4);
            return "provide" + baseName;
        }
        return testMethodName + "Provider";
    }

    private Method resolveSourceMethod(String methodName, ParameterExtractionContext context) {
        try {
            Method testMethod = context.getTestMethod();
            if (testMethod == null) {
                return null;
            }

            Class<?> testClass = testMethod.getDeclaringClass();
            
            // Handle fully qualified method names (ClassName#methodName)
            if (methodName.contains("#")) {
                String[] parts = methodName.split("#", 2);
                String className = parts[0];
                String simpleMethodName = parts[1];
                
                try {
                    Class<?> sourceClass = Class.forName(className);
                    return findMethodByName(sourceClass, simpleMethodName);
                } catch (ClassNotFoundException e) {
                    logger.debug("Could not find class: {}", className, e);
                    return null;
                }
            } else {
                // Simple method name - look in the test class
                return findMethodByName(testClass, methodName);
            }

        } catch (Exception e) {
            logger.debug("Failed to resolve source method: {}", methodName, e);
            return null;
        }
    }

    private Method findMethodByName(Class<?> clazz, String methodName) {
        // Look for static methods with the given name that return Stream, Iterator, or array
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName) 
                && java.lang.reflect.Modifier.isStatic(method.getModifiers())
                && method.getParameterCount() == 0) {
                
                Class<?> returnType = method.getReturnType();
                if (Stream.class.isAssignableFrom(returnType) 
                    || Iterator.class.isAssignableFrom(returnType)
                    || returnType.isArray()) {
                    return method;
                }
            }
        }
        return null;
    }

    private Object invokeSourceMethod(Method sourceMethod) {
        try {
            sourceMethod.setAccessible(true);
            return sourceMethod.invoke(null);
        } catch (Exception e) {
            logger.debug("Failed to invoke source method: {}", sourceMethod.getName(), e);
            return null;
        }
    }

    private Object[] extractArgumentsFromResult(Object result) {
        if (result == null) {
            return new Object[0];
        }

        try {
            if (result instanceof Stream) {
                Stream<?> stream = (Stream<?>) result;
                Object firstElement = stream.findFirst().orElse(null);
                return convertToArgumentArray(firstElement);
            } else if (result instanceof Iterator) {
                Iterator<?> iterator = (Iterator<?>) result;
                if (iterator.hasNext()) {
                    Object firstElement = iterator.next();
                    return convertToArgumentArray(firstElement);
                }
            } else if (result.getClass().isArray()) {
                Object[] array = (Object[]) result;
                if (array.length > 0) {
                    return convertToArgumentArray(array[0]);
                }
            }

            // Fallback: treat the result itself as a single argument
            return new Object[]{result};

        } catch (Exception e) {
            logger.debug("Failed to extract arguments from result", e);
            return new Object[0];
        }
    }

    private Object[] convertToArgumentArray(Object element) {
        if (element == null) {
            return new Object[]{null};
        }

        if (element instanceof Arguments) {
            Arguments args = (Arguments) element;
            return args.get();
        } else if (element instanceof Object[]) {
            return (Object[]) element;
        } else {
            // Single argument
            return new Object[]{element};
        }
    }

    private Object[] parseMethodSourceDisplayName(String displayValue, 
                                                  ParameterExtractionContext context) {
        if (displayValue == null || displayValue.trim().isEmpty()) {
            return new Object[0];
        }

        String trimmed = displayValue.trim();

        // Handle array-like display: "[value1, value2, value3]"
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            String arrayContent = trimmed.substring(1, trimmed.length() - 1);
            return parseCommaSeparatedValues(arrayContent);
        }

        // Handle Arguments.of() display: "Arguments{arguments=[value1, value2, value3]}"
        if (trimmed.startsWith("Arguments{arguments=[") && trimmed.endsWith("]}")) {
            String argsContent = trimmed.substring(21, trimmed.length() - 2);
            return parseCommaSeparatedValues(argsContent);
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
        return value;
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