package io.testomat.testng.extractor;

import org.testng.ITestResult;
import java.util.HashMap;
import java.util.Map;

/**
 * Extracts parameters from TestNG parameterized tests.
 * Handles different parameter configurations and creates structured example data.
 */
public class TestNgParameterExtractor {
    
    /**
     * Extracts example data from TestNG test result parameters.
     * 
     * @param testResult TestNG test result containing parameters
     * @return structured example object or null if no parameters
     */
    public Object extractExample(ITestResult testResult) {
        Object[] parameters = testResult.getParameters();
        if (parameters == null || parameters.length == 0) {
            return null;
        }
        
        // Для одного параметра повертаємо його безпосередньо
        if (parameters.length == 1) {
            return parameters[0];
        }
        
        // Для множинних параметрів створюємо Map з індексами
        Map<String, Object> exampleMap = new HashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            exampleMap.put("param" + i, parameters[i]);
        }
        return exampleMap;
    }
    
    /**
     * Generates unique RID (Run ID) for parameterized test based on parameters.
     * 
     * @param testResult TestNG test result containing parameters
     * @return unique RID string or null if no parameters
     */
    public String generateRid(ITestResult testResult) {
        Object[] parameters = testResult.getParameters();
        if (parameters == null || parameters.length == 0) {
            return null;
        }
        
        StringBuilder ridBuilder = new StringBuilder();
        ridBuilder.append(testResult.getMethod().getMethodName());
        
        // Додаємо хеші параметрів для унікальності
        for (int i = 0; i < parameters.length; i++) {
            Object param = parameters[i];
            String paramString = param != null ? param.toString() : "null";
            int hash = paramString.hashCode();
            ridBuilder.append("-p").append(i).append("h").append(hash);
        }
        
        return ridBuilder.toString();
    }
    
    /**
     * Checks if test result has parameters (is parameterized).
     * 
     * @param testResult TestNG test result
     * @return true if test has parameters, false otherwise
     */
    public boolean isParameterized(ITestResult testResult) {
        Object[] parameters = testResult.getParameters();
        return parameters != null && parameters.length > 0;
    }
}