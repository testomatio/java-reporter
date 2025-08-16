package io.testomat.junit.extractor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing test parameters from JUnit display names.
 * Handles complex parsing scenarios including quoted strings, nested structures,
 * and automatic type detection for extracted parameter values.
 */
public class ParameterParser {

    private static final Pattern[] PATTERNS = {
        Pattern.compile("^[^\\[]+\\[\\d+\\]\\s+(.+)$"),
        Pattern.compile("^[^\\[]+\\s+\\[(.+)\\]$"),
        Pattern.compile("^[^:]+:\\s*(.+)$"),
        Pattern.compile("^[^(]+\\((.+)\\)$"),
        Pattern.compile("^.+?\\s+(.+,.+.*)$"),
        Pattern.compile("^.+?\\s+([^\\s\\[\\(].+)$")
    };

    /**
     * Parses parameters from a JUnit test display name using multiple patterns.
     *
     * @param displayName the test display name to parse
     * @return array of parsed parameter objects or null if no parameters found
     */
    public Object[] parseParametersFromDisplayName(String displayName) {
        if (displayName == null) {
            return null;
        }

        for (Pattern pattern : PATTERNS) {
            Matcher matcher = pattern.matcher(displayName.trim());
            if (matcher.matches()) {
                Object[] parsed = parseParameterString(matcher.group(1).trim());
                if (parsed != null && parsed.length > 0) {
                    return parsed;
                }
            }
        }

        return null;
    }

    private Object[] parseParameterString(String paramString) {
        if (paramString == null) {
            return null;
        }

        try {
            if (!paramString.contains(",")) {
                return new Object[]{parseValue(paramString)};
            }

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

    private String[] smartSplit(String input) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = 0;
        int bracketDepth = 0;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            
            if (handleQuoteStart(c, inQuotes)) {
                inQuotes = true;
                quoteChar = c;
                current.append(c);
            } else if (handleQuoteEnd(c, inQuotes, quoteChar)) {
                inQuotes = false;
                current.append(c);
            } else if (handleBracketOpen(c, inQuotes)) {
                bracketDepth++;
                current.append(c);
            } else if (handleBracketClose(c, inQuotes)) {
                bracketDepth--;
                current.append(c);
            } else if (shouldSplit(c, inQuotes, bracketDepth)) {
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

    private boolean handleQuoteStart(char c, boolean inQuotes) {
        return !inQuotes && (c == '"' || c == '\'');
    }

    private boolean handleQuoteEnd(char c, boolean inQuotes, char quoteChar) {
        return inQuotes && c == quoteChar;
    }

    private boolean handleBracketOpen(char c, boolean inQuotes) {
        return !inQuotes && (c == '(' || c == '[' || c == '{');
    }

    private boolean handleBracketClose(char c, boolean inQuotes) {
        return !inQuotes && (c == ')' || c == ']' || c == '}');
    }

    private boolean shouldSplit(char c, boolean inQuotes, int bracketDepth) {
        return !inQuotes && bracketDepth == 0 && c == ',';
    }

    private Object parseValue(String value) {
        if (value == null) {
            return null;
        }
        
        String trimmed = value.trim();
        
        if (trimmed.isEmpty()) {
            return value;
        }

        if (isQuotedString(trimmed)) {
            return trimmed.substring(1, trimmed.length() - 1);
        }

        if ("null".equalsIgnoreCase(trimmed)) {
            return null;
        }

        if ("true".equalsIgnoreCase(trimmed)) {
            return true;
        }

        if ("false".equalsIgnoreCase(trimmed)) {
            return false;
        }

        Object number = tryParseNumber(trimmed);
        return number != null ? number : value;
    }

    private boolean isQuotedString(String value) {
        return (value.startsWith("\"") && value.endsWith("\"")) ||
               (value.startsWith("'") && value.endsWith("'"));
    }

    private Object tryParseNumber(String value) {
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            } else {
                long longVal = Long.parseLong(value);
                return (longVal >= Integer.MIN_VALUE && longVal <= Integer.MAX_VALUE)
                        ? (int) longVal : longVal;
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }
}