package com.testomatio.reporter.logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Lazy init logger util
 */
public final class LoggerUtils {

    private static final Map<Class<?>, LoggerHolder> loggerHolders = new ConcurrentHashMap<>();

    private LoggerUtils() {
    }

    public static Logger getLogger(Class<?> clazz) {
        return loggerHolders.computeIfAbsent(clazz, LoggerHolder::new).getLogger();
    }

    private static class LoggerHolder {
        private final Logger logger;

        LoggerHolder(Class<?> clazz) {
            this.logger = LoggerConfig.getLogger(clazz);
        }

        Logger getLogger() {
            return logger;
        }
    }
}