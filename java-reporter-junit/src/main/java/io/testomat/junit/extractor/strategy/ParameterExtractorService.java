package io.testomat.junit.extractor.strategy;

import io.testomat.junit.exception.ParameterExtractionException;
import io.testomat.junit.extractor.strategy.handlers.ParameterExtractionHandler;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service that orchestrates parameter extraction from parameterized tests
 * using direct annotation mapping.
 * Automatically selects the appropriate handler based on test method annotations.
 */
public class ParameterExtractorService {

    private static final Logger logger = LoggerFactory.getLogger(ParameterExtractorService.class);
    private final ParameterHandlerRegistry handlerRegistry;

    public ParameterExtractorService() {
        this.handlerRegistry = new ParameterHandlerRegistry();
    }

    /**
     * Constructor for testing with custom handler registry.
     *
     * @param handlerRegistry the custom handler registry
     */
    public ParameterExtractorService(ParameterHandlerRegistry handlerRegistry) {
        this.handlerRegistry = handlerRegistry;
    }

    /**
     * Extracts parameters from a parameterized test using the appropriate strategy.
     *
     * @param extensionContext the JUnit extension context
     * @return extracted parameter object (single value or Map), or null if extraction fails
     */
    public Object extractParameters(ExtensionContext extensionContext) {
        if (extensionContext == null) {
            return null;
        }

        ParameterExtractionContext context = new ParameterExtractionContext(extensionContext);
        if (!context.isValid()) {
            return null;
        }

        Optional<ParameterExtractionHandler> handler = handlerRegistry.findHandler(context);
        if (!handler.isPresent()) {
            logger.debug("No applicable handler found for test: {}", context.getDisplayName());
            return null;
        }

        ParameterExtractionHandler strategy = handler.get();
        try {
            Object result = strategy.extractParameters(context);
            logger.debug("Successfully extracted parameters using {}: {}",
                    strategy.getStrategyName(), context.getDisplayName());
            return result;
        } catch (ParameterExtractionException e) {
            logger.warn("Parameter extraction failed with {}: {}",
                    strategy.getStrategyName(), e.getMessage());
            return null;
        }
    }

    /**
     * Gets the handler registry for testing purposes.
     *
     * @return the handler registry instance
     */
    public ParameterHandlerRegistry getHandlerRegistry() {
        return handlerRegistry;
    }
}
