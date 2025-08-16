package io.testomat.junit.extractor;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Intercepts JUnit test method invocations to capture parameters for parameterized tests.
 * This is the most reliable method for parameter extraction as it captures the actual
 * arguments passed to test methods during execution.
 */
public class InvocationInterceptor implements org.junit.jupiter.api.extension.InvocationInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(InvocationInterceptor.class);
    private static final Map<String, Object[]> storage = new ConcurrentHashMap<>();

    /**
     * Intercepts test method execution to capture parameters.
     *
     * @param invocation the test method invocation
     * @param invocationContext context containing method and arguments
     * @param context the JUnit extension context
     * @throws Throwable if test execution fails
     */
    @Override
    public void interceptTestMethod(Invocation<Void> invocation, 
                                   ReflectiveInvocationContext<Method> invocationContext,
                                   ExtensionContext context) throws Throwable {
        captureParametersIfParameterized(invocationContext, context);
        invocation.proceed();
    }

    /**
     * Retrieves captured parameters for a test.
     *
     * @param context the JUnit extension context
     * @return captured parameter array or null if not available
     */
    public static Object[] getCapturedParameters(ExtensionContext context) {
        return context == null ? null : storage.get(context.getUniqueId());
    }

    /**
     * Cleans up stored parameters to prevent memory leaks.
     *
     * @param context the JUnit extension context
     */
    public static void cleanupParameters(ExtensionContext context) {
        if (context != null) {
            storage.remove(context.getUniqueId());
        }
    }

    private void captureParametersIfParameterized(ReflectiveInvocationContext<Method> invocationContext, 
                                                 ExtensionContext context) {
        Method method = invocationContext.getExecutable();
        if (!method.isAnnotationPresent(ParameterizedTest.class)) {
            return;
        }

        List<Object> arguments = invocationContext.getArguments();
        if (arguments == null || arguments.isEmpty()) {
            return;
        }

        String uniqueId = context.getUniqueId();
        Object[] parameters = arguments.toArray();
        storage.put(uniqueId, parameters);
        logger.debug("Captured {} parameters for test: {}", parameters.length, uniqueId);
    }
}