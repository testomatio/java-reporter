package io.testomat.junit.util;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Utility class for managing JUnit Jupiter Params dependency availability at runtime.
 * This enables graceful degradation when parametrized test support is not available.
 * Provides safe class loading and caching for all JUnit params annotations and classes.
 */
public final class ParameterizedTestSupport {

    private static final boolean PARAMETERIZED_TEST_AVAILABLE = isParameterizedTestAvailable();
    private static final Map<String, Class<?>> CLASS_CACHE = new HashMap<>();
    private static final Map<String, String> ANNOTATION_CLASS_NAMES = new HashMap<>();

    static {
        ANNOTATION_CLASS_NAMES.put("ParameterizedTest",
                "org.junit.jupiter.params.ParameterizedTest");
        ANNOTATION_CLASS_NAMES.put("ValueSource",
                "org.junit.jupiter.params.provider.ValueSource");
        ANNOTATION_CLASS_NAMES.put("EnumSource",
                "org.junit.jupiter.params.provider.EnumSource");
        ANNOTATION_CLASS_NAMES.put("CsvSource",
                "org.junit.jupiter.params.provider.CsvSource");
        ANNOTATION_CLASS_NAMES.put("CsvFileSource",
                "org.junit.jupiter.params.provider.CsvFileSource");
        ANNOTATION_CLASS_NAMES.put("MethodSource",
                "org.junit.jupiter.params.provider.MethodSource");
        ANNOTATION_CLASS_NAMES.put("ArgumentsSource",
                "org.junit.jupiter.params.provider.ArgumentsSource");
        ANNOTATION_CLASS_NAMES.put("NullSource",
                "org.junit.jupiter.params.provider.NullSource");
        ANNOTATION_CLASS_NAMES.put("EmptySource",
                "org.junit.jupiter.params.provider.EmptySource");
        ANNOTATION_CLASS_NAMES.put("NullAndEmptySource",
                "org.junit.jupiter.params.provider.NullAndEmptySource");
        ANNOTATION_CLASS_NAMES.put("Arguments",
                "org.junit.jupiter.params.provider.Arguments");
        ANNOTATION_CLASS_NAMES.put("ArgumentsProvider",
                "org.junit.jupiter.params.provider.ArgumentsProvider");
    }

    private ParameterizedTestSupport() {
    }

    /**
     * Checks if JUnit Jupiter Params dependency is available on the classpath.
     *
     * @return true if ParameterizedTest annotation is available, false otherwise
     */
    public static boolean isAvailable() {
        return PARAMETERIZED_TEST_AVAILABLE;
    }

    /**
     * Safely loads a JUnit params class by simple name.
     *
     * @param simpleName the simple name of the class
     * @return Optional containing the class if available, empty otherwise
     */
    public static Optional<Class<?>> loadClass(String simpleName) {
        if (!PARAMETERIZED_TEST_AVAILABLE) {
            return Optional.empty();
        }

        String className = ANNOTATION_CLASS_NAMES.get(simpleName);
        if (className == null) {
            return Optional.empty();
        }

        return loadClassByName(className);
    }

    /**
     * Safely loads a JUnit params annotation class by simple name.
     *
     * @param simpleName the simple name of the annotation class
     * @return Optional containing the annotation class if available, empty otherwise
     */
    @SuppressWarnings("unchecked")
    public static Optional<Class<? extends Annotation>> loadAnnotationClass(String simpleName) {
        Optional<Class<?>> clazz = loadClass(simpleName);
        if (clazz.isPresent() && clazz.get().isAnnotation()) {
            return Optional.of((Class<? extends Annotation>) clazz.get());
        }
        return Optional.empty();
    }

    /**
     * Safely loads a class by its full qualified name.
     *
     * @param className the fully qualified class name
     * @return Optional containing the class if available, empty otherwise
     */
    public static Optional<Class<?>> loadClassByName(String className) {
        if (!PARAMETERIZED_TEST_AVAILABLE) {
            return Optional.empty();
        }

        if (CLASS_CACHE.containsKey(className)) {
            return Optional.ofNullable(CLASS_CACHE.get(className));
        }

        try {
            Class<?> clazz = Class.forName(className);
            CLASS_CACHE.put(className, clazz);
            return Optional.of(clazz);
        } catch (ClassNotFoundException e) {
            CLASS_CACHE.put(className, null); // Cache the failure
            return Optional.empty();
        }
    }

    /**
     * Checks if an annotation is a JUnit params annotation.
     *
     * @param annotation the annotation to check
     * @return true if it's a known JUnit params annotation, false otherwise
     */
    public static boolean isJunitParamsAnnotation(Annotation annotation) {
        if (!PARAMETERIZED_TEST_AVAILABLE || annotation == null) {
            return false;
        }

        String packageName = annotation.annotationType().getPackage().getName();
        return packageName.startsWith("org.junit.jupiter.params");
    }

    /**
     * Checks if a method has the ParameterizedTest annotation.
     *
     * @param method the method to check
     * @return true if the method has @ParameterizedTest annotation, false otherwise
     */
    public static boolean isParameterizedTest(java.lang.reflect.Method method) {
        if (!PARAMETERIZED_TEST_AVAILABLE || method == null) {
            return false;
        }

        Optional<Class<? extends Annotation>> parameterizedTestClass =
                loadAnnotationClass("ParameterizedTest");

        return parameterizedTestClass.isPresent()
                && method.isAnnotationPresent(parameterizedTestClass.get());
    }

    private static boolean isParameterizedTestAvailable() {
        try {
            Class.forName("org.junit.jupiter.params.ParameterizedTest");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
