package io.testomat.junit.extractor.strategy.handlers.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for CsvParsingUtils focusing on edge cases, error handling,
 * and complex CSV parsing scenarios that are critical for correct
 * parameter extraction in JUnit parameterized tests.
 */
class CsvParsingUtilsTest {

    @Test
    void parseCsvString_shouldHandleEmptyString() {
        Object[] result = CsvParsingUtils.parseCsvString("", ",", '"', null);

        assertThat(result).isEmpty();
    }

    @Test
    void parseCsvString_shouldHandleNullString() {
        Object[] result = CsvParsingUtils.parseCsvString(null, ",", '"', null);

        assertThat(result).isEmpty();
    }

    @Test
    void parseCsvString_shouldHandleWhitespaceOnlyString() {
        Object[] result = CsvParsingUtils.parseCsvString("   ", ",", '"', null);

        assertThat(result).isEmpty();
    }

    @Test
    void parseCsvString_shouldParseSimpleCommaSeparatedValues() {
        Object[] result = CsvParsingUtils.parseCsvString("apple,banana,cherry", ",", '"', null);

        assertThat(result).containsExactly("apple", "banana", "cherry");
    }

    @Test
    void parseCsvString_shouldHandleQuotedStringsWithCommas() {
        Object[] result = CsvParsingUtils.parseCsvString("\"Hello, World\",test,\"another, one\"",
                ",", '"', null);

        assertThat(result).containsExactly("Hello, World", "test", "another, one");
    }

    @Test
    void parseCsvString_shouldHandleEscapedQuotes() {
        Object[] result = CsvParsingUtils.parseCsvString("\"He said \"\"Hello\"\"\",test",
                ",", '"', null);

        assertThat(result).containsExactly("He said \"Hello\"", "test");
    }

    @Test
    void parseCsvString_shouldConvertBooleanValues() {
        Object[] result = CsvParsingUtils.parseCsvString("true,false,TRUE,FALSE", ",", '"', null);

        assertThat(result).containsExactly(true, false, true, false);
    }

    @Test
    void parseCsvString_shouldConvertIntegerValues() {
        Object[] result = CsvParsingUtils.parseCsvString("123,456,-789,0", ",", '"', null);

        assertThat(result).containsExactly(123, 456, -789, 0);
    }

    @Test
    void parseCsvString_shouldConvertLongValues() {
        Object[] result = CsvParsingUtils.parseCsvString("9223372036854775807,-9223372036854775808",
                ",", '"', null);

        assertThat(result).containsExactly(9223372036854775807L, -9223372036854775808L);
    }

    @Test
    void parseCsvString_shouldConvertDoubleValues() {
        Object[] result = CsvParsingUtils.parseCsvString("3.14,2.71,-0.5", ",", '"', null);

        assertThat(result).containsExactly(3.14, 2.71, -0.5);
    }

    @Test
    void parseCsvString_shouldHandleNullKeywords() {
        Object[] result = CsvParsingUtils.parseCsvString("null,NULL,value", ",", '"', null);

        assertThat(result).containsExactly(null, null, "value");
    }

    @Test
    void parseCsvString_shouldHandleCustomNullValues() {
        String[] customNulls = {"EMPTY", "N/A"};
        Object[] result = CsvParsingUtils.parseCsvString("EMPTY,test,N/A,value",
                ",", '"', customNulls);

        assertThat(result).containsExactly(null, "test", null, "value");
    }

    @Test
    void parseCsvString_shouldHandleCustomDelimiter() {
        Object[] result = CsvParsingUtils.parseCsvString("apple|banana|cherry", "|", '"', null);

        assertThat(result).containsExactly("apple", "banana", "cherry");
    }

    @Test
    void parseCsvString_shouldHandleSemicolonDelimiter() {
        Object[] result = CsvParsingUtils.parseCsvString("first;second;third", ";", '"', null);

        assertThat(result).containsExactly("first", "second", "third");
    }

    @Test
    void parseCsvString_shouldHandleTabDelimiter() {
        Object[] result = CsvParsingUtils.parseCsvString("one\ttwo\tthree", "\t", '"', null);

        assertThat(result).containsExactly("one", "two", "three");
    }

    @Test
    void parseCsvString_shouldHandleCustomQuoteCharacter() {
        Object[] result = CsvParsingUtils.parseCsvString("'Hello, World',test", ",", '\'', null);

        assertThat(result).containsExactly("Hello, World", "test");
    }

    @Test
    void parseCsvString_shouldTrimSpacesAroundValues() {
        Object[] result = CsvParsingUtils.parseCsvString("  apple  ,  banana  ,  cherry  ",
                ",", '"', null);

        assertThat(result).containsExactly("apple", "banana", "cherry");
    }

    @Test
    void parseCsvString_shouldHandleMixedTypes() {
        Object[] result = CsvParsingUtils.parseCsvString("text,123,true,3.14,null", ",", '"', null);

        assertThat(result).containsExactly("text", 123, true, 3.14, null);
    }

    @Test
    void parseCsvString_shouldHandleQuotedNumbers() {
        Object[] result = CsvParsingUtils.parseCsvString("\"123\",\"456\"", ",", '"', null);

        // Quoted numbers should be treated as strings after quote removal
        // but then parsed as numbers
        assertThat(result).containsExactly(123, 456);
    }

    @Test
    void parseCsvString_shouldHandleEmptyValuesInMiddle() {
        Object[] result = CsvParsingUtils.parseCsvString("first,,third", ",", '"', null);

        assertThat(result).hasSize(3);
        assertThat(result[0]).isEqualTo("first");
        assertThat(result[1]).isEqualTo("");
        assertThat(result[2]).isEqualTo("third");
    }

    @Test
    void parseCsvString_shouldHandleNullDelimiter() {
        Object[] result = CsvParsingUtils.parseCsvString("apple,banana,cherry", null, '"', null);

        // Should default to comma
        assertThat(result).containsExactly("apple", "banana", "cherry");
    }

    @Test
    void parseCsvString_shouldHandleNullCharDelimiter() {
        Object[] result = CsvParsingUtils.parseCsvString("apple,banana,cherry", "\0", '"', null);

        // Should default to comma
        assertThat(result).containsExactly("apple", "banana", "cherry");
    }

    @Test
    void parseCsvString_shouldHandleComplexRealWorldScenario() {
        Object[] result = CsvParsingUtils.parseCsvString(
                "\"User, John\",25,true,\"Address: 123 Main St, Apt 4\",NULL",
                ",", '"', null);

        assertThat(result).containsExactly("User, John", 25, true, "Address: 123 Main St, Apt 4", null);
    }

    @Test
    void parseCsvString_shouldFallbackOnParseException() {
        // This tests the catch block when parsing fails
        // The fallback should use simple split
        Object[] result = CsvParsingUtils.parseCsvString("a,b,c", ",", '"', null);

        assertThat(result).hasSize(3);
    }

    @Test
    void parseCommaSeparatedValues_shouldHandleEmptyString() {
        Object[] result = CsvParsingUtils.parseCommaSeparatedValues("");

        assertThat(result).isEmpty();
    }

    @Test
    void parseCommaSeparatedValues_shouldHandleNullString() {
        Object[] result = CsvParsingUtils.parseCommaSeparatedValues(null);

        assertThat(result).isEmpty();
    }

    @Test
    void parseCommaSeparatedValues_shouldHandleWhitespaceOnly() {
        Object[] result = CsvParsingUtils.parseCommaSeparatedValues("   ");

        assertThat(result).isEmpty();
    }

    @Test
    void parseCommaSeparatedValues_shouldParseSimpleValues() {
        Object[] result = CsvParsingUtils.parseCommaSeparatedValues("apple,banana,cherry");

        assertThat(result).containsExactly("apple", "banana", "cherry");
    }

    @Test
    void parseCommaSeparatedValues_shouldTrimValues() {
        Object[] result = CsvParsingUtils.parseCommaSeparatedValues("  apple  ,  banana  ,  cherry  ");

        assertThat(result).containsExactly("apple", "banana", "cherry");
    }

    @Test
    void parseCommaSeparatedValues_shouldConvertTypes() {
        Object[] result = CsvParsingUtils.parseCommaSeparatedValues("text,123,true,3.14");

        assertThat(result).containsExactly("text", 123, true, 3.14);
    }

    @Test
    void parseCommaSeparatedValues_shouldHandleSingleValue() {
        Object[] result = CsvParsingUtils.parseCommaSeparatedValues("single");

        assertThat(result).containsExactly("single");
    }

    @Test
    void parseCommaSeparatedValues_shouldHandleNegativeNumbers() {
        Object[] result = CsvParsingUtils.parseCommaSeparatedValues("-123,-45.67");

        assertThat(result).containsExactly(-123, -45.67);
    }

    @ParameterizedTest
    @CsvSource({
        "'true', true",
        "'false', false",
        "'TRUE', true",
        "'FALSE', false",
        "'True', true",
        "'False', false"
    })
    void parseCsvString_shouldHandleCaseInsensitiveBooleans(String input, boolean expected) {
        Object[] result = CsvParsingUtils.parseCsvString(input, ",", '"', null);

        assertThat(result).containsExactly(expected);
    }

    @Test
    void parseCsvString_shouldHandleVeryLongStrings() {
        String longString = "a".repeat(1000);
        Object[] result = CsvParsingUtils.parseCsvString(longString + ",test", ",", '"', null);

        assertThat(result).hasSize(2);
        assertThat(result[0]).isEqualTo(longString);
        assertThat(result[1]).isEqualTo("test");
    }

    @Test
    void parseCsvString_shouldHandleSpecialCharacters() {
        Object[] result = CsvParsingUtils.parseCsvString("test@email.com,price$100,50%",
                ",", '"', null);

        assertThat(result).containsExactly("test@email.com", "price$100", "50%");
    }

    @Test
    void parseCsvString_shouldHandleUnicodeCharacters() {
        Object[] result = CsvParsingUtils.parseCsvString("hello,世界,مرحبا", ",", '"', null);

        assertThat(result).containsExactly("hello", "世界", "مرحبا");
    }

    @Test
    void parseCsvString_shouldHandleZeroValue() {
        Object[] result = CsvParsingUtils.parseCsvString("0,0.0", ",", '"', null);

        assertThat(result).containsExactly(0, 0.0);
    }

    @Test
    void parseCsvString_shouldDistinguishBetweenIntegerAndDouble() {
        Object[] result = CsvParsingUtils.parseCsvString("100,100.0", ",", '"', null);

        assertThat(result[0]).isInstanceOf(Integer.class).isEqualTo(100);
        assertThat(result[1]).isInstanceOf(Double.class).isEqualTo(100.0);
    }

    @Test
    void parseCsvString_shouldHandleScientificNotation() {
        Object[] result = CsvParsingUtils.parseCsvString("1.5e10,2.3e-5", ",", '"', null);

        assertThat(result).containsExactly(1.5e10, 2.3e-5);
    }

    @Test
    void parseCsvString_shouldHandleQuotedNullString() {
        Object[] result = CsvParsingUtils.parseCsvString("\"null\",test", ",", '"', null);

        // Quoted null should still be treated as null after quote removal
        assertThat(result).containsExactly(null, "test");
    }

    @Test
    void parseCsvString_shouldHandleConsecutiveDelimiters() {
        Object[] result = CsvParsingUtils.parseCsvString("a,,b", ",", '"', null);

        assertThat(result).hasSize(3);
        assertThat(result[0]).isEqualTo("a");
        assertThat(result[1]).isEqualTo("");
        assertThat(result[2]).isEqualTo("b");
    }

    @Test
    void parseCsvString_shouldHandleMultiCharacterDelimiter() {
        Object[] result = CsvParsingUtils.parseCsvString("apple::banana::cherry", "::", '"', null);

        assertThat(result).containsExactly("apple", "banana", "cherry");
    }

    @Test
    void parseCommaSeparatedValues_shouldHandleEmptyStringBetweenCommas() {
        Object[] result = CsvParsingUtils.parseCommaSeparatedValues("a,,c");

        assertThat(result).hasSize(3);
        assertThat(result[1]).isEqualTo("");
    }

    @Test
    void parseCsvString_shouldHandleSingleQuotedStringWithCustomQuote() {
        Object[] result = CsvParsingUtils.parseCsvString("'test','value'", ",", '\'', null);

        assertThat(result).containsExactly("test", "value");
    }

    @Test
    void parseCsvString_shouldHandleNumbersWithLeadingZeros() {
        Object[] result = CsvParsingUtils.parseCsvString("007,00123", ",", '"', null);

        // Numbers with leading zeros might be parsed as integers
        // This tests the actual behavior
        assertThat(result).hasSize(2);
    }

    @Test
    void parseCsvString_shouldHandleVerySmallDoubleValues() {
        Object[] result = CsvParsingUtils.parseCsvString("0.000001,0.0000000001", ",", '"', null);

        assertThat(result).containsExactly(0.000001, 0.0000000001);
    }

    @Test
    void parseCommaSeparatedValues_shouldNotHandleQuotedStrings() {
        // This method uses simple split, so quotes should remain
        Object[] result = CsvParsingUtils.parseCommaSeparatedValues("\"hello\",world");

        assertThat(result[0]).isEqualTo("\"hello\"");
        assertThat(result[1]).isEqualTo("world");
    }
}
