package io.testomat.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SafetyUtils {

  private static final Logger logger = LoggerFactory.getLogger(SafetyUtils.class);

  public static void invokeSafety(String action, Runnable runnable) {
    try {
      runnable.run();
    } catch (Throwable e) {
      logger.error("Couldn't perform action: " + action, e);
    }
  }
}
