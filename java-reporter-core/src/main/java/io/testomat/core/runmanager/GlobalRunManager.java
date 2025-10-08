package io.testomat.core.runmanager;

import static io.testomat.core.constants.PropertyNameConstants.CUSTOM_RUN_UID_PROPERTY_NAME;
import static io.testomat.core.constants.PropertyNameConstants.DISABLE_REPORTING_PROPERTY_NAME;
import static io.testomat.core.constants.PropertyNameConstants.RUN_TITLE_PROPERTY_NAME;

import io.testomat.core.artifact.ArtifactLinkDataStorage;
import io.testomat.core.artifact.ReportedTestStorage;
import io.testomat.core.artifact.util.ArtifactKeyGenerator;
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
    private static final Logger log = LoggerFactory.getLogger(GlobalRunManager.class);
    private static final int DELAY_BEFORE_ARTIFACTS_SENDING_MS = 10000;
    private static volatile GlobalRunManager INSTANCE;

    private final PropertyProvider provider;
    private final ClientFactory clientFactory;
    private final AtomicInteger activeSuites = new AtomicInteger(0);
    private final AtomicReference<String> runUid = new AtomicReference<>();
    private final AtomicReference<BatchResultManager> batchManager = new AtomicReference<>();
    private final AtomicReference<ApiInterface> apiClient = new AtomicReference<>();
    private final AtomicBoolean shutdownHookRegistered = new AtomicBoolean(false);
    private volatile long startTime;

    /**
     * Default constructor that initializes all dependencies internally.
     * Used for normal application runtime.
     */
    private GlobalRunManager() {
        this.provider = PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();
        this.clientFactory = TestomatClientFactory.getClientFactory();
    }

    /**
     * Constructor for testing purposes that allows dependency injection.
     * Package-private to allow testing while preventing external instantiation.
     *
     * @param provider      the property provider to use
     * @param clientFactory the client factory to use
     */
    GlobalRunManager(PropertyProvider provider, ClientFactory clientFactory) {
        this.provider = provider;
        this.clientFactory = clientFactory;
    }

    /**
     * Returns the singleton instance of GlobalRunManager.
     * Uses double-checked locking for thread-safe lazy initialization.
     *
     * @return the global run manager instance
     */
    public static GlobalRunManager getInstance() {
        if (INSTANCE == null) {
            synchronized (GlobalRunManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new GlobalRunManager();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Package-private method for testing to set a custom instance.
     * Should only be used in test scenarios.
     *
     * @param instance the instance to set
     */
    static void setInstance(GlobalRunManager instance) {
        synchronized (GlobalRunManager.class) {
            INSTANCE = instance;
        }
    }

    /**
     * Package-private method for testing to reset the singleton instance.
     * Should only be used in test scenarios.
     */
    static void resetInstance() {
        synchronized (GlobalRunManager.class) {
            INSTANCE = null;
        }
    }

    /**
     * Initializes test run if not already initialized.
     * Creates API client, test run UID, batch manager, and registers shutdown hook.
     * Thread-safe operation that ensures single initialization.
     */
    public synchronized void initializeIfNeeded() {
        if (runUid.get() != null || isReportingDisabled()) {
            return;
        }
        if (isReportingDisabled()) {
            log.info("[TESTOMATIO] Testomat.io reporting is disabled");
            return;
        }

        try {
            log.debug("Client factory initialized successfully");

            ApiInterface client = this.clientFactory.createClient();
            log.debug("Client created successfully");

            String uid = defineRunId(client);
            log.debug("Uid defined successfully: {}", uid);

            ArtifactKeyGenerator.initializeRunId(uid);
            log.debug("ArtifactKeyGenerator received uid: {}", uid);

            apiClient.set(client);
            log.debug("Api client is set");

            runUid.set(uid);

            batchManager.set(new BatchResultManager(client, uid));
            log.debug("Batch manager is set");

            startTime = System.currentTimeMillis();
            log.debug("Start time = {}", startTime);

            registerShutdownHook();
            log.debug("Shutdown hook registered");

            log.debug("Global run manger initialized with UID: {}", uid);
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
     * Gets the current run UID if available.
     *
     * @return the current run UID or null if not initialized
     */
    public String getRunUid() {
        return runUid.get();
    }

    /**
     * Gets the number of currently active test suites.
     *
     * @return number of active suites
     */
    public int getActiveSuitesCount() {
        return activeSuites.get();
    }

    /**
     * Finalizes test run by shutting down batch manager and closing API connection.
     * Calculates run duration and sends completion notification to Testomat.io.
     */
    private void finalizeRun() {
        finalizeRun(false);
    }

    /**
     * Finalizes test run with option to force finalization.
     *
     * @param force if true, forces finalization even if there are active suites
     */
    private synchronized void finalizeRun(boolean force) {
        if (!force && activeSuites.get() > 0) {
            log.debug("Skipping finalization - {} active suites remaining", activeSuites.get());
            return;
        }

        String uid = runUid.getAndSet(null);
        if (uid == null) {
            log.debug("Test run already finalized or not initialized");
            return;
        }

        shutdownBatchManager();
        finalizeTestRunWithApi(uid);
    }

    /**
     * Shuts down the batch manager if it exists.
     */
    private void shutdownBatchManager() {
        BatchResultManager manager = batchManager.getAndSet(null);
        if (manager != null) {
            try {
                manager.shutdown();
                log.debug("Batch manager shutdown completed");
            } catch (Exception e) {
                log.error("Error shutting down batch manager: {}", e.getMessage());
            }
        }
    }

    /**
     * Finalizes the test run via API and processes artifacts if present.
     *
     * @param uid the test run unique identifier
     */
    private void finalizeTestRunWithApi(String uid) {
        ApiInterface client = apiClient.getAndSet(null);
        if (client == null) {
            return;
        }

        try {
            float duration = (System.currentTimeMillis() - startTime) / 1000.0f;
            client.finishTestRun(uid, duration);
            log.debug("Test run finished: {} (duration: {}s)", uid, duration);

            processAndSendArtifacts(client, uid);
        } catch (IOException e) {
            log.error("Failed to finish test run {}: {}", uid, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during test run finalization {}: {}", uid, e.getMessage());
        }
    }

    /**
     * Processes and sends artifacts to Testomat.io if any are present.
     *
     * @param client the API client
     * @param uid    the test run unique identifier
     * @throws IOException          if artifact sending fails
     * @throws InterruptedException if thread is interrupted during artifact processing
     */
    private void processAndSendArtifacts(ApiInterface client, String uid)
            throws IOException, InterruptedException {
        if (ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE.isEmpty()) {
            log.debug("No artifacts to send for run: {}", uid);
            return;
        }

        ReportedTestStorage.linkArtifactsToTests(ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE);
        log.info("Getting ready to send artifacts");
        Thread.sleep(DELAY_BEFORE_ARTIFACTS_SENDING_MS);
        log.info("Syncing artifacts");
        client.sendTestWithArtifacts(uid);
        log.debug("Artifacts sent successfully for run: {}", uid);
        ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE.clear();
        log.debug("Artifact storage cleared");
    }

    /**
     * Forces immediate finalization of the test run.
     * Should be used cautiously as it ignores active suites.
     */
    public void forceFinalize() {
        log.warn("Forcing test run finalization");
        finalizeRun(true);
    }

    /**
     * Retrieves test run title from properties.
     *
     * @return configured run title or null if not set
     */
    private String getRunTitle() {
        return this.provider.getProperty(RUN_TITLE_PROPERTY_NAME);
    }

    private String defineRunId(ApiInterface client) throws IOException {
        String customUid;
        try {
            customUid = provider.getProperty(CUSTOM_RUN_UID_PROPERTY_NAME);
        } catch (Exception e) {
            customUid = client.createRun(getRunTitle());
        }
        return customUid;
    }

    private boolean isReportingDisabled() {
        try {
            return provider.getProperty(DISABLE_REPORTING_PROPERTY_NAME) != null
                    && !provider.getProperty(DISABLE_REPORTING_PROPERTY_NAME).trim().isEmpty()
                    && !provider.getProperty(DISABLE_REPORTING_PROPERTY_NAME).equalsIgnoreCase("0");
        } catch (Exception e) {
            return false;
        }
    }
}
