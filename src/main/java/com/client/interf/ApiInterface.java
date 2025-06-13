package com.client.interf;


import com.model.TestResult;
import java.io.IOException;
import java.util.List;

public interface ApiInterface {
    String createTestRun(String title) throws IOException;
    
    void reportTest(String uid, TestResult result) throws IOException;
    
    // Новий batch method
    void reportTests(String uid, List<TestResult> results) throws IOException;
    
    void finishTestRun(String uid, float duration) throws IOException;
}