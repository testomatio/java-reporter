package io.testomat.junit.extractor.strategy.handlers;

import io.testomat.junit.extractor.strategy.ParameterExtractionContext;
import io.testomat.junit.extractor.strategy.handlers.util.CsvParsingUtils;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Parameter extraction handler for @CsvSource annotated parameterized tests.
 * Supports extraction of CSV values with configurable delimiters, null representations,
 * and quote characters. Provides automatic type conversion for parsed values.
 */
public class CsvSourceHandler extends AbstractParameterExtractionHandler {



    @Override
    public String getStrategyName() {
        return "CsvSourceExtractionStrategy";
    }


    @Override
    protected Object parseDisplayNameValue(String valueStr, ParameterExtractionContext context) {
        CsvSource csvSource = context.getAnnotation(CsvSource.class);
        if (csvSource == null) {
            return CsvParsingUtils.parseCommaSeparatedValues(valueStr);
        }
        return parseCsvString(valueStr, csvSource);
    }

    @Override
    protected Object extractFromAnnotation(ParameterExtractionContext context) {
        CsvSource csvSource = context.getAnnotation(CsvSource.class);
        if (csvSource == null) {
            return new Object[0];
        }
        try {
            String[] csvLines = csvSource.value();
            if (csvLines == null || csvLines.length == 0) {
                return new Object[0];
            }

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
            char delimiterChar = csvSource.delimiter();
            String delimiter = String.valueOf(delimiterChar);
            String[] nullValuesArray = new String[0];
            char quoteCharacter = '"';
            if (delimiterChar == '\0' || delimiter.equals("\0")) {
                delimiter = ",";
            }

            return CsvParsingUtils.parseCsvString(csvString, delimiter, quoteCharacter, nullValuesArray);

        } catch (Exception e) {
            logger.debug("Failed to parse CSV string: {}", csvString, e);
            return csvString.split(",");
        }
    }
}
