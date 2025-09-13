package io.testomat.core.runmanager;

import static io.testomat.core.constants.PropertyNameConstants.CUSTOM_RUN_UID_PROPERTY_NAME;
import static io.testomat.core.constants.PropertyNameConstants.RUN_TITLE_PROPERTY_NAME;

import io.testomat.core.batch.BatchResultManager;
import io.testomat.core.client.ApiInterface;
import io.testomat.core.client.ClientFactory;
import io.testomat.core.client.TestomatClientFactory;
import io.testomat.core.model.TestResult;
import io.testomat.core.propertyconfig.impl.PropertyProviderFactoryImpl;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton manager for global test run lifecycle with Testomat.io.
 * Handles test run initialization, suite tracking, result reporting, and finalization.
 * Thread-safe implementation supporting concurrent test execution.
 */
public class GlobalRunManager {
    private static final GlobalRunManager INSTANCE = new GlobalRunManager();
    private static final Logger log = LoggerFactory.getLogger(GlobalRunManager.class);
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
            log.debug("Client factory initialized successfully");
            ApiInterface client = clientFactory.createClient();
            log.debug("Client created successfully");
            String uid = getCustomRunUid(client);
            if (uid != null) {
                log.debug("Custom uid = {}", uid);
            } else {
                log.debug("Custom uid is not provided");
            }

            apiClient.set(client);
            log.debug("Api client is set");
            runUid.set(uid);
            log.debug("Run ID is set: {}", runUid);

            batchManager.set(new BatchResultManager(client, uid));
            log.debug("Batch manager is set");
            startTime = System.currentTimeMillis();
            log.debug("Start time = {}", startTime);

            registerShutdownHook();
            log.debug("Shutdown hook registered");

            log.debug("Global test run initialized with UID: {}", uid);
        } catch (Exception e) {
            log.error("Failed to initialize test run: {}", String.valueOf(e));
        }
    }

    /**
     * Registers JVM shutdown hook for automatic test run finalization.
     * Ensures test run is properly finalized even if application terminates unexpectedly.
     */
    private void registerShutdownHook() {
        if (shutdownHookRegistered.compareAndSet(false, true)) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.debug("JVM is shutting down, finalizing test run...");
                finalizeRun();
            }, "TestRunFinalizer"));
            log.debug("Shutdown hook registered for test run finalization");
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
        log.debug("Active suites remaining: {}", remaining);
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
                log.debug("Test run finished: {}", uid);
            } catch (IOException e) {
                log.error("Failed to finish test run{}", String.valueOf(e.getCause()));
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

    private String getCustomRunUid(ApiInterface client) throws IOException {
        String customUid;
        try {
            customUid = provider.getProperty(CUSTOM_RUN_UID_PROPERTY_NAME);
        } catch (Exception e) {
            customUid = client.createRun(getRunTitle());
        }
        return customUid;
    }
}
