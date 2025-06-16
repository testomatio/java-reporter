package io.testomat.testng;

import io.qameta.allure.TmsLink;
import io.testomat.annotation.TID;
import org.testng.ITestResult;

/**
 * Created by Lolik on 24.01.2025
 */
class TestResultParser {

  static String parseTestNGStatus(int status) {
    return switch (status) {
      case 1 -> "passed";
      case 2 -> "failed";
      case 3 -> "skipped";
      default -> null;
    };
  }

  static String parseTID(ITestResult testResult) {
    var testMethod = testResult.getMethod().getConstructorOrMethod().getMethod();
    if (testMethod.isAnnotationPresent(TID.class)) {
      return testMethod.getAnnotation(TID.class).value();
    }
    if (testMethod.isAnnotationPresent(TmsLink.class)) {
      return testMethod.getAnnotation(TmsLink.class).value();
    }
    return null;
  }
}
