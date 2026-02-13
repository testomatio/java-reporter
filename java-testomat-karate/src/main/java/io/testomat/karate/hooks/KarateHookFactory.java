package io.testomat.karate.hooks;

import com.intuit.karate.RuntimeHook;
import com.intuit.karate.core.RuntimeHookFactory;

public class KarateHookFactory implements RuntimeHookFactory {

    @Override
    public RuntimeHook create() {
        return new KarateHook();
    }
}
