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
  private static String host = "https://beta.testomat.io";
  private static String apiKey = "please-add-your-testomatio-api-key";
  private static String project = "please-add-your-testomatio-project";
  private static long reporterInterval = 5000;
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
    return host + "api/";
  }

  public static String getEnv() {
    return env;
  }



  private static void verifyRequiredProperties() {
    List<String> missingProperties = new ArrayList<>();

    if (apiKey.equals("please-add-your-testomatio-api-key")) {
      missingProperties.add("TESTOMATIO_API_KEY");
    }

    if (project.equals("please-add-your-testomatio-project")) {
      missingProperties.add("TESTOMATIO_PROJECT");
    }

    if (!missingProperties.isEmpty()) {
      missingProperties.forEach(property ->
          log.error("{} is not set. Please set it as an environment variable or add it to testomatio.properties file in resources", property)
      );
      Testomatio.disableReporter();
    }
  }

}
