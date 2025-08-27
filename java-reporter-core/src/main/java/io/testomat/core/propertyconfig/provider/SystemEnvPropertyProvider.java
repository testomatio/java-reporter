package io.testomat.core.propertyconfig.provider;

import io.testomat.core.exception.PropertyNotFoundException;
import io.testomat.core.propertyconfig.interf.AbstractPropertyProvider;
import io.testomat.core.propertyconfig.util.StringUtils;

/**
 * Property provider that reads from system environment variables.
 * Second priority in the property resolution chain, supporting containerized
 * and cloud deployment scenarios where environment variables are preferred.
 * 
 * <p>Example usage: {@code TESTOMATIO_API_KEY=your-api-key}
 * 
 * <p>Automatically converts property keys from dot notation to environment variable format
 * using {@link StringUtils#toEnvStyle(String)} (uppercase with underscores).
 */
public class SystemEnvPropertyProvider extends AbstractPropertyProvider {

    @Override
    public String getProperty(String key) {
        String formattedKey = StringUtils.toEnvStyle(key);
        String value = System.getenv(formattedKey);
        if (!StringUtils.isNullOrEmpty(value)) {
            return value;
        }
        if (next != null) {
            return next.getProperty(key);
        }
        throw new PropertyNotFoundException("No such property: " + key);
    }
}
