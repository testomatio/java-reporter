package io.testomat.allure;

import java.util.Optional;

public interface AllureClient {
    Optional<String> getCurrentTest();

    Optional<String> getCurrentTestOrStep();
}
