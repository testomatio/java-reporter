package io.testomat.karate.hooks;

import com.intuit.karate.RuntimeHook;
import com.intuit.karate.core.RuntimeHookFactory;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class KarateHookFactoryTest {

    @Test
    void createShouldReturnRuntimeHookFactory() {
        RuntimeHookFactory factory = KarateHookFactory.create();

        assertThat(factory).isNotNull();
    }

    @Test
    void factoryCreateShouldReturnDelegatingHook() {
        RuntimeHookFactory factory = KarateHookFactory.create();

        RuntimeHook hook = factory.create();

        assertThat(hook)
            .isNotNull()
            .isInstanceOf(DelegatingHook.class);
    }

    @Test
    void factoryCreateShouldCreateNewHookEachTime() {
        RuntimeHookFactory factory = KarateHookFactory.create();

        RuntimeHook hook1 = factory.create();
        RuntimeHook hook2 = factory.create();

        assertThat(hook1).isNotSameAs(hook2);
    }

    @Test
    void factoryCreateShouldAlwaysCreateNewKarateHook() {
        RuntimeHookFactory factory = KarateHookFactory.create();

        DelegatingHook hook1 = (DelegatingHook) factory.create();
        DelegatingHook hook2 = (DelegatingHook) factory.create();

        assertThat(hook1).isNotSameAs(hook2);
        assertThat(hook1.getKarateHook())
            .isNotSameAs(hook2.getKarateHook());
    }

    @Test
    void createWithUserFactoriesShouldCreateCustomHooks() {
        RuntimeHookFactory userFactory1 = UserHook::new;
        RuntimeHookFactory userFactory2 = UserHook::new;

        RuntimeHookFactory factory =
            KarateHookFactory.create(userFactory1, userFactory2);

        DelegatingHook hook = (DelegatingHook) factory.create();

        assertThat(hook.getCustomHooks())
            .hasSize(2)
            .allMatch(h -> h instanceof UserHook);
    }

    @Test
    void userFactoriesShouldBeInvokedOnEachCreateCall() {
        AtomicInteger counter = new AtomicInteger();

        RuntimeHookFactory userFactory = () -> {
            counter.incrementAndGet();
            return new UserHook();
        };

        RuntimeHookFactory factory =
            KarateHookFactory.create(userFactory);

        factory.create();
        factory.create();

        assertThat(counter.get()).isEqualTo(2);
    }

    @Test
    void createWithNoUserFactoriesShouldCreateDelegatingHookWithEmptyCustomHooks() {
        RuntimeHookFactory factory = KarateHookFactory.create();

        DelegatingHook hook = (DelegatingHook) factory.create();

        assertThat(hook.getCustomHooks()).isEmpty();
    }

    static class UserHook implements RuntimeHook {}
}
