package io.testomat.junit.extractor.strategy;

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
    private final List<ParameterExtractionStrategy> strategies;

    public ParameterExtractorService() {
        this.strategies = new ArrayList<>();
        registerDefaultStrategies();
    }

    /**
     * Constructor for testing with custom strategies.
     */
    public ParameterExtractorService(List<ParameterExtractionStrategy> strategies) {
        this.strategies = new ArrayList<>(strategies);
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

        List<ParameterExtractionStrategy> applicableStrategies = selectStrategies(context);
        if (applicableStrategies.isEmpty()) {
            logger.debug("No applicable strategies found for test: {}", context.getDisplayName());
            return null;
        }

        // Execute the highest priority strategy that supports this test
        ParameterExtractionStrategy strategy = applicableStrategies.get(0);
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
     * Registers a new parameter extraction strategy.
     *
     * @param strategy the strategy to register
     */
    public void registerStrategy(ParameterExtractionStrategy strategy) {
        if (strategy != null) {
            strategies.add(strategy);
        }
    }

    /**
     * Gets all registered strategies.
     */
    public List<ParameterExtractionStrategy> getStrategies() {
        return new ArrayList<>(strategies);
    }

    private void registerDefaultStrategies() {
        // Register built-in strategies
        strategies.add(new ValueSourceExtractionStrategy());
        strategies.add(new EnumSourceExtractionStrategy());
        strategies.add(new CsvSourceExtractionStrategy());
        // TODO: Add other strategies as they are implemented
        // etc.
    }

    private List<ParameterExtractionStrategy> selectStrategies(ParameterExtractionContext context) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(context))
                .sorted(Comparator.comparingInt(ParameterExtractionStrategy::getPriority))
                .collect(Collectors.toList());
    }
}