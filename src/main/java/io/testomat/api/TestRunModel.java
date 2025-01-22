package io.testomat.api;

import com.fasterxml.jackson.annotation.JsonValue;
import io.testomat.TTestRun;
import java.util.List;

/**
 * Created by Lolik on 09.11.2023
 */
class TestRunModel {

  public String id;
  public String title;
  public List<String> tags;
  public String env;
  public String groupTitle;
  public Boolean parallel;
  public StatusEvent statusEvent;
  public List<TestResultModel> tests;
  public Boolean createTests;
  public Integer testsCount;

  public static TestRunModel parse(TTestRun run) {
    TestRunModel model = new TestRunModel();
    model.title = run.getName();
    model.env = run.getEnv();
    model.tests = run.getTestResults().stream().map(TestResultModel::parse).toList();
    return model;
  }


  public enum StatusEvent {
    PASS, FAIL, FINISH, PASS_PARALLEL, FAIL_PARALLEL, FINISH_PARALLEL;

    @JsonValue
    @Override
    public String toString() {
      return super.toString().toLowerCase();
    }
  }

  public static class Builder {
    private final TestRunModel instance;

    public Builder() {
      instance = new TestRunModel();
    }

    public Builder id(String id) {
      instance.id = id;
      return this;
    }

    public Builder title(String title) {
      instance.title = title;
      return this;
    }

    public Builder tags(List<String> tags) {
      instance.tags = tags;
      return this;
    }

    public Builder env(String env) {
      instance.env = env;
      return this;
    }

    public Builder groupTitle(String groupTitle) {
      instance.groupTitle = groupTitle;
      return this;
    }

    public Builder parallel(Boolean parallel) {
      instance.parallel = parallel;
      return this;
    }

    public Builder statusEvent(StatusEvent statusEvent) {
      instance.statusEvent = statusEvent;
      return this;
    }

    public Builder tests(List<TestResultModel> tests) {
      instance.tests = tests;
      return this;
    }

    public Builder createTests(Boolean createTests) {
      instance.createTests = createTests;
      return this;
    }

    public Builder testsCount(Integer testsCount) {
      instance.testsCount = testsCount;
      return this;
    }

    public TestRunModel build() {
      return instance;
    }
  }
}