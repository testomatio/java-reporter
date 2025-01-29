package io.testomat.model;

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
  private String groupTitle;
  private String env;
  private LocalDateTime startedAt;
  private LocalDateTime finishedAt;
  private List<TTestResult> testResults = new ArrayList<>();
  private List<String> tags;
  private Integer testsCount;
  private String ciBuildUrl;

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

  public String getCiBuildUrl() {
    return ciBuildUrl;
  }

  public void setCiBuildUrl(String ciBuildUrl) {
    this.ciBuildUrl = ciBuildUrl;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEnv() {
    return env;
  }

  public void setEnv(String env) {
    this.env = env;
  }

  public String getGroupTitle() {
    return groupTitle;
  }

  public void setGroupTitle(String groupTitle) {
    this.groupTitle = groupTitle;
  }

  public List<String> getTags() {
    return tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  public void setTestsCount(Integer testsCount) {
    this.testsCount = testsCount;
  }

  public Integer getTestsCount() {
    return testsCount;
  }

  public boolean isFinished() {
    return finishedAt != null;
  }

  public String getRunUrl() {
    return String.format("%s/projects/%s/runs/%s", TestomatConfig.TESTOMAT_HOST, TestomatConfig.TESTOMAT_PROJECT, id);
  }

  public String getReportUrl() {
    return getRunUrl() + "/report";
  }

  public List<TTestResult> getTestResults() {
    return testResults;
  }

  public static class Builder {
    private TTestRun instance;

    public Builder() {
      instance = new TTestRun();
    }

    public Builder setId(String id) {
      instance.id = id;
      return this;
    }

    public Builder setName(String name) {
      instance.name = name;
      return this;
    }

    public Builder setEnv(String env) {
      instance.env = env;
      return this;
    }

    public Builder setStartedAt(LocalDateTime startedAt) {
      instance.startedAt = startedAt;
      return this;
    }

    public Builder setFinishedAt(LocalDateTime finishedAt) {
      instance.finishedAt = finishedAt;
      return this;
    }

    public Builder setTestResults(List<TTestResult> testResults) {
      instance.testResults = testResults;
      return this;
    }

    public Builder setTags(List<String> tags) {
      instance.tags = tags;
      return this;
    }

    public TTestRun build() {
      return instance;
    }
  }

}
