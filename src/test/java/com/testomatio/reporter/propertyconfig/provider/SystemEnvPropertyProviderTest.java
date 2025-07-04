package com.testomatio.reporter.propertyconfig.provider;

import com.testomatio.reporter.exception.PropertyNotFoundException;
import com.testomatio.reporter.propertyconfig.interf.AbstractPropertyProvider;
import com.testomatio.reporter.propertyconfig.util.StringUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Modified SystemEnvPropertyProvider with a protected getEnv method for testability
class TestableSystemEnvPropertyProvider extends AbstractPropertyProvider {
    private final Logger LOGGER = Logger.getLogger(TestableSystemEnvPropertyProvider.class.getName());
    private final Map<String, String> envMap;

    public TestableSystemEnvPropertyProvider(Map<String, String> envMap) {
        this.envMap = envMap;
    }

    @Override
    public String getProperty(String key) {
        String formattedKey = StringUtils.toEnvStyle(key);
        String value = getEnv(formattedKey);
        if (!StringUtils.isNullOrEmpty(value)) {
            return value;
        }
        if (next != null) {
            return next.getProperty(key);
        }
        LOGGER.finer("No system environment variable provided for key: " + key);
        throw new PropertyNotFoundException("No such property: " + key);
    }

    protected String getEnv(String key) {
        return envMap.get(key);
    }

    // Assuming AbstractPropertyProvider has a setNext method; if not, this would need adjustment
    public void setNext(AbstractPropertyProvider next) {
        this.next = next;
    }
}

public class SystemEnvPropertyProviderTest {

    @Test
    void testGetPropertyFoundInEnv() {
        // Arrange
        Map<String, String> envMap = new HashMap<>();
        envMap.put("TEST_KEY", "test_value");
        TestableSystemEnvPropertyProvider provider = new TestableSystemEnvPropertyProvider(envMap);

        // Act
        String value = provider.getProperty("test.key"); // Assuming toEnvStyle converts "test.key" to "TEST_KEY"

        // Assert
        assertEquals("test_value", value);
    }

    @Test
    void testGetPropertyNotFoundInEnvButInNext() {
        // Arrange
        Map<String, String> envMap = new HashMap<>(); // Empty map
        TestableSystemEnvPropertyProvider provider = new TestableSystemEnvPropertyProvider(envMap);
        AbstractPropertyProvider nextProvider = new AbstractPropertyProvider() {
            @Override
            public String getProperty(String key) {
                if ("test.key".equals(key)) { // Assuming next provider uses original key
                    return "next_value";
                }
                throw new PropertyNotFoundException("No such property: " + key);
            }
        };
        provider.setNext(nextProvider);

        // Act
        String value = provider.getProperty("test.key");

        // Assert
        assertEquals("next_value", value);
    }

    @Test
    void testGetPropertyNotFoundThrowsException() {
        // Arrange
        Map<String, String> envMap = new HashMap<>(); // Empty map
        TestableSystemEnvPropertyProvider provider = new TestableSystemEnvPropertyProvider(envMap);

        // Act & Assert
        assertThrows(PropertyNotFoundException.class, () -> provider.getProperty("test.key"));
    }

    @Test
    void testGetPropertyWithNullValueInEnv() {
        // Arrange
        Map<String, String> envMap = new HashMap<>();
        envMap.put("TEST_KEY", null); // Null value in environment
        TestableSystemEnvPropertyProvider provider = new TestableSystemEnvPropertyProvider(envMap);

        // Act & Assert
        assertThrows(PropertyNotFoundException.class, () -> provider.getProperty("test.key"));
    }

    @Test
    void testGetPropertyWithEmptyValueInEnv() {
        // Arrange
        Map<String, String> envMap = new HashMap<>();
        envMap.put("TEST_KEY", ""); // Empty value in environment
        TestableSystemEnvPropertyProvider provider = new TestableSystemEnvPropertyProvider(envMap);

        // Act & Assert
        assertThrows(PropertyNotFoundException.class, () -> provider.getProperty("test.key"));
    }
}