package com.testomatio.reporter.property_config.provider;

import java.util.Map;

public class DefaultPropertyStorage {
    public static final Map<String, String> DEFAULTS;

    static {
        DEFAULTS = Map.of(
                "testomatio.batch.size", "10",
                "testomatio.batch.flush.interval", "10",
                "testomatio.url", "https://app.testomat.io/",
                "testomatio.run.title", "Default Run Title",
                "testomatio.log.level", "INFO",
                "testomatio.log.file", "logs/testomatio.log",
                "testomatio.log.console", "true");
    }
}
