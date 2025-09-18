package io.testomat.core.client;

import io.testomat.core.model.TestResult;
import java.io.IOException;
import java.util.List;

/**
 * Primary interface for Testomat.io API operations.
 * Defines the contract for test run lifecycle management and result reporting.
 * Thread-safe implementations should support concurrent test execution scenarios.
 */
public interface ApiInterface {

    /**
     * Creates a new test run in Testomat.io.
     *
     * @param title descriptive title for the test run
     * @return unique test run identifier (UID) for subsequent operations
     * @throws IOException if network request fails or API returns error
     */
    String createRun(String title) throws IOException;

    /**
     * Reports a single test result to the specified test run.
     *
     * @param uid test run identifier from {@link #createRun(String)}
     * @param result test execution result with status and metadata
     * @throws IOException if network request fails or API returns error
     */
    void reportTest(String uid, TestResult result) throws IOException;

    /**
     * Reports multiple test results in a single batch operation.
     * More efficient than individual calls for large test suites.
     *
     * @param uid test run identifier from {@link #createRun(String)}
     * @param results collection of test execution results
     * @throws IOException if network request fails or API returns error
     */
    void reportTests(String uid, List<TestResult> results) throws IOException;

    void sendTestWithArtifacts(String uid);

    /**
     * Finalizes the test run and marks it as completed.
     *
     * @param uid test run identifier from {@link #createRun(String)}
     * @param duration total test run duration in seconds
     * @throws IOException if network request fails or API returns error
     */
    void finishTestRun(String uid, float duration) throws IOException;

    void uploadLinksToTestomatio(String uid);
}
