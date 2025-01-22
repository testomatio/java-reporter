package io.testomat.api;

import org.testng.annotations.Test;

/**
 * Created by Lolik on 22.01.2025
 */
public class CreateTestRunTest {

  private RestClient client = new RestClient();

  @Test
  public void createTestRunWithTitle(){
    TestRunModel testRun = new TestRunModel();
    testRun.title = "Test Run: Only Title";
    client.createTestRun(testRun);
  }

  @Test
  public void createTestRunWithEnv(){
    TestRunModel testRun = new TestRunModel();
    testRun.title = "Test Run: Env";
    testRun.env = "beta";
    client.createTestRun(testRun);
  }

  @Test
  public void createTestRunWithGroup(){
    TestRunModel testRun = new TestRunModel();
    testRun.title = "Test Run: Group";
    testRun.groupTitle = "Test Group";
    client.createTestRun(testRun);
  }


}
