package io.testomat.junit.extractor.strategy;

import io.testomat.junit.extractor.strategy.handlers.ArgumentsSourceHandler;
import io.testomat.junit.extractor.strategy.handlers.CsvFileSourceHandler;
import io.testomat.junit.extractor.strategy.handlers.CsvSourceHandler;
import io.testomat.junit.extractor.strategy.handlers.EmptySourceHandler;
import io.testomat.junit.extractor.strategy.handlers.EnumSourceHandler;
import io.testomat.junit.extractor.strategy.handlers.MethodSourceHandler;
import io.testomat.junit.extractor.strategy.handlers.NullAndEmptySourceHandler;
import io.testomat.junit.extractor.strategy.handlers.NullSourceHandler;
import io.testomat.junit.extractor.strategy.handlers.ParameterExtractionHandler;
import io.testomat.junit.extractor.strategy.handlers.ValueSourceHandler;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Registry that maintains direct mapping between JUnit parameter source annotations
 * and their corresponding extraction handlers.
 * <p>
 * This registry provides O(1) handler lookup based on annotation type, eliminating
 * the need for priority-based selection and filtering operations.
 * </p>
 *
 * @since 1.0
 */
public final class ParameterHandlerRegistry {

    private final Map<Class<? extends Annotation>, ParameterExtractionHandler> handlerMap;

    /**
     * Creates a new registry with default handler mappings.
     */
    public ParameterHandlerRegistry() {
        this.handlerMap = new HashMap<>();
        registerDefaultHandlers();
    }

    /**
     * Creates a registry with custom handler mappings for testing.
     *
     * @param customHandlers the custom handler mappings
     */
    public ParameterHandlerRegistry(Map<Class<? extends Annotation>, ParameterExtractionHandler> customHandlers) {
        this.handlerMap = new HashMap<>(customHandlers);
    }

    /**
     * Finds the appropriate handler for the given parameter extraction context.
     *
     * @param context the parameter extraction context
     * @return the matching handler, or empty if no handler found
     * @throws IllegalStateException if multiple conflicting handlers are found
     */
    public Optional<ParameterExtractionHandler> findHandler(ParameterExtractionContext context) {
        if (!context.isValid()) {
            return Optional.empty();
        }

        ParameterExtractionHandler foundHandler = null;
        Class<? extends Annotation> foundAnnotation = null;

        for (Annotation annotation : context.getAnnotations()) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            ParameterExtractionHandler handler = handlerMap.get(annotationType);
            
            if (handler != null) {
                if (foundHandler != null) {
                    throw new IllegalStateException(String.format(
                        "Multiple parameter source annotations found on test method: @%s and @%s. " +
                        "Only one parameter source annotation is allowed per test method.",
                        foundAnnotation.getSimpleName(), annotationType.getSimpleName()
                    ));
                }
                foundHandler = handler;
                foundAnnotation = annotationType;
            }
        }

        return Optional.ofNullable(foundHandler);
    }

    /**
     * Registers a handler for a specific annotation type.
     *
     * @param annotationType the annotation type
     * @param handler the handler for that annotation
     * @throws IllegalArgumentException if either parameter is null
     */
    public void registerHandler(Class<? extends Annotation> annotationType, ParameterExtractionHandler handler) {
        if (annotationType == null) {
            throw new IllegalArgumentException("Annotation type cannot be null");
        }
        if (handler == null) {
            throw new IllegalArgumentException("Handler cannot be null");
        }
        handlerMap.put(annotationType, handler);
    }

    /**
     * Gets the handler for a specific annotation type.
     *
     * @param annotationType the annotation type
     * @return the handler, or empty if not found
     */
    public Optional<ParameterExtractionHandler> getHandler(Class<? extends Annotation> annotationType) {
        return Optional.ofNullable(handlerMap.get(annotationType));
    }

    /**
     * Gets all registered annotation types.
     *
     * @return set of registered annotation types
     */
    public java.util.Set<Class<? extends Annotation>> getSupportedAnnotations() {
        return new java.util.HashSet<>(handlerMap.keySet());
    }

    private void registerDefaultHandlers() {
        handlerMap.put(ValueSource.class, new ValueSourceHandler());
        handlerMap.put(EnumSource.class, new EnumSourceHandler());
        handlerMap.put(CsvSource.class, new CsvSourceHandler());
        handlerMap.put(CsvFileSource.class, new CsvFileSourceHandler());
        handlerMap.put(MethodSource.class, new MethodSourceHandler());
        handlerMap.put(ArgumentsSource.class, new ArgumentsSourceHandler());
        handlerMap.put(NullAndEmptySource.class, new NullAndEmptySourceHandler());
        handlerMap.put(NullSource.class, new NullSourceHandler());
        handlerMap.put(EmptySource.class, new EmptySourceHandler());
    }
}