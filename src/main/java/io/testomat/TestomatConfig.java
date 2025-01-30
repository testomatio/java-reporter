package io.testomat;

/**
 * Created by Lolik on 21.06.2024
 */
public class TestomatConfig {

  private static String apiKey = "please-add-your-testomatio-api-key";
  private static long reporterInterval = 5000;
  private static String host = "https://beta.testomat.io/";
  private static String project = "empty";
  private static String apiUrl = "https://beta.testomat.io/api";
  private static String env;

  static {
    ConfigLoader.loadFromProperties(TestomatConfig.class, ConfigLoader.loadProperties());
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

}
