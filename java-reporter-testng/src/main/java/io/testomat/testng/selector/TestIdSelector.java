package io.testomat.testng.selector;

import io.testomat.core.annotation.TestId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IMethodSelector;
import org.testng.IMethodSelectorContext;
import org.testng.ITestNGMethod;

/**
 * TestNG method selector for filtering tests by @TestId annotation.
 * Only includes test methods that have @TestId matching the provided filter IDs.
 */
public class TestIdSelector implements IMethodSelector {
    private static final Logger log = LoggerFactory.getLogger(TestIdSelector.class);
    private static int callCount = 0;
    
    public TestIdSelector() {
        System.out.println("*** TestIdSelector CONSTRUCTOR called ***");
        log.debug("TestIdSelector instance created");
    }
    
    @Override
    public boolean includeMethod(IMethodSelectorContext context, 
                               ITestNGMethod method, boolean isTestMethod) {
        try {
            callCount++;
            String providedIds = System.getProperty("ids");
            String methodName = method.getMethodName();
            String className = method.getTestClass().getName();
            
            System.out.println("=== METHOD SELECTOR DEBUG #" + callCount + " ===");
            System.out.println("Method: " + className + "." + methodName);
            System.out.println("Is test method: " + isTestMethod);
            System.out.println("Provided IDs: '" + providedIds + "'");
            
            if (providedIds == null || providedIds.trim().isEmpty()) {
                System.out.println("NO FILTER: All methods included");
                return true;
            }
            
            TestId annotation = method.getConstructorOrMethod()
                    .getMethod().getAnnotation(TestId.class);
            
            System.out.println("@TestId present: " + (annotation != null));
            
            if (annotation == null) {
                System.out.println("EXCLUDED: No @TestId annotation");
                return false;
            }
            
            String testId = annotation.value();
            System.out.println("Found @TestId: '" + testId + "'");
            
            boolean included = isTestIdIncluded(testId, providedIds);
            System.out.println("Include method: " + included);
            System.out.println("=== END METHOD SELECTOR DEBUG ===");
            
            return included;
            
        } catch (Exception e) {
            log.error("Error in TestIdSelector for method: {}", method.getMethodName(), e);
            System.out.println("ERROR in TestIdSelector: " + e.getMessage());
            return true; // Include by default on error
        }
    }
    
    private boolean isTestIdIncluded(String testId, String providedIds) {
        String[] allowedIds = providedIds.split(",");
        for (String allowedId : allowedIds) {
            String trimmed = allowedId.trim();
            System.out.println("  Comparing: '" + testId + "' == '" + trimmed + "'");
            if (!trimmed.isEmpty() && testId.equals(trimmed)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void setTestMethods(List<ITestNGMethod> testMethods) {
        System.out.println("TestIdSelector.setTestMethods called with " 
                          + (testMethods != null ? testMethods.size() : "null") 
                          + " methods");
        log.debug("TestIdSelector.setTestMethods called with {} methods", 
                 testMethods != null ? testMethods.size() : "null");
    }
}
