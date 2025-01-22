package io.testomat.testng;

import io.testomat.Testomat;
import io.testomat.TestomatReporter;
import io.testomat.TestomatStorage;
import io.testomat.annotation.TID;
import io.testomat.model.TTestResult;
import io.testomat.utils.ANSIFormatterUtils;
import java.time.LocalDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

/**
 * Created by Lolik on 22.01.2025
 */
public class TestomatTestNGListener implements IInvokedMethodListener {

  private static final Logger logger = LoggerFactory.getLogger(TestomatTestNGListener.class);

  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    if (method.isConfigurationMethod()) {
      return;
    }
    TestomatStorage.setCurrentTestResult(new TTestResult());
    Testomat.getCurrentTestResult().setStartedAt(LocalDateTime.now());
    Testomat.getCurrentTestResult().setName(method.getTestMethod().getMethodName());
  }

  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    if (method.isConfigurationMethod()) {
      return;
    }
    updateCurrentTestResult(method, testResult);
    TestomatReporter.addResultToReporter(Testomat.getCurrentTestResult());
  }

  public TTestResult updateCurrentTestResult(IInvokedMethod method, ITestResult testResult) {
    TTestResult ttr = TestomatStorage.getCurrentTestResult();
    ttr.setName(method.getTestMethod().getMethodName());
    ttr.setStatus(parseTestNGStatus(testResult.getStatus()));
    ttr.setFinishedAt(LocalDateTime.now());
    ttr.setMessage(ANSIFormatterUtils.code(testResult.getThrowable() != null ? testResult.getThrowable().getMessage() : null));
    ttr.setTestId(parseTID(testResult));
    return ttr;
  }

  private static String parseTID(ITestResult testResult){
    var testMethod = testResult.getMethod().getConstructorOrMethod().getMethod();
    if (testMethod.isAnnotationPresent(TID.class)) {
      return testMethod.getAnnotation(TID.class).value();
    }
    return null;
  }


  private static String parseTestNGStatus(int status) {
    return switch (status) {
      case 1 -> "passed";
      case 2 -> "failed";
      case 3 -> "skipped";
      default -> null;
    };
  }
}
