package io.testomat.junit.extractor.strategy.handlers;

import io.testomat.junit.extractor.strategy.ParameterExtractionContext;
import io.testomat.junit.exception.ParameterExtractionException;
import io.testomat.junit.extractor.strategy.ParameterExtractionHandler;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Strategy for extracting parameters from @CsvFileSource annotated parameterized tests.
 * Supports extraction of CSV values from files specified in @CsvFileSource annotations
 * with various configurations including custom delimiters, null representations, and encoding.
 */
public class CsvFileSourceHandler implements ParameterExtractionHandler {

    private static final Logger logger = LoggerFactory.getLogger(CsvFileSourceHandler.class);
    private static final Pattern DISPLAY_NAME_PATTERN = Pattern.compile("^\\[\\d+\\]\\s*(.*)$");

    @Override
    public boolean supports(ParameterExtractionContext context) {
        return context.isValid() && context.hasAnnotation(CsvFileSource.class);
    }

    @Override
    public Object extractParameters(ParameterExtractionContext context) 
            throws ParameterExtractionException {
        if (!supports(context)) {
            return null;
        }

        try {
            CsvFileSource csvFileSource = context.getAnnotation(CsvFileSource.class);
            if (csvFileSource == null) {
                return null;
            }

            ParseResult parseResult = extractFromDisplayNameWithResult(context, csvFileSource);
            if (parseResult.isSuccessful()) {
                return formatParameters(parseResult.getValue(), context);
            }

            Object[] fileValues = extractFromFile(csvFileSource, context);
            return formatParameters(fileValues, context);

        } catch (Exception e) {
            logger.debug("Failed to extract CsvFileSource parameters for: {}", 
                        context.getDisplayName(), e);
            throw new ParameterExtractionException("Failed to extract CsvFileSource parameters", e);
        }
    }

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public String getStrategyName() {
        return "CsvFileSourceExtractionStrategy";
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
                                                         CsvFileSource csvFileSource) {
        String displayName = context.getDisplayName();
        if (displayName == null || displayName.trim().isEmpty()) {
            return ParseResult.failure();
        }

        Matcher matcher = DISPLAY_NAME_PATTERN.matcher(displayName.trim());
        if (matcher.matches()) {
            String csvStr = matcher.group(1).trim();
            Object[] parsedValues = parseCsvString(csvStr, csvFileSource);
            return ParseResult.success(parsedValues);
        }

        return ParseResult.failure();
    }

    private Object[] extractFromFile(CsvFileSource csvFileSource,
                                    ParameterExtractionContext context) {
        try {
            String[] resources = csvFileSource.resources();
            if (resources == null || resources.length == 0) {
                return new Object[0];
            }

            String resourcePath = resources[0];
            List<String> lines = readCsvFile(resourcePath, csvFileSource);
            
            if (lines.isEmpty()) {
                return new Object[0];
            }

            int numLinesToSkip = csvFileSource.numLinesToSkip();
            if (numLinesToSkip > 0 && lines.size() > numLinesToSkip) {
                lines = lines.subList(numLinesToSkip, lines.size());
            }

            for (String line : lines) {
                if (line != null && !line.trim().isEmpty()) {
                    return parseCsvString(line, csvFileSource);
                }
            }

            return new Object[0];

        } catch (Exception e) {
            logger.debug("Failed to extract CSV from file", e);
            return new Object[0];
        }
    }

    private List<String> readCsvFile(String resourcePath, CsvFileSource csvFileSource)
            throws IOException {
        List<String> lines = new ArrayList<>();
        
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (inputStream != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }
        } else {
            try (BufferedReader reader = new BufferedReader(new FileReader(resourcePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }
        }

        return lines;
    }

    private Object[] parseCsvString(String csvString, CsvFileSource csvFileSource) {
        if (csvString == null || csvString.trim().isEmpty()) {
            return new Object[0];
        }

        try {
            char delimiterChar = csvFileSource.delimiter();
            String delimiter = String.valueOf(delimiterChar);
            String[] nullValuesArray = new String[0];
            char quoteCharacter = '"';
            
            if (delimiterChar == '\0' || delimiter.equals("\0")) {
                delimiter = ",";
            }

            List<String> values = smartCsvSplit(csvString, delimiter, quoteCharacter);
            
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

    private List<String> smartCsvSplit(String input, String delimiter, char quoteCharacter) {
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

        if (trimmed.length() >= 2
            && ((trimmed.startsWith("\"") && trimmed.endsWith("\"")) 
            || (trimmed.startsWith("'") && trimmed.endsWith("'")))) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }

        if (nullValuesArray != null && nullValuesArray.length > 0) {
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

    private Object parseTypedValue(String value) {
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
                if (longVal >= Integer.MIN_VALUE
                    && longVal <= Integer.MAX_VALUE) {
                    return (int) longVal;
                }
                return longVal;
            }
        } catch (NumberFormatException e) {
        }

        return value;
    }

    private Object formatParameters(Object[] values, 
                                    ParameterExtractionContext context) {
        if (values == null || values.length == 0) {
            return new LinkedHashMap<String, Object>();
        }

        Method method = context.getTestMethod();
        if (method == null) {
            Map<String, Object> parameterMap = new LinkedHashMap<>();
            for (int i = 0; i < values.length; i++) {
                parameterMap.put("param" + i, values[i]);
            }
            return parameterMap;
        }

        Map<String, Object> parameterMap = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i++) {
            String parameterName = context.getParameterName(i);
            parameterMap.put(parameterName, values[i]);
        }

        return parameterMap;
    }

}