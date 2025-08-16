package io.testomat.junit.extractor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit extension that captures test parameters through lifecycle callbacks.
 * Uses display name parsing and thread-local storage to extract parameters
 * from parameterized tests when direct interception is not available.
 */
public class ParameterCapture implements BeforeEachCallback, AfterEachCallback {

    private static final Logger logger = LoggerFactory.getLogger(ParameterCapture.class);
    private static final ParameterParser parser = new ParameterParser();
    private static final Map<String, Object[]> storage = new ConcurrentHashMap<>();
    private static final ThreadLocal<Object[]> currentParams = new ThreadLocal<>();

    /**
     * Captures parameters before each test execution.
     *
     * @param context the JUnit extension context
     */
    @Override
    public void beforeEach(ExtensionContext context) {
        currentParams.remove();
        captureParametersIfAvailable(context);
    }

    /**
     * Cleans up thread-local storage after each test.
     *
     * @param context the JUnit extension context
     */
    @Override
    public void afterEach(ExtensionContext context) {
        currentParams.remove();
    }

    /**
     * Retrieves captured parameters for a test.
     *
     * @param context the JUnit extension context
     * @return parameter array or null if not captured
     */
    public static Object[] getCapturedParameters(ExtensionContext context) {
        if (context == null) {
            return null;
        }
        
        Object[] params = storage.get(context.getUniqueId());
        return params != null ? params : currentParams.get();
    }

    /**
     * Manually stores parameters for a test context.
     *
     * @param context the JUnit extension context
     * @param parameters the parameter array to store
     */
    public static void storeParameters(ExtensionContext context, Object[] parameters) {
        if (context != null && parameters != null && parameters.length > 0) {
            String uniqueId = context.getUniqueId();
            storage.put(uniqueId, parameters);
            currentParams.set(parameters);
        }
    }

    /**
     * Removes stored parameters to prevent memory leaks.
     *
     * @param context the JUnit extension context
     */
    public static void cleanupParameters(ExtensionContext context) {
        if (context != null) {
            storage.remove(context.getUniqueId());
        }
    }

    private void captureParametersIfAvailable(ExtensionContext context) {
        try {
            Object[] parameters = parser.parseParametersFromDisplayName(context.getDisplayName());
            if (parameters != null && parameters.length > 0) {
                String uniqueId = context.getUniqueId();
                storage.put(uniqueId, parameters);
                currentParams.set(parameters);
            }
        } catch (Exception e) {
            logger.debug("Failed to extract parameters", e);
        }
    }
}