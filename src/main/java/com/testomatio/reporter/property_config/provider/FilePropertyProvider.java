package com.testomatio.reporter.property_config.provider;

import com.testomatio.reporter.exception.NoPropertyFileException;
import com.testomatio.reporter.exception.PropertyNotFoundException;
import com.testomatio.reporter.property_config.interf.AbstractPropertyProvider;
import com.testomatio.reporter.property_config.util.StringUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import static com.testomatio.reporter.constants.PropertyNameConstants.PROPERTIES_FILE_NAME;

/**
 * Property provider that reads from properties file on classpath.
 * Third priority in the property resolution chain.
 */
public class FilePropertyProvider extends AbstractPropertyProvider {
    private final Logger LOGGER = Logger.getLogger(FilePropertyProvider.class.getName());

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

    /**
     * Loads properties from classpath file.
     *
     * @return Properties object, empty if file not found
     * @throws NoPropertyFileException if file loading fails
     */
    private Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE_NAME)) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            LOGGER.severe("Error loading properties file: " + e.getMessage());
            throw new NoPropertyFileException("Error loading properties file: " + e.getMessage());
        }
        return properties;
    }
}