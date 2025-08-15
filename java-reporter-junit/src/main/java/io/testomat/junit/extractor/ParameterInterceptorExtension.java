package io.testomat.junit.extractor;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.params.ParameterizedTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit 5 extension that intercepts and captures test parameters for parameterized tests.
 * This extension works by implementing ParameterResolver with high priority and storing
 * parameters as they are resolved by JUnit's parameter resolution chain.
 */
public class ParameterInterceptorExtension implements ParameterResolver, BeforeEachCallback, AfterEachCallback {

    private static final Logger log = LoggerFactory.getLogger(ParameterInterceptorExtension.class);
    
    // Thread-safe storage for test parameters keyed by test unique ID
    private static final Map<String, Object[]> parameterStorage = new ConcurrentHashMap<>();
    
    // Thread-local storage for current test parameters during execution
    private static final ThreadLocal<Object[]> currentTestParameters = new ThreadLocal<>();
    
    // Thread-local to track parameter collection for current test
    private static final ThreadLocal<java.util.List<Object>> parameterCollector = new ThreadLocal<>();

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        // Only support parameters for parameterized tests
        if (!isParameterizedTest(extensionContext)) {
            return false;
        }
        
        // We support all parameters to intercept them, but we don't actually resolve them
        // We just want to observe what parameters are being passed
        return false; // Let JUnit's built-in resolvers handle the actual resolution
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        // This won't be called since supportsParameter returns false
        throw new UnsupportedOperationException("This extension observes parameters but doesn't resolve them");
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        
        // Clear any previous data
        currentTestParameters.remove();
        parameterCollector.remove();
        
        if (!isParameterizedTest(context)) {
            return;
        }
        
        // Initialize parameter collector
        parameterCollector.set(new java.util.ArrayList<>());
        
        try {
            // Try to extract parameters using advanced reflection on the context
            Object[] parameters = extractParametersUsingAdvancedReflection(context);
            if (parameters != null && parameters.length > 0) {
                String uniqueId = context.getUniqueId();
                parameterStorage.put(uniqueId, parameters);
                currentTestParameters.set(parameters);
                
            } else {
            }
        } catch (Exception e) {
        }
        
    }

    @Override
    public void afterEach(ExtensionContext context) {
        // Clean up thread-local storage
        currentTestParameters.remove();
        parameterCollector.remove();
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
            return parameters;
        }
        
        // Try thread-local as fallback
        return currentTestParameters.get();
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
     * Checks if the test is parameterized.
     */
    private boolean isParameterizedTest(ExtensionContext context) {
        try {
            Method testMethod = context.getTestMethod().orElse(null);
            return testMethod != null && testMethod.isAnnotationPresent(ParameterizedTest.class);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Advanced reflection-based parameter extraction that looks deeper into JUnit's internals.
     */
    private Object[] extractParametersUsingAdvancedReflection(ExtensionContext context) {
        try {
            
            // Strategy 1: Look for JUnit 5's ParameterizedTestInvocationContext
            Object[] params = tryExtractFromParameterizedTestContext(context);
            if (params != null) return params;
            
            // Strategy 2: Look for TestTemplateInvocationContext 
            params = tryExtractFromTestTemplateContext(context);
            if (params != null) return params;
            
            // Strategy 3: Look in parent contexts
            params = tryExtractFromParentContext(context);
            if (params != null) return params;
            
            // Strategy 4: Look for argument providers or test descriptors
            params = tryExtractFromTestDescriptor(context);
            if (params != null) return params;
            
            return null;
            
        } catch (Exception e) {
            return null;
        }
    }

    private Object[] tryExtractFromParameterizedTestContext(ExtensionContext context) {
        try {
            Class<?> contextClass = context.getClass();
            
            // Look for JUnit's specific parameterized test context classes
            if (contextClass.getName().contains("ParameterizedTest")) {
                return extractParametersFromSpecificContext(context, "ParameterizedTest context");
            }
            
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private Object[] tryExtractFromTestTemplateContext(ExtensionContext context) {
        try {
            Class<?> contextClass = context.getClass();
            
            // Look for TestTemplate context classes
            if (contextClass.getName().contains("TestTemplate")) {
                return extractParametersFromSpecificContext(context, "TestTemplate context");
            }
            
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private Object[] tryExtractFromParentContext(ExtensionContext context) {
        try {
            if (context.getParent().isPresent()) {
                ExtensionContext parent = context.getParent().get();
                return extractParametersFromSpecificContext(parent, "parent context");
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private Object[] tryExtractFromTestDescriptor(ExtensionContext context) {
        try {
            // Try to access test descriptor through reflection
            java.lang.reflect.Method getTestDescriptor = context.getClass().getMethod("getTestDescriptor");
            Object testDescriptor = getTestDescriptor.invoke(context);
            
            if (testDescriptor != null) {
                return extractParametersFromTestDescriptor(testDescriptor);
            }
            
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private Object[] extractParametersFromSpecificContext(ExtensionContext context, String contextType) {
        try {
            Class<?> contextClass = context.getClass();
            java.lang.reflect.Field[] allFields = getAllFieldsFromHierarchy(contextClass);
            
            
            for (java.lang.reflect.Field field : allFields) {
                String fieldName = field.getName().toLowerCase();
                
                // Look for fields that might contain arguments/parameters
                if (isParameterField(fieldName)) {
                    field.setAccessible(true);
                    Object value = field.get(context);
                    
                    if (value != null) {
                        
                        Object[] params = extractParametersFromValue(value, field.getName());
                        if (params != null && params.length > 0) {
                            return params;
                        }
                    }
                }
            }
            
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isParameterField(String fieldName) {
        return fieldName.contains("argument") || 
               fieldName.contains("parameter") || 
               fieldName.contains("args") ||
               fieldName.equals("arguments") ||
               fieldName.contains("value") ||
               fieldName.contains("invocation") ||
               fieldName.contains("context");
    }

    private Object[] extractParametersFromValue(Object value, String fieldName) {
        try {
            // Direct Object[] check
            if (value instanceof Object[]) {
                Object[] array = (Object[]) value;
                if (array.length > 0) {
                    return array;
                }
            }
            
            // Check if it's a List
            if (value instanceof java.util.List) {
                java.util.List<?> list = (java.util.List<?>) value;
                if (!list.isEmpty()) {
                    Object[] array = list.toArray();
                    return array;
                }
            }
            
            // Check if it has an "arguments" or "parameters" method/field
            Object[] params = tryExtractFromObject(value, fieldName);
            if (params != null) return params;
            
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private Object[] tryExtractFromObject(Object obj, String parentFieldName) {
        try {
            Class<?> objClass = obj.getClass();
            
            // Try methods that might return arguments
            java.lang.reflect.Method[] methods = objClass.getMethods();
            for (java.lang.reflect.Method method : methods) {
                String methodName = method.getName().toLowerCase();
                if ((methodName.contains("argument") || methodName.contains("parameter")) && 
                    method.getParameterCount() == 0) {
                    
                    Object result = method.invoke(obj);
                    if (result instanceof Object[]) {
                        Object[] params = (Object[]) result;
                        return params;
                    }
                }
            }
            
            // Try fields
            java.lang.reflect.Field[] fields = objClass.getDeclaredFields();
            for (java.lang.reflect.Field field : fields) {
                String fieldName = field.getName().toLowerCase();
                if (fieldName.contains("argument") || fieldName.contains("parameter")) {
                    field.setAccessible(true);
                    Object value = field.get(obj);
                    if (value instanceof Object[]) {
                        Object[] params = (Object[]) value;
                        return params;
                    }
                }
            }
            
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private Object[] extractParametersFromTestDescriptor(Object testDescriptor) {
        // Similar extraction logic for test descriptor
        try {
            return tryExtractFromObject(testDescriptor, "testDescriptor");
        } catch (Exception e) {
            return null;
        }
    }

    private java.lang.reflect.Field[] getAllFieldsFromHierarchy(Class<?> clazz) {
        java.util.List<java.lang.reflect.Field> allFields = new java.util.ArrayList<>();
        Class<?> currentClass = clazz;
        
        while (currentClass != null && currentClass != Object.class) {
            java.lang.reflect.Field[] fields = currentClass.getDeclaredFields();
            allFields.addAll(java.util.Arrays.asList(fields));
            currentClass = currentClass.getSuperclass();
        }
        
        return allFields.toArray(new java.lang.reflect.Field[0]);
    }
}