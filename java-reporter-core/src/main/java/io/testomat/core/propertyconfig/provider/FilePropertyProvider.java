package io.testomat.core.propertyconfig.provider;


import static io.testomat.core.constants.PropertyNameConstants.PROPERTIES_FILE_NAME;

import io.testomat.core.exception.NoPropertyFileException;
import io.testomat.core.exception.PropertyNotFoundException;
import io.testomat.core.propertyconfig.interf.AbstractPropertyProvider;
import io.testomat.core.propertyconfig.util.StringUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Property provider that reads from properties file on classpath.
 * Third priority in the property resolution chain.
 */
public class FilePropertyProvider extends AbstractPropertyProvider {

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
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream(PROPERTIES_FILE_NAME)) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            throw new NoPropertyFileException("Error loading properties file: " + e.getMessage());
        }
        return properties;
    }
}
