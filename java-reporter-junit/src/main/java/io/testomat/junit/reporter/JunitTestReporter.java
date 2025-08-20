package io.testomat.junit.reporter;

import io.testomat.core.exception.ReportTestResultException;
import io.testomat.core.model.TestMetadata;
import io.testomat.core.model.TestResult;
import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.junit.constructor.JUnitTestResultConstructor;
import io.testomat.junit.extractor.JunitMetaDataExtractor;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread-safe reporter for JUnit test results that integrates with the Testomat reporting system.
 * This class handles the extraction of test metadata, construction of test results, and
 * submission to the global run manager while ensuring thread safety through per-test locking.
 */
public class JunitTestReporter {
    private static final Logger log = LoggerFactory.getLogger(JunitTestReporter.class);

    private final JUnitTestResultConstructor resultConstructor;
    private final JunitMetaDataExtractor metaDataExtractor;
    private final GlobalRunManager runManager;
    private final ConcurrentHashMap<String, ReentrantLock> testLocks;

    /**
     * Creates a new JunitTestReporter with default dependencies.
     */
    public JunitTestReporter() {
        this.metaDataExtractor = new JunitMetaDataExtractor();
        this.resultConstructor = new JUnitTestResultConstructor(metaDataExtractor);
        this.runManager = GlobalRunManager.getInstance();
        this.testLocks = new ConcurrentHashMap<>();
    }

    /**
     * Creates a new JunitTestReporter with injected dependencies.
     *
     * @param resultConstructor the test result constructor, must not be null
     * @param metaDataExtractor the metadata extractor, must not be null
     * @param runManager        the global run manager, must not be null
     * @throws IllegalArgumentException if any parameter is null
     */
    public JunitTestReporter(JUnitTestResultConstructor resultConstructor,
                             JunitMetaDataExtractor metaDataExtractor,
                             GlobalRunManager runManager) {
        this.resultConstructor = Objects.requireNonNull(resultConstructor,
                "resultConstructor cannot be null");
        this.metaDataExtractor = Objects.requireNonNull(metaDataExtractor,
                "metaDataExtractor cannot be null");
        this.runManager = Objects.requireNonNull(runManager, "runManager cannot be null");
        this.testLocks = new ConcurrentHashMap<>();
    }

    /**
     * Reports a test result to the Testomat system in a thread-safe manner.
     *
     * @param context the JUnit extension context, must not be null
     * @param status  the test status (e.g., "passed", "failed", "skipped")
     * @param message optional message or error details
     * @throws IllegalArgumentException if context is null
     */
    public void reportTestResult(ExtensionContext context, String status, String message) {
        Objects.requireNonNull(context, "ExtensionContext cannot be null");

        if (!runManager.isActive()) {
            log.debug("Run manager is not active, skipping test result reporting for: {}",
                    context.getDisplayName());
            return;
        }

        String lockKey = generateLockKey(context);
        ReentrantLock lock = testLocks.computeIfAbsent(lockKey, k -> new ReentrantLock());

        lock.lock();
        try {
            doReportTestResult(context, status, message);
        } finally {
            lock.unlock();
            cleanupLockIfUnused(lockKey, lock);
        }
    }

    private void doReportTestResult(ExtensionContext context, String status, String message) {
        TestMetadata metadata = null;
        try {
            metadata = metaDataExtractor.extractTestMetadata(context);
            TestResult result = resultConstructor.constructTestRunResult(
                    metadata, message, status, context);

            log.debug("Reporting test result: title='{}', status='{}', rid='{}'",
                    result.getTitle(), status, result.getRid());

            runManager.reportTest(result);

            log.info("Successfully reported test result for: {} with status: {}",
                    result.getTitle(), status);

        } catch (Exception e) {
            String testName = metadata != null ? metadata.getTitle() : context.getDisplayName();
            String errorMessage =
                    String.format("Failed to report test result for: %s (uniqueId: %s)",
                            testName, context.getUniqueId());
            log.error(errorMessage, e);
            throw new ReportTestResultException(errorMessage, e);
        }
    }

    /**
     * Generates a unique lock key for the given test context.
     * Combines the context's unique ID with thread and timestamp information
     * to ensure uniqueness even for parameterized tests without explicit names.
     *
     * @param context the test context
     * @return a unique lock key
     */
    private String generateLockKey(ExtensionContext context) {
        String uniqueId = context.getUniqueId();
        long timestamp = System.nanoTime();
        long threadId = Thread.currentThread().getId();

        return String.format("%s-t%d-n%d", uniqueId, threadId, timestamp);
    }

    /**
     * Removes the lock from the map if no threads are waiting for it.
     * This prevents memory leaks from accumulating unused locks.
     *
     * @param lockKey the key of the lock to potentially remove
     * @param lock    the lock to check for queued threads
     */
    private void cleanupLockIfUnused(String lockKey, ReentrantLock lock) {
        if (!lock.hasQueuedThreads()) {
            testLocks.remove(lockKey);
        }
    }
}
