package io.testomat.cucumber.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Cucumber may recreate the listener instance for each retry within the same JVM
 * so the processed keys and test IDs are kept here instead of in the listener.
 */
public final class CucumberTestRegistry {

    private static final List<String> processedTests =
            Collections.synchronizedList(new ArrayList<>());
    private static final Set<String> testIds =
            Collections.synchronizedSet(new HashSet<>());

    private CucumberTestRegistry() {
    }

    /**
     * Checks whether the composite test key was already processed in this JVM.
     *
     * @param key the composite test key
     * @return true if the key was already processed
     */
    public static boolean isProcessed(String key) {
        return processedTests.contains(key);
    }

    /**
     * Checks whether the test ID was already seen in this JVM.
     *
     * @param testId the test ID
     * @return true if the test ID was already seen
     */
    public static boolean containsTestId(String testId) {
        return testIds.contains(testId);
    }

    /**
     * Records the composite test key and test ID.
     *
     * @param key    the composite test key
     * @param testId the test ID
     */
    public static void add(String key, String testId) {
        processedTests.add(key);
        testIds.add(testId);
    }

    /**
     * Clears all tracked state. Used by tests to isolate scenarios.
     */
    static void reset() {
        processedTests.clear();
        testIds.clear();
    }
}
