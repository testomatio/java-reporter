package io.testomat.testng;

import io.testomat.ITestRunListener;
import io.testomat.Testomatio;
import io.testomat.TestomatioReporter;
import io.testomat.api.TestRunResponse;
import io.testomat.api.TestomatioApi;
import io.testomat.utils.SafetyUtils;
import java.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ISuite;
import org.testng.ISuiteListener;

/**
 * Created by Lolik on 22.01.2025
 */
public class TestomatioSuiteTestNGListener implements ISuiteListener {

  private static final Logger logger = LoggerFactory.getLogger(TestomatioSuiteTestNGListener.class);
  private final ServiceLoader<ITestRunListener> loader = ServiceLoader.load(ITestRunListener.class);
  private final TestomatioApi api = new TestomatioApi();

  public void onStart(ISuite suite) {
    SafetyUtils.invokeSafety("TestomatSuiteTestNGListener:onStart", () -> {
      loader.forEach(listener -> listener.beforeCreate(Testomatio.getTestRun()));
      if (!Testomatio.isEnabled()) {
        return;
      }
      TestomatioReporter.startReporter();
      createTestRun(suite);
      loader.forEach(listener -> listener.afterCreate(Testomatio.getTestRun()));
    });
  }

  public void onFinish(ISuite suite) {
    if (!Testomatio.isEnabled()) {
      return;
    }
    SafetyUtils.invokeSafety("TestomatSuiteTestNGListener:onFinish", () -> {
      loader.forEach(listener -> listener.beforeFinish(Testomatio.getTestRun()));
      TestomatioReporter.stopReporter();
      api.finishTestRun(Testomatio.getTestRun().getId());
      logger.info("Finished TestRun: {}", Testomatio.getTestRun().getReportUrl());
      loader.forEach(listener -> listener.afterFinish(Testomatio.getTestRun()));
    });
  }

  public void createTestRun(ISuite suite) {
    if (Testomatio.getTestRun().getName() == null) {
      Testomatio.getTestRun().setName(suite.getName());
    }
    Testomatio.getTestRun().setTestsCount(suite.getAllMethods().size());
    TestRunResponse response = api.createTestRun(Testomatio.getTestRun());
    Testomatio.getTestRun().setId(response.getUid());
    logger.info("Created TestRun: {}", response.getUrl());
  }

}
