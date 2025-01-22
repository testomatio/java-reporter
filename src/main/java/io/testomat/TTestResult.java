package io.testomat;

import io.testomat.utils.ANSIFormatterUtils;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
  private Map<String, Object> attributes;
  private List<String> artifacts;
  private String code;

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

  public Map<String, Object> getAttributes() {
    return attributes;
  }

  public void setAttributes(Map<String, Object> attributes) {
    this.attributes = attributes;
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


  public void addAttribute(String name, Object value) {
    if (attributes == null) {
      attributes = new LinkedHashMap<>();
    }
    attributes.put(name, value);
  }

  public void addArtifact(String artifact) {
    if (artifacts == null) {
      artifacts = new ArrayList<>();
    }
    artifacts.add(artifact);
  }

  public String getLog() {
    StringBuilder log = new StringBuilder();
    if (stackTrace != null) {
      log.append(stackTrace).append("\n\n");
    }
    if (attributes != null) {
      log.append(ANSIFormatterUtils.bold("---Test Attributes---")).append("\n");
      log.append(attributes.entrySet().stream()
          .map(e -> e.getKey() + ": " + e.getValue())
          .collect(Collectors.joining("\n")));
      log.append("\n\n");
    }
    if (!steps.isEmpty()) {
      log.append(ANSIFormatterUtils.bold("---Steps---")).append("\n");
      log.append(getStepsLog(steps, 0));
    }
    return log.toString();
  }

  private String getStepsLog(List<TStepResult> steps, int level) {
    StringBuilder sb = new StringBuilder();
    for (TStepResult step : steps) {
      sb.append(step.getLog());
      sb.append(getStepsLog(step.getSteps(), level + 1));
    }
    return sb.toString();
  }
}
