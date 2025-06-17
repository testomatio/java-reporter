package com.testomatio.reporter.logger;

import com.testomatio.reporter.property_config.impl.PropertyProviderFactoryImpl;
import com.testomatio.reporter.property_config.interf.PropertyProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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

    private static volatile PropertyProvider propertyProvider;
    private static final List<Handler> handlers = new ArrayList<>();

    private static volatile boolean initializationAttempted = false;
    private static volatile boolean initializationSuccessful = false;
    private static final Object initLock = new Object();

    public static void ensureInitialized() {
        if (initializationAttempted) {
            return;
        }

        synchronized (initLock) {
            if (initializationAttempted) {
                return;
            }

            initializationAttempted = true;

            try {
                if (propertyProvider == null) {
                    propertyProvider = PropertyProviderFactoryImpl
                            .getPropertyProviderFactory()
                            .getPropertyProvider();
                }

                performInitialization();
                initializationSuccessful = true;

            } catch (Exception e) {
                System.err.println("[TESTOMATIO] Logger initialization failed: " + e.getMessage());
                // Fallback до базової консольної ініціалізації
                setupFallbackLogging();
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

    private static void performInitialization() {
        configureLogLevel();
        configureHandlers();
        addShutdownHook();
    }

    private static void configureLogLevel() {
        Logger rootLogger = Logger.getLogger(ROOT_LOGGER_NAME);

        try {
            String logLevel = propertyProvider != null ?
                    propertyProvider.getProperty(TESTOMATIO_LOG_LEVEL) : null;

            if (logLevel != null && !logLevel.trim().isEmpty()) {
                Level level = Level.parse(logLevel.toUpperCase().trim());
                rootLogger.setLevel(level);
                System.out.println("[TESTOMATIO] Log level set to: " + level);
            } else {
                rootLogger.setLevel(Level.INFO);
                System.out.println("[TESTOMATIO] Using default log level: INFO");
            }
        } catch (IllegalArgumentException e) {
            System.err.println("[TESTOMATIO] Invalid log level. Using INFO level. " +
                    "Valid levels: OFF, SEVERE, WARNING, INFO, FINE, FINER, FINEST, ALL");
            rootLogger.setLevel(Level.INFO);
        } catch (Exception e) {
            System.err.println("[TESTOMATIO] Error configuring log level: " + e.getMessage());
            rootLogger.setLevel(Level.INFO);
        }
    }

    private static void configureHandlers() {
        Logger rootLogger = Logger.getLogger(ROOT_LOGGER_NAME);

        Handler[] existingHandlers = rootLogger.getHandlers();
        for (Handler handler : existingHandlers) {
            rootLogger.removeHandler(handler);
        }
        boolean hasHandlers = false;

        try {
            String logFile = propertyProvider != null ?
                    propertyProvider.getProperty(TESTOMATIO_LOG_FILE) : null;

            if (logFile != null && !logFile.trim().isEmpty()) {
                if (setupFileHandler(rootLogger, logFile.trim())) {
                    hasHandlers = true;
                }
            }
        } catch (Exception e) {
            System.err.println("[TESTOMATIO] Error setting up file handler: " + e.getMessage());
        }

        try {
            String consoleEnabled = propertyProvider != null ?
                    propertyProvider.getProperty(TESTOMATIO_LOG_CONSOLE) : null;

            if (!"false".equalsIgnoreCase(consoleEnabled)) {
                if (setupConsoleHandler(rootLogger)) {
                    hasHandlers = true;
                }
            }
        } catch (Exception e) {
            System.err.println("[TESTOMATIO] Error setting up console handler: " + e.getMessage());
        }

        if (!hasHandlers) {
            setupConsoleHandler(rootLogger);
        }
    }

    private static boolean setupFileHandler(Logger logger, String fileName) {
        try {
            validateFilePath(fileName);
            FileHandler fileHandler = new FileHandler(fileName, true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);

            synchronized (handlers) {
                handlers.add(fileHandler);
            }

            System.out.println("[TESTOMATIO] File logging enabled: " + fileName);
            return true;

        } catch (IOException e) {
            System.err.println("[TESTOMATIO] Failed to setup file logging: " + e.getMessage());
        } catch (SecurityException e) {
            System.err.println("[TESTOMATIO] Security error setting up file logging: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("[TESTOMATIO] Invalid file path: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[TESTOMATIO] Unexpected error setting up file logging: " + e.getMessage());
        }
        return false;
    }

    private static boolean setupConsoleHandler(Logger logger) {
        try {
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(consoleHandler);

            synchronized (handlers) {
                handlers.add(consoleHandler);
            }

            return true;

        } catch (SecurityException e) {
            System.err.println("[TESTOMATIO] Security error setting up console logging: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[TESTOMATIO] Unexpected error setting up console logging: " + e.getMessage());
        }
        return false;
    }

    private static void validateFilePath(String fileName) {
        try {
            Path path = Paths.get(fileName);
            Path parentDir = path.getParent();

            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid file path: " + fileName, e);
        }
    }

    private static void setupFallbackLogging() {
        try {
            Logger rootLogger = Logger.getLogger(ROOT_LOGGER_NAME);

            if (rootLogger.getHandlers().length == 0) {
                ConsoleHandler consoleHandler = new ConsoleHandler();
                consoleHandler.setFormatter(new SimpleFormatter());
                consoleHandler.setLevel(Level.INFO);
                rootLogger.addHandler(consoleHandler);
                rootLogger.setLevel(Level.INFO);

                synchronized (handlers) {
                    handlers.add(consoleHandler);
                }

                System.out.println("[TESTOMATIO] Fallback console logging enabled");
            }
        } catch (Exception e) {
            System.err.println("[TESTOMATIO] Complete logging setup failure: " + e.getMessage());
        }
    }

    private static void addShutdownHook() {
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                synchronized (handlers) {
                    for (Handler handler : handlers) {
                        try {
                            handler.close();
                        } catch (Exception e) {
                            System.err.println("[TESTOMATIO] Error closing handler: " + e.getMessage());
                        }
                    }
                    handlers.clear();
                }
            }));
        } catch (Exception e) {
            System.err.println("[TESTOMATIO] Failed to add shutdown hook: " + e.getMessage());
        }
    }

    public static synchronized void reset() {
        initializationAttempted = false;
        initializationSuccessful = false;
        propertyProvider = null;

        synchronized (handlers) {
            for (Handler handler : handlers) {
                try {
                    handler.close();
                } catch (Exception e) {
                }
            }
            handlers.clear();
        }

        Logger rootLogger = Logger.getLogger(ROOT_LOGGER_NAME);
        Handler[] existingHandlers = rootLogger.getHandlers();
        for (Handler handler : existingHandlers) {
            rootLogger.removeHandler(handler);
        }
    }
}