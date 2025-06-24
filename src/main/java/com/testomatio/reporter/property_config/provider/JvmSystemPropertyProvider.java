package com.testomatio.reporter.property_config.provider;

import com.testomatio.reporter.exception.PropertyNotFoundException;
import com.testomatio.reporter.property_config.interf.AbstractPropertyProvider;
import com.testomatio.reporter.property_config.util.StringUtils;
import java.util.logging.Logger;

/**
 * Property provider that reads from JVM system properties.
 * First priority in the property resolution chain.
 */
public class JvmSystemPropertyProvider extends AbstractPropertyProvider {
    private final Logger LOGGER = Logger.getLogger(JvmSystemPropertyProvider.class.getName());

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
        LOGGER.finer("No property loaded as JMV parameter");
        throw new PropertyNotFoundException("No such property: " + key);
    }
}