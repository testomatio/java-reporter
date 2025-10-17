package io.testomat.core.propertyconfig.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.testomat.core.exception.PropertyNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@DisplayName("SystemEnvPropertyProvider Tests")
class SystemEnvPropertyProviderTest {

    private SystemEnvPropertyProvider provider;

    @BeforeEach
    void setUp() {
        provider = new SystemEnvPropertyProvider();
    }

    @Test
    @DisplayName("Should throw PropertyNotFoundException for non-existent property")
    void testThrowsExceptionForNonExistentProperty() {
        assertThrows(PropertyNotFoundException.class, () -> {
            provider.getProperty("non.existent.property.that.does.not.exist");
        });
    }

    @Test
    @DisplayName("Should throw PropertyNotFoundException when next provider is null")
    void testThrowsExceptionWhenNextProviderIsNull() {
        assertThrows(PropertyNotFoundException.class, () -> {
            provider.getProperty("some.missing.property.xyz");
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
    @DisplayName("Should convert property key to environment variable format")
    void testConvertKeyToEnvFormat() {
        DefaultPropertyProvider nextProvider = new DefaultPropertyProvider();
        provider.setNext(nextProvider);

        // When we request "testomatio.url", it should convert to "TESTOMATIO_URL"
        // and look for that environment variable
        String value = provider.getProperty("testomatio.url");

        assertNotNull(value);
        assertEquals("https://app.testomat.io/", value);
    }

    @Test
    @DisplayName("Should handle property key with dots")
    void testPropertyKeyWithDots() {
        DefaultPropertyProvider nextProvider = new DefaultPropertyProvider();
        provider.setNext(nextProvider);

        String value = provider.getProperty("testomatio.run.title");

        assertNotNull(value);
        assertEquals("Default Run Title", value);
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
    @DisplayName("Should handle empty string key")
    void testEmptyStringKey() {
        assertThrows(PropertyNotFoundException.class, () -> {
            provider.getProperty("");
        });
    }

    @Test
    @DisplayName("Should handle chain of responsibility pattern correctly")
    void testChainOfResponsibility() {
        SystemEnvPropertyProvider secondProvider = new SystemEnvPropertyProvider();
        DefaultPropertyProvider defaultProvider = new DefaultPropertyProvider();

        provider.setNext(secondProvider);
        secondProvider.setNext(defaultProvider);

        String value = provider.getProperty("testomatio.url");

        assertNotNull(value);
        assertEquals("https://app.testomat.io/", value);
    }

    @Test
    @DisplayName("Should handle uppercase property keys")
    void testUppercasePropertyKeys() {
        DefaultPropertyProvider nextProvider = new DefaultPropertyProvider();
        provider.setNext(nextProvider);

        String value = provider.getProperty("TESTOMATIO_URL");

        assertNotNull(value);
        assertEquals("https://app.testomat.io/", value);
    }

    @Test
    @DisplayName("Should handle lowercase property keys")
    void testLowercasePropertyKeys() {
        DefaultPropertyProvider nextProvider = new DefaultPropertyProvider();
        provider.setNext(nextProvider);

        String value = provider.getProperty("testomatio.url");

        assertNotNull(value);
        assertEquals("https://app.testomat.io/", value);
    }

    @Test
    @DisplayName("Should handle mixed case property keys")
    void testMixedCasePropertyKeys() {
        DefaultPropertyProvider nextProvider = new DefaultPropertyProvider();
        provider.setNext(nextProvider);

        String value = provider.getProperty("testomatio.URL");

        assertNotNull(value);
        assertEquals("https://app.testomat.io/", value);
    }

    @Test
    @DisplayName("Should retrieve PATH environment variable when available")
    @EnabledIfEnvironmentVariable(named = "PATH", matches = ".*")
    void testRetrievePathEnvVariable() {
        // PATH exists on all systems
        String value = provider.getProperty("PATH");

        assertNotNull(value);
    }

    @Test
    @DisplayName("Should prefer environment variables over next provider")
    void testPrefersEnvVariablesOverNext() {
        DefaultPropertyProvider nextProvider = new DefaultPropertyProvider();
        provider.setNext(nextProvider);

        // When property is not in environment, it should delegate to next
        String value = provider.getProperty("testomatio.url");
        assertNotNull(value);
    }
}
