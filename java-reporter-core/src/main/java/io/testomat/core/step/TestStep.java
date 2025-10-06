package io.testomat.core.step;

import java.util.ArrayList;
import java.util.List;

public class TestStep {
    private String category;
    private String stepTitle;
    private double duration;
    private List<TestStep> substeps = new ArrayList<>();

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
}
