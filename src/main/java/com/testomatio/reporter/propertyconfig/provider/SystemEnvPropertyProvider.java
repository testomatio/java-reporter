package com.testomatio.reporter.propertyconfig.provider;

import com.testomatio.reporter.exception.PropertyNotFoundException;
import com.testomatio.reporter.propertyconfig.interf.AbstractPropertyProvider;
import com.testomatio.reporter.propertyconfig.util.StringUtils;

/**
 * Property provider that reads from system environment variables.
 * Second priority in the property resolution chain.
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
