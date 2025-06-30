package com.testomatio.reporter.propertyconfig.provider;

import com.testomatio.reporter.exception.PropertyNotFoundException;
import com.testomatio.reporter.logger.LoggerUtils;
import com.testomatio.reporter.propertyconfig.interf.AbstractPropertyProvider;
import com.testomatio.reporter.propertyconfig.util.StringUtils;
import java.util.logging.Logger;
import lombok.Data;

/**
 * Property provider that reads from JVM system properties.
 * First priority in the property resolution chain.
 */
@Data
public class JvmSystemPropertyProvider extends AbstractPropertyProvider {
    private static final Logger LOGGER = LoggerUtils.getLogger(JvmSystemPropertyProvider.class);

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
