package io.testomat.karate.hooks;

import com.intuit.karate.RuntimeHook;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KarateHookFactoryTest {

    @Test
    void shouldCreateKarateHook() {
        KarateHookFactory factory = new KarateHookFactory();

        RuntimeHook hook = factory.create();

        assertThat(hook).isNotNull();
        assertThat(hook).isInstanceOf(KarateHook.class);
    }

    @Test
    void shouldCreateNewInstanceEachTime() {
        KarateHookFactory factory = new KarateHookFactory();

        RuntimeHook hook1 = factory.create();
        RuntimeHook hook2 = factory.create();

        assertThat(hook1).isNotSameAs(hook2);
    }

    @Test
    void shouldCreateNewInstanceInParallel() throws Exception {
        KarateHookFactory factory = new KarateHookFactory();

        int threads = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        try {
            List<Callable<RuntimeHook>> tasks = IntStream.range(0, threads)
                .mapToObj(i -> (Callable<RuntimeHook>) factory::create)
                .collect(Collectors.toList());

            List<Future<RuntimeHook>> futures = executor.invokeAll(tasks);

            List<RuntimeHook> hooks = new ArrayList<>();
            for (Future<RuntimeHook> future : futures) {
                hooks.add(future.get());
            }

            assertThat(hooks)
                .hasSize(threads)
                .doesNotHaveDuplicates()
                .allMatch(h -> h instanceof KarateHook);
        } finally {
            executor.shutdownNow();
        }
    }

}

