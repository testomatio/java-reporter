package io.testomat.junit.extractor.strategy.handlers;

import io.testomat.junit.extractor.strategy.ParameterExtractionContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Parameter extraction handler for @MethodSource annotated parameterized tests.
 * Supports extraction of parameter values from methods specified in @MethodSource annotations
 * with automatic method name resolution and argument handling for various return types.
 */
public class MethodSourceHandler extends AbstractParameterExtractionHandler {


    @Override
    public String getStrategyName() {
        return "MethodSourceExtractionStrategy";
    }

    @Override
    protected Object parseDisplayNameValue(String valueStr, ParameterExtractionContext context) {
        return parseMethodSourceDisplayName(valueStr, context);
    }

    @Override
    protected Object extractFromAnnotation(ParameterExtractionContext context) {
        MethodSource methodSource = context.getAnnotation(MethodSource.class);
        if (methodSource == null) {
            return new Object[0];
        }
        return extractFromMethodSource(methodSource, context);
    }

    private Object[] extractFromMethodSource(MethodSource methodSource, 
                                            ParameterExtractionContext context) {
        try {
            String[] methodNames = methodSource.value();
            
            if (methodNames.length == 0) {
                Method testMethod = context.getTestMethod();
                if (testMethod != null) {
                    methodNames = new String[]{inferMethodName(testMethod.getName())};
                }
            }
            
            if (methodNames.length == 0) {
                return new Object[0];
            }

            String methodName = methodNames[0];
            Method sourceMethod = resolveSourceMethod(methodName, context);
            if (sourceMethod != null) {
                Object result = invokeSourceMethod(sourceMethod);
                return extractArgumentsFromResult(result);
            }

            return new Object[0];

        } catch (Exception e) {
            logger.debug("Failed to extract parameters from method source", e);
            return new Object[0];
        }
    }

    private String inferMethodName(String testMethodName) {
        if (testMethodName.startsWith("test")) {
            String baseName = testMethodName.substring(4);
            return "provide" + baseName;
        }
        return testMethodName + "Provider";
    }

    private Method resolveSourceMethod(String methodName, ParameterExtractionContext context) {
        try {
            Method testMethod = context.getTestMethod();
            if (testMethod == null) {
                return null;
            }

            Class<?> testClass = testMethod.getDeclaringClass();
            
            if (methodName.contains("#")) {
                String[] parts = methodName.split("#", 2);
                String className = parts[0];
                String simpleMethodName = parts[1];
                
                try {
                    Class<?> sourceClass = Class.forName(className);
                    return findMethodByName(sourceClass, simpleMethodName);
                } catch (ClassNotFoundException e) {
                    logger.debug("Could not find class: {}", className, e);
                    return null;
                }
            } else {
                return findMethodByName(testClass, methodName);
            }

        } catch (Exception e) {
            logger.debug("Failed to resolve source method: {}", methodName, e);
            return null;
        }
    }

    private Method findMethodByName(Class<?> clazz, String methodName) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName) 
                && java.lang.reflect.Modifier.isStatic(method.getModifiers())
                && method.getParameterCount() == 0) {
                
                Class<?> returnType = method.getReturnType();
                if (Stream.class.isAssignableFrom(returnType) 
                    || Iterator.class.isAssignableFrom(returnType)
                    || returnType.isArray()) {
                    return method;
                }
            }
        }
        return null;
    }

    private Object invokeSourceMethod(Method sourceMethod) {
        try {
            sourceMethod.setAccessible(true);
            return sourceMethod.invoke(null);
        } catch (Exception e) {
            logger.debug("Failed to invoke source method: {}", sourceMethod.getName(), e);
            return null;
        }
    }

    private Object[] extractArgumentsFromResult(Object result) {
        if (result == null) {
            return new Object[0];
        }

        try {
            if (result instanceof Stream) {
                Stream<?> stream = (Stream<?>) result;
                Object firstElement = stream.findFirst().orElse(null);
                return convertToArgumentArray(firstElement);
            } else if (result instanceof Iterator) {
                Iterator<?> iterator = (Iterator<?>) result;
                if (iterator.hasNext()) {
                    Object firstElement = iterator.next();
                    return convertToArgumentArray(firstElement);
                }
            } else if (result.getClass().isArray()) {
                Object[] array = (Object[]) result;
                if (array.length > 0) {
                    return convertToArgumentArray(array[0]);
                }
            }

                return new Object[]{result};

        } catch (Exception e) {
            logger.debug("Failed to extract arguments from result", e);
            return new Object[0];
        }
    }

    private Object[] convertToArgumentArray(Object element) {
        if (element == null) {
            return new Object[]{null};
        }

        if (element instanceof Arguments) {
            Arguments args = (Arguments) element;
            return args.get();
        } else if (element instanceof Object[]) {
            return (Object[]) element;
        } else {
                return new Object[]{element};
        }
    }

    private Object[] parseMethodSourceDisplayName(String displayValue, 
                                                  ParameterExtractionContext context) {
        if (displayValue == null || displayValue.trim().isEmpty()) {
            return new Object[0];
        }

        String trimmed = displayValue.trim();

        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            String arrayContent = trimmed.substring(1, trimmed.length() - 1);
            return parseCommaSeparatedValues(arrayContent);
        }

        if (trimmed.startsWith("Arguments{arguments=[") && trimmed.endsWith("]}")) {
            String argsContent = trimmed.substring(21, trimmed.length() - 2);
            return parseCommaSeparatedValues(argsContent);
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


}