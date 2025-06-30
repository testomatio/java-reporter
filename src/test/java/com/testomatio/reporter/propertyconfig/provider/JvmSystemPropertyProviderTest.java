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
    private static final String CONVERTED_ENV_KEY = "test_key"; // ENV_STYLE_KEY converted to system property format

    private JvmSystemPropertyProvider provider;
    private Set<String> propertiesToClean; // Відстежуємо всі властивості які встановлюємо
    private AbstractPropertyProvider originalNextProvider; // Зберігаємо оригінальний next provider

    @BeforeEach
    public void setUp() {
        provider = new JvmSystemPropertyProvider();
        propertiesToClean = new HashSet<>();
        originalNextProvider = getNextProvider(provider); // Зберігаємо оригінальний стан
    }

    @AfterEach
    public void tearDown() {
        // Очищуємо всі встановлені властивості
        for (String property : propertiesToClean) {
            System.clearProperty(property);
        }
        propertiesToClean.clear();

        // Відновлюємо оригінальний next provider
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
        // fromEnvStyle перетворює TEST_KEY на "test.key" (припущення)
        setSystemProperty(KEY, "value2");

        String result = provider.getProperty(ENV_STYLE_KEY);

        assertEquals("value2", result);
    }

    @Test
    public void testDoesNotDelegateWhenPropertyExists() {
        setSystemProperty(KEY, "value3");

        // Встановлюємо next provider який кидає виключення при виклику
        AbstractPropertyProvider throwingProvider = new AbstractPropertyProvider() {
            @Override
            public String getProperty(String key) {
                throw new AssertionError("Next provider should not be called when property exists");
            }
        };
        setNextProvider(provider, throwingProvider);

        String result = provider.getProperty(KEY);

        assertEquals("value3", result);
    }

    @Test
    public void testDelegateToNextWhenNotFound() {
        // Встановлюємо next provider який повертає відоме значення
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
        // Переконуємося що next provider null
        setNextProvider(provider, null);

        assertThrows(PropertyNotFoundException.class,
                () -> provider.getProperty("missingKey"));
    }

    @Test
    public void testMultiplePropertiesIsolation() {
        // Тест для перевірки ізольованості між різними властивостями
        setSystemProperty("prop1", "value1");
        setSystemProperty("prop2", "value2");

        assertEquals("value1", provider.getProperty("prop1"));
        assertEquals("value2", provider.getProperty("prop2"));

        assertThrows(PropertyNotFoundException.class,
                () -> provider.getProperty("prop3"));
    }

    // Допоміжні методи для безпечної роботи з системними властивостями та reflection

    /**
     * Встановлює системну властивість і додає її до списку для очищення
     */
    private void setSystemProperty(String key, String value) {
        System.setProperty(key, value);
        propertiesToClean.add(key);
    }

    /**
     * Безпечно отримує next provider через reflection
     */
    private AbstractPropertyProvider getNextProvider(AbstractPropertyProvider provider) {
        try {
            Field nextField = AbstractPropertyProvider.class.getDeclaredField("next");
            nextField.setAccessible(true);
            return (AbstractPropertyProvider) nextField.get(provider);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get next provider", e);
        }
    }

    /**
     * Безпечно встановлює next provider через reflection
     */
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