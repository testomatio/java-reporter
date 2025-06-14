package com.core.util;

import com.client.impl.TestomatClientFactory;
import com.client.interf.ApiInterface;
import com.model.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class ApiDebugUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiDebugUtil.class);
    
    public static void testSingleReport() {
        try {
            ApiInterface client = TestomatClientFactory.getClientFactory().createClient();
            String runId = client.createTestRun("Debug Test Run - " + System.currentTimeMillis());
            LOGGER.info("Created test run: {}", runId);
            
            TestResult testResult = new TestResult(
                "Debug Test",
                null, // no test_id
                "Debug Suite",
                "debug.java",
                "passed",
                null,
                null
            );
            
            client.reportTest(runId, testResult);
            LOGGER.info("Reported single test successfully");
            
            client.finishTestRun(runId, 1.0f);
            LOGGER.info("Finished test run successfully");
            
        } catch (Exception e) {
            LOGGER.error("Debug test failed", e);
        }
    }
    
    public static void testBatchReport() {
        try {
            ApiInterface client = TestomatClientFactory.getClientFactory().createClient();
            String runId = client.createTestRun("Debug Batch Test Run - " + System.currentTimeMillis());
            LOGGER.info("Created test run: {}", runId);
            
            List<TestResult> results = List.of(
                new TestResult("Debug Test 1", null, "Debug Suite", "debug.java", "passed", null, null),
                new TestResult("Debug Test 2", null, "Debug Suite", "debug.java", "failed", "Test failed", "Stack trace...")
            );
            
            client.reportTests(runId, results);
            LOGGER.info("Reported batch tests successfully");
            
            client.finishTestRun(runId, 2.0f);
            LOGGER.info("Finished test run successfully");
            
        } catch (Exception e) {
            LOGGER.error("Debug batch test failed", e);
        }
    }
    
    public static void main(String[] args) {
        System.out.println("Testing single report...");
        testSingleReport();
        
        System.out.println("\nTesting batch report...");
        testBatchReport();
    }
}