package io.testomat.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.testng.annotations.Test;

/**
 * Created by Lolik on 22.01.2025
 */
public class AddTestResultsToTestRunTest {

  private RestClient client = new RestClient();

  @Test
  public void finishEmptyTestRun(){
    TestRunModel testRun = new TestRunModel();
    testRun.title = "Test Run: Finish Empty";
    TestRunResponse response = client.createTestRun(testRun);
    testRun.statusEvent = TestRunModel.StatusEvent.FINISH;
    testRun.id = response.uid;
    client.updateTestRun(testRun);
  }

  @Test
  public void addTestResultsToTestRun(){
    TestRunModel testRun = new TestRunModel();
    testRun.title = "Test Run: Add Test Results";
    TestRunResponse response = client.createTestRun(testRun);
    System.out.println("Test Run ID: " + response.uid);
    TestResultModel result1 = new TestResultModel();
    result1.title = "Test witout ID";
    result1.status = TestResultModel.Status.PASSED;
    testRun.tests = new ArrayList<>();
    testRun.tests.add(result1);
    testRun.id = response.uid;

    TestResultModel result2 = new TestResultModel();
    result2.title = "Test with ID";
    result2.status = TestResultModel.Status.PASSED;
    result2.testId = "b77dfcc8";
    testRun.tests.add(result2);
    client.addTestResultsToTestRun(testRun);
  }

  @Test
  public void addTestResultsWithSteps(){
    TestRunModel testRun = new TestRunModel();
    testRun.title = "Test Run: Add Test Results with Steps";
    TestRunResponse response = client.createTestRun(testRun);
    System.out.println("Test Run ID: " + response.uid);
    testRun.id = response.uid;

    TestResultModel result = new TestResultModel.Builder()
        .title("Test with Steps")
        .status(TestResultModel.Status.PASSED)
        .testId("7de5517d")
        .meta(Map.of("Key 1", "Value 1"))
        .build();

    TestStepModel step1 = new TestStepModel();
    step1.title = "Step 1";

    TestStepModel step2 = new TestStepModel();
    step2.title = "Step 2";

    result.steps = List.of(step1, step2);
    testRun.tests = List.of(result);

    client.addTestResultsToTestRun(testRun);
  }

}
