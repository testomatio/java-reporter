package com.testomatio.reporter.propertyconfig.util;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringUtilsTest {

    @Test
    void testToEnvStyle_NullInput() {
        String result = StringUtils.toEnvStyle(null);
        assertEquals("", result);
    }

    @Test
    void testToEnvStyle_EmptyString() {
        String result = StringUtils.toEnvStyle("");
        assertEquals("", result);
    }

    @Test
    void testToEnvStyle_SimpleString() {
        String result = StringUtils.toEnvStyle("simple");
        assertEquals("SIMPLE", result);
    }

    @Test
    void testToEnvStyle_StringWithDots() {
        String result = StringUtils.toEnvStyle("my.property.name");
        assertEquals("MY_PROPERTY_NAME", result);
    }

    @Test
    void testToEnvStyle_StringWithMultipleDots() {
        String result = StringUtils.toEnvStyle("a.b.c.d.e");
        assertEquals("A_B_C_D_E", result);
    }

    @Test
    void testToEnvStyle_StringWithConsecutiveDots() {
        String result = StringUtils.toEnvStyle("my..property");
        assertEquals("MY__PROPERTY", result);
    }

    @Test
    void testToEnvStyle_MixedCase() {
        String result = StringUtils.toEnvStyle("MyProperty.Name");
        assertEquals("MYPROPERTY_NAME", result);
    }

    @Test
    void testToEnvStyle_AlreadyUpperCase() {
        String result = StringUtils.toEnvStyle("ALREADY.UPPER");
        assertEquals("ALREADY_UPPER", result);
    }

    @Test
    void testToEnvStyle_WithNumbers() {
        String result = StringUtils.toEnvStyle("property123.value456");
        assertEquals("PROPERTY123_VALUE456", result);
    }

    @Test
    void testToEnvStyle_WithSpecialCharacters() {
        String result = StringUtils.toEnvStyle("prop-erty.val@ue");
        assertEquals("PROP-ERTY_VAL@UE", result);
    }

    @Test
    void testToEnvStyle_OnlyDots() {
        String result = StringUtils.toEnvStyle("...");
        assertEquals("___", result);
    }

    @Test
    void testToEnvStyle_SingleCharacter() {
        String result = StringUtils.toEnvStyle("a");
        assertEquals("A", result);
    }

    @Test
    void testToEnvStyle_SingleDot() {
        String result = StringUtils.toEnvStyle(".");
        assertEquals("_", result);
    }

    // Tests for fromEnvStyle method

    @Test
    void testFromEnvStyle_NullInput() {
        String result = StringUtils.fromEnvStyle(null);
        assertEquals("", result);
    }

    @Test
    void testFromEnvStyle_EmptyString() {
        String result = StringUtils.fromEnvStyle("");
        assertEquals("", result);
    }

    @Test
    void testFromEnvStyle_SimpleString() {
        String result = StringUtils.fromEnvStyle("SIMPLE");
        assertEquals("simple", result);
    }

    @Test
    void testFromEnvStyle_StringWithUnderscores() {
        String result = StringUtils.fromEnvStyle("MY_PROPERTY_NAME");
        assertEquals("my.property.name", result);
    }

    @Test
    void testFromEnvStyle_StringWithMultipleUnderscores() {
        String result = StringUtils.fromEnvStyle("A_B_C_D_E");
        assertEquals("a.b.c.d.e", result);
    }

    @Test
    void testFromEnvStyle_StringWithConsecutiveUnderscores() {
        String result = StringUtils.fromEnvStyle("MY__PROPERTY");
        assertEquals("my..property", result);
    }

    @Test
    void testFromEnvStyle_MixedCase() {
        String result = StringUtils.fromEnvStyle("MyProperty_Name");
        assertEquals("myproperty.name", result);
    }

    @Test
    void testFromEnvStyle_AlreadyLowerCase() {
        String result = StringUtils.fromEnvStyle("already_lower");
        assertEquals("already.lower", result);
    }

    @Test
    void testFromEnvStyle_WithNumbers() {
        String result = StringUtils.fromEnvStyle("PROPERTY123_VALUE456");
        assertEquals("property123.value456", result);
    }

    @Test
    void testFromEnvStyle_WithSpecialCharacters() {
        String result = StringUtils.fromEnvStyle("PROP-ERTY_VAL@UE");
        assertEquals("prop-erty.val@ue", result);
    }

    @Test
    void testFromEnvStyle_OnlyUnderscores() {
        String result = StringUtils.fromEnvStyle("___");
        assertEquals("...", result);
    }

    @Test
    void testFromEnvStyle_SingleCharacter() {
        String result = StringUtils.fromEnvStyle("A");
        assertEquals("a", result);
    }

    @Test
    void testFromEnvStyle_SingleUnderscore() {
        String result = StringUtils.fromEnvStyle("_");
        assertEquals(".", result);
    }

    // Tests for isNullOrEmpty method

    @Test
    void testIsNullOrEmpty_NullValue() {
        assertTrue(StringUtils.isNullOrEmpty(null));
    }

    @Test
    void testIsNullOrEmpty_EmptyString() {
        assertTrue(StringUtils.isNullOrEmpty(""));
    }

    @Test
    void testIsNullOrEmpty_Whitespace() {
        assertFalse(StringUtils.isNullOrEmpty(" "));
        assertFalse(StringUtils.isNullOrEmpty("   "));
        assertFalse(StringUtils.isNullOrEmpty("\t"));
        assertFalse(StringUtils.isNullOrEmpty("\n"));
    }

    @Test
    void testIsNullOrEmpty_NonEmptyString() {
        assertFalse(StringUtils.isNullOrEmpty("a"));
        assertFalse(StringUtils.isNullOrEmpty("test"));
        assertFalse(StringUtils.isNullOrEmpty("123"));
        assertFalse(StringUtils.isNullOrEmpty("!@#$%"));
    }

    // Parameterized tests for comprehensive coverage

    @ParameterizedTest
    @MethodSource("provideStringsForToEnvStyle")
    void testToEnvStyle_Parameterized(String input, String expected) {
        assertEquals(expected, StringUtils.toEnvStyle(input));
    }

    private static Stream<Arguments> provideStringsForToEnvStyle() {
        return Stream.of(
                Arguments.of("simple.property", "SIMPLE_PROPERTY"),
                Arguments.of("db.connection.url", "DB_CONNECTION_URL"),
                Arguments.of("my.app.config.value", "MY_APP_CONFIG_VALUE"),
                Arguments.of("CamelCase.Property", "CAMELCASE_PROPERTY"),
                Arguments.of("with123numbers.here456", "WITH123NUMBERS_HERE456"),
                Arguments.of("start.end", "START_END"),
                Arguments.of(".start", "_START"),
                Arguments.of("end.", "END_"),
                Arguments.of("no_dots_here", "NO_DOTS_HERE")
        );
    }

    @ParameterizedTest
    @MethodSource("provideStringsForFromEnvStyle")
    void testFromEnvStyle_Parameterized(String input, String expected) {
        assertEquals(expected, StringUtils.fromEnvStyle(input));
    }

    private static Stream<Arguments> provideStringsForFromEnvStyle() {
        return Stream.of(
                Arguments.of("SIMPLE_PROPERTY", "simple.property"),
                Arguments.of("DB_CONNECTION_URL", "db.connection.url"),
                Arguments.of("MY_APP_CONFIG_VALUE", "my.app.config.value"),
                Arguments.of("CAMELCASE_PROPERTY", "camelcase.property"),
                Arguments.of("WITH123NUMBERS_HERE456", "with123numbers.here456"),
                Arguments.of("START_END", "start.end"),
                Arguments.of("_START", ".start"),
                Arguments.of("END_", "end."),
                Arguments.of("NO.DOTS.HERE", "no.dots.here")
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testToEnvStyle_NullAndEmpty(String input) {
        assertEquals("", StringUtils.toEnvStyle(input));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testFromEnvStyle_NullAndEmpty(String input) {
        assertEquals("", StringUtils.fromEnvStyle(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"test", "TEST", "Test", "tEsT"})
    void testIsNullOrEmpty_NonEmpty(String input) {
        assertFalse(StringUtils.isNullOrEmpty(input));
    }

    // Round-trip conversion tests

    @Test
    void testRoundTripConversion_SimpleCase() {
        String original = "my.property.name";
        String envStyle = StringUtils.toEnvStyle(original);
        String backToOriginal = StringUtils.fromEnvStyle(envStyle);
        assertEquals(original, backToOriginal);
    }

    @Test
    void testRoundTripConversion_ComplexCase() {
        String original = "complex.property.with.many.dots";
        String envStyle = StringUtils.toEnvStyle(original);
        assertEquals("COMPLEX_PROPERTY_WITH_MANY_DOTS", envStyle);
        String backToOriginal = StringUtils.fromEnvStyle(envStyle);
        assertEquals(original, backToOriginal);
    }

    @Test
    void testRoundTripConversion_EdgeCases() {
        // Test edge cases that might not survive round-trip due to case sensitivity
        String original = "MiXeD.CaSe.Property";
        String envStyle = StringUtils.toEnvStyle(original);
        String backToOriginal = StringUtils.fromEnvStyle(envStyle);
        // Note: original case is lost in conversion
        assertEquals("mixed.case.property", backToOriginal);
    }

    // Unicode and special character tests

    @Test
    void testToEnvStyle_UnicodeCharacters() {
        String result = StringUtils.toEnvStyle("привет.мир");
        assertEquals("ПРИВЕТ_МИР", result);
    }

    @Test
    void testFromEnvStyle_UnicodeCharacters() {
        String result = StringUtils.fromEnvStyle("ПРИВЕТ_МИР");
        assertEquals("привет.мир", result);
    }

    @Test
    void testIsNullOrEmpty_UnicodeCharacters() {
        assertFalse(StringUtils.isNullOrEmpty("привет"));
        assertFalse(StringUtils.isNullOrEmpty("世界"));
        assertFalse(StringUtils.isNullOrEmpty("🎉"));
    }
}