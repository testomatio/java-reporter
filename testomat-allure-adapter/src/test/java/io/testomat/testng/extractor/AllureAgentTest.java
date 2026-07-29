package io.testomat.testng.extractor;

import static org.assertj.core.api.Assertions.assertThat;

import io.qameta.allure.TmsLink;
import io.testomat.agent.AllureAgent;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class AllureAgentTest {

    @Test
    void transformsTmsExtractorLoadedAfterInstallation() throws NoSuchMethodException {
        AllureAgent.install();
        Method method = TestClass.class.getDeclaredMethod("testMethod");

        String testId = new RuntimeExtractor().getTestId(method);

        assertThat(testId).isEqualTo("TMS-123");
    }

    private static class RuntimeExtractor {
        String getTestId(Method method) {
            return null;
        }
    }

    private static class TestClass {
        @TmsLink("TMS-123")
        void testMethod() {
        }
    }
}
