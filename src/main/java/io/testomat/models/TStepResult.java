package io.testomat.models;

import io.testomat.utils.ANSIFormatterUtils;
import io.testomat.utils.StringFormatterUtils;
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
  private String status;
  private String title;
  private LocalDateTime startedAt;
  private LocalDateTime finishedAt;
  private Map<String, Object> attributes;
  private Map<String, Object> parameters;
  private List<Object> arguments;
  private int depth;


  public TStepResult(String methodName, TStepResult parentStep) {
    this.title = methodName;
    this.parent = parentStep;
    this.steps = new ArrayList<>();
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

  public String getStatusSymbol() {
    if (status == null) {
      return "❓";
    }
    return switch (status) {
      case "passed" -> "✅";
      case "failed" -> "❌";
      case "skipped" -> "⚠️";
      default -> "❓";
    };
  }

  public String getLog() {
    StringBuilder sb = new StringBuilder();
    sb.append("  ".repeat(Math.max(0, depth)));
    sb.append(getStatusSymbol()).append(" ").append(StringFormatterUtils.capitalizeAndSplit(title));
    sb.append(" (").append(getDuration()).append(" ms) ").append("\n");
    StringBuilder sbParams = new StringBuilder();
    parameters.forEach(
        (key, value) -> sbParams.append("  ".repeat(Math.max(0, depth))).append("   - ").append(key).append(": ")
            .append(StringFormatterUtils.shorten(String.valueOf(value), 50)).append("\n"));
    sb.append(ANSIFormatterUtils.italic(sbParams.toString()));
    return sb.toString();
  }

}
