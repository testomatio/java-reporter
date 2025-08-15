package io.testomat.junit.extractor;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple and reliable parameter extractor for JUnit 5 parameterized tests.
 * Uses method invocation interception to capture actual parameter values.
 * This is the most reliable approach for JUnit 5 parameter extraction.
 */
public class SimpleParameterExtractor implements InvocationInterceptor {

    private static final Logger log = LoggerFactory.getLogger(SimpleParameterExtractor.class);
    
    // Thread-safe storage for captured parameters keyed by test unique ID
    private static final Map<String, Object[]> parameterStorage = new ConcurrentHashMap<>();

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, 
                                   ReflectiveInvocationContext<Method> invocationContext,
                                   ExtensionContext extensionContext) throws Throwable {
        
        Method testMethod = invocationContext.getExecutable();
        String uniqueId = extensionContext.getUniqueId();
        
        
        // Check if this is a parameterized test
        if (testMethod.isAnnotationPresent(ParameterizedTest.class)) {
            // Get the actual arguments passed to the test method
            java.util.List<Object> arguments = invocationContext.getArguments();
            if (arguments != null && !arguments.isEmpty()) {
                Object[] params = arguments.toArray();
                
                // Store the parameters for later retrieval
                parameterStorage.put(uniqueId, params);
                
            }
        } else {
        }
        
        // Continue with the actual test execution
        invocation.proceed();
    }

    /**
     * Retrieves captured parameters for a given test context.
     */
    public static Object[] getCapturedParameters(ExtensionContext context) {
        if (context == null) {
            return null;
        }
        
        String uniqueId = context.getUniqueId();
        return parameterStorage.get(uniqueId);
    }

    /**
     * Extracts parameters and formats them for reporting.
     * Returns single parameter directly or Map for multiple parameters.
     */
    public static Object extractFormattedParameters(ExtensionContext context) {
        Object[] parameters = getCapturedParameters(context);
        if (parameters == null || parameters.length == 0) {
            return null;
        }

        // Single parameter - return directly
        if (parameters.length == 1) {
            return parameters[0];
        }

        // Multiple parameters - return as map with parameter names
        Method testMethod = context.getTestMethod().orElse(null);
        if (testMethod == null) {
            return parameters;
        }

        Map<String, Object> paramMap = new HashMap<>();
        Parameter[] methodParams = testMethod.getParameters();
        
        for (int i = 0; i < parameters.length; i++) {
            String paramName;
            if (i < methodParams.length && methodParams[i].isNamePresent()) {
                paramName = methodParams[i].getName();
                // Check if it's a real name or synthetic
                if (paramName.matches("arg\\d+")) {
                    paramName = "param" + i;
                }
            } else {
                paramName = "param" + i;
            }
            paramMap.put(paramName, parameters[i]);
        }

        return paramMap;
    }

    /**
     * Checks if parameters are available for the given context.
     */
    public static boolean hasParameters(ExtensionContext context) {
        if (context == null) {
            return false;
        }
        
        String uniqueId = context.getUniqueId();
        return parameterStorage.containsKey(uniqueId);
    }

    /**
     * Cleans up stored parameters for a test to prevent memory leaks.
     */
    public static void cleanupParameters(ExtensionContext context) {
        if (context != null) {
            String uniqueId = context.getUniqueId();
            Object[] removed = parameterStorage.remove(uniqueId);
            if (removed != null) {
            }
        }
    }

    /**
     * Checks if the test method is a parameterized test.
     */
    public static boolean isParameterizedTest(ExtensionContext context) {
        try {
            Method testMethod = context.getTestMethod().orElse(null);
            return testMethod != null && testMethod.isAnnotationPresent(ParameterizedTest.class);
        } catch (Exception e) {
            return false;
        }
    }
}