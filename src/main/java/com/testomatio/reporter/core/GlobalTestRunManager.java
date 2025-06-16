package com.testomatio.reporter.core;

import com.testomatio.reporter.client.ApiInterface;
import com.testomatio.reporter.client.ClientFactory;
import com.testomatio.reporter.client.TestomatClientFactory;
import com.testomatio.reporter.core.batch.BatchResultManager;
import com.testomatio.reporter.model.TestResult;
import com.testomatio.reporter.property_config.impl.PropertyProviderFactoryImpl;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.testomatio.reporter.constants.PropertyNameConstants.RUN_TITLE_PROPERTY_NAME;

/**
 * Automatically initializes on first use and finalizes on JVM shutdown.
 */
public class GlobalTestRunManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalTestRunManager.class);
    private static final GlobalTestRunManager INSTANCE = new GlobalTestRunManager();

    private final AtomicReference<String> runUid = new AtomicReference<>();
    private final AtomicReference<BatchResultManager> batchManager = new AtomicReference<>();
    private final AtomicReference<ApiInterface> apiClient = new AtomicReference<>();
    private final AtomicBoolean shutdownHookRegistered = new AtomicBoolean(false);

    private volatile long startTime;

    private final String runTitle = PropertyProviderFactoryImpl.getPropertyProviderFactory()
            .getPropertyProvider()
            .getProperty(RUN_TITLE_PROPERTY_NAME);

    private GlobalTestRunManager() {
    }

    public static GlobalTestRunManager getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes test run if needed. Called automatically on first test or suite start.
     */
    public synchronized void initializeIfNeeded() {
        if (runUid.get() != null) {
            return;
        }

        try {
            ClientFactory clientFactory = TestomatClientFactory.getClientFactory();
            ApiInterface client = clientFactory.createClient();
            String uid = client.createTestRun(runTitle);

            apiClient.set(client);
            runUid.set(uid);
            batchManager.set(new BatchResultManager(client, uid));
            startTime = System.currentTimeMillis();

            registerShutdownHook();
            LOGGER.info("Global test run initialized with UID: {} (auto-finish on shutdown)", uid);

        } catch (Exception e) {
            LOGGER.error("Failed to initialize test run", e);
        }
    }

    /**
     * Called when a test suite starts. Ensures test run is initialized.
     */
    public void onSuiteStart() {
        initializeIfNeeded();
    }

    /**
     * Reports a test result to the batch manager.
     */
    public void reportTest(TestResult result) {
        initializeIfNeeded();

        BatchResultManager manager = batchManager.get();
        if (manager != null) {
            manager.addResult(result);
        }
    }

    /**
     * Checks if the test run is currently active.
     */
    public boolean isActive() {
        return runUid.get() != null;
    }

    /**
     * Manually finishes the test run. Usually called by shutdown hook.
     */
    public synchronized void finishRun() {
        if (runUid.get() == null) {
            return;
        }

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
                LOGGER.info("Test run finished: {} (duration: {}s)", uid, duration);
            } catch (IOException e) {
                LOGGER.error("Failed to finish test run", e);
            }
        }
    }

    /**
     * Registers JVM shutdown hook to automatically finish test run.
     */
    private void registerShutdownHook() {
        if (shutdownHookRegistered.compareAndSet(false, true)) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (runUid.get() != null) {
                    LOGGER.debug("JVM shutdown detected, finishing test run...");
                    finishRun();
                }
            }, "TestomatShutdownHook"));

            LOGGER.debug("Shutdown hook registered for automatic test run finalization");
        }
    }
}