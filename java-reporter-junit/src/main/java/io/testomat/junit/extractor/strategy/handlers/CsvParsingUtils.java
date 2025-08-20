package io.testomat.junit.extractor.strategy.handlers;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for parsing CSV-formatted strings with support for custom delimiters,
 * quote characters, and null value handling.
 * This class provides methods for parsing CSV data with proper quote handling,
 * delimiter customization, and automatic type conversion.
 */
public final class CsvParsingUtils {

    private static final Logger logger = LoggerFactory.getLogger(CsvParsingUtils.class);

    private CsvParsingUtils() {

    }

    /**
     * Parses a CSV string into an array of objects with automatic type conversion.
     *
     * @param csvString the CSV string to parse
     * @param delimiter the delimiter character (default: comma)
     * @param quoteCharacter the quote character (default: double quote)
     * @param nullValuesArray array of strings that should be treated as null
     * @return array of parsed and typed objects
     */
    public static Object[] parseCsvString(String csvString, String delimiter, 
                                        char quoteCharacter, String[] nullValuesArray) {
        if (csvString == null || csvString.trim().isEmpty()) {
            return new Object[0];
        }

        try {
            String actualDelimiter = (delimiter == null || delimiter.equals("\0")) ? "," : delimiter;
            List<String> values = smartCsvSplit(csvString, actualDelimiter, quoteCharacter);
            Object[] result = new Object[values.size()];
            for (int i = 0; i < values.size(); i++) {
                result[i] = processValue(values.get(i), nullValuesArray);
            }

            return result;

        } catch (Exception e) {
            logger.debug("Failed to parse CSV string: {}", csvString, e);
            return csvString.split(",");
        }
    }

    /**
     * Parses comma-separated values with simple splitting and type conversion.
     *
     * @param content the content to parse
     * @return array of parsed and typed objects
     */
    public static Object[] parseCommaSeparatedValues(String content) {
        if (content == null || content.trim().isEmpty()) {
            return new Object[0];
        }

        String[] parts = content.split(",");
        Object[] result = new Object[parts.length];
        
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            result[i] = parseTypedValue(part);
        }
        
        return result;
    }

    /**
     * Intelligently splits a CSV string respecting quotes and custom delimiters.
     *
     * @param input the CSV string to split
     * @param delimiter the delimiter to use
     * @param quoteCharacter the quote character to respect
     * @return list of parsed string values
     */
    private static List<String> smartCsvSplit(String input, String delimiter, char quoteCharacter) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            
            if (c == quoteCharacter) {
                if (inQuotes && i + 1 < input.length() && input.charAt(i + 1) == quoteCharacter) {
                    current.append(quoteCharacter);
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (!inQuotes && matchesDelimiter(input, i, delimiter)) {
                result.add(current.toString().trim());
                current.setLength(0);
                i += delimiter.length() - 1;
            } else {
                current.append(c);
            }
        }
        
        result.add(current.toString().trim());
        return result;
    }

    private static boolean matchesDelimiter(String input, int position, String delimiter) {
        if (position + delimiter.length() > input.length()) {
            return false;
        }
        return input.substring(position, position + delimiter.length()).equals(delimiter);
    }

    private static Object processValue(String value, String[] nullValuesArray) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        if (trimmed.length() >= 2 
            && ((trimmed.startsWith("\"") && trimmed.endsWith("\"")) 
            || (trimmed.startsWith("'") && trimmed.endsWith("'")))) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }

        if (nullValuesArray != null) {
            for (String nullValue : nullValuesArray) {
                if (trimmed.equals(nullValue)) {
                    return null;
                }
            }
        }

        if ("null".equals(trimmed) || "NULL".equals(trimmed)) {
            return null;
        }

        return parseTypedValue(trimmed);
    }

    private static Object parseTypedValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return value;
        }

        String trimmed = value.trim();

        if ("true".equalsIgnoreCase(trimmed)) {
            return true;
        }
        if ("false".equalsIgnoreCase(trimmed)) {
            return false;
        }

        try {
            if (trimmed.contains(".")) {
                return Double.parseDouble(trimmed);
            } else {
                long longVal = Long.parseLong(trimmed);
                if (longVal >= Integer.MIN_VALUE && longVal <= Integer.MAX_VALUE) {
                    return (int) longVal;
                }
                return longVal;
            }
        } catch (NumberFormatException e) {

        }

        return value;
    }
}