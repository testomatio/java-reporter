package io.testomat.junit.extractor;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Advanced parameter extractor that intercepts test method invocations to capture
 * the actual parameters passed to parameterized test methods.
 */
public class TestMethodParameterExtractor implements InvocationInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TestMethodParameterExtractor.class);
    
    // Storage for captured parameters keyed by test unique ID
    private static final Map<String, Object[]> parameterStorage = new ConcurrentHashMap<>();

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, 
                                   ReflectiveInvocationContext<Method> invocationContext,
                                   ExtensionContext extensionContext) throws Throwable {
        
        String uniqueId = extensionContext.getUniqueId();
        Method testMethod = invocationContext.getExecutable();
        
        
        
        // Check if this is a parameterized test
        if (testMethod.isAnnotationPresent(ParameterizedTest.class)) {
            
            // Get the actual arguments passed to the test method
            java.util.List<Object> arguments = invocationContext.getArguments();
            if (arguments != null && !arguments.isEmpty()) {
                Object[] params = arguments.toArray();
                
                
                for (int i = 0; i < params.length; i++) {
                }
                
                // Store the parameters
                parameterStorage.put(uniqueId, params);
                
            } else {
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
        
        Object[] parameters = parameterStorage.get(uniqueId);
        
        if (parameters != null) {
        } else {
        }
        
        return parameters;
    }

    /**
     * Cleans up stored parameters for a test to prevent memory leaks.
     */
    public static void cleanupParameters(ExtensionContext context) {
        if (context != null) {
            String uniqueId = context.getUniqueId();
            parameterStorage.remove(uniqueId);
        }
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
     * Formats a parameter for console display, making invisible characters visible.
     */
    private static String formatParameterForConsole(Object param) {
        if (param == null) {
            return "NULL";
        }
        
        if (param instanceof String) {
            String str = (String) param;
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
        
        return param.toString() + " (" + param.getClass().getSimpleName() + ")";
    }
}