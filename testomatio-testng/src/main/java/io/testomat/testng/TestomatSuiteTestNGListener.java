package io.testomat.testng;

import io.testomat.ITestRunListener;
import io.testomat.Testomat;
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

  public void onStart(ISuite suite) {
    SafetyUtils.invokeSafety("TestomatSuiteTestNGListener:onStart", () -> {
      loader.forEach(listener -> listener.beforeCreate(Testomat.getTestRun()));
      if (!Testomat.isEnabled()) {
        return;
      }
      TestomatReporter.startReporter();
      createTestRun(suite);
      loader.forEach(listener -> listener.afterCreate(Testomat.getTestRun()));
    });
  }

  public void onFinish(ISuite suite) {
    if (!Testomat.isEnabled()) {
      return;
    }
    SafetyUtils.invokeSafety("TestomatSuiteTestNGListener:onFinish", () -> {
      loader.forEach(listener -> listener.beforeFinish(Testomat.getTestRun()));
      TestomatReporter.stopReporter();
      api.finishTestRun(Testomat.getTestRun().getId());
      logger.info("Finished TestRun: {}", Testomat.getTestRun().getReportUrl());
      loader.forEach(listener -> listener.afterFinish(Testomat.getTestRun()));
    });
  }

  public void createTestRun(ISuite suite) {
    if (Testomat.getTestRun().getName() == null) {
      Testomat.getTestRun().setName(suite.getName());
    }
    Testomat.getTestRun().setTestsCount(suite.getAllMethods().size());
    TestRunResponse response = api.createTestRun(Testomat.getTestRun());
    Testomat.getTestRun().setId(response.getUid());
    logger.info("Created TestRun: {}", response.getUrl());
  }

}
