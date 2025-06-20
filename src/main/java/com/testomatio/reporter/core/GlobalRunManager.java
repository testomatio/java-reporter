package com.testomatio.reporter.core;

import com.testomatio.reporter.client.ApiInterface;
import com.testomatio.reporter.client.ClientFactory;
import com.testomatio.reporter.client.TestomatClientFactory;
import com.testomatio.reporter.core.batch.BatchResultManager;
import com.testomatio.reporter.model.TestRunResult;
import com.testomatio.reporter.property_config.impl.PropertyProviderFactoryImpl;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import static com.testomatio.reporter.constants.PropertyNameConstants.RUN_TITLE_PROPERTY_NAME;
import static com.testomatio.reporter.logger.LoggerUtils.getLogger;

public class GlobalRunManager {
    private static final GlobalRunManager INSTANCE = new GlobalRunManager();

    final AtomicInteger activeSuites = new AtomicInteger(0);
    final AtomicReference<String> runUid = new AtomicReference<>();
    final AtomicReference<BatchResultManager> batchManager = new AtomicReference<>();
    final AtomicReference<ApiInterface> apiClient = new AtomicReference<>();
    private final AtomicBoolean shutdownHookRegistered = new AtomicBoolean(false);
    private final Logger LOGGER = getLogger(GlobalRunManager.class);
    volatile long startTime;


    private GlobalRunManager() {
    }

    public static GlobalRunManager getInstance() {
        return INSTANCE;
    }

    public synchronized void initializeIfNeeded() {
        if (runUid.get() != null) {
            return;
        }

        try {
            ClientFactory clientFactory = TestomatClientFactory.getClientFactory();
            ApiInterface client = clientFactory.createClient();
            String uid = client.createRun(getRunTitle());

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

    private void registerShutdownHook() {
        if (shutdownHookRegistered.compareAndSet(false, true)) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOGGER.info("JVM is shutting down, finalizing test run...");
                finalizeRun();
            }, "TestRunFinalizer"));
            LOGGER.finer("Shutdown hook registered for test run finalization");
        }
    }

    public void incrementSuiteCounter() {
        activeSuites.incrementAndGet();
        initializeIfNeeded();
    }

    public void decrementSuiteCounter() {
        int remaining = activeSuites.decrementAndGet();
        LOGGER.finer("Active suites remaining: " + remaining);
    }

    public void reportTest(TestRunResult result) {
        BatchResultManager manager = batchManager.get();
        if (manager != null) {
            manager.addResult(result);
        }
    }

    public boolean isActive() {
        return runUid.get() != null;
    }

    void finalizeRun() {
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

    String getRunTitle() {
        return PropertyProviderFactoryImpl.getPropertyProviderFactory()
                .getPropertyProvider().getProperty(RUN_TITLE_PROPERTY_NAME);
    }
}