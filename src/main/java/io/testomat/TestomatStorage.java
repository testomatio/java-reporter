package io.testomat;

import io.testomat.models.TStepResult;
import io.testomat.models.TTestResult;
import io.testomat.models.TTestRun;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lolik on 18.06.2024
 */
public class TestomatStorage {

  protected static final TTestRun testRun = new TTestRun();
  protected static final ThreadLocal<TTestResult> currentTestResult = ThreadLocal.withInitial(TTestResult::new);
  protected static final ThreadLocal<TStepResult> currentStep = new ThreadLocal<>();
  protected static final ThreadLocal<List<TStepResult>> testSteps = ThreadLocal.withInitial(ArrayList::new);

  protected static TTestRun getTestRun(){
    return testRun;
  }

  protected static TTestResult getCurrentTestResult(){
    return currentTestResult.get();
  }

  protected static void setCurrentTestResult(TTestResult testResult){
    currentTestResult.set(testResult);
  }


}
