package io.testomat.karate.hooks;

import com.intuit.karate.RuntimeHook;
import com.intuit.karate.core.RuntimeHookFactory;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Factory for creating {@link RuntimeHook} instances used for Testomat.io
 * integration with Karate.
 *
 * <p>This factory produces a {@link DelegatingHook} that wraps an internal
 * {@link KarateHook} and optionally delegates execution to user-provided
 * {@link RuntimeHook} implementations.</p>
 *
 * <p><strong>Thread-safety:</strong> Each invocation of the returned
 * {@link RuntimeHookFactory} creates a new {@link RuntimeHook} instance.
 * This design is safe for parallel execution.</p>
 */
public class KarateHookFactory {

    /**
     * Creates a {@link RuntimeHookFactory} to be registered in the Karate runtime.
     *
     * <p>User hooks must be provided as factories (for example {@code MyHook::new}),
     * not as hook instances. Each factory is invoked once per thread.</p>
     *
     * @param userFactories zero or more {@link RuntimeHookFactory} instances
     *                      used to create user-defined hooks
     * @return a {@link RuntimeHookFactory} that produces a new {@link RuntimeHook}
     *         for each invocation
     */
    public static RuntimeHookFactory create(RuntimeHookFactory... userFactories) {
        return () -> new DelegatingHook(
                new KarateHook(),
            Stream.of(userFactories)
                .map(RuntimeHookFactory::create)
                .collect(Collectors.toList())
        );
    }
}
