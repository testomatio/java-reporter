package io.testomat.core.propertyconfig.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.testomat.core.exception.PropertyNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DefaultPropertyProvider Tests")
class DefaultPropertyProviderTest {

    private DefaultPropertyProvider provider;

    @BeforeEach
    void setUp() {
        provider = new DefaultPropertyProvider();
    }

    @Test
    @DisplayName("Should retrieve default URL property")
    void testGetDefaultUrl() {
        String value = provider.getProperty("testomatio.url");

        assertNotNull(value);
        assertEquals("https://app.testomat.io/", value);
    }

    @Test
    @DisplayName("Should retrieve default run title property")
    void testGetDefaultRunTitle() {
        String value = provider.getProperty("testomatio.run.title");

        assertNotNull(value);
        assertEquals("Default Run Title", value);
    }

    @Test
    @DisplayName("Should convert env style key to dot notation")
    void testConvertEnvStyleKeyToDotNotation() {
        String value = provider.getProperty("TESTOMATIO_URL");

        assertNotNull(value);
        assertEquals("https://app.testomat.io/", value);
    }

    @Test
    @DisplayName("Should throw PropertyNotFoundException for non-existent property")
    void testThrowsExceptionForNonExistentProperty() {
        assertThrows(PropertyNotFoundException.class, () -> {
            provider.getProperty("non.existent.property");
        });
    }

    @Test
    @DisplayName("Should throw PropertyNotFoundException for null property")
    void testThrowsExceptionForNullProperty() {
        assertThrows(PropertyNotFoundException.class, () -> {
            provider.getProperty("testomatio.nonexistent");
        });
    }

    @Test
    @DisplayName("Should handle property lookup with mixed case")
    void testMixedCasePropertyLookup() {
        String value = provider.getProperty("TESTOMATIO_RUN_TITLE");

        assertNotNull(value);
        assertEquals("Default Run Title", value);
    }

    @Test
    @DisplayName("Should not delegate to next provider")
    void testDoesNotDelegateToNextProvider() {
        DefaultPropertyProvider mockNext = new DefaultPropertyProvider() {
            @Override
            public String getProperty(String key) {
                return "should-not-be-called";
            }
        };

        provider.setNext(mockNext);

        // Even with next provider set, it should throw exception for non-existent property
        assertThrows(PropertyNotFoundException.class, () -> {
            provider.getProperty("non.existent.property");
        });
    }

    @Test
    @DisplayName("Should handle empty string key")
    void testEmptyStringKey() {
        assertThrows(PropertyNotFoundException.class, () -> {
            provider.getProperty("");
        });
    }

    @Test
    @DisplayName("Should retrieve all default properties")
    void testRetrieveAllDefaultProperties() {
        // Test that we can retrieve all known default properties
        String url = provider.getProperty("testomatio.url");
        String runTitle = provider.getProperty("testomatio.run.title");

        assertNotNull(url);
        assertNotNull(runTitle);
        assertEquals("https://app.testomat.io/", url);
        assertEquals("Default Run Title", runTitle);
    }

    @Test
    @DisplayName("Should handle property key with underscores")
    void testPropertyKeyWithUnderscores() {
        String value = provider.getProperty("TESTOMATIO_RUN_TITLE");

        assertNotNull(value);
        assertEquals("Default Run Title", value);
    }

    @Test
    @DisplayName("Should handle property key with dots")
    void testPropertyKeyWithDots() {
        String value = provider.getProperty("testomatio.run.title");

        assertNotNull(value);
        assertEquals("Default Run Title", value);
    }

    @Test
    @DisplayName("Should provide consistent results for same property")
    void testConsistentResults() {
        String value1 = provider.getProperty("testomatio.url");
        String value2 = provider.getProperty("testomatio.url");

        assertEquals(value1, value2);
    }

    @Test
    @DisplayName("Should handle uppercase property keys")
    void testUppercasePropertyKeys() {
        String value = provider.getProperty("TESTOMATIO_URL");

        assertNotNull(value);
        assertEquals("https://app.testomat.io/", value);
    }

    @Test
    @DisplayName("Should handle lowercase property keys")
    void testLowercasePropertyKeys() {
        String value = provider.getProperty("testomatio.url");

        assertNotNull(value);
        assertEquals("https://app.testomat.io/", value);
    }
}
