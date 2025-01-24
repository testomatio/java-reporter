package io.testomat.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestomatSafetyUtils {

  private static final Logger logger = LoggerFactory.getLogger(TestomatSafetyUtils.class);

  public static void invokeSafety(String action, Runnable runnable) {
    try {
      runnable.run();
    } catch (Throwable e) {
      logger.error("Couldn't perform action: " + action, e);
    }
  }
}
