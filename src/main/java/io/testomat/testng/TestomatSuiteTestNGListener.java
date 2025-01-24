package io.testomat.testng;

import io.testomat.ITestRunListener;
import io.testomat.Testomat;
import io.testomat.TestomatConfig;
import io.testomat.TestomatReporter;
import io.testomat.api.TestRunResponse;
import io.testomat.api.TestomatApi;
import io.testomat.utils.SafetyUtils;
import java.util.ServiceLoader;
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
        Thread.sleep(TestomatConfig.getReporterInterval());
      } catch (InterruptedException e) {
        logger.info("Realtime Reporter interrupted");
      }
      TestomatReporter.sendTestResults();
    }
  });

  ServiceLoader<ITestRunListener> loader = ServiceLoader.load(ITestRunListener.class);

  public void onStart(ISuite suite) {
    SafetyUtils.invokeSafety("TestomatSuiteTestNGListener:onStart", () -> {
      loader.forEach(listener -> listener.beforeCreate(Testomat.getTestRun()));
      Testomat.getTestRun().setName(suite.getName());
      TestRunResponse response = api.createTestRun(Testomat.getTestRun());
      Testomat.getTestRun().setId(response.getUid());
      logger.info("created run: {}", response.getUrl());
      realtimeReporter.start();
      loader.forEach(listener -> listener.afterCreate(Testomat.getTestRun()));
    });
  }

  public void onFinish(ISuite suite) {
    SafetyUtils.invokeSafety("TestomatSuiteTestNGListener:onFinish", () -> {
      loader.forEach(listener -> listener.beforeFinish(Testomat.getTestRun()));
      realtimeReporter.interrupt();
      api.finishTestRun(Testomat.getTestRun().getId());
      logger.info("finished run: {}", Testomat.getTestRun().getId());
      loader.forEach(listener -> listener.afterFinish(Testomat.getTestRun()));
    });
  }

}
