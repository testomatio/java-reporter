package com.core;

import com.client.ApiInterface;
import com.client.ClientFactory;
import com.client.TestomatClientFactory;
import com.core.batch.BatchResultManager;
import com.model.TestResult;
import com.property_config.impl.PropertyProviderFactoryImpl;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.constants.PropertyNameConstants.RUN_TITLE_PROPERTY_NAME;

public class GlobalTestRunManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalTestRunManager.class);
    private static final GlobalTestRunManager INSTANCE = new GlobalTestRunManager();

    private final AtomicInteger activeSuites = new AtomicInteger(0);
    private final AtomicReference<String> runUid = new AtomicReference<>();
    private final AtomicReference<BatchResultManager> batchManager = new AtomicReference<>();
    private final AtomicReference<ApiInterface> apiClient = new AtomicReference<>();
    private volatile long startTime;

    private final String runTitle = PropertyProviderFactoryImpl.getPropertyProviderFactory()
            .getPropertyProvider()
            .getProperty(RUN_TITLE_PROPERTY_NAME);

    private GlobalTestRunManager() {
    }

    public static GlobalTestRunManager getInstance() {
        return INSTANCE;
    }

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

            LOGGER.info("Global test run initialized with UID: {}", uid);
        } catch (Exception e) {
            LOGGER.error("Failed to initialize test run <-", e);
        }
    }

    public void incrementSuiteCounter() {
        activeSuites.incrementAndGet();
        initializeIfNeeded();
    }

    public void decrementSuiteCounter() {
        int remaining = activeSuites.decrementAndGet();
        if (remaining == 0) {
            finalizeRun();
        }
    }

    public void reportTest(TestResult result) {
        BatchResultManager manager = batchManager.get();
        if (manager != null) {
            manager.addResult(result);
        }
    }

    public boolean isActive() {
        return runUid.get() != null;
    }

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
                LOGGER.info("Test run finished: {}", uid);
            } catch (IOException e) {
                LOGGER.error("Failed to finish test run", e);
            }
        }
    }
}