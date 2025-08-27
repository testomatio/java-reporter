package io.testomat.core.propertyconfig.util;

import static io.testomat.core.constants.PropertyNameConstants.HOST_URL_PROPERTY_NAME;
import static io.testomat.core.constants.PropertyNameConstants.RUN_TITLE_PROPERTY_NAME;
import static io.testomat.core.constants.PropertyValuesConstants.DEFAULT_RUN_TITLE;
import static io.testomat.core.constants.PropertyValuesConstants.DEFAULT_URL;

import java.util.Map;

/**
 * Centralized storage for default property values used as final fallback.
 * Contains sensible defaults for essential Testomat.io properties to ensure
 * the library functions even when no explicit configuration is provided.
 * 
 * <p>Default values include:
 * <ul>
 *   <li>API URL - Default Testomat.io service endpoint</li>
 *   <li>Run title - Generic test run identifier</li>
 * </ul>
 * 
 * <p>This class uses static initialization to create an immutable map of defaults
 * that can be safely accessed concurrently from multiple threads.
 */
public class DefaultPropertiesStorage {
    /** Immutable map of property names to their default values */
    public static final Map<String, String> DEFAULTS;

    static {
        DEFAULTS = Map.of(
                HOST_URL_PROPERTY_NAME, DEFAULT_URL,
                RUN_TITLE_PROPERTY_NAME, DEFAULT_RUN_TITLE);
    }
}
