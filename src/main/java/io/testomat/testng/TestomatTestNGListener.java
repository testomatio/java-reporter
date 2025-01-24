package io.testomat.testng;

import io.testomat.Testomat;
import io.testomat.TestomatReporter;
import io.testomat.TestomatStorage;
import io.testomat.annotation.TID;
import io.testomat.model.TTestResult;
import io.testomat.utils.ANSIFormatterUtils;
import io.testomat.utils.ExceptionSourceCodePointer;
import io.testomat.utils.StringFormatterUtils;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
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
    TTestResult ttr = Testomat.getCurrentTestResult();
    ttr.setName(StringFormatterUtils.capitalizeAndSplit(method.getTestMethod().getMethodName()));
    ttr.setStatus(parseTestNGStatus(testResult.getStatus()));
    ttr.setFinishedAt(LocalDateTime.now());
    if (testResult.getThrowable() != null) {
      ttr.setMessage(testResult.getThrowable().getMessage());
      ttr.setStackTrace(
          ExceptionSourceCodePointer.parseExceptionSourceCodeFragment(testResult.getThrowable().getStackTrace(), true) +
              "\n\n" +
              Arrays.stream(testResult.getThrowable().getStackTrace()).map(StackTraceElement::toString)
                  .collect(Collectors.joining(System.lineSeparator())));
    }
    ttr.setTestId(parseTID(testResult));
    for (int i = 0; i < testResult.getParameters().length; i++) {
      String name = method.getTestMethod().getConstructorOrMethod().getMethod().getParameters()[i].getName();
      ttr.addParameter(name, testResult.getParameters()[i]);
    }
    return ttr;
  }

  private static String parseTID(ITestResult testResult) {
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
