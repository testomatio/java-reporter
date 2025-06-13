package com.property_config.util;

public class StringUtils {
    private static final String EMPTY_STRING = "";
    private static final String DOT = ".";
    private static final String UNDERSCORE = "_";

    public static String toEnvStyle(String inputKey) {
        if (isNullOrEmpty(inputKey)) {
            return EMPTY_STRING;
        }
        return inputKey.toUpperCase().replace(DOT, UNDERSCORE);
    }

    public static String fromEnvStyle(String inputKey) {
        if (isNullOrEmpty(inputKey)) {
            return EMPTY_STRING;
        }
        return inputKey.toLowerCase().replace(UNDERSCORE, DOT);
    }

    public static boolean isNullOrEmpty(String inputKey) {
        return inputKey == null || inputKey.isEmpty();
    }
}
