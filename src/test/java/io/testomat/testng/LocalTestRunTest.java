package io.testomat.testng;

import io.testomat.Testomat;
import io.testomat.annotation.Step;
import io.testomat.annotation.TID;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Created by Lolik on 22.01.2025
 */
@Listeners({TestomatSuiteTestNGListener.class, TestomatTestNGListener.class})
public class LocalTestRunTest {


  @TID("59e9adbc")
  @Test
  public void successTest() {
    System.out.println("Test passed");
  }

  @TID("4c51423e")
  @Test
  public void failedTest() {
    System.out.println("Test failed");
    throw new RuntimeException("Test failed");
  }

  @TID("68a968dc")
  @Test
  public void successTestWithSteps(){
    step1();
    step2();
  }

  @Step
  public void step1() {
    System.out.println("Step 1");
  }

  @Step
  public void step2() {
    System.out.println("Step 2");
  }

}
