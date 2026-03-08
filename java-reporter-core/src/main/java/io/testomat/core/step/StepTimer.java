package io.testomat.core.step;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StepTimer {

    private static final Map<String, Long> START_TIMES = new ConcurrentHashMap<>();

    public static void start(String key) {
        START_TIMES.put(key, System.currentTimeMillis());
    }

    public static long stop(String key) {
        Long start = START_TIMES.remove(key);
        if (start == null) {
            return -1;
        }
        return System.currentTimeMillis() - start;
    }
}
