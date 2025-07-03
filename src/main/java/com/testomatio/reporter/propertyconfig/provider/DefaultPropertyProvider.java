package com.testomatio.reporter.propertyconfig.provider;

import com.testomatio.reporter.exception.PropertyNotFoundException;
import com.testomatio.reporter.propertyconfig.interf.AbstractPropertyProvider;
import com.testomatio.reporter.propertyconfig.util.DefaultPropertiesStorage;
import com.testomatio.reporter.propertyconfig.util.StringUtils;

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
