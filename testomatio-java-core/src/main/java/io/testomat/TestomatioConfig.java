package io.testomat;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Lolik on 21.06.2024
 */
public class TestomatioConfig {

  private static final Logger log = LoggerFactory.getLogger(TestomatioConfig.class);
  private static String apiKey = "please-add-your-testomatio-api-key";
  private static long reporterInterval = 5000;
  private static String host = "https://beta.testomat.io/";
  private static String project = "empty";
  private static String apiUrl = "https://beta.testomat.io/api";
  private static String env;

  static {
    ConfigLoader.loadFromProperties(TestomatioConfig.class, ConfigLoader.loadProperties());
    verifyRequiredProperties();
  }

  public static String getApiKey() {
    return apiKey;
  }

  public static long getReporterInterval() {
    return reporterInterval;
  }

  public static String getHost() {
    return host;
  }

  public static String getProject() {
    return project;
  }

  public static String getApiUrl() {
    return apiUrl;
  }

  public static String getEnv() {
    return env;
  }



  private static void verifyRequiredProperties() {
    List<String> missingProperties = new ArrayList<>();

    if (apiKey.equals("please-add-your-testomatio-api-key")) {
      missingProperties.add("Testomatio API key");
    }

    if (project.equals("empty")) {
      missingProperties.add("Testomatio project");
    }

    if (!missingProperties.isEmpty()) {
      missingProperties.forEach(property ->
          log.error("{} is not set. Please add it to your testomatio.properties file or set it as an environment variable.", property)
      );
      Testomatio.disableReporter();
    }
  }

}
