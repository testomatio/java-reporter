package com.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestFrameworkDetector {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestFrameworkDetector.class);
    
    public enum Framework {
        JUNIT5, TESTNG, UNKNOWN
    }
    
    public static Framework detectFramework() {
        try {
            Class.forName("org.testng.TestNG");
            LOGGER.debug("TestNG detected");
            return Framework.TESTNG;
        } catch (ClassNotFoundException e) {
        }
        
        try {
            Class.forName("org.junit.jupiter.api.Test");
            LOGGER.debug("JUnit 5 detected");
            return Framework.JUNIT5;
        } catch (ClassNotFoundException e) {
        }
        
        LOGGER.warn("No supported test framework detected");
        return Framework.UNKNOWN;
    }
    
    public static boolean isJUnit5Available() {
        return detectFramework() == Framework.JUNIT5;
    }
    
    public static boolean isTestNGAvailable() {
        return detectFramework() == Framework.TESTNG;
    }
}