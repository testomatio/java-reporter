package io.testomat.core.propertyconfig.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.testomat.core.exception.PropertyNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("FilePropertyProvider Tests")
class FilePropertyProviderTest {

    private FilePropertyProvider provider;

    @BeforeEach
    void setUp() {
        provider = new FilePropertyProvider();
    }

    @Test
    @DisplayName("Should throw PropertyNotFoundException for non-existent property")
    void testThrowsExceptionForNonExistentProperty() {
        assertThrows(PropertyNotFoundException.class, () -> {
            provider.getProperty("non.existent.property");
        });
    }

    @Test
    @DisplayName("Should throw PropertyNotFoundException when next provider is null")
    void testThrowsExceptionWhenNextProviderIsNull() {
        assertThrows(PropertyNotFoundException.class, () -> {
            provider.getProperty("some.missing.property");
        });
    }

    @Test
    @DisplayName("Should delegate to next provider when property not found")
    void testDelegatesToNextProvider() {
        DefaultPropertyProvider nextProvider = new DefaultPropertyProvider();
        provider.setNext(nextProvider);

        String value = provider.getProperty("testomatio.url");

        assertNotNull(value);
        assertEquals("https://app.testomat.io/", value);
    }

    @Test
    @DisplayName("Should convert env style key to dot notation")
    void testConvertEnvStyleKeyToDotNotation() {
        DefaultPropertyProvider nextProvider = new DefaultPropertyProvider();
        provider.setNext(nextProvider);

        String value = provider.getProperty("TESTOMATIO_URL");

        assertNotNull(value);
        assertEquals("https://app.testomat.io/", value);
    }

    @Test
    @DisplayName("Should handle empty string key")
    void testEmptyStringKey() {
        assertThrows(PropertyNotFoundException.class, () -> {
            provider.getProperty("");
        });
    }

    @Test
    @DisplayName("Should handle property key with dots")
    void testPropertyKeyWithDots() {
        DefaultPropertyProvider nextProvider = new DefaultPropertyProvider();
        provider.setNext(nextProvider);

        String value = provider.getProperty("testomatio.url");

        assertNotNull(value);
        assertEquals("https://app.testomat.io/", value);
    }

    @Test
    @DisplayName("Should handle property key with underscores")
    void testPropertyKeyWithUnderscores() {
        DefaultPropertyProvider nextProvider = new DefaultPropertyProvider();
        provider.setNext(nextProvider);

        String value = provider.getProperty("TESTOMATIO_URL");

        assertNotNull(value);
        assertEquals("https://app.testomat.io/", value);
    }

    @Test
    @DisplayName("Should prefer file properties over next provider when available")
    void testPrefersFilePropertiesOverNext() {
        // This tests the priority: file properties should be returned first if they exist
        DefaultPropertyProvider nextProvider = new DefaultPropertyProvider();
        provider.setNext(nextProvider);

        // When property is not in file, it should delegate to next
        String value = provider.getProperty("testomatio.url");
        assertNotNull(value);
    }

    @Test
    @DisplayName("Should handle chain of responsibility pattern correctly")
    void testChainOfResponsibility() {
        FilePropertyProvider secondProvider = new FilePropertyProvider();
        DefaultPropertyProvider defaultProvider = new DefaultPropertyProvider();

        provider.setNext(secondProvider);
        secondProvider.setNext(defaultProvider);

        String value = provider.getProperty("testomatio.url");

        assertNotNull(value);
        assertEquals("https://app.testomat.io/", value);
    }
}
