package io.testomat.testng;

import io.testomat.Testomat;
import io.testomat.TestomatConfig;
import io.testomat.TestomatReporter;
import io.testomat.api.TestRunResponse;
import io.testomat.api.TestomatApi;
import io.testomat.model.TTestRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ISuite;
import org.testng.ISuiteListener;

/**
 * Created by Lolik on 22.01.2025
 */
public class TestomatSuiteTestNGListener implements ISuiteListener {

  private static final Logger logger = LoggerFactory.getLogger(TestomatSuiteTestNGListener.class);
  private TestomatApi api = new TestomatApi();
  private static final Thread realtimeReporter = new Thread(() -> {
    while (!Testomat.getTestRun().isFinished()) {
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        logger.info("Realtime reporter interrupted");
      }
      TestomatReporter.sendTestResults();
    }
  });

  public void onStart(ISuite suite) {
    ListenerUtils.invokeSafety("Create test run", () -> {
      Testomat.getTestRun().setName(suite.getName());
      TestRunResponse response = api.createTestRun(Testomat.getTestRun());
      Testomat.getTestRun().setId(response.getUid());
      logger.info("created run: {}", response.getUrl());
      realtimeReporter.start();
    });
  }

  public void onFinish(ISuite suite) {
    ListenerUtils.invokeSafety("Finish test run", () -> {
      realtimeReporter.interrupt();
      api.finishTestRun(Testomat.getTestRun().getId());
      logger.info("finished run: {}", Testomat.getTestRun().getId());
    });
  }

}
