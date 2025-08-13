package io.testomat.junit.extractor;

import io.testomat.core.annotation.TestId;
import io.testomat.core.annotation.Title;
import io.testomat.core.exception.NoMethodInContextException;
import io.testomat.core.model.TestMetadata;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JunitMetaDataExtractor {

    private static final Logger log = LoggerFactory.getLogger(JunitMetaDataExtractor.class);
    private static final AtomicLong ridCounter = new AtomicLong(0);

    public TestMetadata extractTestMetadata(ExtensionContext context) {
        Method testMethod = getTestMethod(context);
        String title = getTestTitle(context);
        Class<?> testClass = context.getRequiredTestClass();
        String suiteTitle = testClass.getSimpleName();
        String file = getFilePath(testClass);
        String testId = getTestId(testMethod);

        return new TestMetadata(title, testId, suiteTitle, file);
    }

    /**
     * Extracts test parameters for parameterized tests.
     * Returns single parameter directly or Map for multiple parameters.
     *
     * @param context JUnit extension context
     * @return parameter object(s) or null if not parameterized
     */
    public Object extractTestParameters(ExtensionContext context) {
        if (!isParameterizedTest(context)) {
            return null;
        }

        Method testMethod = getTestMethod(context);
        Parameter[] parameters = testMethod.getParameters();
        Object[] parameterValues = getParameterValues(context);

        if (parameterValues == null || parameterValues.length == 0) {
            log.debug("No parameter values found for parameterized test: {}",
                    testMethod.getName());
            return null;
        }

        // Single parameter - return directly (as per testomat.io documentation)
        if (parameterValues.length == 1) {
            return parameterValues[0];
        }

        // Multiple parameters - return as Map with parameter names
        Map<String, Object> parameterMap = new HashMap<>();
        String[] parameterNames = extractParameterNames(testMethod, parameterValues.length);

        for (int i = 0; i < parameterValues.length; i++) {
            String paramName = i < parameterNames.length ? parameterNames[i] : "param" + i;
            parameterMap.put(paramName, parameterValues[i]);
        }

        return parameterMap;
    }

    /**
     * Extracts parameter names using multiple strategies.
     */
    private String[] extractParameterNames(Method method, int paramCount) {
        String[] names = new String[paramCount];
        Parameter[] methodParameters = method.getParameters();
        boolean hasRealNames = false;

        for (int i = 0; i < Math.min(paramCount, methodParameters.length); i++) {
            Parameter param = methodParameters[i];
            if (param.isNamePresent()) {
                String name = param.getName();
                // Check if it's a real name (not synthetic like arg0, arg1)
                if (!name.matches("arg\\d+")) {
                    names[i] = name;
                    hasRealNames = true;
                } else {
                    names[i] = "param" + i;
                }
            } else {
                names[i] = "param" + i;
            }
        }

        if (hasRealNames) {
            log.debug("Using parameter names from method signature: {}",
                    String.join(", ", names));
        } else {
            log.debug("Using fallback parameter names (compile with -parameters flag for real names): {}",
                    String.join(", ", names));
        }

        // Fill remaining positions with default names
        for (int i = methodParameters.length; i < paramCount; i++) {
            names[i] = "param" + i;
        }

        return names;
    }

    public boolean isParameterizedTest(ExtensionContext context) {
        try {
            Method testMethod = getTestMethod(context);
            return testMethod.isAnnotationPresent(ParameterizedTest.class);
        } catch (Exception e) {
            log.debug("Failed to check if test is parameterized: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Enhanced parameter extraction using JUnit's extension context.
     * More reliable than parsing display names.
     */
    private Object[] getParameterValues(ExtensionContext context) {
        try {
            // Strategy 1: Try to get from context store (if available)
            Object[] storedParams = tryGetParametersFromStore(context);
            if (storedParams != null && storedParams.length > 0) {
                return storedParams;
            }

            // Strategy 2: Parse from display name (fallback)
            return parseParametersFromDisplayName(context);

        } catch (Exception e) {
            log.debug("Failed to extract parameter values for test: {}",
                    context.getDisplayName(), e);
            return new Object[0];
        }
    }

    /**
     * Attempts to retrieve parameters from JUnit's context store.
     */
    private Object[] tryGetParametersFromStore(ExtensionContext context) {
        try {
            // Try to get parameters from different store scopes
            ExtensionContext.Store store = null;
            Object[] parameters = null;
            
            // Strategy 1: Try current context store
            store = context.getStore(ExtensionContext.Namespace.GLOBAL);
            parameters = getParametersFromStore(store, context);
            if (parameters != null) {
                return parameters;
            }
            
            // Strategy 2: Try parent context store (for parameterized tests)
            if (context.getParent().isPresent()) {
                store = context.getParent().get().getStore(ExtensionContext.Namespace.GLOBAL);
                parameters = getParametersFromStore(store, context);
                if (parameters != null) {
                    return parameters;
                }
            }
            
            // Strategy 3: Try root context store
            store = context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL);
            parameters = getParametersFromStore(store, context);
            if (parameters != null) {
                return parameters;
            }
            
            // Strategy 4: Try to access JUnit internal parameter storage using reflection
            return tryReflectiveParameterAccess(context);
            
        } catch (Exception e) {
            log.trace("Could not access JUnit context store", e);
            return null;
        }
    }
    
    private Object[] getParametersFromStore(ExtensionContext.Store store, ExtensionContext context) {
        if (store == null) return null;
        
        try {
            // Try common parameter storage keys
            String testKey = context.getUniqueId() + ".parameters";
            Object stored = store.get(testKey);
            if (stored instanceof Object[]) {
                return (Object[]) stored;
            }
            
            // Try alternative key patterns
            String methodKey = context.getDisplayName() + ".args";
            stored = store.get(methodKey);
            if (stored instanceof Object[]) {
                return (Object[]) stored;
            }
            
            // Try generic parameter key
            stored = store.get("test.parameters");
            if (stored instanceof Object[]) {
                return (Object[]) stored;
            }
            
        } catch (Exception e) {
            log.trace("Error accessing store with context {}: {}", context.getUniqueId(), e.getMessage());
        }
        
        return null;
    }
    
    private Object[] tryReflectiveParameterAccess(ExtensionContext context) {
        try {
            // Attempt to access JUnit's internal ParameterizedTestExtensionContext
            // This is a fallback for when parameters are not stored in the public store
            Class<?> contextClass = context.getClass();
            
            // Look for fields that might contain parameter information
            java.lang.reflect.Field[] fields = contextClass.getDeclaredFields();
            for (java.lang.reflect.Field field : fields) {
                if (field.getName().toLowerCase().contains("parameter") || 
                    field.getName().toLowerCase().contains("argument")) {
                    
                    field.setAccessible(true);
                    Object value = field.get(context);
                    
                    if (value instanceof Object[]) {
                        log.debug("Found parameters via reflection in field: {}", field.getName());
                        return (Object[]) value;
                    }
                }
            }
            
            // Try parent class fields if available
            Class<?> superClass = contextClass.getSuperclass();
            if (superClass != null) {
                java.lang.reflect.Field[] superFields = superClass.getDeclaredFields();
                for (java.lang.reflect.Field field : superFields) {
                    if (field.getName().toLowerCase().contains("parameter") || 
                        field.getName().toLowerCase().contains("argument")) {
                        
                        field.setAccessible(true);
                        Object value = field.get(context);
                        
                        if (value instanceof Object[]) {
                            log.debug("Found parameters via reflection in superclass field: {}", field.getName());
                            return (Object[]) value;
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            log.trace("Reflective parameter access failed: {}", e.getMessage());
        }
        
        return null;
    }

    /**
     * Fallback method to parse parameters from display name.
     */
    private Object[] parseParametersFromDisplayName(ExtensionContext context) {
        String displayName = context.getDisplayName();
        log.trace("Parsing parameters from display name: {}", displayName);

        if (displayName.contains("[") && displayName.contains("]")) {
            // Try different display name formats
            String paramPart = null;

            // Format 1: "methodName[1] argument1, argument2"
            if (displayName.contains("] ")) {
                paramPart = displayName.substring(displayName.indexOf("] ") + 2).trim();
            }
            // Format 2: "methodName [argument1, argument2]"
            else if (displayName.contains(" [") && displayName.endsWith("]")) {
                int start = displayName.indexOf(" [") + 2;
                int end = displayName.lastIndexOf("]");
                paramPart = displayName.substring(start, end).trim();
            }

            if (paramPart != null && !paramPart.isEmpty()) {
                return parseParameterString(paramPart);
            }
        }

        return new Object[0];
    }

    /**
     * Parses parameter string into individual parameter values.
     */
    private Object[] parseParameterString(String paramString) {
        // Simple comma-based splitting (can be enhanced for complex cases)
        String[] params = paramString.split(",");
        Object[] result = new Object[params.length];

        for (int i = 0; i < params.length; i++) {
            result[i] = parseParameterValue(params[i].trim());
        }

        return result;
    }

    private String getFilePath(Class<?> testClass) {
        String packagePath = testClass.getPackage().getName().replace('.', '/');
        String className = testClass.getSimpleName() + ".java";
        return packagePath + "/" + className;
    }

    private String getTestTitle(Method method) {
        Title titleAnnotation = method.getAnnotation(Title.class);
        return titleAnnotation != null ? titleAnnotation.value() : method.getName();
    }

    private String getTestTitle(ExtensionContext extensionContext) {
        String baseTitle = extensionContext.getDisplayName();
        System.out.println("baseTitle: " + baseTitle);
        
        try {
            // Check if this is a parameterized test with a custom name
            Method testMethod = getTestMethod(extensionContext);
            System.out.println("actual method name: " + testMethod.getName());
            ParameterizedTest parameterizedTest = testMethod.getAnnotation(ParameterizedTest.class);
            
            if (parameterizedTest != null) {
                System.out.println("parameterizedTest not null");
                String parameterizedTestName = parameterizedTest.name();
                System.out.println("parameterizedTestName is " + parameterizedTestName);
                
                // Check if name property is present and not default/empty
                if (parameterizedTestName != null && !parameterizedTestName.trim().isEmpty() 
                    && !parameterizedTestName.equals("{default_display_name}")) {
                    
                    // Use actual method name as base title, not the display name which might be the custom name
                    String actualMethodName = testMethod.getName();
                    String enhancedTitle = actualMethodName + " |>" + parameterizedTestName;
                    log.info("Enhanced parameterized test title: {}", enhancedTitle);
                    System.out.println("Final enhanced title: " + enhancedTitle);
                    return enhancedTitle;
                } else {
                    // No custom name property - return actual method name for parameterized tests
                    String actualMethodName = testMethod.getName();
                    log.info("Parameterized test title (no custom name): {}", actualMethodName);
                    System.out.println("Parameterized test using method name: " + actualMethodName);
                    return actualMethodName;
                }
            }
        } catch (Exception e) {
            log.debug("Failed to check parameterized test name: {}", e.getMessage());
        }
        
        log.info("Test title: {}", baseTitle);
        return baseTitle;
    }

    private String getTestId(Method method) {
        TestId testIdAnnotation = method.getAnnotation(TestId.class);
        return testIdAnnotation != null ? testIdAnnotation.value() : null;
    }

    private Method getTestMethod(ExtensionContext context) {
        return context.getTestMethod().orElseThrow(
                () -> new NoMethodInContextException(
                        "No test method found in " + context.getDisplayName()));
    }

    private Object parseParameterValue(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        // Remove quotes if present
        if ((value.startsWith("\"") && value.endsWith("\"")) ||
                (value.startsWith("'") && value.endsWith("'"))) {
            value = value.substring(1, value.length() - 1);
        }

        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            // Not an integer
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
            // Not a double
        }

        return value;
    }
}