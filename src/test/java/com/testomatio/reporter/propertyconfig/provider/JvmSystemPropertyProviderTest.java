package com.testomatio.reporter.propertyconfig.provider;

import com.testomatio.reporter.exception.PropertyNotFoundException;
import com.testomatio.reporter.propertyconfig.interf.AbstractPropertyProvider;
import java.lang.reflect.Field;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JvmSystemPropertyProviderTest {
    private static final String KEY = "test.key";
    private static final String ENV_STYLE_KEY = "TEST_KEY";
    private JvmSystemPropertyProvider provider;

    @BeforeEach
    public void setUp() {
        provider = new JvmSystemPropertyProvider();
    }

    @AfterEach
    public void tearDown() {
        System.clearProperty(KEY);
        System.clearProperty(KEY.replace('.', '_'));
    }

    @Test
    public void testGetExistingProperty() {
        System.setProperty(KEY, "value1");
        String result = provider.getProperty(KEY);
        assertEquals("value1", result);
    }

    @Test
    public void testGetExistingPropertyWithEnvStyleKey() {
        // fromEnvStyle should convert TEST_KEY to "test.key"
        System.setProperty(KEY, "value2");
        String result = provider.getProperty(ENV_STYLE_KEY);
        assertEquals("value2", result);
    }

    @Test
    public void testDoesNotDelegateWhenPropertyExists() throws Exception {
        System.setProperty(KEY, "value3");
        // set next provider that throws if called
        AbstractPropertyProvider dummy = new AbstractPropertyProvider() {
            @Override
            public String getProperty(String key) {
                throw new AssertionError("Next provider should not be called");
            }
        };
        Field nextField = AbstractPropertyProvider.class.getDeclaredField("next");
        nextField.setAccessible(true);
        nextField.set(provider, dummy);

        String result = provider.getProperty(KEY);
        assertEquals("value3", result);
    }

    @Test
    public void testDelegateToNextWhenNotFound() throws Exception {
        // set next provider to return a known value
        AbstractPropertyProvider dummy = new AbstractPropertyProvider() {
            @Override
            public String getProperty(String key) {
                return "delegated";
            }
        };
        Field nextField = AbstractPropertyProvider.class.getDeclaredField("next");
        nextField.setAccessible(true);
        nextField.set(provider, dummy);

        String result = provider.getProperty("unknownKey");
        assertEquals("delegated", result);
    }

    @Test
    public void testThrowsWhenNotFoundAndNoNext() {
        assertThrows(PropertyNotFoundException.class, () -> provider.getProperty("missingKey"));
    }
}
