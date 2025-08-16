package io.testomat.junit.extractor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Improved parameter extractor that uses better strategies to get test parameters
 * from JUnit 5 parameterized tests.
 */
public class ImprovedParameterExtractor extends BaseParameterExtractor {

    private static final Logger log = LoggerFactory.getLogger(ImprovedParameterExtractor.class);


    /**
     * Extracts parameters from a parameterized test context.
     */
    public Object extractTestParameters(ExtensionContext context) {
        String uniqueId = context.getUniqueId();

        
        if (!isParameterizedTest(context)) {
            log.info("Test is not parameterized: {}", uniqueId);
            return null;
        }
        
        log.info("Parameterized test detected: {}", uniqueId);


        try {
            Method testMethod = context.getTestMethod().orElse(null);
            if (testMethod == null) {
                return null;
            }


            // Strategy 1: Use TestMethodParameterExtractor (method invocation interception)
            Object[] parameters = TestMethodParameterExtractor.getCapturedParameters(context);
            log.info("Strategy 1 - TestMethodParameterExtractor: {} parameters found", 
                    parameters != null ? parameters.length : 0);
            if (parameters != null && parameters.length > 0) {
                Object result = formatParameters(parameters, testMethod);
                log.info("Strategy 1 SUCCESS: {}", result);
                return result;
            }

            // Strategy 2: Use ParameterCaptureExtension (context-based reflection)
            parameters = ParameterCaptureExtension.getCapturedParameters(context);
            log.info("Strategy 2 - ParameterCaptureExtension: {} parameters found", 
                    parameters != null ? parameters.length : 0);
            if (parameters != null && parameters.length > 0) {
                Object result = formatParameters(parameters, testMethod);
                log.info("Strategy 2 SUCCESS: {}", result);
                return result;
            }

            // Strategy 3: Legacy reflection approach
            parameters = tryExtractParametersFromJUnitContext(context);
            log.info("Strategy 3 - Legacy reflection: {} parameters found", 
                    parameters != null ? parameters.length : 0);
            if (parameters != null && parameters.length > 0) {
                Object result = formatParameters(parameters, testMethod);
                log.info("Strategy 3 SUCCESS: {}", result);
                return result;
            }

            // Strategy 4: Enhanced display name parsing (fallback)
            parameters = parseParametersFromDisplayName(context.getDisplayName());
            log.info("Strategy 4 - Display name parsing: {} parameters found from '{}'", 
                    parameters != null ? parameters.length : 0, context.getDisplayName());
            if (parameters != null && parameters.length > 0) {
                Object result = formatParameters(parameters, testMethod);
                log.info("Strategy 4 SUCCESS: {}", result);
                return result;
            }

            log.info("All parameter extraction strategies failed for: {}", uniqueId);
            return null;

        } catch (Exception e) {
            return null;
        }
    }


    /**
     * Tries to extract parameters from JUnit's internal context using reflection.
     * This approach looks for the actual parameter values that JUnit stores internally.
     */
    private Object[] tryExtractParametersFromJUnitContext(ExtensionContext context) {
        try {
            Class<?> contextClass = context.getClass();
            // Look for fields that might contain parameter information
            java.lang.reflect.Field[] allFields = getAllFields(contextClass);
            
            for (java.lang.reflect.Field field : allFields) {
                String fieldName = field.getName().toLowerCase();
                
                // Look for fields that likely contain the test arguments
                if (fieldName.contains("argument") || fieldName.contains("parameter") || 
                    fieldName.equals("args") || fieldName.equals("arguments")) {
                    
                    field.setAccessible(true);
                    Object value = field.get(context);
                    
                    
                    if (value instanceof Object[]) {
                        Object[] params = (Object[]) value;
                        if (params.length > 0) {
                            return params;
                        } else {
                        }
                    } else if (value != null) {
                    }
                }
            }

            // Try to find parameters in the test descriptor or invocation context
            return tryExtractFromTestDescriptor(context);

        } catch (Exception e) {
            return null;
        }
    }


    /**
     * Tries to extract parameters from test descriptor or invocation context.
     */
    private Object[] tryExtractFromTestDescriptor(ExtensionContext context) {
        try {
            // Try to access JUnit's TestDescriptor which might have parameter info
            java.lang.reflect.Method getTestDescriptor = context.getClass().getMethod("getTestDescriptor");
            Object testDescriptor = getTestDescriptor.invoke(context);
            if (testDescriptor != null) {
                // Look for parameter-related methods or fields in test descriptor
                return extractParametersFromTestDescriptor(testDescriptor);
            }
        } catch (Exception e) {
        }
        
        return null;
    }

    /**
     * Extracts parameters from a JUnit TestDescriptor object.
     */
    private Object[] extractParametersFromTestDescriptor(Object testDescriptor) {
            Class<?> descriptorClass = testDescriptor.getClass();
            
            // Look for methods that might return arguments
            java.lang.reflect.Method[] methods = descriptorClass.getMethods();
            for (java.lang.reflect.Method method : methods) {
                String methodName = method.getName().toLowerCase();
                if ((methodName.contains("argument") || methodName.contains("parameter")) && 
                    method.getParameterCount() == 0) {

                    Object result;
                    try {
                        result = method.invoke(testDescriptor);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                    if (result instanceof Object[]) {
                        return (Object[]) result;
                    }
                }
            }

        
        return null;
    }



    /**
     * Formats parameters for reporting based on count and method signature.
     */
    private Object formatParameters(Object[] parameters, Method testMethod) {
        // Always return parameters as map with parameter names for consistency
        Map<String, Object> paramMap = new HashMap<>();
        Parameter[] methodParams = testMethod.getParameters();
        
        // If no parameters captured but method expects them, create null entries
        if (parameters == null || parameters.length == 0) {
            if (methodParams.length > 0) {
                for (int i = 0; i < methodParams.length; i++) {
                    String paramName;
                    if (methodParams[i].isNamePresent()) {
                        paramName = methodParams[i].getName();
                        // Check if it's a real name or synthetic
                        if (paramName.matches("arg\\d+")) {
                            paramName = "param" + i;
                        }
                    } else {
                        paramName = "param" + i;
                    }
                    paramMap.put(paramName, null);
                }
            } else {
                return null; // Not a parameterized test
            }
        } else {
            // Use captured parameters
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
        }

        return paramMap;
    }
}