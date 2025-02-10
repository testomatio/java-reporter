package io.testomat.testng;

import io.testomat.Testomatio;
import io.testomat.TestomatioReporter;
import io.testomat.TestomatioStorage;
import io.testomat.model.TTestResult;
import io.testomat.utils.ExceptionSourceCodePointer;
import io.testomat.utils.SafetyUtils;
import io.testomat.utils.StringFormatterUtils;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

/**
 * Created by Lolik on 22.01.2025
 */
public class TestomatioTestNGListener implements IInvokedMethodListener {


  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    if (!Testomatio.isEnabled()) {
      return;
    }
    SafetyUtils.invokeSafety("TestomatTestNGListener:beforeInvocation", () -> {
      if (method.isConfigurationMethod()) {
        return;
      }
      TestomatioStorage.setCurrentTestResult(new TTestResult());
      Testomatio.getCurrentTestResult()
          .setName(StringFormatterUtils.capitalizeAndSplit(method.getTestMethod().getMethodName()));
      Testomatio.getCurrentTestResult().setStartedAt(LocalDateTime.now());
      Testomatio.getCurrentTestResult().setTestIdIfNull(TestResultParser.parseTID(testResult));
      Testomatio.getCurrentTestResult().setTestFullName(
          testResult.getMethod().getRealClass().getName() + "." + testResult.getMethod().getMethodName());
    });

  }

  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    if (!Testomatio.isEnabled()) {
      return;
    }
    SafetyUtils.invokeSafety("TestomatTestNGListener:afterInvocation", () -> {
      if (method.isConfigurationMethod()) {
        return;
      }
      updateCurrentTestResult(method, testResult);
      TestomatioReporter.addResultToReporter(Testomatio.getCurrentTestResult());
    });
  }

  public TTestResult updateCurrentTestResult(IInvokedMethod method, ITestResult testResult) {
    TTestResult ttr = Testomatio.getCurrentTestResult();
    ttr.setName(StringFormatterUtils.capitalizeAndSplit(method.getTestMethod().getMethodName()));
    ttr.setStatus(TestResultParser.parseTestNGStatus(testResult.getStatus()));
    ttr.setFinishedAt(LocalDateTime.now());
    if (testResult.getThrowable() != null) {
      ttr.setMessage(testResult.getThrowable().getMessage());
      ttr.setStackTrace(
          ExceptionSourceCodePointer.parseExceptionSourceCodeFragment(testResult.getMethod().getMethodName(), testResult.getThrowable().getStackTrace(), true) +
              "\n\n" +
              Arrays.stream(testResult.getThrowable().getStackTrace()).map(StackTraceElement::toString)
                  .collect(Collectors.joining(System.lineSeparator())));
    }
    for (int i = 0; i < testResult.getParameters().length; i++) {
      String name = method.getTestMethod().getConstructorOrMethod().getMethod().getParameters()[i].getName();
      ttr.addParameter(name, testResult.getParameters()[i]);
    }
    return ttr;
  }

}
