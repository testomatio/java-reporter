package com.core.autoconfig;

import com.core.util.TestFrameworkDetector;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Автоматична конфігурація для підключення відповідних listeners
 * на основі доступних тестових фреймворків.
 */
public class AutoConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoConfiguration.class);
    @Getter
    private static volatile boolean initialized = false;
    
    static {
        initialize();
    }
    
    public static void initialize() {
        if (initialized) {
            return;
        }
        
        synchronized (AutoConfiguration.class) {
            if (initialized) {
                return;
            }
            
            TestFrameworkDetector.Framework framework = TestFrameworkDetector.detectFramework();
            LOGGER.info("Detected test framework: {}", framework);
            
            switch (framework) {
                case JUNIT5:
                    LOGGER.info("JUnit 5 integration will be loaded automatically via ServiceLoader");
                    break;
                case TESTNG:
                    LOGGER.info("TestNG integration will be loaded automatically via ServiceLoader");
                    break;
                case UNKNOWN:
                    LOGGER.warn("No supported test framework detected. Please ensure JUnit 5 or TestNG is in classpath");
                    break;
            }
            
            initialized = true;
        }
    }
}