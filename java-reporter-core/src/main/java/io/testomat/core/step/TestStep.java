package io.testomat.core.step;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TestStep {
    private UUID id;
    private String category;
    private String stepTitle;
    private StepStatus status;
    private double duration;
    private List<TestStep> substeps = new ArrayList<>();
    private String[] artifacts;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStepTitle() {
        return stepTitle;
    }

    public void setStepTitle(String stepTitle) {
        this.stepTitle = stepTitle;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public List<TestStep> getSubsteps() {
        return substeps;
    }

    public void setSubsteps(List<TestStep> substeps) {
        this.substeps = substeps;
    }

    public String[] getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(String[] artifacts) {
        this.artifacts = artifacts;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public StepStatus getStatus() {
        return status;
    }

    public void setStatus(StepStatus status) {
        this.status = status;
    }
}
