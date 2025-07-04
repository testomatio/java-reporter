package com.testomatio.reporter.core;

import static com.testomatio.reporter.constants.PropertyNameConstants.CUSTOM_RUN_UID_PROPERTY_NAME;
import static com.testomatio.reporter.constants.PropertyNameConstants.RUN_TITLE_PROPERTY_NAME;
import static com.testomatio.reporter.logger.LoggerUtils.getLogger;

import com.testomatio.reporter.client.ApiInterface;
import com.testomatio.reporter.client.ClientFactory;
import com.testomatio.reporter.client.TestomatClientFactory;
import com.testomatio.reporter.core.batch.BatchResultManager;
import com.testomatio.reporter.model.TestResult;
import com.testomatio.reporter.propertyconfig.impl.PropertyProviderFactoryImpl;
import com.testomatio.reporter.propertyconfig.interf.PropertyProvider;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * Singleton manager for global test run lifecycle with Testomat.io.
 * Handles test run initialization, suite tracking, result reporting, and finalization.
 * Thread-safe implementation supporting concurrent test execution.
 */
public class GlobalRunManager {
    private static final GlobalRunManager INSTANCE = new GlobalRunManager();
    private static final Logger LOGGER = getLogger(GlobalRunManager.class);

    private final PropertyProvider provider
            = PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();
    private final AtomicInteger activeSuites = new AtomicInteger(0);
    private final AtomicReference<String> runUid = new AtomicReference<>();
    private final AtomicReference<BatchResultManager> batchManager = new AtomicReference<>();
    private final AtomicReference<ApiInterface> apiClient = new AtomicReference<>();
    private final AtomicBoolean shutdownHookRegistered = new AtomicBoolean(false);
    private volatile long startTime;

    private GlobalRunManager() {
    }

    /**
     * Returns the singleton instance of GlobalRunManager.
     *
     * @return the global run manager instance
     */
    public static GlobalRunManager getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes test run if not already initialized.
     * Creates API client, test run UID, batch manager, and registers shutdown hook.
     * Thread-safe operation that ensures single initialization.
     */
    public synchronized void initializeIfNeeded() {
        if (runUid.get() != null) {
            return;
        }

        try {
            ClientFactory clientFactory = TestomatClientFactory.getClientFactory();
            ApiInterface client = clientFactory.createClient();
            String uid = getRunUid(client);

            apiClient.set(client);
            runUid.set(uid);

            batchManager.set(new BatchResultManager(client, uid));
            startTime = System.currentTimeMillis();

            registerShutdownHook();

            LOGGER.fine("Global test run initialized with UID: " + uid);
        } catch (Exception e) {
            LOGGER.severe("Failed to initialize test run: " + e);
        }
    }

    /**
     * Registers JVM shutdown hook for automatic test run finalization.
     * Ensures test run is properly finalized even if application terminates unexpectedly.
     */
    private void registerShutdownHook() {
        if (shutdownHookRegistered.compareAndSet(false, true)) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOGGER.info("JVM is shutting down, finalizing test run...");
                finalizeRun();
            }, "TestRunFinalizer"));
            LOGGER.finer("Shutdown hook registered for test run finalization");
        }
    }

    /**
     * Increments active suite counter and initializes run if needed.
     * Called when a test suite starts execution.
     */
    public void incrementSuiteCounter() {
        activeSuites.incrementAndGet();
        initializeIfNeeded();
    }

    /**
     * Decrements active suite counter.
     * Called when a test suite completes execution.
     */
    public void decrementSuiteCounter() {
        int remaining = activeSuites.decrementAndGet();
        LOGGER.finer("Active suites remaining: " + remaining);
    }

    /**
     * Reports individual test result to batch manager.
     *
     * @param result test case result to report
     */
    public void reportTest(TestResult result) {
        BatchResultManager manager = batchManager.get();
        if (manager != null) {
            manager.addResult(result);
        }
    }

    /**
     * Checks if test run is currently active.
     *
     * @return true if test run is initialized and active
     */
    public boolean isActive() {
        return runUid.get() != null;
    }

    /**
     * Finalizes test run by shutting down batch manager and closing API connection.
     * Calculates run duration and sends completion notification to Testomat.io.
     */
    private void finalizeRun() {
        BatchResultManager manager = batchManager.getAndSet(null);
        if (manager != null) {
            manager.shutdown();
        }

        String uid = runUid.getAndSet(null);
        ApiInterface client = apiClient.getAndSet(null);

        if (uid != null && client != null) {
            try {
                float duration = (System.currentTimeMillis() - startTime) / 1000.0f;
                client.finishTestRun(uid, duration);
                LOGGER.info("Test run finished: " + uid);
            } catch (IOException e) {
                LOGGER.severe("Failed to finish test run" + e.getCause());
            }
        }
    }

    /**
     * Retrieves test run title from properties.
     *
     * @return configured run title or null if not set
     */
    private String getRunTitle() {
        return PropertyProviderFactoryImpl.getPropertyProviderFactory()
                .getPropertyProvider().getProperty(RUN_TITLE_PROPERTY_NAME);
    }

    private String getRunUid(ApiInterface client) throws IOException {
        String customUid;
        try {
            customUid = provider.getProperty(CUSTOM_RUN_UID_PROPERTY_NAME);
        } catch (Exception e) {
            customUid = client.createRun(getRunTitle());
        }
        return customUid;
    }
}
