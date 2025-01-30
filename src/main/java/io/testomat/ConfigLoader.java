package io.testomat;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Lolik on 30.01.2025
 */
class ConfigLoader {

  private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
  private static final String PREFIX = "testomatio.";

  private static final Properties properties = new Properties();

  static Properties loadProperties() {
    loadFromPropertiesFile();
    loadFromEnvironment();
    logger.info("Properties loaded");
    return properties;
  }

  private static void loadFromPropertiesFile() {
    try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("testomatio.properties")) {
      properties.load(is);
    } catch (IOException e) {
      logger.error("Warning: Could not load properties file: {}", e.getMessage());
    }
  }

  private static void loadFromEnvironment() {
    System.getenv().entrySet().stream()
        .filter(entry -> entry.getKey().startsWith("TESTOMATIO"))
        .forEach(entry -> properties.setProperty(
            entry.getKey().toLowerCase().replace('_', '.'),
            entry.getValue()
        ));
  }

  static void loadFromProperties(Class<?> clazz, Properties properties) {
    for (Field field : clazz.getDeclaredFields()) {
      if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;
      field.setAccessible(true);
      String key = PREFIX + camelToDot(field.getName());
      String value = properties.getProperty(key);
      if (value != null) {
        try {
          Object convertedValue = convertType(field.getType(), value);
          field.set(null, convertedValue);
        } catch (IllegalAccessException e) {
          throw new RuntimeException("Error setting property: " + key, e);
        }
      }
    }
  }

  private static String camelToDot(String camelCase) {
    return camelCase.replaceAll("([a-z])([A-Z])", "$1.$2").toLowerCase();
  }

  private static Object convertType(Class<?> type, String value) {
    if (type == int.class || type == Integer.class) {
      return Integer.parseInt(value);
    } else if (type == boolean.class || type == Boolean.class) {
      return Boolean.parseBoolean(value);
    } else if (type == double.class || type == Double.class) {
      return Double.parseDouble(value);
    } else if (type == long.class || type == Long.class) {
      return Long.parseLong(value);
    } else {
      return value;
    }
  }


}
