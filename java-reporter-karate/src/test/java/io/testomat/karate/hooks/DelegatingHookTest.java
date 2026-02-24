package io.testomat.karate.hooks;

import com.intuit.karate.*;
import com.intuit.karate.core.FeatureRuntime;
import com.intuit.karate.core.ScenarioRuntime;
import com.intuit.karate.core.Step;
import com.intuit.karate.core.StepResult;
import com.intuit.karate.http.HttpRequest;
import com.intuit.karate.http.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DelegatingHookTest {

    @Mock
    KarateHook karateHook;

    @Mock
    RuntimeHook customHook1;

    @Mock
    RuntimeHook customHook2;

    @Mock
    ScenarioRuntime scenarioRuntime;

    @Mock
    FeatureRuntime featureRuntime;

    @Mock
    Suite suite;

    @Mock
    Step step;

    @Mock
    StepResult stepResult;

    @Mock
    HttpRequest httpRequest;

    @Mock
    Response response;

    DelegatingHook delegatingHook;

    @BeforeEach
    void setUp() {
        delegatingHook = new DelegatingHook(
            karateHook,
            List.of(customHook1, customHook2)
        );
    }

    @Test
    void beforeScenarioCallsAllHooksAndAggregatesBooleanResult() {
        when(karateHook.beforeScenario(scenarioRuntime)).thenReturn(false);
        when(customHook1.beforeScenario(scenarioRuntime)).thenReturn(false);
        when(customHook2.beforeScenario(scenarioRuntime)).thenReturn(true);

        boolean result = delegatingHook.beforeScenario(scenarioRuntime);

        assertThat(result).isTrue();

        verify(karateHook).beforeScenario(scenarioRuntime);
        verify(customHook1).beforeScenario(scenarioRuntime);
        verify(customHook2).beforeScenario(scenarioRuntime);
    }

    @Test
    void afterScenarioCallsCustomHooksFirstThenKarateHook() {
        delegatingHook.afterScenario(scenarioRuntime);

        InOrder inOrder = inOrder(customHook1, customHook2, karateHook);
        inOrder.verify(customHook1).afterScenario(scenarioRuntime);
        inOrder.verify(customHook2).afterScenario(scenarioRuntime);
        inOrder.verify(karateHook).afterScenario(scenarioRuntime);
    }

    @Test
    void afterScenarioOutlineCallsCustomHooksFirstThenKarateHook() {
        delegatingHook.afterScenarioOutline(scenarioRuntime);

        InOrder inOrder = inOrder(customHook1, customHook2, karateHook);
        inOrder.verify(customHook1).afterScenarioOutline(scenarioRuntime);
        inOrder.verify(customHook2).afterScenarioOutline(scenarioRuntime);
        inOrder.verify(karateHook).afterScenarioOutline(scenarioRuntime);
    }

    @Test
    void beforeFeatureAggregatesBooleanResult() {
        when(karateHook.beforeFeature(featureRuntime)).thenReturn(false);
        when(customHook1.beforeFeature(featureRuntime)).thenReturn(false);
        when(customHook2.beforeFeature(featureRuntime)).thenReturn(false);

        boolean result = delegatingHook.beforeFeature(featureRuntime);

        assertThat(result).isFalse();
    }

    @Test
    void afterFeatureCallsCustomHooksFirstThenKarateHook() {
        delegatingHook.afterFeature(featureRuntime);

        InOrder inOrder = inOrder(customHook1, customHook2, karateHook);
        inOrder.verify(customHook1).afterFeature(featureRuntime);
        inOrder.verify(customHook2).afterFeature(featureRuntime);
        inOrder.verify(karateHook).afterFeature(featureRuntime);
    }

    @Test
    void beforeSuiteCallsKarateFirstThenCustoms() {
        delegatingHook.beforeSuite(suite);

        InOrder inOrder = inOrder(karateHook, customHook1, customHook2);
        inOrder.verify(karateHook).beforeSuite(suite);
        inOrder.verify(customHook1).beforeSuite(suite);
        inOrder.verify(customHook2).beforeSuite(suite);
    }

    @Test
    void afterSuiteCallsCustomsFirstThenKarate() {
        delegatingHook.afterSuite(suite);

        InOrder inOrder = inOrder(customHook1, customHook2, karateHook);
        inOrder.verify(customHook1).afterSuite(suite);
        inOrder.verify(customHook2).afterSuite(suite);
        inOrder.verify(karateHook).afterSuite(suite);
    }

    @Test
    void beforeStepAggregatesBooleanResult() {
        when(karateHook.beforeStep(step, scenarioRuntime)).thenReturn(true);
        when(customHook1.beforeStep(step, scenarioRuntime)).thenReturn(false);
        when(customHook2.beforeStep(step, scenarioRuntime)).thenReturn(false);

        boolean result = delegatingHook.beforeStep(step, scenarioRuntime);

        assertThat(result).isTrue();
    }

    @Test
    void afterStepCallsCustomsFirstThenKarate() {
        delegatingHook.afterStep(stepResult, scenarioRuntime);

        InOrder inOrder = inOrder(customHook1, customHook2, karateHook);
        inOrder.verify(customHook1).afterStep(stepResult, scenarioRuntime);
        inOrder.verify(customHook2).afterStep(stepResult, scenarioRuntime);
        inOrder.verify(karateHook).afterStep(stepResult, scenarioRuntime);
    }

    @Test
    void beforeHttpCallCallsKarateFirstThenCustoms() {
        delegatingHook.beforeHttpCall(httpRequest, scenarioRuntime);

        InOrder inOrder = inOrder(karateHook, customHook1, customHook2);
        inOrder.verify(karateHook).beforeHttpCall(httpRequest, scenarioRuntime);
        inOrder.verify(customHook1).beforeHttpCall(httpRequest, scenarioRuntime);
        inOrder.verify(customHook2).beforeHttpCall(httpRequest, scenarioRuntime);
    }

    @Test
    void afterHttpCallCallsCustomsFirstThenKarate() {
        delegatingHook.afterHttpCall(httpRequest, response, scenarioRuntime);

        InOrder inOrder = inOrder(customHook1, customHook2, karateHook);
        inOrder.verify(customHook1).afterHttpCall(httpRequest, response, scenarioRuntime);
        inOrder.verify(customHook2).afterHttpCall(httpRequest, response, scenarioRuntime);
        inOrder.verify(karateHook).afterHttpCall(httpRequest, response, scenarioRuntime);
    }
}

