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
    TestRun testRun = new TestRun();
    testRun.title = "Test Run: Finish Empty";
    TestRunResponse response = client.createTestRun(testRun);
    testRun.statusEvent = TestRun.StatusEvent.FINISH;
    testRun.id = response.uid;
    client.updateTestRun(testRun);
  }

  @Test
  public void addTestResultsToTestRun(){
    TestRun testRun = new TestRun();
    testRun.title = "Test Run: Add Test Results";
    TestRunResponse response = client.createTestRun(testRun);
    System.out.println("Test Run ID: " + response.uid);
    TestResult result1 = new TestResult();
    result1.title = "Test witout ID";
    result1.status = TestResult.Status.PASSED;
    testRun.tests = new ArrayList<>();
    testRun.tests.add(result1);
    testRun.id = response.uid;

    TestResult result2 = new TestResult();
    result2.title = "Test with ID";
    result2.status = TestResult.Status.PASSED;
    result2.testId = "b77dfcc8";
    testRun.tests.add(result2);
    client.addTestResultsToTestRun(testRun);
  }

  @Test
  public void addTestResultsWithSteps(){
    TestRun testRun = new TestRun();
    testRun.title = "Test Run: Add Test Results with Steps";
    TestRunResponse response = client.createTestRun(testRun);
    System.out.println("Test Run ID: " + response.uid);
    testRun.id = response.uid;

    TestResult result = new TestResult.Builder()
        .title("Test with Steps")
        .status(TestResult.Status.PASSED)
        .testId("7de5517d")
        .meta(Map.of("Key 1", "Value 1"))
        .build();

    TestStep step1 = new TestStep();
    step1.title = "Step 1";

    TestStep step2 = new TestStep();
    step2.title = "Step 2";

    result.steps = List.of(step1, step2);
    testRun.tests = List.of(result);

    client.addTestResultsToTestRun(testRun);
  }

}
