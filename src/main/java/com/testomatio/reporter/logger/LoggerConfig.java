package com.testomatio.reporter.logger;

import com.testomatio.reporter.property_config.impl.PropertyProviderFactoryImpl;
import com.testomatio.reporter.property_config.interf.PropertyProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static com.testomatio.reporter.constants.PropertyNameConstants.TESTOMATIO_LOG_CONSOLE;
import static com.testomatio.reporter.constants.PropertyNameConstants.TESTOMATIO_LOG_FILE;
import static com.testomatio.reporter.constants.PropertyNameConstants.TESTOMATIO_LOG_LEVEL;

public class LoggerConfig {
    private static final String ROOT_LOGGER_NAME = "com.testomatio.reporter";

    private static volatile boolean initialized = false;
    private static PropertyProvider propertyProvider;

    public static synchronized void ensureInitialized() {
        if (initialized) return;

        try {
            propertyProvider = PropertyProviderFactoryImpl
                    .getPropertyProviderFactory()
                    .getPropertyProvider();
            initialize();
        } catch (Exception e) {
            System.err.println("[TESTOMATIO] Logger initialization failed: " + e.getMessage());
            setupFallback();
        }
        initialized = true;
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

        for (var handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }
        rootLogger.setUseParentHandlers(false);

        String levelStr = getProperty(TESTOMATIO_LOG_LEVEL);
        Level level = parseLevel(levelStr);
        rootLogger.setLevel(level);

        boolean hasHandlers = false;

        String logFile = getProperty(TESTOMATIO_LOG_FILE);
        if (logFile != null && !logFile.trim().isEmpty()) {
            hasHandlers |= addFileHandler(rootLogger, logFile.trim(), level);
        }

        String consoleEnabled = getProperty(TESTOMATIO_LOG_CONSOLE);
        if (!"false".equalsIgnoreCase(consoleEnabled)) {
            hasHandlers |= addConsoleHandler(rootLogger, level);
        }

        if (!hasHandlers) {
            addConsoleHandler(rootLogger, level);
        }
    }

    private static Level parseLevel(String levelStr) {
        if (levelStr == null || levelStr.trim().isEmpty()) {
            return Level.INFO;
        }

        try {
            return Level.parse(levelStr.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            System.err.println("[TESTOMATIO] Invalid log level '" + levelStr + "'. Using INFO.");
            return Level.INFO;
        }
    }

    private static boolean addFileHandler(Logger logger, String fileName, Level level) {
        try {
            var path = Paths.get(fileName);
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }

            FileHandler handler = new FileHandler(fileName, true);
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(level);
            logger.addHandler(handler);

            return true;
        } catch (IOException e) {
            System.err.println("[TESTOMATIO] Failed to setup file logging: " + e.getMessage());
            return false;
        }
    }

    private static boolean addConsoleHandler(Logger logger, Level level) {
        try {
            ConsoleHandler handler = new ConsoleHandler();
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(level);
            logger.addHandler(handler);


            return true;
        } catch (Exception e) {
            System.err.println("[TESTOMATIO] Failed to setup console logging: " + e.getMessage());
            return false;
        }
    }

    private static void setupFallback() {
        try {
            Logger rootLogger = Logger.getLogger(ROOT_LOGGER_NAME);
            rootLogger.setUseParentHandlers(false);

            if (rootLogger.getHandlers().length == 0) {
                ConsoleHandler handler = new ConsoleHandler();
                handler.setFormatter(new SimpleFormatter());
                handler.setLevel(Level.INFO);
                rootLogger.addHandler(handler);
                rootLogger.setLevel(Level.INFO);

                System.out.println("[TESTOMATIO] Fallback console logging enabled");
            }
        } catch (Exception e) {
            System.err.println("[TESTOMATIO] Complete logging setup failure: " + e.getMessage());
        }
    }

    private static String getProperty(String key) {
        return propertyProvider != null ? propertyProvider.getProperty(key) : null;
    }
}