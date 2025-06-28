package com.testomatio.reporter.property_config.provider;

import com.testomatio.reporter.exception.PropertyNotFoundException;
import com.testomatio.reporter.property_config.interf.AbstractPropertyProvider;
import com.testomatio.reporter.property_config.util.DefaultPropertiesStorage;
import com.testomatio.reporter.property_config.util.StringUtils;
import java.util.logging.Logger;

import static com.testomatio.reporter.logger.LoggerConfig.getLogger;

/**
 * Property provider that provides default values as final fallback.
 * Last priority in the property resolution chain.
 */
public class DefaultPropertyProvider extends AbstractPropertyProvider {
    private static final Logger LOGGER = getLogger(DefaultPropertyProvider.class);

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