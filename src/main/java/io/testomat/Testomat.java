package io.testomat;

import io.testomat.models.TTestResult;
import io.testomat.models.TTestRun;

/**
 * Created by Lolik on 18.06.2024
 */
public class Testomat {

  public static TTestResult getCurrentTestResult(){
    return TestomatStorage.getCurrentTestResult();
  }

  public static TTestRun getTestRun(){
    return TestomatStorage.getTestRun();
  }

  public static void addArtifact(String url){
    getCurrentTestResult().addArtifact(url);
  }

  public static boolean isEnabled(){
    return getTestRun().getId() != null;
  }




}
