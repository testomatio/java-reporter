package io.testomat.api;

import java.util.List;
import java.util.Map;

/**
 * Created by Lolik on 20.12.2024
 */
class TestStepModel {

  public String category;
  public String title;
  public Long duration;
  public String status;
  public String error;
  public List<TestStepModel> steps;
  public Map<String, Object> options;

  public Builder toBuilder() {
    return new Builder(this);
  }

  public static class Builder {
    private final TestStepModel instance;

    public Builder() {
      instance = new TestStepModel();
    }

    public Builder(TestStepModel model) {
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

    public Builder status(String status) {
      instance.status = status;
      return this;
    }

    public Builder error(String error) {
      instance.error = error;
      return this;
    }

    public Builder steps(List<TestStepModel> steps) {
      instance.steps = steps;
      return this;
    }

    public Builder options(Map<String, Object> options) {
      instance.options = options;
      return this;
    }

    public TestStepModel build() {
      return instance;
    }
  }
}