package io.testomat.core.propertyconfig.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("StringUtils Tests")
class StringUtilsTest {

    @Test
    @DisplayName("Should convert to environment variable style")
    void testToEnvStyle() {
        assertEquals("TESTOMATIO", StringUtils.toEnvStyle("testomatio"));
        assertEquals("TESTOMATIO_URL", StringUtils.toEnvStyle("testomatio.url"));
        assertEquals("TESTOMATIO_API_KEY", StringUtils.toEnvStyle("testomatio.api.key"));
    }

    @Test
    @DisplayName("Should handle empty string in toEnvStyle")
    void testToEnvStyleWithEmptyString() {
        assertEquals("", StringUtils.toEnvStyle(""));
    }

    @Test
    @DisplayName("Should handle null in toEnvStyle")
    void testToEnvStyleWithNull() {
        assertEquals("", StringUtils.toEnvStyle(null));
    }

    @Test
    @DisplayName("Should convert already uppercase string in toEnvStyle")
    void testToEnvStyleWithUppercase() {
        assertEquals("TESTOMATIO", StringUtils.toEnvStyle("TESTOMATIO"));
        assertEquals("TESTOMATIO_URL", StringUtils.toEnvStyle("TESTOMATIO_URL"));
    }

    @Test
    @DisplayName("Should handle mixed case in toEnvStyle")
    void testToEnvStyleWithMixedCase() {
        assertEquals("TESTOMATIO_URL", StringUtils.toEnvStyle("testomatio.URL"));
        assertEquals("TEST_MIXED_CASE", StringUtils.toEnvStyle("test.Mixed.Case"));
    }

    @Test
    @DisplayName("Should convert from environment variable style to dot notation")
    void testFromEnvStyle() {
        assertEquals("testomatio", StringUtils.fromEnvStyle("TESTOMATIO"));
        assertEquals("testomatio.url", StringUtils.fromEnvStyle("TESTOMATIO_URL"));
        assertEquals("testomatio.api.key", StringUtils.fromEnvStyle("TESTOMATIO_API_KEY"));
    }

    @Test
    @DisplayName("Should handle empty string in fromEnvStyle")
    void testFromEnvStyleWithEmptyString() {
        assertEquals("", StringUtils.fromEnvStyle(""));
    }

    @Test
    @DisplayName("Should handle null in fromEnvStyle")
    void testFromEnvStyleWithNull() {
        assertEquals("", StringUtils.fromEnvStyle(null));
    }

    @Test
    @DisplayName("Should convert already lowercase string in fromEnvStyle")
    void testFromEnvStyleWithLowercase() {
        assertEquals("testomatio", StringUtils.fromEnvStyle("testomatio"));
        assertEquals("testomatio.url", StringUtils.fromEnvStyle("testomatio.url"));
    }

    @Test
    @DisplayName("Should handle mixed case in fromEnvStyle")
    void testFromEnvStyleWithMixedCase() {
        assertEquals("testomatio.url", StringUtils.fromEnvStyle("Testomatio_URL"));
        assertEquals("test.mixed.case", StringUtils.fromEnvStyle("TEST_Mixed_Case"));
    }

    @Test
    @DisplayName("Should detect null strings with isNullOrEmpty")
    void testIsNullOrEmptyWithNull() {
        assertTrue(StringUtils.isNullOrEmpty(null));
    }

    @Test
    @DisplayName("Should detect empty strings with isNullOrEmpty")
    void testIsNullOrEmptyWithEmpty() {
        assertTrue(StringUtils.isNullOrEmpty(""));
    }

    @Test
    @DisplayName("Should return false for non-empty strings with isNullOrEmpty")
    void testIsNullOrEmptyWithNonEmpty() {
        assertFalse(StringUtils.isNullOrEmpty("testomatio"));
        assertFalse(StringUtils.isNullOrEmpty("a"));
        assertFalse(StringUtils.isNullOrEmpty(" "));
    }

    @Test
    @DisplayName("Should handle whitespace in isNullOrEmpty")
    void testIsNullOrEmptyWithWhitespace() {
        assertFalse(StringUtils.isNullOrEmpty(" "));
        assertFalse(StringUtils.isNullOrEmpty("  "));
        assertFalse(StringUtils.isNullOrEmpty("\t"));
        assertFalse(StringUtils.isNullOrEmpty("\n"));
    }

    @Test
    @DisplayName("Should handle round-trip conversion to and from env style")
    void testRoundTripConversion() {
        String original = "testomatio.api.key";
        String envStyle = StringUtils.toEnvStyle(original);
        String backToDot = StringUtils.fromEnvStyle(envStyle);

        assertEquals("TESTOMATIO_API_KEY", envStyle);
        assertEquals(original, backToDot);
    }

    @Test
    @DisplayName("Should handle strings with no dots or underscores")
    void testStringsWithoutDelimiters() {
        assertEquals("TESTOMATIO", StringUtils.toEnvStyle("testomatio"));
        assertEquals("testomatio", StringUtils.fromEnvStyle("TESTOMATIO"));
    }

    @Test
    @DisplayName("Should handle strings with multiple consecutive dots")
    void testMultipleConsecutiveDots() {
        assertEquals("TEST__KEY", StringUtils.toEnvStyle("test..key"));
    }

    @Test
    @DisplayName("Should handle strings with multiple consecutive underscores")
    void testMultipleConsecutiveUnderscores() {
        assertEquals("test..key", StringUtils.fromEnvStyle("TEST__KEY"));
    }

    @Test
    @DisplayName("Should handle single character strings")
    void testSingleCharacter() {
        assertEquals("A", StringUtils.toEnvStyle("a"));
        assertEquals("a", StringUtils.fromEnvStyle("A"));
    }

    @Test
    @DisplayName("Should handle numeric characters")
    void testNumericCharacters() {
        assertEquals("TEST_123_KEY", StringUtils.toEnvStyle("test.123.key"));
        assertEquals("test.123.key", StringUtils.fromEnvStyle("TEST_123_KEY"));
    }

    @Test
    @DisplayName("Should handle special property names")
    void testSpecialPropertyNames() {
        assertEquals("TESTOMATIO_ENVIRONMENT",
                StringUtils.toEnvStyle("testomatio.environment"));
        assertEquals("TESTOMATIO_RUN_GROUP_TITLE",
                StringUtils.toEnvStyle("testomatio.run.group.title"));
        assertEquals("TESTOMATIO_CREATE_TEST",
                StringUtils.toEnvStyle("testomatio.create.test"));
    }
}
