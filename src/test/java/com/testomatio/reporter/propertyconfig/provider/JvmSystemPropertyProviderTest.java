package com.testomatio.reporter.propertyconfig.provider;

import com.testomatio.reporter.exception.PropertyNotFoundException;
import com.testomatio.reporter.propertyconfig.interf.AbstractPropertyProvider;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JvmSystemPropertyProviderTest {
    private static final String KEY = "test.key";
    private static final String ENV_STYLE_KEY = "TEST_KEY";
    private static final String CONVERTED_ENV_KEY = "test_key";

    private JvmSystemPropertyProvider provider;
    private Set<String> propertiesToClean;
    private AbstractPropertyProvider originalNextProvider;

    @BeforeEach
    public void setUp() {
        provider = new JvmSystemPropertyProvider();
        propertiesToClean = new HashSet<>();
        originalNextProvider = getNextProvider(provider);
    }

    @AfterEach
    public void tearDown() {
        for (String property : propertiesToClean) {
            System.clearProperty(property);
        }
        propertiesToClean.clear();

        setNextProvider(provider, originalNextProvider);
    }

    @Test
    public void testGetExistingProperty() {
        setSystemProperty(KEY, "value1");

        String result = provider.getProperty(KEY);

        assertEquals("value1", result);
    }

    @Test
    public void testGetExistingPropertyWithEnvStyleKey() {
        setSystemProperty(KEY, "value2");

        String result = provider.getProperty(ENV_STYLE_KEY);

        assertEquals("value2", result);
    }

    @Test
    public void testDoesNotDelegateWhenPropertyExists() {
        setSystemProperty(KEY, "value3");

        AbstractPropertyProvider throwingProvider = new AbstractPropertyProvider() {
            @Override
            public String getProperty(String key) {
                throw new AssertionError(
                        "Next provider should not be called when property exists");
            }
        };
        setNextProvider(provider, throwingProvider);

        String result = provider.getProperty(KEY);

        assertEquals("value3", result);
    }

    @Test
    public void testDelegateToNextWhenNotFound() {
        AbstractPropertyProvider delegateProvider = new AbstractPropertyProvider() {
            @Override
            public String getProperty(String key) {
                return "delegated";
            }
        };
        setNextProvider(provider, delegateProvider);

        String result = provider.getProperty("unknownKey");

        assertEquals("delegated", result);
    }

    @Test
    public void testThrowsWhenNotFoundAndNoNext() {
        setNextProvider(provider, null);

        assertThrows(PropertyNotFoundException.class,
                () -> provider.getProperty("missingKey"));
    }

    @Test
    public void testMultiplePropertiesIsolation() {
        setSystemProperty("prop1", "value1");
        setSystemProperty("prop2", "value2");

        assertEquals("value1", provider.getProperty("prop1"));
        assertEquals("value2", provider.getProperty("prop2"));

        assertThrows(PropertyNotFoundException.class,
                () -> provider.getProperty("prop3"));
    }

    private void setSystemProperty(String key, String value) {
        System.setProperty(key, value);
        propertiesToClean.add(key);
    }

    private AbstractPropertyProvider getNextProvider(AbstractPropertyProvider provider) {
        try {
            Field nextField = AbstractPropertyProvider.class.getDeclaredField("next");
            nextField.setAccessible(true);
            return (AbstractPropertyProvider) nextField.get(provider);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get next provider", e);
        }
    }

    private void setNextProvider(AbstractPropertyProvider provider, AbstractPropertyProvider next) {
        try {
            Field nextField = AbstractPropertyProvider.class.getDeclaredField("next");
            nextField.setAccessible(true);
            nextField.set(provider, next);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set next provider", e);
        }
    }
}