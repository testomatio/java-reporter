package io.testomat.api;

import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by Lolik on 22.01.2025
 */
public class RestClientTest {

  private RestClient client = new RestClient();

  @Test
  public void createTestRun(){
    var testRun = new TestRun.Builder()
        .title("Reporter API: Create Test Run")
        .build();
    TestRunResponse response = client.createTestRun(testRun);
    Assert.assertNotNull(response.uid, "Test Run ID is null");
  }

  @Test
  public void updateTestRun(){
    var testRun = new TestRun.Builder()
        .title("Reporter API: Update Test Run")
        .parallel(true)
        .build();
    TestRunResponse response = client.createTestRun(testRun);

    var testRunUpdate = new TestRun.Builder()
        .id(response.uid)
        .title("Reporter API: Update Test Run - Updated")
        .statusEvent(TestRun.StatusEvent.PASS_PARALLEL)
        .build();
    TestRunResponse resp = client.updateTestRun(testRunUpdate);
    System.out.println(resp);
  }

  @Test
  public void addTestResultsToTestRun(){
    var testRun = new TestRun.Builder()
        .title("Reporter API: Add Test Results to Test Run")
        .parallel(true)
        .build();
    TestRunResponse response = client.createTestRun(testRun);

    var testResult = new TestResult.Builder()
        .title("Test Result 1")
        .status(TestResult.Status.PASSED)
        .build();

    testRun.id = response.uid;
    testRun.tests = List.of(testResult);

    String response1 = client.addTestResultsToTestRun(testRun);
    System.out.println(response1);

    var finishRun = new TestRun.Builder()
        .id(response.uid)
        .statusEvent(TestRun.StatusEvent.FINISH)
        .build();
    TestRunResponse response2 = client.updateTestRun(finishRun);
  }


}
