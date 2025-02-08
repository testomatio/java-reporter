package io.testomat.api;

import io.testomat.model.TTestRun;

/**
 * Created by Lolik on 18.11.2024
 */
public class TestomatioApi {

  private final RestClient client = new RestClient();

  public TestRunResponse createTestRun(TTestRun run) {
    return client.createTestRun(TestRun.parse(run));
  }

  public TestRunResponse updateTestRun(TTestRun run) {
    return client.updateTestRun(TestRun.parse(run));
  }

  public TestRunResponse finishTestRun(String id) {
    TestRun testRun = new TestRun.Builder().id(id).
        statusEvent(TestRun.StatusEvent.FINISH).build();
    return client.updateTestRun(testRun);
  }

  public void addTestResultsToTestRun(TTestRun run) {
    client.addTestResultsToTestRun(TestRun.parse(run));
  }

}
