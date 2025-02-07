package io.testomat.api;

import io.testomat.model.TTestRun;
import org.testng.annotations.Test;

/**
 * Created by Lolik on 22.01.2025
 */
public class TestomatApiTest {

  private TestomatApi api = new TestomatApi();
  private String testRunId;

  @Test
  public void createTestRun() {
    TTestRun testRun = new TTestRun();
    testRun.setName("Testomat API: Create Test Run");
    TestRunResponse response = api.createTestRun(testRun);
    testRunId = response.uid;
  }

  @Test(dependsOnMethods = "createTestRun")
  public void updateTestRun(){
    TTestRun testRun = new TTestRun();
    testRun.setName("Testomat API: Create Test Run (Updated)");
    testRun.setId(testRunId);
    TestRunResponse response = api.updateTestRun(testRun);
  }

  @Test(dependsOnMethods = {"createTestRun", "updateTestRun"})
  public void finishTestRun(){
    api.finishTestRun(testRunId);
  }

}
