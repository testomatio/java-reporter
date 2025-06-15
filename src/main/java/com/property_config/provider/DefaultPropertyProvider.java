package com.property_config.provider;


import com.exception.NoPropertyFileException;
import com.exception.PropertyNotFoundException;
import com.property_config.interf.AbstractPropertyProvider;
import com.property_config.util.StringUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.constants.PropertyNameConstants.PROPERTIES_FILE_NAME;

public class DefaultPropertyProvider extends AbstractPropertyProvider {
    private final Logger LOGGER = LoggerFactory.getLogger(DefaultPropertyProvider.class);

    @Override
    public String getProperty(String key) {
        Properties properties = loadProperties();
        String formatedKey = StringUtils.fromEnvStyle(key);
        String value = properties.getProperty(formatedKey);
        if (!StringUtils.isNullOrEmpty(value)) {
            return value;
        }
        if (next != null) {
            return next.getProperty(key);
        }
        throw new PropertyNotFoundException("No such property: " + key);
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE_NAME)) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            LOGGER.error("Error loading properties file: {}", e.getMessage());
            throw new NoPropertyFileException("Error loading properties file: " + e.getMessage());
        }
        return properties;
    }
}
