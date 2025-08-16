package io.testomat.junit.extractor;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;

/**
 * Base class containing common parameter extraction utilities
 * used by various parameter extractors.
 */
public abstract class BaseParameterExtractor {

    // Common patterns for JUnit 5 parameterized test display names
    protected static final Pattern[] DISPLAY_NAME_PATTERNS = {
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
     * Checks if the test method is a parameterized test.
     */
    public static boolean isParameterizedTest(ExtensionContext context) {
        try {
            Method testMethod = context.getTestMethod().orElse(null);
            return testMethod != null && testMethod.isAnnotationPresent(ParameterizedTest.class);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Enhanced display name parsing with multiple pattern matching.
     */
    protected Object[] parseParametersFromDisplayName(String displayName) {
        if (displayName == null) {
            return null;
        }

        for (Pattern pattern : DISPLAY_NAME_PATTERNS) {
            Matcher matcher = pattern.matcher(displayName.trim());
            if (matcher.matches()) {
                String paramPart = matcher.group(1).trim();
                Object[] parsed = parseParameterString(paramPart);
                if (parsed != null && parsed.length > 0) {
                    return parsed;
                }
            }
        }

        return null;
    }

    /**
     * Parses a parameter string into individual values with better type detection.
     */
    protected Object[] parseParameterString(String paramString) {
        if (paramString == null) {
            return null;
        }

        try {
            // Handle single parameter case (don't trim - preserve whitespace parameters)
            if (!paramString.contains(",")) {
                Object parsed = parseValue(paramString);
                return new Object[]{parsed};
            }

            // Split respecting quotes and brackets
            String[] parts = smartSplit(paramString);
            Object[] result = new Object[parts.length];

            for (int i = 0; i < parts.length; i++) {
                result[i] = parseValue(parts[i]);
            }

            return result;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Smart string splitting that respects quotes and nested structures.
     */
    protected String[] smartSplit(String input) {
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
    protected Object parseValue(String value) {
        if (value == null) {
            return null;
        }
        
        // Don't trim - preserve whitespace parameters as-is
        String trimmed = value.trim();
        
        // If the value is empty after trimming, return the original (could be whitespace)
        if (trimmed.isEmpty()) {
            return value;
        }

        // Handle quoted strings (use trimmed for quote detection)
        if ((trimmed.startsWith("\"") && trimmed.endsWith("\""))
                || (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
            return trimmed.substring(1, trimmed.length() - 1);
        }

        // Handle null (use trimmed for comparison)
        if ("null".equalsIgnoreCase(trimmed)) {
            return null;
        }

        // Handle booleans (use trimmed for comparison)
        if ("true".equalsIgnoreCase(trimmed)) {
            return true;
        }
        if ("false".equalsIgnoreCase(trimmed)) {
            return false;
        }

        // Handle numbers (use trimmed for parsing)
        try {
            if (trimmed.contains(".")) {
                return Double.parseDouble(trimmed);
            } else {
                long longVal = Long.parseLong(trimmed);
                return (longVal >= Integer.MIN_VALUE && longVal <= Integer.MAX_VALUE)
                        ? (int) longVal : longVal;
            }
        } catch (NumberFormatException ignored) {
            // Not a number
        }

        return value; // Return original value (preserving whitespace)
    }

    /**
     * Gets all fields from class hierarchy.
     */
    protected java.lang.reflect.Field[] getAllFields(Class<?> clazz) {
        java.util.List<java.lang.reflect.Field> allFields = new java.util.ArrayList<>();
        Class<?> currentClass = clazz;
        
        while (currentClass != null && currentClass != Object.class) {
            java.lang.reflect.Field[] fields = currentClass.getDeclaredFields();
            allFields.addAll(java.util.Arrays.asList(fields));
            currentClass = currentClass.getSuperclass();
        }
        
        return allFields.toArray(new java.lang.reflect.Field[0]);
    }
}