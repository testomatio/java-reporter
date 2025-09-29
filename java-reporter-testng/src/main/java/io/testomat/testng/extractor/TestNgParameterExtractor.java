package io.testomat.testng.extractor;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.Parameters;

/**
 * Extracts parameters from TestNG parameterized tests.
 * Enhanced to provide meaningful parameter names using multiple strategies.
 */
public class TestNgParameterExtractor {
    private static final Logger log = LoggerFactory.getLogger(TestNgParameterExtractor.class);

    /**
     * Extracts example data from TestNG test result parameters.
     * Uses multiple strategies to get meaningful parameter names:
     * 1. @Parameters annotation values
     * 2. Compiled parameter names (requires -parameters compiler flag)
     * 3. Fallback to param0, param1, etc.
     *
     * @param testResult TestNG test result containing parameters
     * @return structured example object or null if no parameters
     */
    public Object extractExample(ITestResult testResult) {
        Object[] parameters = testResult.getParameters();
        if (parameters == null || parameters.length == 0) {
            return null;
        }

        if (parameters.length == 1) {
            return parameters[0];
        }

        Method method = testResult.getMethod().getConstructorOrMethod().getMethod();
        String[] parameterNames = extractParameterNames(method, parameters.length);

        Map<String, Object> exampleMap = new HashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            String paramName = i < parameterNames.length ? parameterNames[i] : "param" + i;
            exampleMap.put(paramName, parameters[i]);
        }

        return exampleMap;
    }

    /**
     * Extracts parameter names using multiple strategies.
     *
     * @param method     test method
     * @param paramCount number of parameters
     * @return array of parameter names
     */
    private String[] extractParameterNames(Method method, int paramCount) {
        String[] names = new String[paramCount];

        // Strategy 1: Try to get names from @Parameters annotation
        String[] annotationNames = getParameterNamesFromAnnotation(method);
        if (annotationNames != null && annotationNames.length >= paramCount) {
            System.arraycopy(annotationNames, 0, names, 0, paramCount);
            log.debug("Using parameter names from @Parameters annotation: {}",
                    String.join(", ", annotationNames));
            return names;
        }

        // Strategy 2: Try to get names from compiled parameter information
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
            log.debug("Using fallback parameter names "
                            + "(compile with -parameters flag for real names): {}",
                    String.join(", ", names));
        }

        // Fill remaining positions with default names
        for (int i = methodParameters.length; i < paramCount; i++) {
            names[i] = "param" + i;
        }

        return names;
    }

    /**
     * Extracts parameter names from @Parameters annotation.
     *
     * @param method test method
     * @return parameter names from annotation or null if not present
     */
    private String[] getParameterNamesFromAnnotation(Method method) {
        Parameters parametersAnnotation = method.getAnnotation(Parameters.class);
        if (parametersAnnotation != null) {
            String[] value = parametersAnnotation.value();
            if (value.length > 0) {
                return value;
            }
        }
        return null;
    }

    /**
     * Generates unique RID (Run ID) for test based on class name, method name and parameters.
     * Uses readable parameter values when possible.
     * For non-parameterized tests, returns className.methodName.
     *
     * @param testResult TestNG test result containing parameters
     * @return unique RID string (never null)
     */
    public String generateRid(ITestResult testResult) {
        StringBuilder ridBuilder = new StringBuilder();
        ridBuilder.append(testResult.getTestClass().getName())
                  .append(".")
                  .append(testResult.getMethod().getMethodName());

        Object[] parameters = testResult.getParameters();
        if (parameters == null || parameters.length == 0) {
            return ridBuilder.toString();
        }

        Method method = testResult.getMethod().getConstructorOrMethod().getMethod();
        String[] parameterNames = extractParameterNames(method, parameters.length);

        for (int i = 0; i < parameters.length; i++) {
            Object param = parameters[i];
            String paramString = param != null ? param.toString() : "null";
            String paramName = i < parameterNames.length ? parameterNames[i] : "param" + i;

            // For short, simple values, use them directly with parameter name
            if (paramString.length() <= 20 && paramString.matches("[a-zA-Z0-9._-]+")) {
                ridBuilder.append("-").append(paramName).append("_").append(paramString);
            } else {
                // For complex values, use hash with parameter name
                int hash = Math.abs(paramString.hashCode());
                ridBuilder.append("-").append(paramName).append("_h").append(hash);
            }
        }

        return ridBuilder.toString();
    }
}
