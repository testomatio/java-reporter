package io.testomat;

import io.testomat.model.TTestRun;

/**
 * Created by Lolik on 24.01.2025
 */
public class CustomTestRunListener implements ITestRunListener {

  @Override
  public void beforeCreate(TTestRun testRun) {
    System.out.println("beforeCreate");
    testRun.setEnv("beta");
    testRun.setGroupTitle("Group 1");
  }

  @Override
  public void afterCreate(TTestRun testRun) {
    System.out.println("afterCreate");
  }

  @Override
  public void beforeFinish(TTestRun testRun) {
    System.out.println("beforeFinish");
  }

  @Override
  public void afterFinish(TTestRun testRun) {
    System.out.println("afterFinish");
  }
}
