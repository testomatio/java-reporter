package com.testomatio.reporter.property_config.provider;

import java.util.Map;

import static com.testomatio.reporter.constants.PropertyNameConstants.BATCH_FLUSH_INTERVAL_PROPERTY_NAME;
import static com.testomatio.reporter.constants.PropertyNameConstants.BATCH_SIZE_PROPERTY_NAME;
import static com.testomatio.reporter.constants.PropertyNameConstants.HOST_URL_PROPERTY_NAME;
import static com.testomatio.reporter.constants.PropertyNameConstants.RUN_TITLE_PROPERTY_NAME;
import static com.testomatio.reporter.constants.PropertyNameConstants.TESTOMATIO_LOG_CONSOLE;
import static com.testomatio.reporter.constants.PropertyNameConstants.TESTOMATIO_LOG_FILE;
import static com.testomatio.reporter.constants.PropertyNameConstants.TESTOMATIO_LOG_LEVEL;
import static com.testomatio.reporter.constants.PropertyValuesConstants.DEFAULT_BATCH_SIZE;
import static com.testomatio.reporter.constants.PropertyValuesConstants.DEFAULT_CONSOLE_STATE;
import static com.testomatio.reporter.constants.PropertyValuesConstants.DEFAULT_FLUSH_INTERVAL_SECONDS;
import static com.testomatio.reporter.constants.PropertyValuesConstants.DEFAULT_LOG_FILE_NAME;
import static com.testomatio.reporter.constants.PropertyValuesConstants.DEFAULT_LOG_LEVEL;
import static com.testomatio.reporter.constants.PropertyValuesConstants.DEFAULT_RUN_TITLE;
import static com.testomatio.reporter.constants.PropertyValuesConstants.DEFAULT_URL;

/**
 * Storage for default property values used as final fallback.
 * Contains sensible defaults for all configurable properties.
 */
public class DefaultPropertyStorage {
    public static final Map<String, String> DEFAULTS;

    static {
        DEFAULTS = Map.of(
                BATCH_SIZE_PROPERTY_NAME, String.valueOf(DEFAULT_BATCH_SIZE),
                BATCH_FLUSH_INTERVAL_PROPERTY_NAME, String.valueOf(DEFAULT_FLUSH_INTERVAL_SECONDS),
                HOST_URL_PROPERTY_NAME, DEFAULT_URL,
                RUN_TITLE_PROPERTY_NAME, DEFAULT_RUN_TITLE,
                TESTOMATIO_LOG_LEVEL, DEFAULT_LOG_LEVEL,
                TESTOMATIO_LOG_FILE, DEFAULT_LOG_FILE_NAME,
                TESTOMATIO_LOG_CONSOLE, DEFAULT_CONSOLE_STATE);
    }
}