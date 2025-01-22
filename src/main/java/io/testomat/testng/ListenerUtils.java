package io.testomat.testng;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListenerUtils {

  private static final Logger logger = LoggerFactory.getLogger(ListenerUtils.class);

  public static void invokeSafety(String action, Runnable runnable) {
    try {
      runnable.run();
    } catch (Throwable e) {
      logger.error("Couldn't perform listener action: " + action, e);
    }
  }
}
