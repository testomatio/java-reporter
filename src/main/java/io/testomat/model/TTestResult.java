package io.testomat.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lolik on 17.06.2024
 */
public class TTestResult {

  private String testId;
  private String name;
  private String testFullName;
  private LocalDateTime startedAt;
  private LocalDateTime finishedAt;
  private Long duration;
  private String status;
  private List<TStepResult> steps = new ArrayList<>();
  private String message;
  private String stackTrace;
  private Map<String, Object> parameters;
  private List<String> artifacts;
  private String code;
  private TStepResult currentStep;

  public TStepResult getCurrentStep() {
    return currentStep;
  }

  public void setCurrentStep(TStepResult currentStep) {
    this.currentStep = currentStep;
  }

  public String getTestId() {
    return testId;
  }

  public void setTestId(String testId) {
    this.testId = testId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTestFullName() {
    return testFullName;
  }

  public void setTestFullName(String testFullName) {
    this.testFullName = testFullName;
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

  public Long getDuration() {
    return duration;
  }

  public void setDuration(Long duration) {
    this.duration = duration;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public List<TStepResult> getSteps() {
    return steps;
  }

  public void setSteps(List<TStepResult> steps) {
    this.steps = steps;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getStackTrace() {
    return stackTrace;
  }

  public void setStackTrace(String stackTrace) {
    this.stackTrace = stackTrace;
  }

  public Map<String, Object> getParameters() {
    return parameters;
  }

  public void setParameters(Map<String, Object> parameters) {
    this.parameters = parameters;
  }

  public List<String> getArtifacts() {
    return artifacts;
  }

  public void setArtifacts(List<String> artifacts) {
    this.artifacts = artifacts;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public void setFinishedAt(LocalDateTime now) {
    this.finishedAt = now;
    this.duration = startedAt != null ? Duration.between(startedAt, finishedAt).toMillis() : 0;
  }


  public void addParameter(String key, Object value) {
    if (parameters == null) {
      parameters = new LinkedHashMap<>();
    }
    parameters.put(key, value);
  }

  public void addArtifact(String artifact) {
    if (artifacts == null) {
      artifacts = new ArrayList<>();
    }
    artifacts.add(artifact);
  }



  public static class Builder {
    private TTestResult instance;

    public Builder() {
      instance = new TTestResult();
    }

    public Builder setTestId(String testId) {
      instance.testId = testId;
      return this;
    }

    public Builder setName(String name) {
      instance.name = name;
      return this;
    }

    public Builder setTestFullName(String testFullName) {
      instance.testFullName = testFullName;
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

    public Builder setDuration(Long duration) {
      instance.duration = duration;
      return this;
    }

    public Builder setStatus(String status) {
      instance.status = status;
      return this;
    }

    public Builder setSteps(List<TStepResult> steps) {
      instance.steps = steps;
      return this;
    }

    public Builder setMessage(String message) {
      instance.message = message;
      return this;
    }

    public Builder setStackTrace(String stackTrace) {
      instance.stackTrace = stackTrace;
      return this;
    }

    public Builder setParameters(Map<String, Object> parameters) {
      instance.parameters = parameters;
      return this;
    }

    public Builder setArtifacts(List<String> artifacts) {
      instance.artifacts = artifacts;
      return this;
    }

    public Builder setCode(String code) {
      instance.code = code;
      return this;
    }

    public TTestResult build() {
      return instance;
    }
  }


}
