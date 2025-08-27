package io.testomat.core.propertyconfig.util;

/**
 * String utilities for property key format conversion and validation.
 * Handles transformation between different property naming conventions used
 * by various property sources (system properties, environment variables, files).
 */
public class StringUtils {
    private static final String EMPTY_STRING = "";
    private static final String DOT = ".";
    private static final String UNDERSCORE = "_";

    /**
     * Converts property key to environment variable style (uppercase with underscores).
     * Transforms dot notation to environment variable convention.
     * 
     * <p>Example: {@code "testomatio.api.key"} → {@code "TESTOMATIO_API_KEY"}
     * 
     * @param inputKey property key in dot notation
     * @return environment variable style key, or empty string if input is null/empty
     */
    public static String toEnvStyle(String inputKey) {
        if (isNullOrEmpty(inputKey)) {
            return EMPTY_STRING;
        }
        return inputKey.toUpperCase().replace(DOT, UNDERSCORE);
    }

    /**
     * Converts property key from environment variable style to standard dot notation.
     * Transforms uppercase underscore format to lowercase dot notation.
     * 
     * <p>Example: {@code "TESTOMATIO_API_KEY"} → {@code "testomatio.api.key"}
     * 
     * @param inputKey property key in environment variable style  
     * @return dot notation style key, or empty string if input is null/empty
     */
    public static String fromEnvStyle(String inputKey) {
        if (isNullOrEmpty(inputKey)) {
            return EMPTY_STRING;
        }
        return inputKey.toLowerCase().replace(UNDERSCORE, DOT);
    }

    /**
     * Checks if string is null or empty.
     * 
     * @param inputKey string to check
     * @return true if string is null or has zero length
     */
    public static boolean isNullOrEmpty(String inputKey) {
        return inputKey == null || inputKey.isEmpty();
    }
}
