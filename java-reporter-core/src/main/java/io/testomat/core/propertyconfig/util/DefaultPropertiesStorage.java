package io.testomat.core.propertyconfig.util;

import static io.testomat.core.constants.PropertyNameConstants.HOST_URL_PROPERTY_NAME;
import static io.testomat.core.constants.PropertyNameConstants.RUN_TITLE_PROPERTY_NAME;
import static io.testomat.core.constants.PropertyNameConstants.TESTOMATIO_LOG_CONSOLE;
import static io.testomat.core.constants.PropertyNameConstants.TESTOMATIO_LOG_FILE;
import static io.testomat.core.constants.PropertyNameConstants.TESTOMATIO_LOG_LEVEL;
import static io.testomat.core.constants.PropertyValuesConstants.DEFAULT_CONSOLE_STATE;
import static io.testomat.core.constants.PropertyValuesConstants.DEFAULT_LIG_LEVEL;
import static io.testomat.core.constants.PropertyValuesConstants.DEFAULT_LOG_FILE;
import static io.testomat.core.constants.PropertyValuesConstants.DEFAULT_RUN_TITLE;
import static io.testomat.core.constants.PropertyValuesConstants.DEFAULT_URL;

import java.util.Map;

/**
 * Storage for default property values used as final fallback.
 * Contains sensible defaults for all configurable properties.
 */
public class DefaultPropertiesStorage {
    public static final Map<String, String> DEFAULTS;

    static {
        DEFAULTS = Map.of(
                HOST_URL_PROPERTY_NAME, DEFAULT_URL,
                RUN_TITLE_PROPERTY_NAME, DEFAULT_RUN_TITLE,
                TESTOMATIO_LOG_LEVEL, DEFAULT_LIG_LEVEL,
                TESTOMATIO_LOG_FILE, DEFAULT_LOG_FILE,
                TESTOMATIO_LOG_CONSOLE, DEFAULT_CONSOLE_STATE);
    }
}
