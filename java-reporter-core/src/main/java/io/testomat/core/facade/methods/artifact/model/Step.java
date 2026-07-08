package io.testomat.core.facade.methods.artifact.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.testomat.core.step.StepStatus;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Step {

    private String category;
    private String title;
    private StepStatus status;
    private String log;
    private String error;
    private double duration;
    private final List<String> artifacts;
    private final List<Step> steps;

    public Step(String title, StepStatus status, String log, String error, double duration, String category,
        List<String> artifacts, List<Step> steps) {
        this.title = title;
        this.status = status;
        this.log = log;
        this.error = error;
        this.duration = duration;
        this.category = category;
        this.artifacts = artifacts;
        this.steps = steps;
    }

    public String getCategory() {
        return category;
    }

    public double getDuration() {
        return duration;
    }

    public String getError() {
        return error;
    }

    public String getLog() {
        return log;
    }

    public StepStatus getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getArtifacts() {
        return artifacts;
    }

    public List<Step> getSteps() {
        return steps;
    }
}
