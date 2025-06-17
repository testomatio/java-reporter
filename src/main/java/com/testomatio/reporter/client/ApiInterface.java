package com.testomatio.reporter.client;


import com.testomatio.reporter.model.TestRunResult;
import java.io.IOException;
import java.util.List;

public interface ApiInterface {
    String createRun(String title) throws IOException;
    
    void reportTest(String uid, TestRunResult result) throws IOException;
    
    // Новий batch method
    void reportTests(String uid, List<TestRunResult> results) throws IOException;
    
    void finishTestRun(String uid, float duration) throws IOException;
}