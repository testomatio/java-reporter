package io.testomat.karate.hooks;

import com.intuit.karate.RuntimeHook;
import com.intuit.karate.core.RuntimeHookFactory;

/**
 * Factory for creating {@link RuntimeHook} instances used for Testomat.io integration.
 * <p>
 * Produces {@link KarateHook} instances to be registered in the Karate runtime.
 */
public class KarateHookFactory implements RuntimeHookFactory {


    /**
     * Creates a new {@link RuntimeHook} instance.
     *
     * @return a new {@link KarateHook}
     */
    @Override
    public RuntimeHook create() {
        return new KarateHook();
    }
}
