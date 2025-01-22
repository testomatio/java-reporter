package io.testomat.api;

import io.testomat.TTestRun;

/**
 * Created by Lolik on 18.11.2024
 */
public class TestomatApi {

  private final RestClient client = new RestClient();

  public TestRunResponse createTestRun(TTestRun run) {
    TestRunModel testRun = TestRunModel.parse(run);
    return client.createTestRun(testRun);
  }

  public TestRunResponse updateTestRun(TTestRun run) {
    TestRunModel testRunModel = TestRunModel.parse(run);
    return client.updateTestRun(testRunModel);
  }

  public TestRunResponse finishTestRun(TTestRun run) {
    TestRunModel testRunModel = TestRunModel.parse(run);
    testRunModel.statusEvent = TestRunModel.StatusEvent.FINISH;
    return client.updateTestRun(testRunModel);
  }

  public void addTestResultsToTestRun(TTestRun run) {
    TestRunModel testRunModel = TestRunModel.parse(run);
    client.addTestResultsToTestRun(testRunModel);
  }

}
