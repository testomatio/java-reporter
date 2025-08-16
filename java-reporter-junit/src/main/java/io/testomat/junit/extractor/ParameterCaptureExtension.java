package io.testomat.junit.extractor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit 5 extension that captures test parameters for parameterized tests.
 * This extension uses a combination of beforeEach callback and reflection
 * to capture parameters since JUnit 5's parameterized test parameters
 * are resolved by built-in resolvers that we can't easily intercept.
 */
public class ParameterCaptureExtension extends BaseParameterExtractor implements BeforeEachCallback, AfterEachCallback {

    private static final Logger log = LoggerFactory.getLogger(ParameterCaptureExtension.class);
    
    // Thread-safe storage for test parameters keyed by test unique ID
    private static final Map<String, Object[]> parameterStorage = new ConcurrentHashMap<>();
    
    // Thread-local storage for current test parameters during execution
    private static final ThreadLocal<Object[]> currentParameters = new ThreadLocal<>();

    @Override
    public void beforeEach(ExtensionContext context) {
        // Clear any previous parameters for this thread
        currentParameters.remove();
        
        // Try to capture parameters from the context if available
        try {
            Object[] parameters = extractParametersFromContext(context);
            if (parameters != null && parameters.length > 0) {
                String uniqueId = context.getUniqueId();
                parameterStorage.put(uniqueId, parameters);
                currentParameters.set(parameters);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        // Clean up thread-local storage
        currentParameters.remove();
        
        // Keep parameters in storage for reporting - they will be cleaned up by the test reporter
    }

    /**
     * Retrieves captured parameters for a given test context.
     * 
     * @param context the extension context
     * @return captured parameters or null if none available
     */
    public static Object[] getCapturedParameters(ExtensionContext context) {
        if (context == null) {
            return null;
        }
        
        String uniqueId = context.getUniqueId();
        Object[] parameters = parameterStorage.get(uniqueId);
        
        if (parameters != null) {
            return parameters;
        }
        
        // Try thread-local as fallback
        parameters = currentParameters.get();
        if (parameters != null) {
            return parameters;
        }
        
        return null;
    }

    /**
     * Manually stores parameters for a test context.
     * This can be called by other extensions that have access to test parameters.
     * 
     * @param context the extension context
     * @param parameters the test parameters
     */
    public static void storeParameters(ExtensionContext context, Object[] parameters) {
        if (context != null && parameters != null && parameters.length > 0) {
            String uniqueId = context.getUniqueId();
            parameterStorage.put(uniqueId, parameters);
            currentParameters.set(parameters);
        }
    }

    /**
     * Cleans up stored parameters for a test to prevent memory leaks.
     * 
     * @param context the extension context
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
     * Attempts to extract parameters from the extension context using various strategies.
     */
    private Object[] extractParametersFromContext(ExtensionContext context) {
        try {
            // Strategy 1: Try to access JUnit's internal parameter storage through reflection
            Object[] parameters = tryReflectiveParameterExtraction(context);
            if (parameters != null && parameters.length > 0) {
                return parameters;
            }

            // Strategy 2: Parse from display name (enhanced version)
            parameters = parseParametersFromDisplayName(context);
            if (parameters != null && parameters.length > 0) {
                return parameters;
            }

        } catch (Exception e) {
        }
        
        return null;
    }

    /**
     * Enhanced reflective parameter extraction that looks for JUnit 5's internal parameter storage.
     */
    private Object[] tryReflectiveParameterExtraction(ExtensionContext context) {
        try {
            Class<?> contextClass = context.getClass();
            
            // Look for JUnit 5's ParameterizedTestExtensionContext
            if (contextClass.getName().contains("ParameterizedTest")) {
                // Try to find the arguments field
                java.lang.reflect.Field[] fields = contextClass.getDeclaredFields();
                for (java.lang.reflect.Field field : fields) {
                    String fieldName = field.getName().toLowerCase();
                    if (fieldName.contains("argument") || fieldName.contains("parameter")) {
                        field.setAccessible(true);
                        Object value = field.get(context);
                        
                        if (value instanceof Object[]) {
                            return (Object[]) value;
                        }
                    }
                }
                
                // Try superclass fields
                Class<?> superClass = contextClass.getSuperclass();
                if (superClass != null) {
                    java.lang.reflect.Field[] superFields = superClass.getDeclaredFields();
                    for (java.lang.reflect.Field field : superFields) {
                        String fieldName = field.getName().toLowerCase();
                        if (fieldName.contains("argument") || fieldName.contains("parameter")) {
                            field.setAccessible(true);
                            Object value = field.get(context);
                            
                            if (value instanceof Object[]) {
                                return (Object[]) value;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        
        return null;
    }

    /**
     * Enhanced display name parsing that handles various JUnit 5 parameter formats.
     */
    private Object[] parseParametersFromDisplayName(ExtensionContext context) {
        return parseParametersFromDisplayName(context.getDisplayName());
    }


}