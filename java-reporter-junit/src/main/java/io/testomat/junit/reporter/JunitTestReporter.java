package io.testomat.junit.reporter;

import io.testomat.core.exception.ReportTestResultException;
import io.testomat.core.model.TestMetadata;
import io.testomat.core.model.TestResult;
import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.junit.constructor.JUnitTestResultConstructor;
import io.testomat.junit.extractor.JunitMetaDataExtractor;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JunitTestReporter {
    private static final Logger log = LoggerFactory.getLogger(JunitTestReporter.class);

    private final JUnitTestResultConstructor resultConstructor;
    private final JunitMetaDataExtractor metaDataExtractor;
    private final GlobalRunManager runManager;
    private final ConcurrentHashMap<String, ReentrantLock> testLocks;

    public JunitTestReporter() {
        this.metaDataExtractor = new JunitMetaDataExtractor();
        this.resultConstructor = new JUnitTestResultConstructor(metaDataExtractor);
        this.runManager = GlobalRunManager.getInstance();
        this.testLocks = new ConcurrentHashMap<>();
    }

    public JunitTestReporter(JUnitTestResultConstructor resultConstructor,
                             JunitMetaDataExtractor metaDataExtractor,
                             GlobalRunManager runManager) {
        this.runManager = runManager;
        this.resultConstructor = resultConstructor;
        this.metaDataExtractor = metaDataExtractor;
        this.testLocks = new ConcurrentHashMap<>();
    }

    public void reportTestResult(ExtensionContext context, String status, String message) {
        if (!runManager.isActive()) {
            return;
        }

        String lockKey = generateLockKey(context);
        ReentrantLock lock = testLocks.computeIfAbsent(lockKey, k -> new ReentrantLock());

        lock.lock();
        try {
            doReportTestResult(context, status, message);
        } finally {
            lock.unlock();
            if (!lock.hasQueuedThreads()) {
                testLocks.remove(lockKey);
            }
        }
    }

    private void doReportTestResult(ExtensionContext context, String status, String message) {
        TestMetadata metadata = null;
        try {
            
            metadata = metaDataExtractor.extractTestMetadata(context);
            
            TestResult result = resultConstructor.constructTestRunResult(
                    metadata, message, status, context);

            String uniqueId = context.getUniqueId();
            
            
            if (result.getExample() != null) {
                
                // Additional detailed parameter analysis
                if (result.getExample() instanceof java.util.Map) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> paramMap = (java.util.Map<String, Object>) result.getExample();
                } else {
                }
                
            } else {
            }

            log.info("Test RID: {}", result.getRid());
            log.info("Sending test result to API: title='{}', status='{}', example='{}', rid='{}'", 
                    result.getTitle(), status, result.getExample(), result.getRid());
            runManager.reportTest(result);

        } catch (Exception e) {
            String testName = metadata != null ? metadata.getTitle() : "Unknown Test";
            String uniqueId = context.getUniqueId();
            throw new ReportTestResultException("Failed to report test result for: " + testName, e);
        }
    }

    /**
     * Generates lock key that ensures uniqueness even when context.getUniqueId()
     * is the same for retries (which happens when @ParameterizedTest has no name parameter).
     */
    private String generateLockKey(ExtensionContext context) {
        String uniqueId = context.getUniqueId();

        // Add timestamp and thread info to ensure uniqueness for each execution
        // This fixes the issue where retries have the same uniqueId when no name parameter is specified
        long timestamp = System.nanoTime();
        long threadId = Thread.currentThread().getId();

        String enhancedKey = uniqueId + "-t" + threadId + "-n" + timestamp;


        return enhancedKey;
    }

    /**
     * Formats the example parameter for console display, handling special cases.
     */
    private String formatExampleForConsole(Object example) {
        if (example == null) {
            return "NULL";
        }
        
        if (example instanceof String) {
            String str = (String) example;
            if (str.isEmpty()) {
                return "EMPTY_STRING (\"\")";
            }
            
            // Replace invisible characters with visible representations
            String display = str
                .replace(" ", "·")        // space -> middle dot
                .replace("\t", "→")       // tab -> arrow
                .replace("\n", "↵")       // newline -> return symbol
                .replace("\r", "⤶");      // carriage return -> symbol
            
            if (str.isBlank()) {
                return "WHITESPACE (\"" + display + "\") [length=" + str.length() + "]";
            }
            
            return "\"" + display + "\"";
        }
        
        if (example instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> map = (java.util.Map<String, Object>) example;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (java.util.Map.Entry<String, Object> entry : map.entrySet()) {
                if (!first) sb.append(", ");
                sb.append(entry.getKey()).append(": ");
                sb.append(formatExampleForConsole(entry.getValue()));
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }
        
        return example + " (" + example.getClass().getSimpleName() + ")";
    }
}