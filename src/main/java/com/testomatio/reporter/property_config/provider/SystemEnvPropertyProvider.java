package com.testomatio.reporter.property_config.provider;


import com.testomatio.reporter.exception.PropertyNotFoundException;
import com.testomatio.reporter.property_config.interf.AbstractPropertyProvider;
import com.testomatio.reporter.property_config.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemEnvPropertyProvider extends AbstractPropertyProvider {
    private final Logger LOGGER = LoggerFactory.getLogger(SystemEnvPropertyProvider.class);

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
        LOGGER.warn("No system environment variable provided for key: {}", key);
        throw new PropertyNotFoundException("No such property: " + key);
    }
}
