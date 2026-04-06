package io.testomat.testomat;

import io.testomat.core.facade.Testomatio;

/**
 * Testomat client implementation delegating calls to Testomatio API.
 */
public class TestomatClientImpl implements TestomatClient {

    /** Sends test level artifact. */
    @Override
    public void artifact(String path) {
        Testomatio.artifact(path);
    }

    /** Sends step level artifact. */
    @Override
    public void stepArtifact(String path) {
        Testomatio.stepArtifact(path);
    }

}
