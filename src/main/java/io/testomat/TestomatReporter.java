package io.testomat;


import io.testomat.api.TestomatApiClient;
import io.testomat.models.TTestResult;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Lolik on 18.06.2024
 */
public class TestomatReporter {

  private static final Logger logger = LoggerFactory.getLogger(TestomatReporter.class);
  protected static final List<TTestResult> unpublishedResults = new ArrayList<>();
  private static final List<TTestResult> resultsBatch = new ArrayList<>();
  private static final TestomatApiClient client = new TestomatApiClient();


  protected static void addResultToReporter(TTestResult result) {
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

  public static void sendTestResults() {
    resultsBatch.addAll(pollAllUnpublishedResults());
    if (resultsBatch.isEmpty()) {
      return;
    }
    //TODO: Implement API on Java HTTP Client
   /* client.reporter.updateTestResult(
        Testomat.getTestRun().getId(),
        TestomatTestRun.builder()
            .tests(resultsBatch.stream().map(TestomatTestResult::parse)
                .collect(Collectors.toList()))
            .build()
    ).execute();*/
    resultsBatch.clear();
  }

}
