package io.testomat.junit.extractor.strategy;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Strategy for extracting parameters from @CsvSource annotated parameterized tests.
 * Supports extraction of CSV values from @CsvSource annotations with various configurations
 * including custom delimiters, null representations, and quote characters.
 */
public class CsvSourceExtractionStrategy implements ParameterExtractionStrategy {

    private static final Logger logger = LoggerFactory.getLogger(CsvSourceExtractionStrategy.class);
    private static final Pattern DISPLAY_NAME_PATTERN = Pattern.compile("^\\[\\d+\\]\\s*(.*)$");

    @Override
    public boolean supports(ParameterExtractionContext context) {
        return context.isValid() && context.hasAnnotation(CsvSource.class);
    }

    @Override
    public Object extractParameters(ParameterExtractionContext context) 
            throws ParameterExtractionException {
        if (!supports(context)) {
            return null;
        }

        try {
            CsvSource csvSource = context.getAnnotation(CsvSource.class);
            if (csvSource == null) {
                return null;
            }

            // Try to extract from display name first 
            // (most reliable for getting actual parameter values)
            ParseResult parseResult = extractFromDisplayNameWithResult(context, csvSource);
            if (parseResult.isSuccessful()) {
                return formatParameters(parseResult.getValue(), context);
            }

            // Fallback: extract from annotation 
            // (won't give us the specific values for this invocation)
            Object[] annotationValues = extractFromAnnotation(csvSource, context);
            return formatParameters(annotationValues, context);

        } catch (Exception e) {
            logger.debug("Failed to extract CsvSource parameters for: {}", 
                        context.getDisplayName(), e);
            throw new ParameterExtractionException("Failed to extract CsvSource parameters", e);
        }
    }

    @Override
    public int getPriority() {
        return 10; // Standard priority for simple source annotations
    }

    @Override
    public String getStrategyName() {
        return "CsvSourceExtractionStrategy";
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
                                                         CsvSource csvSource) {
        String displayName = context.getDisplayName();
        if (displayName == null || displayName.trim().isEmpty()) {
            return ParseResult.failure();
        }

        // JUnit 5 parameterized tests format: "[1] value1, value2, value3" etc.
        Matcher matcher = DISPLAY_NAME_PATTERN.matcher(displayName.trim());
        if (matcher.matches()) {
            String csvStr = matcher.group(1).trim();
            Object[] parsedValues = parseCsvString(csvStr, csvSource);
            return ParseResult.success(parsedValues);
        }

        return ParseResult.failure();
    }

    private Object[] extractFromAnnotation(CsvSource csvSource, 
                                           ParameterExtractionContext context) {
        try {
            String[] csvLines = csvSource.value();
            if (csvLines == null || csvLines.length == 0) {
                return new Object[0];
            }

            // Return first CSV line parsed as fallback
            String firstLine = csvLines[0];
            return parseCsvString(firstLine, csvSource);

        } catch (Exception e) {
            logger.debug("Failed to extract CSV from annotation", e);
            return new Object[0];
        }
    }

    private Object[] parseCsvString(String csvString, CsvSource csvSource) {
        if (csvString == null || csvString.trim().isEmpty()) {
            return new Object[0];
        }

        try {
            // Get configuration from annotation
            char delimiterChar = csvSource.delimiter();
            String delimiter = String.valueOf(delimiterChar);
            String[] nullValuesArray = new String[0]; // Default empty array
            char quoteCharacter = '"'; // Default quote character
            
            // Use default delimiter if delimiter is default character
            if (delimiterChar == '\0' || delimiter.equals("\0")) {
                delimiter = ",";
            }

            // Parse CSV string
            List<String> values = smartCsvSplit(csvString, delimiter, quoteCharacter);
            
            // Convert to typed objects and handle null values
            Object[] result = new Object[values.size()];
            for (int i = 0; i < values.size(); i++) {
                result[i] = processValue(values.get(i), nullValuesArray);
            }

            return result;

        } catch (Exception e) {
            logger.debug("Failed to parse CSV string: {}", csvString, e);
            // Fallback to simple comma split
            return csvString.split(",");
        }
    }

    private List<String> smartCsvSplit(String input, String delimiter, char quoteCharacter) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            
            if (c == quoteCharacter) {
                // Handle quote character
                if (inQuotes && i + 1 < input.length() && input.charAt(i + 1) == quoteCharacter) {
                    // Escaped quote (double quote)
                    current.append(quoteCharacter);
                    i++; // Skip next quote
                } else {
                    // Toggle quote state
                    inQuotes = !inQuotes;
                }
            } else if (!inQuotes && matchesDelimiter(input, i, delimiter)) {
                // Found delimiter outside quotes
                result.add(current.toString().trim());
                current.setLength(0);
                i += delimiter.length() - 1; // Skip delimiter characters
            } else {
                current.append(c);
            }
        }
        
        // Add last value
        result.add(current.toString().trim());
        return result;
    }

    private boolean matchesDelimiter(String input, int position, String delimiter) {
        if (position + delimiter.length() > input.length()) {
            return false;
        }
        return input.substring(position, position + delimiter.length()).equals(delimiter);
    }

    private Object processValue(String value, String[] nullValuesArray) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        // Remove surrounding quotes if present
        if (trimmed.length() >= 2 
            && ((trimmed.startsWith("\"") && trimmed.endsWith("\"")) 
            || (trimmed.startsWith("'") && trimmed.endsWith("'")))) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }

        // Handle null values
        if (nullValuesArray != null && nullValuesArray.length > 0) {
            for (String nullValue : nullValuesArray) {
                if (trimmed.equals(nullValue)) {
                    return null;
                }
            }
        }

        // Handle default null representations
        if ("null".equals(trimmed) || "NULL".equals(trimmed)) {
            return null;
        }

        // Try to parse as different types
        return parseTypedValue(trimmed);
    }

    private Object parseTypedValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return value;
        }

        String trimmed = value.trim();

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
