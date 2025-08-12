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
        String title = getTestTitle(testMethod);
        Class<?> testClass = context.getRequiredTestClass();
        String suiteTitle = testClass.getSimpleName();
        String file = getFilePath(testClass);
        String testId = getTestId(testMethod);

        return new TestMetadata(title, testId, suiteTitle, file);
    }

    public Map<String, Object> extractTestParameters(ExtensionContext context) {
        if (!isParameterizedTest(context)) {
            return new HashMap<>();
        }

        Method testMethod = getTestMethod(context);
        Parameter[] parameters = testMethod.getParameters();
        Object[] parameterValues = getParameterValues(context);

        Map<String, Object> parameterMap = new HashMap<>();

        if (parameterValues != null && parameterValues.length == parameters.length) {
            for (int i = 0; i < parameters.length; i++) {
                String paramName = parameters[i].getName();
                Object paramValue = parameterValues[i];
                parameterMap.put(paramName, paramValue);
            }
        }

        return parameterMap;
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

    public synchronized String generateRid(ExtensionContext context,
                                           Map<String, Object> parameters) {
        if (!isParameterizedTest(context)) {
            return null;
        }

        Method testMethod = getTestMethod(context);
        String methodName = testMethod.getName();

        String uniqueId = context.getUniqueId();
        if (uniqueId != null && uniqueId.contains("[")) {
            String ridSuffix = extractRidFromUniqueId(uniqueId);
            return methodName + "-" + ridSuffix;
        }

        if (!parameters.isEmpty()) {
            String parameterBasedRid = generateRidFromParameters(methodName, parameters);
            if (parameterBasedRid != null) {
                return parameterBasedRid;
            }
        }

        long counter = ridCounter.incrementAndGet();
        return methodName + "-" + counter;
    }

    private String generateRidFromParameters(String methodName, Map<String, Object> parameters) {
        try {
            StringBuilder ridBuilder = new StringBuilder(methodName);
            ridBuilder.append("-");

            parameters.values().forEach(value -> {
                String paramStr = value != null ? value.toString() : "null";
                String cleanParam = sanitizeParameterForRid(paramStr);
                ridBuilder.append(cleanParam).append("-");
            });

            if (ridBuilder.length() > 0 && ridBuilder.charAt(ridBuilder.length() - 1) == '-') {
                ridBuilder.setLength(ridBuilder.length() - 1);
            }

            String result = ridBuilder.toString();

            if (result.length() > 50) {
                result = result.substring(0, 47) + "...";
            }

            return result;

        } catch (Exception e) {
            log.debug("Failed to generate RID from parameters: {}", parameters, e);
            return null;
        }
    }

    private String sanitizeParameterForRid(String paramValue) {
        if (paramValue == null || paramValue.isEmpty()) {
            return "empty";
        }

        String sanitized = paramValue.replaceAll("[^a-zA-Z0-9]", "");

        if (sanitized.isEmpty()) {
            return "special";
        }

        if (sanitized.length() > 15) {
            sanitized = sanitized.substring(0, 15);
        }

        return sanitized.toLowerCase();
    }

    private String extractRidFromUniqueId(String uniqueId) {
        try {
            if (uniqueId.contains("[test-template-invocation:#")) {
                String invocation = uniqueId.substring(
                        uniqueId.lastIndexOf("[test-template-invocation:#") + 27,
                        uniqueId.lastIndexOf("]")
                );
                return invocation;
            }

            if (uniqueId.contains("[") && uniqueId.contains("]")) {
                String lastBracket = uniqueId.substring(uniqueId.lastIndexOf("[") + 1);
                if (lastBracket.contains("]")) {
                    lastBracket = lastBracket.substring(0, lastBracket.indexOf("]"));
                    if (lastBracket.matches("\\d+")) {
                        return lastBracket;
                    }
                }
            }

            return String.valueOf(ridCounter.incrementAndGet());
        } catch (Exception e) {
            log.debug("Failed to extract RID from unique ID: {}, using counter instead",
                    uniqueId, e);
            return String.valueOf(ridCounter.incrementAndGet());
        }
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

    private String getTestId(Method method) {
        TestId testIdAnnotation = method.getAnnotation(TestId.class);
        return testIdAnnotation != null ? testIdAnnotation.value() : null;
    }

    private Method getTestMethod(ExtensionContext context) {
        return context.getTestMethod().orElseThrow(
                () -> new NoMethodInContextException(
                        "No test method found in " + context.getDisplayName()));
    }

    private Object[] getParameterValues(ExtensionContext context) {
        try {
            String displayName = context.getDisplayName();

            if (displayName.contains("[") && displayName.contains("]")) {
                String paramPart = displayName.substring(displayName.indexOf(']') + 1).trim();
                if (!paramPart.isEmpty()) {
                    String[] params = paramPart.split(",");
                    Object[] result = new Object[params.length];
                    for (int i = 0; i < params.length; i++) {
                        result[i] = parseParameterValue(params[i].trim());
                    }
                    return result;
                }
            }

            return new Object[0];
        } catch (Exception e) {
            log.debug("Failed to extract parameter values from display name: {}",
                    context.getDisplayName(), e);
            return new Object[0];
        }
    }

    private Object parseParameterValue(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            log.debug("Failed to parse integer: {}", value);
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
            log.debug("Failed to parse double: {}", value);
        }

        return value;
    }
}
