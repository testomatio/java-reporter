package io.testomat.testng;

import io.testomat.annotation.Step;
import io.testomat.annotation.TID;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Created by Lolik on 22.01.2025
 */
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
  public void successTestWithSteps() {
    step1();
    step2();
  }

  @Test
  public void failedTestWithSteps() {
    step1();
    step2();
    step3WithFail();
  }

  @Test
  public void innerSteps() {
    step1();
    step2();
    step4WithInnerSteps();
  }

  @Test
  public void stepWithDuration() {
    step5WithSleep();
  }

  @Test
  public void stepWithParameters() {
    step6WithParameters("value1", 2);
  }


  @Test(dataProvider = "dataProvider")
  public void testWithDataProvider(String param1, int param2) {
    System.out.println("Test with data provider: " + param1 + " " + param2);
  }

  int count = 0;

  @TID("70b4ca32")
  @Test(retryAnalyzer = MyRetryAnalyzer.class)
  public void testWithRetry() throws InterruptedException {
    Thread.sleep(2000);
    if (count < 1) {
      count++;
      throw new RuntimeException("Test failed");
    }
  }

  @DataProvider
  public Object[][] dataProvider() {
    return new Object[][] {{"value1", 1}, {"value2", 2}};
  }

  @Step
  public void step1() {
    System.out.println("Step 1");
  }

  @Step
  public void step2() {
    System.out.println("Step 2");
  }

  @Step
  public void step3WithFail() {
    throw new RuntimeException("Test failed");
  }

  @Step
  public void step4WithInnerSteps() {
    step1();
    step2();
  }


  @Step
  public void step5WithSleep() {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Step
  public void step6WithParameters(String param1, int param2) {
    System.out.println("Step 6 with parameters: " + param1 + " " + param2);
  }
}
