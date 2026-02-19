package io.testomat.karate.hooks;

import com.intuit.karate.RuntimeHook;
import com.intuit.karate.Suite;
import com.intuit.karate.core.FeatureRuntime;
import com.intuit.karate.core.ScenarioRuntime;
import com.intuit.karate.core.Step;
import com.intuit.karate.core.StepResult;
import com.intuit.karate.http.HttpRequest;
import com.intuit.karate.http.Response;
import java.util.List;

/**
 * A {@link RuntimeHook} that delegates all Karate lifecycle events to an
 * internal {@link KarateHook} and a list of user-provided hooks.
 *
 * <p>The internal hook is invoked first for {@code before*} callbacks and last
 * for {@code after*} callbacks. Boolean results are aggregated using logical
 * OR.</p>
 *
 * <p>Instances are created per execution thread and are safe for parallel
 * execution.</p>
 */
public final class DelegatingHook implements RuntimeHook {

    private final KarateHook karateHook;
    private final List<RuntimeHook> customHooks;

    public DelegatingHook(KarateHook karateHook,
            List<RuntimeHook> customHooks) {
        this.karateHook = karateHook;
        this.customHooks = customHooks;
    }

    KarateHook getKarateHook() {
        return karateHook;
    }

    List<RuntimeHook> getCustomHooks() {
        return customHooks;
    }

    @Override
    public boolean beforeScenario(ScenarioRuntime sr) {
        boolean result = karateHook.beforeScenario(sr);
        for (RuntimeHook h : customHooks) {
            result |= h.beforeScenario(sr);
        }
        return result;
    }

    @Override
    public void afterScenario(ScenarioRuntime sr) {
        for (RuntimeHook h : customHooks) {
            h.afterScenario(sr);
        }
        karateHook.afterScenario(sr);
    }

    @Override
    public void afterScenarioOutline(ScenarioRuntime sr) {
        for (RuntimeHook h : customHooks) {
            h.afterScenarioOutline(sr);
        }
        karateHook.afterScenarioOutline(sr);
    }

    @Override
    public boolean beforeFeature(FeatureRuntime fr) {
        boolean result = karateHook.beforeFeature(fr);
        for (RuntimeHook h : customHooks) {
            result |= h.beforeFeature(fr);
        }
        return result;
    }

    @Override
    public void afterFeature(FeatureRuntime fr) {
        for (RuntimeHook h : customHooks) {
            h.afterFeature(fr);
        }
        karateHook.afterFeature(fr);
    }

    @Override
    public void beforeSuite(Suite suite) {
        karateHook.beforeSuite(suite);
        for (RuntimeHook h : customHooks) {
            h.beforeSuite(suite);
        }
    }

    @Override
    public void afterSuite(Suite suite) {
        for (RuntimeHook h : customHooks) {
            h.afterSuite(suite);
        }
        karateHook.afterSuite(suite);
    }

    @Override
    public boolean beforeStep(Step step, ScenarioRuntime sr) {
        boolean result = karateHook.beforeStep(step, sr);
        for (RuntimeHook h : customHooks) {
            result |= h.beforeStep(step, sr);
        }
        return result;
    }

    @Override
    public void afterStep(StepResult result, ScenarioRuntime sr) {
        for (RuntimeHook h : customHooks) {
            h.afterStep(result, sr);
        }
        karateHook.afterStep(result, sr);
    }

    @Override
    public void beforeHttpCall(HttpRequest request, ScenarioRuntime sr) {
        karateHook.beforeHttpCall(request, sr);
        for (RuntimeHook h : customHooks) {
            h.beforeHttpCall(request, sr);
        }
    }

    @Override
    public void afterHttpCall(HttpRequest request,
            Response response,
            ScenarioRuntime sr) {
        for (RuntimeHook h : customHooks) {
            h.afterHttpCall(request, response, sr);
        }
        karateHook.afterHttpCall(request, response, sr);
    }
}
