package io.testomat.core.propertyconfig.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.testomat.core.exception.PropertyNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("JvmSystemPropertyProvider Tests")
class JvmSystemPropertyProviderTest {

    private JvmSystemPropertyProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JvmSystemPropertyProvider();
    }

    @AfterEach
    void cleanup() {
        // Clean up any system properties set during tests
        System.clearProperty("testomatio");
        System.clearProperty("testomatio.url");
        System.clearProperty("testomatio.api.key");
        System.clearProperty("test.property");
    }

    @Test
    @DisplayName("Should retrieve property from system properties")
    void testGetPropertyFromSystemProperties() {
        System.setProperty("testomatio", "test-api-key");

        String value = provider.getProperty("testomatio");

        assertEquals("test-api-key", value);
    }

    @Test
    @DisplayName("Should retrieve property with dot notation")
    void testGetPropertyWithDotNotation() {
        System.setProperty("testomatio.url", "https://app.testomat.io");

        String value = provider.getProperty("testomatio.url");

        assertEquals("https://app.testomat.io", value);
    }

    @Test
    @DisplayName("Should convert env style key to dot notation")
    void testConvertEnvStyleKeyToDotNotation() {
        System.setProperty("testomatio.api.key", "my-key");

        String value = provider.getProperty("TESTOMATIO_API_KEY");

        assertEquals("my-key", value);
    }

    @Test
    @DisplayName("Should throw PropertyNotFoundException when property not found and no next provider")
    void testThrowsExceptionWhenPropertyNotFound() {
        assertThrows(PropertyNotFoundException.class, () -> {
            provider.getProperty("non.existent.property");
        });
    }

    @Test
    @DisplayName("Should delegate to next provider when property not found")
    void testDelegateToNextProvider() {
        JvmSystemPropertyProvider nextProvider = new JvmSystemPropertyProvider() {
            @Override
            public String getProperty(String key) {
                return "from-next-provider";
            }
        };

        provider.setNext(nextProvider);

        String value = provider.getProperty("non.existent.property");

        assertEquals("from-next-provider", value);
    }

    @Test
    @DisplayName("Should handle empty string property value")
    void testHandleEmptyStringValue() {
        System.setProperty("test.property", "");

        assertThrows(PropertyNotFoundException.class, () -> {
            provider.getProperty("test.property");
        });
    }

    @Test
    @DisplayName("Should handle property with special characters")
    void testPropertyWithSpecialCharacters() {
        System.setProperty("test.property", "value-with-special-chars-@#$%");

        String value = provider.getProperty("test.property");

        assertEquals("value-with-special-chars-@#$%", value);
    }

    @Test
    @DisplayName("Should handle property with spaces")
    void testPropertyWithSpaces() {
        System.setProperty("test.property", "value with spaces");

        String value = provider.getProperty("test.property");

        assertEquals("value with spaces", value);
    }

    @Test
    @DisplayName("Should handle property with multiple dots")
    void testPropertyWithMultipleDots() {
        System.setProperty("testomatio.run.group.title", "Regression Tests");

        String value = provider.getProperty("testomatio.run.group.title");

        assertEquals("Regression Tests", value);
    }

    @Test
    @DisplayName("Should prefer system property over next provider")
    void testPreferSystemPropertyOverNextProvider() {
        System.setProperty("testomatio", "from-system-property");

        JvmSystemPropertyProvider nextProvider = new JvmSystemPropertyProvider() {
            @Override
            public String getProperty(String key) {
                return "from-next-provider";
            }
        };

        provider.setNext(nextProvider);

        String value = provider.getProperty("testomatio");

        assertEquals("from-system-property", value);
    }

    @Test
    @DisplayName("Should handle numeric property values")
    void testNumericPropertyValues() {
        System.setProperty("test.property", "12345");

        String value = provider.getProperty("test.property");

        assertEquals("12345", value);
    }

    @Test
    @DisplayName("Should handle boolean property values")
    void testBooleanPropertyValues() {
        System.setProperty("test.property", "true");

        String value = provider.getProperty("test.property");

        assertEquals("true", value);
    }

    @Test
    @DisplayName("Should handle URL property values")
    void testUrlPropertyValues() {
        System.setProperty("testomatio.url", "https://app.testomat.io/api/v1");

        String value = provider.getProperty("testomatio.url");

        assertEquals("https://app.testomat.io/api/v1", value);
    }

    @Test
    @DisplayName("Should handle multiline property values")
    void testMultilinePropertyValues() {
        String multilineValue = "line1\nline2\nline3";
        System.setProperty("test.property", multilineValue);

        String value = provider.getProperty("test.property");

        assertEquals(multilineValue, value);
    }
}
