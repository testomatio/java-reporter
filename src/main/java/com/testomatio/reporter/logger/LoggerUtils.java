package com.testomatio.reporter.logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Універсальна утиліта для отримання логгерів з lazy initialization
 */
public final class LoggerUtils {
    
    private static final Map<Class<?>, LoggerHolder> loggerHolders = new ConcurrentHashMap<>();
    
    private LoggerUtils() {
        // Utility class
    }
    
    /**
     * Отримує логгер для вказаного класу з lazy initialization
     * 
     * @param clazz клас для якого потрібен логгер
     * @return налаштований логгер
     */
    public static Logger getLogger(Class<?> clazz) {
        return loggerHolders.computeIfAbsent(clazz, LoggerHolder::new).getLogger();
    }
    
    /**
     * Внутрішній клас для lazy initialization логгера
     */
    private static class LoggerHolder {
        private final Logger logger;
        
        LoggerHolder(Class<?> clazz) {
            this.logger = LoggerConfig.getLogger(clazz);
        }
        
        Logger getLogger() {
            return logger;
        }
    }
    
    /**
     * Очищає кеш логгерів (корисно для тестів)
     */
    public static void clearCache() {
        loggerHolders.clear();
    }
}