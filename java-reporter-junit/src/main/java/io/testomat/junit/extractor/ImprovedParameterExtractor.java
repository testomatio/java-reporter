package io.testomat.junit.extractor;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Improved parameter extractor that uses better strategies to get test parameters
 * from JUnit 5 parameterized tests.
 */
public class ImprovedParameterExtractor {

    private static final Logger log = LoggerFactory.getLogger(ImprovedParameterExtractor.class);

    // Updated patterns for actual JUnit 5 parameterized test display names
    // Note: Most JUnit 5 display names don't include parameter values by default
    private static final Pattern[] DISPLAY_NAME_PATTERNS = {
        // Pattern for custom display names with values: "methodName[1] arg1, arg2, arg3"
        Pattern.compile("^[^\\[]+\\[\\d+\\]\\s+(.+)$"),
        // Pattern for custom display names with brackets: "methodName [arg1, arg2, arg3]"
        Pattern.compile("^[^\\[]+\\s+\\[(.+)\\]$"),
        // Pattern for @ParameterizedTest(name = "...") custom formats: "Custom: arg1 and arg2"
        Pattern.compile("^[^:]+:\\s*(.+)$"),
        // Pattern for parentheses format: "methodName(arg1, arg2)"
        Pattern.compile("^[^(]+\\((.+)\\)$"),
        // Pattern for space-separated values at end: "prefix value1, value2"
        Pattern.compile("^.+?\\s+(.+,.+.*)$"),
        // Pattern for single value after space: "prefix value"
        Pattern.compile("^.+?\\s+([^\\s\\[\\(].+)$")
    };

    /**
     * Extracts parameters from a parameterized test context.
     */
    public Object extractTestParameters(ExtensionContext context) {
        String uniqueId = context.getUniqueId();
        String displayName = context.getDisplayName();
        
        
        if (!isParameterizedTest(context)) {
            return null;
        }


        try {
            Method testMethod = context.getTestMethod().orElse(null);
            if (testMethod == null) {
                return null;
            }


            // Strategy 1: Use TestMethodParameterExtractor (method invocation interception)
            Object[] parameters = TestMethodParameterExtractor.getCapturedParameters(context);
            if (parameters != null && parameters.length > 0) {
                Object result = formatParameters(parameters, testMethod);
                return result;
            }

            // Strategy 2: Use ParameterInterceptorExtension (context-based reflection)
            parameters = ParameterInterceptorExtension.getCapturedParameters(context);
            if (parameters != null && parameters.length > 0) {
                Object result = formatParameters(parameters, testMethod);
                return result;
            }

            // Strategy 3: Legacy reflection approach
            parameters = tryExtractParametersFromJUnitContext(context);
            if (parameters != null && parameters.length > 0) {
                Object result = formatParameters(parameters, testMethod);
                return result;
            }

            // Strategy 4: Enhanced display name parsing (fallback)
            parameters = parseParametersFromDisplayName(context.getDisplayName());
            if (parameters != null && parameters.length > 0) {
                Object result = formatParameters(parameters, testMethod);
                return result;
            }

            return null;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Checks if the test method is a parameterized test.
     */
    public boolean isParameterizedTest(ExtensionContext context) {
        try {
            Method testMethod = context.getTestMethod().orElse(null);
            return testMethod != null && testMethod.isAnnotationPresent(ParameterizedTest.class);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Tries to extract parameters from JUnit's internal context using reflection.
     * This approach looks for the actual parameter values that JUnit stores internally.
     */
    private Object[] tryExtractParametersFromJUnitContext(ExtensionContext context) {
        try {
            Class<?> contextClass = context.getClass();
            // Look for fields that might contain parameter information
            java.lang.reflect.Field[] allFields = getAllFields(contextClass);
            
            for (java.lang.reflect.Field field : allFields) {
                String fieldName = field.getName().toLowerCase();
                
                // Look for fields that likely contain the test arguments
                if (fieldName.contains("argument") || fieldName.contains("parameter") || 
                    fieldName.equals("args") || fieldName.equals("arguments")) {
                    
                    field.setAccessible(true);
                    Object value = field.get(context);
                    
                    
                    if (value instanceof Object[]) {
                        Object[] params = (Object[]) value;
                        if (params.length > 0) {
                            return params;
                        } else {
                        }
                    } else if (value != null) {
                    }
                }
            }

            // Try to find parameters in the test descriptor or invocation context
            return tryExtractFromTestDescriptor(context);

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets all fields from class hierarchy.
     */
    private java.lang.reflect.Field[] getAllFields(Class<?> clazz) {
        java.util.List<java.lang.reflect.Field> allFields = new java.util.ArrayList<>();
        Class<?> currentClass = clazz;
        
        while (currentClass != null && currentClass != Object.class) {
            java.lang.reflect.Field[] fields = currentClass.getDeclaredFields();
            allFields.addAll(java.util.Arrays.asList(fields));
            currentClass = currentClass.getSuperclass();
        }
        
        return allFields.toArray(new java.lang.reflect.Field[0]);
    }

    /**
     * Tries to extract parameters from test descriptor or invocation context.
     */
    private Object[] tryExtractFromTestDescriptor(ExtensionContext context) {
        try {
            // Try to access JUnit's TestDescriptor which might have parameter info
            java.lang.reflect.Method getTestDescriptor = context.getClass().getMethod("getTestDescriptor");
            if (getTestDescriptor != null) {
                Object testDescriptor = getTestDescriptor.invoke(context);
                if (testDescriptor != null) {
                    // Look for parameter-related methods or fields in test descriptor
                    return extractParametersFromTestDescriptor(testDescriptor);
                }
            }
        } catch (Exception e) {
        }
        
        return null;
    }

    /**
     * Extracts parameters from a JUnit TestDescriptor object.
     */
    private Object[] extractParametersFromTestDescriptor(Object testDescriptor) {
        try {
            Class<?> descriptorClass = testDescriptor.getClass();
            
            // Look for methods that might return arguments
            java.lang.reflect.Method[] methods = descriptorClass.getMethods();
            for (java.lang.reflect.Method method : methods) {
                String methodName = method.getName().toLowerCase();
                if ((methodName.contains("argument") || methodName.contains("parameter")) && 
                    method.getParameterCount() == 0) {
                    
                    Object result = method.invoke(testDescriptor);
                    if (result instanceof Object[]) {
                        return (Object[]) result;
                    }
                }
            }
        } catch (Exception e) {
        }
        
        return null;
    }

    /**
     * Enhanced display name parsing with multiple pattern matching.
     */
    private Object[] parseParametersFromDisplayName(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            return null;
        }


        for (int i = 0; i < DISPLAY_NAME_PATTERNS.length; i++) {
            Pattern pattern = DISPLAY_NAME_PATTERNS[i];
            
            Matcher matcher = pattern.matcher(displayName.trim());
            if (matcher.matches()) {
                String paramPart = matcher.group(1).trim();
                
                Object[] parsed = parseParameterString(paramPart);
                if (parsed != null && parsed.length > 0) {
                    return parsed;
                } else {
                }
            } else {
            }
        }

        return null;
    }

    /**
     * Parses a parameter string into individual values with better type detection.
     */
    private Object[] parseParameterString(String paramString) {
        if (paramString == null || paramString.trim().isEmpty()) {
            return null;
        }

        try {
            // Handle single parameter case
            if (!paramString.contains(",")) {
                Object parsed = parseValue(paramString.trim());
                return new Object[]{parsed};
            }

            // Split respecting quotes and brackets
            String[] parts = smartSplit(paramString);
            Object[] result = new Object[parts.length];

            for (int i = 0; i < parts.length; i++) {
                result[i] = parseValue(parts[i].trim());
            }

            return result;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Smart string splitting that respects quotes and nested structures.
     */
    private String[] smartSplit(String input) {
        java.util.List<String> parts = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = 0;
        int bracketDepth = 0;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            
            if (!inQuotes && (c == '"' || c == '\'')) {
                inQuotes = true;
                quoteChar = c;
                current.append(c);
            } else if (inQuotes && c == quoteChar) {
                inQuotes = false;
                current.append(c);
            } else if (!inQuotes && (c == '(' || c == '[' || c == '{')) {
                bracketDepth++;
                current.append(c);
            } else if (!inQuotes && (c == ')' || c == ']' || c == '}')) {
                bracketDepth--;
                current.append(c);
            } else if (!inQuotes && bracketDepth == 0 && c == ',') {
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
     * Parses a single parameter value with type detection.
     */
    private Object parseValue(String value) {
        if (value == null) return null;
        
        value = value.trim();
        if (value.isEmpty()) return "";

        // Handle quoted strings
        if ((value.startsWith("\"") && value.endsWith("\"")) ||
            (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }

        // Handle null
        if ("null".equalsIgnoreCase(value)) return null;

        // Handle booleans
        if ("true".equalsIgnoreCase(value)) return true;
        if ("false".equalsIgnoreCase(value)) return false;

        // Handle numbers
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            } else {
                long longVal = Long.parseLong(value);
                return (longVal >= Integer.MIN_VALUE && longVal <= Integer.MAX_VALUE) ? 
                       (int) longVal : longVal;
            }
        } catch (NumberFormatException ignored) {
            // Not a number
        }

        return value; // Return as string
    }

    /**
     * Formats parameters for reporting based on count and method signature.
     */
    private Object formatParameters(Object[] parameters, Method testMethod) {
        if (parameters == null || parameters.length == 0) {
            return null;
        }

        // Always return parameters as map with parameter names for consistency
        Map<String, Object> paramMap = new HashMap<>();
        Parameter[] methodParams = testMethod.getParameters();
        
        for (int i = 0; i < parameters.length; i++) {
            String paramName;
            if (i < methodParams.length && methodParams[i].isNamePresent()) {
                paramName = methodParams[i].getName();
                // Check if it's a real name or synthetic
                if (paramName.matches("arg\\d+")) {
                    paramName = "param" + i;
                }
            } else {
                paramName = "param" + i;
            }
            paramMap.put(paramName, parameters[i]);
        }

        return paramMap;
    }
}