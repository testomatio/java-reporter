package io.testomat.core.step;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StepData {
    private final List<String> directories =
        Collections.synchronizedList(new ArrayList<>());

    private final List<String> artifacts =
        Collections.synchronizedList(new ArrayList<>());

    public List<String> getDirectories() {
        return directories;
    }

    public List<String> getArtifacts() {
        return artifacts;
    }
}