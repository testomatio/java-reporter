package io.testomat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Lolik on 21.06.2024
 */
public class TestomatConfig {

  public static Properties properties = loadProperties();

  public static final String TESTOMAT_HOST = properties.getProperty("testomat.host");
  public static final String TESTOMAT_PROJECT = properties.getProperty("testomat.project");
  public static final String TESTOMAT_API_URL = properties.getProperty("testomat.api.url");
  public static final String TESTOMAT_DUMMY = properties.getProperty("testomat.dummy");

  public static String getProperty(String propertyName){
    return properties.getProperty(propertyName);
  }

  public static String getApiKey(){
    return properties.getProperty("TESTOMAT_API_KEY");
  }

  public static long getReporterInterval(){
    String property = properties.getProperty("testomat.reporter.inverval", "5000");
    return Long.parseLong(property);
  }

  static Properties loadProperties() {
    try(InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("testomat.properties")) {
      Properties properties = new Properties();
      properties.load(is);
      System.out.println("Loaded properties: "+properties);
      properties.putAll(System.getenv());
      return properties;
    } catch (IOException e) {
      throw new RuntimeException("Failed to load testomat.properties", e);
    }
  }

}
