package com.testomatio.reporter.client;


import com.testomatio.reporter.model.TestCaseResult;
import java.io.IOException;
import java.util.List;

public interface ApiInterface {
    String createRun(String title) throws IOException;
    
    void reportTest(String uid, TestCaseResult result) throws IOException;
    
    void reportTests(String uid, List<TestCaseResult> results) throws IOException;
    
    void finishTestRun(String uid, float duration) throws IOException;
}