package io.testomat.allure;

import io.qameta.allure.AllureLifecycle;
import java.util.Optional;

/**
 * Allure client implementation for accessing current test context.
 */
public class AllureClientImpl implements AllureClient {

    private final AllureLifecycle lifecycle;

    /**
     * @param lifecycle Allure lifecycle instance
     */
    public AllureClientImpl(AllureLifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    /**
     * @return current test UUID if present
     */
    @Override
    public Optional<String> getCurrentTest() {
        return lifecycle.getCurrentTestCase();
    }

    /**
     * @return current test or step UUID if present
     */
    @Override
    public Optional<String> getCurrentTestOrStep() {
        return lifecycle.getCurrentTestCaseOrStep();
    }

}
