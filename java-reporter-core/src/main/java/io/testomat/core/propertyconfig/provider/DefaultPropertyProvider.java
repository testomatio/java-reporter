package io.testomat.core.propertyconfig.provider;


import io.testomat.core.exception.PropertyNotFoundException;
import io.testomat.core.propertyconfig.interf.AbstractPropertyProvider;
import io.testomat.core.propertyconfig.util.DefaultPropertiesStorage;
import io.testomat.core.propertyconfig.util.StringUtils;

/**
 * Property provider that provides built-in default values as final fallback.
 * Lowest priority in the property resolution chain, ensuring that essential
 * properties always have reasonable default values when not explicitly configured.
 * 
 * <p>Default values are stored in {@link DefaultPropertiesStorage} and include
 * fallback values for API URLs, timeouts, and other essential configuration.
 * This provider never delegates to a next provider since it's the end of the chain.
 */
public class DefaultPropertyProvider extends AbstractPropertyProvider {

    @Override
    public String getProperty(String key) {
        String formattedKey = StringUtils.fromEnvStyle(key);

        String value = DefaultPropertiesStorage.DEFAULTS.get(formattedKey);
        if (!StringUtils.isNullOrEmpty(value)) {
            return value;
        }
        throw new PropertyNotFoundException("No such property: " + key);
    }
}
