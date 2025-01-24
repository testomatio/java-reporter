package io.testomat;

import io.testomat.model.TTestRun;

/**
 * Created by Lolik on 24.01.2025
 */
public interface ITestRunListener {

  default void beforeCreate(TTestRun testRun) {
  }

  default void afterCreate(TTestRun testRun) {
  }

  default void beforeFinish(TTestRun testRun) {
  }

  default void afterFinish(TTestRun testRun) {
  }

}
