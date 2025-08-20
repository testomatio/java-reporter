package io.testomat.junit.extractor.strategy;

import io.testomat.junit.exception.ParameterExtractionException;

/**
 * Strategy interface for extracting parameters from different types of JUnit parameterized test annotations.
 * Each implementation handles parameter extraction for a specific annotation type (e.g., @ValueSource, @CsvSource).
 */
public interface ParameterExtractionHandler {

    /**
     * Determines if this strategy can handle parameter extraction for the given test context.
     *
     * @param context the parameter extraction context containing test method and annotations
     * @return true if this strategy supports the test method's annotations
     */
    boolean supports(ParameterExtractionContext context);

    /**
     * Extracts parameters from the test method using this strategy's specific logic.
     *
     * @param context the parameter extraction context containing test method and annotations
     * @return extracted parameter value (single value for simple params, Map for multiple params), 
     *         or null if extraction fails
     * @throws ParameterExtractionException if parameter extraction encounters an error
     */
    Object extractParameters(ParameterExtractionContext context) throws ParameterExtractionException;

    /**
     * Gets the priority of this strategy when multiple strategies could apply to the same test.
     * Lower numbers indicate higher priority.
     *
     * @return priority value (0 = highest priority)
     */
    int getPriority();

    /**
     * Gets the name of this strategy for debugging and logging purposes.
     *
     * @return human-readable strategy name
     */
    String getStrategyName();
}