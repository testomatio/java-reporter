package io.testomat.junit.extractor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParameterCapture implements BeforeEachCallback, AfterEachCallback {

    private static final Logger logger = LoggerFactory.getLogger(ParameterCapture.class);
    private static final ParameterParser parser = new ParameterParser();
    private static final Map<String, Object[]> storage = new ConcurrentHashMap<>();
    private static final ThreadLocal<Object[]> currentParams = new ThreadLocal<>();

    @Override
    public void beforeEach(ExtensionContext context) {
        currentParams.remove();
        captureParametersIfAvailable(context);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        currentParams.remove();
    }

    public static Object[] getCapturedParameters(ExtensionContext context) {
        if (context == null) {
            return null;
        }
        
        Object[] params = storage.get(context.getUniqueId());
        return params != null ? params : currentParams.get();
    }

    public static void storeParameters(ExtensionContext context, Object[] parameters) {
        if (context != null && parameters != null && parameters.length > 0) {
            String uniqueId = context.getUniqueId();
            storage.put(uniqueId, parameters);
            currentParams.set(parameters);
        }
    }

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