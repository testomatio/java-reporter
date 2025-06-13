package com.property_config.provider;


import com.exception.PropertyNotFoundException;
import com.property_config.interf.AbstractPropertyProvider;
import com.property_config.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JvmSystemPropertyProvider extends AbstractPropertyProvider {
    private final Logger LOGGER = LoggerFactory.getLogger(JvmSystemPropertyProvider.class);

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
        LOGGER.warn("No property loaded as JMV parameter");
        throw new PropertyNotFoundException("No such property: " + key);
    }
}