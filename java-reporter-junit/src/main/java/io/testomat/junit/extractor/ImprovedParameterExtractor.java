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
        
        log.info("=== PARAMETER EXTRACTION START ===");
        log.info("Test ID: {}", uniqueId);
        log.info("Display Name: {}", displayName);
        
        System.out.println("\n🔍 PARAMETER EXTRACTION START");
        System.out.println("  🆔 Test ID: " + uniqueId);
        System.out.println("  🏷️  Display Name: " + displayName);
        
        if (!isParameterizedTest(context)) {
            log.info("Test is not parameterized, skipping parameter extraction");
            System.out.println("  ℹ️  Test is not parameterized, skipping parameter extraction");
            return null;
        }

        log.info("Confirmed: Test is parameterized");
        System.out.println("  ✅ Confirmed: Test is parameterized");

        try {
            Method testMethod = context.getTestMethod().orElse(null);
            if (testMethod == null) {
                log.warn("No test method found in context");
                return null;
            }

            log.info("Test method: {}", testMethod.getName());
            log.info("Method parameter count: {}", testMethod.getParameterCount());
            
            System.out.println("  📋 Test method: " + testMethod.getName());
            System.out.println("  🔢 Method parameter count: " + testMethod.getParameterCount());

            // Strategy 1: Use TestMethodParameterExtractor (method invocation interception)
            log.info("--- Strategy 1: Method Invocation Interception ---");
            System.out.println("  🎯 Strategy 1: Method Invocation Interception");
            Object[] parameters = TestMethodParameterExtractor.getCapturedParameters(context);
            if (parameters != null && parameters.length > 0) {
                log.info("SUCCESS: Retrieved {} parameters from method invocation interception", parameters.length);
                System.out.println("  ✅ SUCCESS: Retrieved " + parameters.length + " parameters from method invocation");
                for (int i = 0; i < parameters.length; i++) {
                    log.info("  Parameter {}: {} (type: {})", i, parameters[i], 
                            parameters[i] != null ? parameters[i].getClass().getSimpleName() : "null");
                    System.out.println("    📋 Parameter " + i + ": " + parameters[i] + " (type: " + 
                            (parameters[i] != null ? parameters[i].getClass().getSimpleName() : "null") + ")");
                }
                Object result = formatParameters(parameters, testMethod);
                log.info("Formatted result: {}", result);
                log.info("=== PARAMETER EXTRACTION END (SUCCESS) ===");
                System.out.println("  📦 Formatted result: " + result);
                System.out.println("🔍 PARAMETER EXTRACTION END (SUCCESS)\n");
                return result;
            }

            // Strategy 2: Use ParameterInterceptorExtension (context-based reflection)
            log.info("--- Strategy 2: Parameter Interceptor Extension ---");
            parameters = ParameterInterceptorExtension.getCapturedParameters(context);
            if (parameters != null && parameters.length > 0) {
                log.info("SUCCESS: Retrieved {} parameters from parameter interceptor", parameters.length);
                for (int i = 0; i < parameters.length; i++) {
                    log.info("  Parameter {}: {} (type: {})", i, parameters[i], 
                            parameters[i] != null ? parameters[i].getClass().getSimpleName() : "null");
                }
                Object result = formatParameters(parameters, testMethod);
                log.info("Formatted result: {}", result);
                log.info("=== PARAMETER EXTRACTION END (SUCCESS) ===");
                return result;
            }

            // Strategy 3: Legacy reflection approach
            log.info("--- Strategy 3: Legacy JUnit Internal Context ---");
            parameters = tryExtractParametersFromJUnitContext(context);
            if (parameters != null && parameters.length > 0) {
                log.info("SUCCESS: Extracted {} parameters from JUnit internal context", parameters.length);
                for (int i = 0; i < parameters.length; i++) {
                    log.info("  Parameter {}: {} (type: {})", i, parameters[i], 
                            parameters[i] != null ? parameters[i].getClass().getSimpleName() : "null");
                }
                Object result = formatParameters(parameters, testMethod);
                log.info("Formatted result: {}", result);
                log.info("=== PARAMETER EXTRACTION END (SUCCESS) ===");
                return result;
            }

            // Strategy 4: Enhanced display name parsing (fallback)
            log.info("--- Strategy 4: Display Name Parsing (Fallback) ---");
            parameters = parseParametersFromDisplayName(context.getDisplayName());
            if (parameters != null && parameters.length > 0) {
                log.info("SUCCESS: Parsed {} parameters from display name: {}", parameters.length, context.getDisplayName());
                for (int i = 0; i < parameters.length; i++) {
                    log.info("  Parameter {}: {} (type: {})", i, parameters[i], 
                            parameters[i] != null ? parameters[i].getClass().getSimpleName() : "null");
                }
                Object result = formatParameters(parameters, testMethod);
                log.info("Formatted result: {}", result);
                log.info("=== PARAMETER EXTRACTION END (SUCCESS) ===");
                return result;
            }

            log.warn("FAILURE: No parameters found for test: {}", context.getDisplayName());
            log.info("=== PARAMETER EXTRACTION END (FAILURE) ===");
            System.out.println("  ❌ FAILURE: No parameters found for test: " + context.getDisplayName());
            System.out.println("🔍 PARAMETER EXTRACTION END (FAILURE)\n");
            return null;

        } catch (Exception e) {
            log.error("EXCEPTION: Failed to extract parameters for test: {}", context.getDisplayName(), e);
            log.info("=== PARAMETER EXTRACTION END (EXCEPTION) ===");
            System.out.println("  ❌ EXCEPTION: Failed to extract parameters for test: " + context.getDisplayName());
            System.out.println("  💥 Error: " + e.getMessage());
            System.out.println("🔍 PARAMETER EXTRACTION END (EXCEPTION)\n");
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
            log.trace("Failed to check if test is parameterized: {}", e.getMessage());
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
            log.info("Context class: {}", contextClass.getName());
            
            // Look for fields that might contain parameter information
            java.lang.reflect.Field[] allFields = getAllFields(contextClass);
            log.info("Total fields to examine: {}", allFields.length);
            
            for (java.lang.reflect.Field field : allFields) {
                String fieldName = field.getName().toLowerCase();
                log.debug("Examining field: {} (type: {})", field.getName(), field.getType().getSimpleName());
                
                // Look for fields that likely contain the test arguments
                if (fieldName.contains("argument") || fieldName.contains("parameter") || 
                    fieldName.equals("args") || fieldName.equals("arguments")) {
                    
                    log.info("Found potential parameter field: {}", field.getName());
                    field.setAccessible(true);
                    Object value = field.get(context);
                    
                    log.info("Field '{}' value: {} (type: {})", field.getName(), value, 
                            value != null ? value.getClass().getSimpleName() : "null");
                    
                    if (value instanceof Object[]) {
                        Object[] params = (Object[]) value;
                        if (params.length > 0) {
                            log.info("SUCCESS: Found parameters in field '{}': {}", fieldName, java.util.Arrays.toString(params));
                            return params;
                        } else {
                            log.info("Field '{}' is empty array", fieldName);
                        }
                    } else if (value != null) {
                        log.info("Field '{}' is not an Object array", fieldName);
                    }
                }
            }

            log.info("No parameter fields found, trying test descriptor approach");
            // Try to find parameters in the test descriptor or invocation context
            return tryExtractFromTestDescriptor(context);

        } catch (Exception e) {
            log.warn("Reflection-based parameter extraction failed", e);
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
            log.trace("Failed to extract from test descriptor", e);
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
            log.trace("Failed to extract parameters from test descriptor", e);
        }
        
        return null;
    }

    /**
     * Enhanced display name parsing with multiple pattern matching.
     */
    private Object[] parseParametersFromDisplayName(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            log.info("Display name is null or empty");
            return null;
        }

        log.info("Trying to parse display name: '{}'", displayName);

        for (int i = 0; i < DISPLAY_NAME_PATTERNS.length; i++) {
            Pattern pattern = DISPLAY_NAME_PATTERNS[i];
            log.debug("Trying pattern {}: {}", i, pattern.pattern());
            
            Matcher matcher = pattern.matcher(displayName.trim());
            if (matcher.matches()) {
                String paramPart = matcher.group(1).trim();
                log.info("SUCCESS: Matched pattern {} with parameters: '{}'", pattern.pattern(), paramPart);
                
                Object[] parsed = parseParameterString(paramPart);
                if (parsed != null && parsed.length > 0) {
                    log.info("Successfully parsed {} parameters from string", parsed.length);
                    return parsed;
                } else {
                    log.warn("Pattern matched but failed to parse parameters from: '{}'", paramPart);
                }
            } else {
                log.debug("Pattern {} did not match", pattern.pattern());
            }
        }

        log.warn("No pattern matched for display name: '{}'", displayName);
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
            log.trace("Failed to parse parameter string: {}", paramString, e);
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

        // Single parameter - return directly
        if (parameters.length == 1) {
            return parameters[0];
        }

        // Multiple parameters - return as map with parameter names
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