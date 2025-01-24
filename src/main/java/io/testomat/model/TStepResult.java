package io.testomat.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lolik on 17.06.2024
 */
public class TStepResult {

  private TStepResult parent;
  private List<TStepResult> steps;
  private String title;
  private String status;
  private String error;
  private LocalDateTime startedAt;
  private LocalDateTime finishedAt;
  private Map<String, Object> attributes;
  private Map<String, Object> parameters;
  private List<Object> arguments;


  public TStepResult(String methodName, TStepResult parentStep) {
    this.title = methodName;
    this.parent = parentStep;
    this.steps = new ArrayList<>();
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public List<TStepResult> getSteps() {
    return steps;
  }

  public void startTime() {
    this.startedAt = LocalDateTime.now();
  }

  public void stopTime() {
    this.finishedAt = LocalDateTime.now();
  }

  public String getTitle() {
    return title;
  }

  public Long getDuration() {
    if (startedAt == null || finishedAt == null) {
      return 0L;
    }
    return java.time.Duration.between(startedAt, finishedAt).toMillis();
  }

  public void addInnerStep(TStepResult step) {
    this.steps.add(step);
  }

  public void addAttribute(String key, Object value) {
    if (attributes == null) {
      attributes = new LinkedHashMap<>();
    }
    attributes.put(key, value);
  }

  public void setParameters(Map<String, Object> parameters) {
    this.parameters = parameters;
  }

  public void setArguments(List<Object> arguments) {
    this.arguments = arguments;
  }


  public TStepResult getParent() {
    return parent;
  }


  public void setStatus(String status) {
    this.status = status;
  }

  public Map<String, Object> getParameters(){
    return parameters;
  }

  @Override
  public String toString() {
    return "Step " + title;
  }
}
