package io.testomat.api;

import com.fasterxml.jackson.annotation.JsonValue;
import io.testomat.model.TTestResult;
import java.util.List;
import java.util.Map;

/**
 * Created by Lolik on 09.11.2023
 */
class TestResult {

  public String title;
  public Status status;
  public String suiteTitle;
  public String suiteId;
  public String testId;
  public String message;
  public String stack;
  public Long runTime;
  public Map<String, Object> example;
  public List<String> artifacts;
  public List<TestStep> steps;
  public Map<String, Object> meta;
  public String code;
  public Boolean create;

  public enum Status {
    PASSED, FAILED, SKIPPED;

    @JsonValue
    @Override
    public String toString() {
      return super.toString().toLowerCase();
    }
  }

  public static TestResult parse(TTestResult result){
    TestResult model = new TestResult();
    model.title = result.getName();
    model.status = Status.valueOf(result.getStatus().toUpperCase());
    model.runTime = result.getDuration();
    model.testId = result.getTestId();
    model.message = result.getMessage();
    model.stack = result.getStackTrace();
    model.example = result.getParameters();
    model.meta = result.getMeta();
    model.code = result.getCode();
    model.artifacts = result.getArtifacts();
    model.steps = result.getSteps().stream().map(TestStep::parse).toList();
    return model;
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public static class Builder {
    private final TestResult instance;

    public Builder() {
      instance = new TestResult();
    }

    public Builder(TestResult model) {
      instance = model;
    }

    public Builder title(String title) {
      instance.title = title;
      return this;
    }

    public Builder status(Status status) {
      instance.status = status;
      return this;
    }

    public Builder suiteTitle(String suiteTitle) {
      instance.suiteTitle = suiteTitle;
      return this;
    }

    public Builder suiteId(String suiteId) {
      instance.suiteId = suiteId;
      return this;
    }

    public Builder testId(String testId) {
      instance.testId = testId;
      return this;
    }

    public Builder message(String message) {
      instance.message = message;
      return this;
    }

    public Builder stack(String stack) {
      instance.stack = stack;
      return this;
    }

    public Builder runTime(Long runTime) {
      instance.runTime = runTime;
      return this;
    }

    public Builder example(Map<String, Object> example) {
      instance.example = example;
      return this;
    }

    public Builder artifacts(List<String> artifacts) {
      instance.artifacts = artifacts;
      return this;
    }

    public Builder steps(List<TestStep> steps) {
      instance.steps = steps;
      return this;
    }

    public Builder meta(Map<String, Object> meta) {
      instance.meta = meta;
      return this;
    }

    public Builder code(String code) {
      instance.code = code;
      return this;
    }

    public Builder create(Boolean create) {
      instance.create = create;
      return this;
    }

    public TestResult build() {
      return instance;
    }
  }
}