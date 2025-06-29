package com.testomatio.reporter.propertyconfig.provider;

import com.testomatio.reporter.exception.PropertyNotFoundException;
import com.testomatio.reporter.logger.LoggerUtils;
import com.testomatio.reporter.propertyconfig.interf.AbstractPropertyProvider;
import com.testomatio.reporter.propertyconfig.util.StringUtils;
import java.util.logging.Logger;

/**
 * Property provider that reads from system environment variables.
 * Second priority in the property resolution chain.
 */
public class SystemEnvPropertyProvider extends AbstractPropertyProvider {
    private static final Logger LOGGER = LoggerUtils.getLogger(SystemEnvPropertyProvider.class);

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
        LOGGER.finer("No system environment variable provided for key: " + key);
        throw new PropertyNotFoundException("No such property: " + key);
    }
}
