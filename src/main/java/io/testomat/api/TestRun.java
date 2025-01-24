package io.testomat.api;

import com.fasterxml.jackson.annotation.JsonValue;
import io.testomat.model.TTestRun;
import java.util.List;

/**
 * Created by Lolik on 09.11.2023
 */
class TestRun {

  public String id;
  public String title;
  public List<String> tags;
  public String env;
  public String groupTitle;
  public Boolean parallel;
  public StatusEvent statusEvent;
  public List<TestResult> tests;
  public Boolean createTests;
  public Integer testsCount;

  public static TestRun parse(TTestRun run) {
    TestRun testRun = new TestRun();
    testRun.id = run.getId();
    testRun.title = run.getName();
    testRun.env = run.getEnv();
    testRun.groupTitle = run.getGroupTitle();
    testRun.tests = run.getTestResults().stream().map(TestResult::parse).toList();

    return testRun;
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
    private final TestRun instance;

    public Builder() {
      instance = new TestRun();
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

    public Builder tests(List<TestResult> tests) {
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

    public TestRun build() {
      return instance;
    }
  }
}