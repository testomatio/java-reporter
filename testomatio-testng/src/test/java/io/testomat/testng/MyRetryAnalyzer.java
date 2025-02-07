package io.testomat.testng;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Created by Lolik on 27.01.2025
 */
public class MyRetryAnalyzer implements IRetryAnalyzer {

  private int count = 0;

  @Override
  public boolean retry(ITestResult iTestResult) {
    if (count < 1) {
      count++;
      return true;
    }
    return false;
  }
}
