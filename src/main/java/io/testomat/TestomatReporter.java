package io.testomat;


import io.testomat.api.TestomatApi;
import io.testomat.model.TTestResult;
import io.testomat.model.TTestRun;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Lolik on 18.06.2024
 */
public class TestomatReporter {

  private static final Logger logger = LoggerFactory.getLogger(TestomatReporter.class);
  private static final List<TTestResult> unpublishedResults = new ArrayList<>();
  private static final List<TTestResult> resultsBatch = new ArrayList<>();
  private static final TestomatApi api = new TestomatApi();


  public static void addResultToReporter(TTestResult result) {
    synchronized (unpublishedResults) {
      unpublishedResults.add(result);
    }
  }

  private static List<TTestResult> pollAllUnpublishedResults() {
    List<TTestResult> copy;
    synchronized (unpublishedResults) {
      copy = new ArrayList<>(unpublishedResults);
      unpublishedResults.clear();
    }
    return copy;
  }

  public static synchronized void sendTestResults() {
    resultsBatch.addAll(pollAllUnpublishedResults());
    if (resultsBatch.isEmpty()) {
      return;
    }
    TTestRun testRun = new TTestRun();
    testRun.setId(Testomat.getTestRun().getId());
    testRun.setTestResults(resultsBatch);
    api.addTestResultsToTestRun(testRun);
    resultsBatch.clear();
  }

}
