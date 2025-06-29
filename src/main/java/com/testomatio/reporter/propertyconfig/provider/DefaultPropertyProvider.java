package com.testomatio.reporter.propertyconfig.provider;

import com.testomatio.reporter.exception.PropertyNotFoundException;
import com.testomatio.reporter.logger.LoggerUtils;
import com.testomatio.reporter.propertyconfig.interf.AbstractPropertyProvider;
import com.testomatio.reporter.propertyconfig.util.DefaultPropertiesStorage;
import com.testomatio.reporter.propertyconfig.util.StringUtils;
import java.util.logging.Logger;

/**
 * Property provider that provides default values as final fallback.
 * Last priority in the property resolution chain.
 */
public class DefaultPropertyProvider extends AbstractPropertyProvider {
    private static final Logger LOGGER = LoggerUtils.getLogger(DefaultPropertyProvider.class);

    @Override
    public String getProperty(String key) {
        String formattedKey = StringUtils.fromEnvStyle(key);

        String value = DefaultPropertiesStorage.DEFAULTS.get(formattedKey);
        if (!StringUtils.isNullOrEmpty(value)) {
            return value;
        }
        LOGGER.finer("Property not found: " + formattedKey);
        throw new PropertyNotFoundException("No such property: " + key);
    }
}
