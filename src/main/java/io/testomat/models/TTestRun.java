package io.testomat.models;

import io.testomat.TestomatConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lolik on 17.06.2024
 */

public class TTestRun {

  private String id;
  private String name;
  private LocalDateTime startedAt;
  private LocalDateTime finishedAt;
  private List<TTestResult> testResults = new ArrayList<>();

  public String getId() {
    return id;
  }

  public boolean isFinished() {
    return finishedAt != null;
  }

  public String getRunUrl() {
    return TestomatConfig.TESTOMAT_HOST + "projects/" + TestomatConfig.TESTOMAT_PROJECT + "/runs/" + id;
  }

  public String getReportUrl() {
    return getRunUrl() + "/report";
  }

  public List<TTestResult> getTestResults() {
    return testResults;
  }
}
