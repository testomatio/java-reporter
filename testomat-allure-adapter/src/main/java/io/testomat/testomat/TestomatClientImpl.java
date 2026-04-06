package io.testomat.testomat;

import io.testomat.core.facade.Testomatio;

public class TestomatClientImpl implements TestomatClient {

    @Override
    public void artifact(String path) {
        Testomatio.artifact(path);
    }

    @Override
    public void stepArtifact(String path) {
        Testomatio.stepArtifact(path);
    }

}
