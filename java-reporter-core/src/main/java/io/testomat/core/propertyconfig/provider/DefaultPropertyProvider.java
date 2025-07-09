package io.testomat.core.propertyconfig.provider;


import io.testomat.core.exception.PropertyNotFoundException;
import io.testomat.core.propertyconfig.interf.AbstractPropertyProvider;
import io.testomat.core.propertyconfig.util.DefaultPropertiesStorage;
import io.testomat.core.propertyconfig.util.StringUtils;

/**
 * Property provider that provides default values as final fallback.
 * Last priority in the property resolution chain.
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
