package com.testomatio.reporter.propertyconfig.provider;

import com.testomatio.reporter.exception.PropertyNotFoundException;
import com.testomatio.reporter.propertyconfig.interf.AbstractPropertyProvider;
import com.testomatio.reporter.propertyconfig.util.StringUtils;

/**
 * Property provider that reads from JVM system properties.
 * First priority in the property resolution chain.
 */

public class JvmSystemPropertyProvider extends AbstractPropertyProvider {

    public JvmSystemPropertyProvider() {
    }

    @Override
    public String getProperty(String key) {
        String formatedKey = StringUtils.fromEnvStyle(key);
        String value = System.getProperty(formatedKey);
        if (!StringUtils.isNullOrEmpty(value)) {
            return value;
        }
        if (next != null) {
            return next.getProperty(key);
        }
        throw new PropertyNotFoundException("No such property: " + key);
    }
}
