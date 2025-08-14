package io.testomat.junit.extractor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit 5 extension that captures test parameters for parameterized tests.
 * This extension uses a combination of beforeEach callback and reflection
 * to capture parameters since JUnit 5's parameterized test parameters
 * are resolved by built-in resolvers that we can't easily intercept.
 */
public class ParameterCaptureExtension implements BeforeEachCallback, AfterEachCallback {

    private static final Logger log = LoggerFactory.getLogger(ParameterCaptureExtension.class);
    
    // Thread-safe storage for test parameters keyed by test unique ID
    private static final Map<String, Object[]> parameterStorage = new ConcurrentHashMap<>();
    
    // Thread-local storage for current test parameters during execution
    private static final ThreadLocal<Object[]> currentParameters = new ThreadLocal<>();

    @Override
    public void beforeEach(ExtensionContext context) {
        // Clear any previous parameters for this thread
        currentParameters.remove();
        
        // Try to capture parameters from the context if available
        try {
            Object[] parameters = extractParametersFromContext(context);
            if (parameters != null && parameters.length > 0) {
                String uniqueId = context.getUniqueId();
                parameterStorage.put(uniqueId, parameters);
                currentParameters.set(parameters);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        // Clean up thread-local storage
        currentParameters.remove();
        
        // Keep parameters in storage for reporting - they will be cleaned up by the test reporter
    }

    /**
     * Retrieves captured parameters for a given test context.
     * 
     * @param context the extension context
     * @return captured parameters or null if none available
     */
    public static Object[] getCapturedParameters(ExtensionContext context) {
        if (context == null) {
            return null;
        }
        
        String uniqueId = context.getUniqueId();
        Object[] parameters = parameterStorage.get(uniqueId);
        
        if (parameters != null) {
            return parameters;
        }
        
        // Try thread-local as fallback
        parameters = currentParameters.get();
        if (parameters != null) {
            return parameters;
        }
        
        return null;
    }

    /**
     * Manually stores parameters for a test context.
     * This can be called by other extensions that have access to test parameters.
     * 
     * @param context the extension context
     * @param parameters the test parameters
     */
    public static void storeParameters(ExtensionContext context, Object[] parameters) {
        if (context != null && parameters != null && parameters.length > 0) {
            String uniqueId = context.getUniqueId();
            parameterStorage.put(uniqueId, parameters);
            currentParameters.set(parameters);
        }
    }

    /**
     * Cleans up stored parameters for a test to prevent memory leaks.
     * 
     * @param context the extension context
     */
    public static void cleanupParameters(ExtensionContext context) {
        if (context != null) {
            String uniqueId = context.getUniqueId();
            Object[] removed = parameterStorage.remove(uniqueId);
            if (removed != null) {
            }
        }
    }

    /**
     * Attempts to extract parameters from the extension context using various strategies.
     */
    private Object[] extractParametersFromContext(ExtensionContext context) {
        try {
            // Strategy 1: Try to access JUnit's internal parameter storage through reflection
            Object[] parameters = tryReflectiveParameterExtraction(context);
            if (parameters != null && parameters.length > 0) {
                return parameters;
            }

            // Strategy 2: Parse from display name (enhanced version)
            parameters = parseParametersFromDisplayName(context);
            if (parameters != null && parameters.length > 0) {
                return parameters;
            }

        } catch (Exception e) {
        }
        
        return null;
    }

    /**
     * Enhanced reflective parameter extraction that looks for JUnit 5's internal parameter storage.
     */
    private Object[] tryReflectiveParameterExtraction(ExtensionContext context) {
        try {
            Class<?> contextClass = context.getClass();
            
            // Look for JUnit 5's ParameterizedTestExtensionContext
            if (contextClass.getName().contains("ParameterizedTest")) {
                // Try to find the arguments field
                java.lang.reflect.Field[] fields = contextClass.getDeclaredFields();
                for (java.lang.reflect.Field field : fields) {
                    String fieldName = field.getName().toLowerCase();
                    if (fieldName.contains("argument") || fieldName.contains("parameter")) {
                        field.setAccessible(true);
                        Object value = field.get(context);
                        
                        if (value instanceof Object[]) {
                            return (Object[]) value;
                        }
                    }
                }
                
                // Try superclass fields
                Class<?> superClass = contextClass.getSuperclass();
                if (superClass != null) {
                    java.lang.reflect.Field[] superFields = superClass.getDeclaredFields();
                    for (java.lang.reflect.Field field : superFields) {
                        String fieldName = field.getName().toLowerCase();
                        if (fieldName.contains("argument") || fieldName.contains("parameter")) {
                            field.setAccessible(true);
                            Object value = field.get(context);
                            
                            if (value instanceof Object[]) {
                                return (Object[]) value;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        
        return null;
    }

    /**
     * Enhanced display name parsing that handles various JUnit 5 parameter formats.
     */
    private Object[] parseParametersFromDisplayName(ExtensionContext context) {
        String displayName = context.getDisplayName();

        if (displayName == null || displayName.trim().isEmpty()) {
            return null;
        }

        try {
            // Format 1: "methodName[1] argument1, argument2"
            if (displayName.contains("] ")) {
                String paramPart = displayName.substring(displayName.indexOf("] ") + 2).trim();
                return parseParameterString(paramPart);
            }
            
            // Format 2: "methodName [argument1, argument2]"
            if (displayName.contains(" [") && displayName.endsWith("]")) {
                int start = displayName.indexOf(" [") + 2;
                int end = displayName.lastIndexOf("]");
                String paramPart = displayName.substring(start, end).trim();
                return parseParameterString(paramPart);
            }
            
            // Format 3: "methodName(argument1, argument2)"
            if (displayName.contains("(") && displayName.endsWith(")")) {
                int start = displayName.indexOf("(") + 1;
                int end = displayName.lastIndexOf(")");
                String paramPart = displayName.substring(start, end).trim();
                return parseParameterString(paramPart);
            }
            
        } catch (Exception e) {
        }

        return null;
    }

    /**
     * Enhanced parameter string parsing with better type detection.
     */
    private Object[] parseParameterString(String paramString) {
        if (paramString == null || paramString.trim().isEmpty()) {
            return null;
        }

        try {
            // Handle single parameter (no commas)
            if (!paramString.contains(",")) {
                Object parsed = parseParameterValue(paramString.trim());
                return new Object[]{parsed};
            }

            // Split by comma but respect quoted strings
            String[] rawParams = smartSplit(paramString);
            Object[] result = new Object[rawParams.length];

            for (int i = 0; i < rawParams.length; i++) {
                result[i] = parseParameterValue(rawParams[i].trim());
            }

            return result;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Smart string splitting that respects quoted strings and nested structures.
     */
    private String[] smartSplit(String input) {
        java.util.List<String> parts = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        int depth = 0;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            
            if (c == '"' || c == '\'') {
                inQuotes = !inQuotes;
                current.append(c);
            } else if (!inQuotes && (c == '(' || c == '[' || c == '{')) {
                depth++;
                current.append(c);
            } else if (!inQuotes && (c == ')' || c == ']' || c == '}')) {
                depth--;
                current.append(c);
            } else if (!inQuotes && depth == 0 && c == ',') {
                parts.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        
        if (current.length() > 0) {
            parts.add(current.toString());
        }

        return parts.toArray(new String[0]);
    }

    /**
     * Enhanced parameter value parsing with better type detection.
     */
    private Object parseParameterValue(String value) {
        if (value == null) {
            return null;
        }

        value = value.trim();
        
        if (value.isEmpty()) {
            return "";
        }

        // Handle quoted strings
        if ((value.startsWith("\"") && value.endsWith("\"")) ||
            (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }

        // Handle null
        if ("null".equalsIgnoreCase(value)) {
            return null;
        }

        // Handle booleans
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }

        // Handle numbers
        try {
            if (value.contains(".")) {
                // Check for float vs double
                if (value.length() <= 7) { // Rough heuristic for float precision
                    return Float.parseFloat(value);
                } else {
                    return Double.parseDouble(value);
                }
            } else {
                // Check for int vs long
                long longValue = Long.parseLong(value);
                if (longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE) {
                    return (int) longValue;
                } else {
                    return longValue;
                }
            }
        } catch (NumberFormatException ignored) {
            // Not a number, treat as string
        }

        // Handle special string values that might indicate specific types
        if (value.startsWith("[") && value.endsWith("]")) {
            // Might be an array representation, but for now treat as string
            return value;
        }
        
        if (value.startsWith("{") && value.endsWith("}")) {
            // Might be an object representation, but for now treat as string
            return value;
        }

        return value;
    }
}