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
import io.testomat.junit.util.ParameterizedTestSupport;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
    public ParameterHandlerRegistry(Map<Class<? extends Annotation>,
            ParameterExtractionHandler> customHandlers) {
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
        if (!context.isValid() || !ParameterizedTestSupport.isAvailable()) {
            return Optional.empty();
        }

        ParameterExtractionHandler foundHandler = null;
        Class<? extends Annotation> foundAnnotation = null;

        for (Annotation annotation : context.getAnnotations()) {
            // Only process JUnit params annotations
            if (!ParameterizedTestSupport.isJunitParamsAnnotation(annotation)) {
                continue;
            }

            Class<? extends Annotation> annotationType = annotation.annotationType();
            ParameterExtractionHandler handler = handlerMap.get(annotationType);

            if (handler != null) {
                if (foundHandler != null) {
                    throw new IllegalStateException(String.format(
                            "Multiple parameter source annotations found on method: @%s and @%s. "
                                    + "Only one parameter source annotation is allowed method.",
                            foundAnnotation.getSimpleName(), annotationType.getSimpleName()
                    ));
                }
                foundHandler = handler;
                foundAnnotation = annotationType;
            }
        }

        return Optional.ofNullable(foundHandler);
    }

    private void registerDefaultHandlers() {
        // Only register handlers if JUnit params is available
        if (!ParameterizedTestSupport.isAvailable()) {
            return;
        }

        registerHandler("ValueSource", new ValueSourceHandler());
        registerHandler("EnumSource", new EnumSourceHandler());
        registerHandler("CsvSource", new CsvSourceHandler());
        registerHandler("CsvFileSource", new CsvFileSourceHandler());
        registerHandler("MethodSource", new MethodSourceHandler());
        registerHandler("ArgumentsSource", new ArgumentsSourceHandler());
        registerHandler("NullAndEmptySource", new NullAndEmptySourceHandler());
        registerHandler("NullSource", new NullSourceHandler());
        registerHandler("EmptySource", new EmptySourceHandler());
    }

    private void registerHandler(String annotationName, ParameterExtractionHandler handler) {
        ParameterizedTestSupport.loadAnnotationClass(annotationName)
                .ifPresent(annotationClass -> handlerMap.put(annotationClass, handler));
    }
}
