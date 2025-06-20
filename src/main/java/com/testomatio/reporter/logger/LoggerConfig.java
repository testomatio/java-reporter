package com.testomatio.reporter.logger;

import com.testomatio.reporter.property_config.impl.PropertyProviderFactoryImpl;
import com.testomatio.reporter.property_config.interf.PropertyProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static com.testomatio.reporter.constants.PropertyNameConstants.TESTOMATIO_LOG_CONSOLE;
import static com.testomatio.reporter.constants.PropertyNameConstants.TESTOMATIO_LOG_FILE;
import static com.testomatio.reporter.constants.PropertyNameConstants.TESTOMATIO_LOG_LEVEL;

public class LoggerConfig {
    private static final String ROOT_LOGGER_NAME = "com.testomatio.reporter";
    private static final String LOG_PREFIX = "[TESTOMATIO] ";

    private static volatile boolean initialized = false;
    private static final Object initLock = new Object();
    private static final ConcurrentLinkedQueue<Handler> handlers = new ConcurrentLinkedQueue<>();
    private static volatile PropertyProvider propertyProvider;

    public static void ensureInitialized() {
        if (initialized) return;

        synchronized (initLock) {
            if (initialized) return;

            try {
                propertyProvider = PropertyProviderFactoryImpl
                        .getPropertyProviderFactory()
                        .getPropertyProvider();

                initialize();
                initialized = true;
            } catch (Exception e) {
                logError("Logger initialization failed: " + e.getMessage());
                setupFallbackLogging();
                initialized = true;
            }
        }
    }

    public static Logger getLogger(String name) {
        ensureInitialized();
        return Logger.getLogger(ROOT_LOGGER_NAME + "." + name);
    }

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getSimpleName());
    }

    private static void initialize() {
        Logger rootLogger = Logger.getLogger(ROOT_LOGGER_NAME);
        clearExistingHandlers(rootLogger);

        configureLogLevel(rootLogger);
        configureHandlers(rootLogger);
        addShutdownHook();
    }

    private static void configureLogLevel(Logger logger) {
        Level level = parseLogLevel(getProperty(TESTOMATIO_LOG_LEVEL));
        logger.setLevel(level);
        logInfo("Log level set to: " + level);
    }

    private static Level parseLogLevel(String logLevel) {
        if (logLevel == null || logLevel.trim().isEmpty()) {
            return Level.INFO;
        }

        try {
            return Level.parse(logLevel.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            logError("Invalid log level. Using INFO. Valid levels: OFF, SEVERE, WARNING, INFO, FINE, FINER, FINEST, ALL");
            return Level.INFO;
        }
    }

    private static void configureHandlers(Logger logger) {
        boolean hasHandlers = false;

        String logFile = getProperty(TESTOMATIO_LOG_FILE);
        if (logFile != null && !logFile.trim().isEmpty()) {
            hasHandlers |= createFileHandler(logger, logFile.trim());
        }

        if (!"false".equalsIgnoreCase(getProperty(TESTOMATIO_LOG_CONSOLE))) {
            hasHandlers |= createConsoleHandler(logger);
        }

        if (!hasHandlers) {
            createConsoleHandler(logger);
        }
    }

    private static boolean createFileHandler(Logger logger, String fileName) {
        return executeWithErrorHandling(() -> {
            validateAndCreatePath(fileName);
            FileHandler handler = new FileHandler(fileName, true);
            handler.setFormatter(new SimpleFormatter());
            addHandler(logger, handler);
            return true;
        }, "Failed to setup file logging");
    }

    private static boolean createConsoleHandler(Logger logger) {
        return executeWithErrorHandling(() -> {
            ConsoleHandler handler = new ConsoleHandler();
            handler.setFormatter(new SimpleFormatter());
            addHandler(logger, handler);
            return true;
        }, "Failed to setup console logging");
    }

    private static void validateAndCreatePath(String fileName) throws IOException {
        Path path = Paths.get(fileName);
        Path parentDir = path.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
    }

    private static void addHandler(Logger logger, Handler handler) {
        logger.addHandler(handler);
        handlers.add(handler);
    }

    private static void setupFallbackLogging() {
        executeWithErrorHandling(() -> {
            Logger rootLogger = Logger.getLogger(ROOT_LOGGER_NAME);
            if (rootLogger.getHandlers().length == 0) {
                ConsoleHandler handler = new ConsoleHandler();
                handler.setFormatter(new SimpleFormatter());
                handler.setLevel(Level.INFO);
                rootLogger.addHandler(handler);
                rootLogger.setLevel(Level.INFO);
                handlers.add(handler);
                logInfo("Fallback console logging enabled");
            }
            return true;
        }, "Complete logging setup failure");
    }

    private static void addShutdownHook() {
        executeWithErrorHandling(() -> {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                handlers.forEach(handler -> {
                    try {
                        handler.close();
                    } catch (Exception e) {
                        logError("Error closing handler: " + e.getMessage());
                    }
                });
                handlers.clear();
            }));
            return true;
        }, "Failed to add shutdown hook");
    }

    private static void clearExistingHandlers(Logger logger) {
        for (Handler handler : logger.getHandlers()) {
            logger.removeHandler(handler);
        }
    }

    private static String getProperty(String key) {
        return propertyProvider != null ? propertyProvider.getProperty(key) : null;
    }

    private static boolean executeWithErrorHandling(ThrowingSupplier<Boolean> action, String errorMsg) {
        try {
            return action.get();
        } catch (Exception e) {
            logError(errorMsg + ": " + e.getMessage());
            return false;
        }
    }

    private static void logInfo(String message) {
        System.out.println(LOG_PREFIX + message);
    }

    private static void logError(String message) {
        System.err.println(LOG_PREFIX + message);
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}