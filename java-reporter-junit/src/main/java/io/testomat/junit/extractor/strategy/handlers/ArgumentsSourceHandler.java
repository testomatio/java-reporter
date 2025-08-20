package io.testomat.junit.extractor.strategy.handlers;

import io.testomat.junit.extractor.strategy.ParameterExtractionContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

/**
 * Parameter extraction handler for @ArgumentsSource annotated parameterized tests.
 * Supports extraction of parameter values from custom ArgumentsProvider implementations
 * specified in @ArgumentsSource annotations. Handles complex argument providers with
 * automatic instantiation and invocation.
 */
public class ArgumentsSourceHandler extends AbstractParameterExtractionHandler {


    @Override
    public int getPriority() {
        return 20;
    }

    @Override
    public String getStrategyName() {
        return "ArgumentsSourceExtractionStrategy";
    }

    @Override
    protected Class<? extends Annotation> getSupportedAnnotationType() {
        return ArgumentsSource.class;
    }

    @Override
    protected Object parseDisplayNameValue(String valueStr, ParameterExtractionContext context) {
        return parseArgumentsSourceDisplayName(valueStr, context);
    }

    @Override
    protected Object extractFromAnnotation(ParameterExtractionContext context) {
        ArgumentsSource argumentsSource = context.getAnnotation(ArgumentsSource.class);
        if (argumentsSource == null) {
            return new Object[0];
        }
        return extractFromArgumentsProvider(argumentsSource, context);
    }

    private Object[] extractFromArgumentsProvider(ArgumentsSource argumentsSource, 
                                                 ParameterExtractionContext context) {
        try {
            Class<? extends ArgumentsProvider> providerClass = argumentsSource.value();
            if (providerClass == null) {
                return new Object[0];
            }

            ArgumentsProvider provider = instantiateProvider(providerClass);
            if (provider == null) {
                return new Object[0];
            }

            ExtensionContext extensionContext = context.getExtensionContext();
            if (extensionContext == null) {
                return new Object[0];
            }

            Stream<? extends Arguments> argumentsStream = provider.provideArguments(extensionContext);
            if (argumentsStream == null) {
                return new Object[0];
            }

            Arguments firstArguments = argumentsStream.findFirst().orElse(null);
            if (firstArguments != null) {
                return firstArguments.get();
            }

            return new Object[0];

        } catch (Exception e) {
            logger.debug("Failed to extract parameters from ArgumentsProvider", e);
            return new Object[0];
        }
    }

    private ArgumentsProvider instantiateProvider(Class<? extends ArgumentsProvider> providerClass) {
        try {
            Constructor<? extends ArgumentsProvider> defaultConstructor = 
                providerClass.getDeclaredConstructor();
            defaultConstructor.setAccessible(true);
            return defaultConstructor.newInstance();
            
        } catch (Exception e) {
            logger.debug("Failed to instantiate ArgumentsProvider with default constructor: {}", 
                        providerClass.getName(), e);
            
            try {
                Constructor<?>[] constructors = providerClass.getDeclaredConstructors();
                for (Constructor<?> constructor : constructors) {
                    if (constructor.getParameterCount() == 0) {
                        constructor.setAccessible(true);
                        return (ArgumentsProvider) constructor.newInstance();
                    }
                }
                
                if (constructors.length > 0) {
                    Constructor<?> firstConstructor = constructors[0];
                    firstConstructor.setAccessible(true);
                    Object[] nullArgs = new Object[firstConstructor.getParameterCount()];
                    return (ArgumentsProvider) firstConstructor.newInstance(nullArgs);
                }
                
            } catch (Exception fallbackException) {
                logger.debug("Failed to instantiate ArgumentsProvider with fallback approaches: {}", 
                            providerClass.getName(), fallbackException);
            }
        }
        
        return null;
    }

    private Object[] parseArgumentsSourceDisplayName(String displayValue, 
                                                     ParameterExtractionContext context) {
        if (displayValue == null || displayValue.trim().isEmpty()) {
            return new Object[0];
        }

        String trimmed = displayValue.trim();

        if (trimmed.startsWith("Arguments{arguments=[") && trimmed.endsWith("]}")) {
            String argsContent = trimmed.substring(21, trimmed.length() - 2);
            return parseCommaSeparatedValues(argsContent);
        }

        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            String arrayContent = trimmed.substring(1, trimmed.length() - 1);
            return parseCommaSeparatedValues(arrayContent);
        }

        if (trimmed.contains("(") && trimmed.contains(")")) {
            int startParen = trimmed.indexOf('(');
            int endParen = trimmed.lastIndexOf(')');
            if (startParen >= 0 && endParen > startParen) {
                String argsContent = trimmed.substring(startParen + 1, endParen);
                return parseCommaSeparatedValues(argsContent);
            }
        }

        if (trimmed.contains(",")) {
            return parseCommaSeparatedValues(trimmed);
        }

        return new Object[]{parseTypedValue(trimmed)};
    }

    private Object[] parseCommaSeparatedValues(String content) {
        if (content == null || content.trim().isEmpty()) {
            return new Object[0];
        }

        String[] parts = content.split(",");
        Object[] result = new Object[parts.length];
        
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            result[i] = parseTypedValue(part);
        }
        
        return result;
    }

    @Override
    protected Object parseTypedValue(String value) {
        String trimmed = removeQuotes(value);
        return super.parseTypedValue(trimmed);
    }
}