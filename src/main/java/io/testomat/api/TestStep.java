package io.testomat.api;

import io.testomat.model.TStepResult;
import java.util.List;
import java.util.Map;

/**
 * Created by Lolik on 20.12.2024
 */
class TestStep {

  public String category;
  public String title;
  public Long duration;
  public String error;
  public List<TestStep> steps;
  public Map<String, Object> options;

  public static TestStep parse(TStepResult step) {
    return null;
   /* return new TestStep.Builder()
        .category(step.getCategory())
        .title(step.get())
        .duration(step.getDuration())
        .status(ste)
        .error(step.getError())
        .steps(step.getSteps().stream().map(TestStep::parse).toList())
        .options(step.getOptions())
        .build();*/
  }
  public Builder toBuilder() {
    return new Builder(this);
  }

  public static class Builder {
    private final TestStep instance;

    public Builder() {
      instance = new TestStep();
    }

    public Builder(TestStep model) {
      instance = model;
    }

    public Builder category(String category) {
      instance.category = category;
      return this;
    }

    public Builder title(String title) {
      instance.title = title;
      return this;
    }

    public Builder duration(Long duration) {
      instance.duration = duration;
      return this;
    }

    public Builder error(String error) {
      instance.error = error;
      return this;
    }

    public Builder steps(List<TestStep> steps) {
      instance.steps = steps;
      return this;
    }

    public Builder options(Map<String, Object> options) {
      instance.options = options;
      return this;
    }

    public TestStep build() {
      return instance;
    }
  }
}