package com.testomatio.reporter.property_config.provider;


import com.testomatio.reporter.exception.PropertyNotFoundException;
import com.testomatio.reporter.property_config.interf.AbstractPropertyProvider;
import com.testomatio.reporter.property_config.util.StringUtils;
import java.util.logging.Logger;

public class SystemEnvPropertyProvider extends AbstractPropertyProvider {
    private final Logger LOGGER = Logger.getLogger(SystemEnvPropertyProvider.class.getName());

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
