package io.testomat.allure;

import io.qameta.allure.AllureLifecycle;
import java.util.Optional;

public class AllureClientImpl implements AllureClient {

    private final AllureLifecycle lifecycle;

    public AllureClientImpl(AllureLifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    @Override
    public Optional<String> getCurrentTest() {
        return lifecycle.getCurrentTestCase();
    }

    @Override
    public Optional<String> getCurrentTestOrStep() {
        return lifecycle.getCurrentTestCaseOrStep();
    }

}
