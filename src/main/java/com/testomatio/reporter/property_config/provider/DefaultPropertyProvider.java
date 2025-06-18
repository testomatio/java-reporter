package com.testomatio.reporter.property_config.provider;

import com.testomatio.reporter.exception.PropertyNotFoundException;
import com.testomatio.reporter.property_config.interf.AbstractPropertyProvider;
import com.testomatio.reporter.property_config.util.StringUtils;

public class DefaultPropertyProvider extends AbstractPropertyProvider {

    @Override
    public String getProperty(String key) {
        String formattedKey = StringUtils.fromEnvStyle(key);

        String value = DefaultPropertyStorage.DEFAULTS.get(formattedKey);
        if (!StringUtils.isNullOrEmpty(value)) {
            return value;
        }
        throw new PropertyNotFoundException("No such property: " + key);
    }
}