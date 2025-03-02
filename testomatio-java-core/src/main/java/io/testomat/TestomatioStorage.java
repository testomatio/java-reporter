package io.testomat;

import io.testomat.model.TTestResult;
import io.testomat.model.TTestRun;

/**
 * Created by Lolik on 18.06.2024
 */
public class TestomatioStorage {

  static final TTestRun testRun = new TTestRun();
  static final ThreadLocal<TTestResult> currentTestResult = ThreadLocal.withInitial(TTestResult::new);

  static TTestRun getTestRun(){
    return testRun;
  }

 public static TTestResult getCurrentTestResult(){
    return currentTestResult.get();
  }

  public static void setCurrentTestResult(TTestResult testResult){
    currentTestResult.set(testResult);
  }

}
