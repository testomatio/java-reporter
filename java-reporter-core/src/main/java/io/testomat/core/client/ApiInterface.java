package io.testomat.core.client;

import io.testomat.core.model.TestResult;
import java.io.IOException;
import java.util.List;

/**
 * API interface for interacting with Testomat.io platform.
 * Provides methods for test run lifecycle management and result reporting.
 */
public interface ApiInterface {

    /**
     * Creates new test run.
     *
     * @param title test run title
     * @return unique test run identifier
     * @throws IOException if API request fails
     */
    String createRun(String title) throws IOException;

    /**
     * Reports single test result.
     *
     * @param uid test run identifier
     * @param result test result to report
     * @throws IOException if API request fails
     */
    void reportTest(String uid, TestResult result) throws IOException;

    /**
     * Reports multiple test results in batch.
     *
     * @param uid test run identifier
     * @param results test results to report
     * @throws IOException if API request fails
     */
    void reportTests(String uid, List<TestResult> results) throws IOException;

    /**
     * Marks test run as finished.
     *
     * @param uid test run identifier
     * @param duration test run duration in seconds
     * @throws IOException if API request fails
     */
    void finishTestRun(String uid, float duration) throws IOException;
}
