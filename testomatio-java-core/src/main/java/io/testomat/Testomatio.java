package io.testomat;

import io.testomat.model.TTestResult;
import io.testomat.model.TTestRun;

/**
 * Created by Lolik on 18.06.2024
 */
public class Testomatio {

  private static boolean isEnabled = true;

  public static TTestResult getCurrentTestResult(){
    return TestomatioStorage.getCurrentTestResult();
  }

  public static TTestRun getTestRun(){
    return TestomatioStorage.getTestRun();
  }

  public static void addArtifact(String url){
    getCurrentTestResult().addArtifact(url);
  }

  public static boolean isEnabled(){
    return isEnabled;
  }

  public static void disableReporter(){
    isEnabled = false;
  }

  public static void step(String title){
    Stepper.startStep(title, "passed");
    Stepper.stopStep();
  }


}
