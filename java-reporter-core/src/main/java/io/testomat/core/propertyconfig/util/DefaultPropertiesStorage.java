package io.testomat.core.propertyconfig.util;

import static io.testomat.core.constants.PropertyNameConstants.HOST_URL_PROPERTY_NAME;
import static io.testomat.core.constants.PropertyNameConstants.RUN_TITLE_PROPERTY_NAME;
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
                RUN_TITLE_PROPERTY_NAME, DEFAULT_RUN_TITLE);
    }
}
