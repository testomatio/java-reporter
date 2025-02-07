package io.testomat;


import io.testomat.api.TestomatApi;
import io.testomat.model.TTestResult;
import io.testomat.model.TTestRun;
import io.testomat.utils.SafetyUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Lolik on 18.06.2024
 */
public class TestomatReporter {

  private static final Logger logger = LoggerFactory.getLogger(TestomatReporter.class);
  private static final List<TTestResult> unpublishedResults = new ArrayList<>();
  private static final TestomatApi api = new TestomatApi();
  private static final List<TTestResult> resultsBatch = new ArrayList<>();
  private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  public static void startReporter() {
    long interval = TestomatConfig.getReporterInterval();
    scheduler.scheduleAtFixedRate(TestomatReporter::sendTestResults, 1000, interval, TimeUnit.MILLISECONDS);
  }

  public static void stopReporter() {
    logger.info("Stopping TestomatReporter");
    scheduler.shutdown();
    try {
      if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
        scheduler.shutdownNow();
      }
    } catch (InterruptedException e) {
      scheduler.shutdownNow();
      Thread.currentThread().interrupt();
    }
    sendTestResults();
  }


  public static void addResultToReporter(TTestResult result) {
    logger.info("Adding test result to TestomatReporter: {}", result.getName() +" "+result.getStatus());
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

  private static synchronized void sendTestResults() {
    resultsBatch.addAll(pollAllUnpublishedResults());
    if (resultsBatch.isEmpty()) {
      return;
    }
    SafetyUtils.invokeSafety("TestomatReporter:sendTestResults", () -> {
      resultsBatch.addAll(pollAllUnpublishedResults());
      if (resultsBatch.isEmpty()) {
        return;
      }
      TTestRun testRun = new TTestRun();
      testRun.setId(Testomat.getTestRun().getId());
      testRun.setTestResults(resultsBatch);
      api.addTestResultsToTestRun(testRun);
      resultsBatch.clear();
    });

  }

}
