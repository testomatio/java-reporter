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
  private final ServiceLoader<ITestRunListener> loader = ServiceLoader.load(ITestRunListener.class);
  private final TestomatApi api = new TestomatApi();
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

  public void onStart(ISuite suite) {
    SafetyUtils.invokeSafety("TestomatSuiteTestNGListener:onStart", () -> {
      loader.forEach(listener -> listener.beforeCreate(Testomat.getTestRun()));
      createTestRun(suite);
      realtimeReporter.start();
      loader.forEach(listener -> listener.afterCreate(Testomat.getTestRun()));
    });
  }

  public void onFinish(ISuite suite) {
    SafetyUtils.invokeSafety("TestomatSuiteTestNGListener:onFinish", () -> {
      loader.forEach(listener -> listener.beforeFinish(Testomat.getTestRun()));
      realtimeReporter.interrupt();
      api.finishTestRun(Testomat.getTestRun().getId());
      logger.info("Finished TestRun: {}", Testomat.getTestRun().getReportUrl());
      loader.forEach(listener -> listener.afterFinish(Testomat.getTestRun()));
    });
  }

  public void createTestRun(ISuite suite){
    if(Testomat.getTestRun().getName() == null){
      Testomat.getTestRun().setName(suite.getName());
    }
    Testomat.getTestRun().setTestsCount(suite.getAllMethods().size());
    TestRunResponse response = api.createTestRun(Testomat.getTestRun());
    Testomat.getTestRun().setId(response.getUid());
    logger.info("Created TestRun: {}", response.getUrl());
  }

}
