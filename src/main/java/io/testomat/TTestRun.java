package io.testomat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lolik on 17.06.2024
 */

public class TTestRun {

  private String id;
  private String statusEvent;
  private String name;
  private String env;
  private LocalDateTime startedAt;
  private LocalDateTime finishedAt;
  private List<TTestResult> testResults = new ArrayList<>();

  public void setId(String id) {
    this.id = id;
  }

  public LocalDateTime getStartedAt() {
    return startedAt;
  }

  public void setStartedAt(LocalDateTime startedAt) {
    this.startedAt = startedAt;
  }

  public LocalDateTime getFinishedAt() {
    return finishedAt;
  }

  public void setFinishedAt(LocalDateTime finishedAt) {
    this.finishedAt = finishedAt;
  }

  public void setTestResults(List<TTestResult> testResults) {
    this.testResults = testResults;
  }

  public String getId() {
    return id;
  }

  public String getName(){
    return name;
  }

  public void setName(String name){
    this.name = name;
  }

  public String getEnv(){
    return env;
  }

  public void setEnv(String env){
    this.env = env;
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
