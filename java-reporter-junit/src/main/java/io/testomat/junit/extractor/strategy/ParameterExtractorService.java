package io.testomat.junit.extractor.strategy;

import io.testomat.junit.exception.ParameterExtractionException;
import io.testomat.junit.extractor.strategy.handlers.ArgumentsSourceHandler;
import io.testomat.junit.extractor.strategy.handlers.CsvFileSourceHandler;
import io.testomat.junit.extractor.strategy.handlers.CsvSourceHandler;
import io.testomat.junit.extractor.strategy.handlers.EmptySourceHandler;
import io.testomat.junit.extractor.strategy.handlers.EnumSourceHandler;
import io.testomat.junit.extractor.strategy.handlers.MethodSourceHandler;
import io.testomat.junit.extractor.strategy.handlers.NullAndEmptySourceHandler;
import io.testomat.junit.extractor.strategy.handlers.NullSourceHandler;
import io.testomat.junit.extractor.strategy.handlers.ValueSourceHandler;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service that orchestrates parameter extraction from parameterized tests using multiple strategies.
 * Automatically selects and applies the appropriate strategy based on test method annotations.
 */
public class ParameterExtractorService {

    private static final Logger logger = LoggerFactory.getLogger(ParameterExtractorService.class);
    private final List<ParameterExtractionHandler> handlers;

    public ParameterExtractorService() {
        this.handlers = new ArrayList<>();
        registerDefaultStrategies();
    }

    /**
     * Constructor for testing with custom strategies.
     */
    public ParameterExtractorService(List<ParameterExtractionHandler> handlers) {
        this.handlers = new ArrayList<>(handlers);
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

        List<ParameterExtractionHandler> applicableStrategies = selectStrategies(context);
        if (applicableStrategies.isEmpty()) {
            logger.debug("No applicable strategies found for test: {}", context.getDisplayName());
            return null;
        }

        ParameterExtractionHandler strategy = applicableStrategies.get(0);
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
     * Gets all registered strategies.
     */
    public List<ParameterExtractionHandler> getHandlers() {
        return new ArrayList<>(handlers);
    }

    private void registerDefaultStrategies() {
        handlers.add(new ValueSourceHandler());
        handlers.add(new EnumSourceHandler());
        handlers.add(new CsvSourceHandler());
        handlers.add(new CsvFileSourceHandler());
        handlers.add(new MethodSourceHandler());
        handlers.add(new ArgumentsSourceHandler());
        handlers.add(new NullAndEmptySourceHandler());
        handlers.add(new NullSourceHandler());
        handlers.add(new EmptySourceHandler());
    }

    private List<ParameterExtractionHandler> selectStrategies(ParameterExtractionContext context) {
        return handlers.stream()
                .filter(strategy -> strategy.supports(context))
                .sorted(Comparator.comparingInt(ParameterExtractionHandler::getPriority))
                .collect(Collectors.toList());
    }
}