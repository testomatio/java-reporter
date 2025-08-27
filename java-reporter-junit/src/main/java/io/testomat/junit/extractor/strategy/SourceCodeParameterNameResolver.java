package io.testomat.junit.extractor.strategy;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import io.testomat.junit.methodexporter.filefinder.FileFinder;
import io.testomat.junit.methodexporter.parser.FileParser;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralized resolver for extracting parameter names from source code.
 * Uses JavaParser to parse method signatures and extract actual parameter names.
 * <p>
 * Features:
 * - Thread-safe caching for performance
 * - Fallback to reflection-based names when source parsing fails
 * - Integration with existing FileFinder and FileParser infrastructure
 * - Robust error handling and logging
 */
public class SourceCodeParameterNameResolver {

    private static final Logger logger =
            LoggerFactory.getLogger(SourceCodeParameterNameResolver.class);
    private static final SourceCodeParameterNameResolver INSTANCE =
            new SourceCodeParameterNameResolver();

    private final Map<String, List<String>> parameterNameCache = new ConcurrentHashMap<>();
    private final FileFinder fileFinder;
    private final FileParser fileParser;

    /**
     * Constructor for testing with custom dependencies
     */
    SourceCodeParameterNameResolver(FileFinder fileFinder, FileParser fileParser) {
        this.fileFinder = fileFinder;
        this.fileParser = fileParser;
    }

    SourceCodeParameterNameResolver() {
        this.fileFinder = new FileFinder();
        this.fileParser = new FileParser();
    }

    public static SourceCodeParameterNameResolver getInstance() {
        return INSTANCE;
    }

    /**
     * Resolves parameter names for a given method, using source code parsing when possible.
     * Falls back to reflection-based names if source parsing fails.
     *
     * @param method the method to resolve parameter names for
     * @return list of parameter names, or empty list if method is null
     */
    public List<String> resolveParameterNames(Method method) {
        if (method == null) {
            return new ArrayList<>();
        }

        String cacheKey = createCacheKey(method);

        List<String> cachedNames = parameterNameCache.get(cacheKey);
        if (cachedNames != null) {
            logger.trace("Retrieved parameter names from cache for method: {}", cacheKey);
            return new ArrayList<>(cachedNames);
        }

        List<String> sourceNames = parseParameterNamesFromSource(method);
        if (sourceNames != null && !sourceNames.isEmpty()) {
            logger.debug("Successfully parsed param names from source for method: {}", cacheKey);
            parameterNameCache.put(cacheKey, sourceNames);
            return new ArrayList<>(sourceNames);
        }

        List<String> reflectionNames = getReflectionBasedNames(method);
        logger.debug("Using reflection-based parameter names for method: {}", cacheKey);
        parameterNameCache.put(cacheKey, reflectionNames);
        return new ArrayList<>(reflectionNames);
    }

    /**
     * Gets a single parameter name by index, with appropriate fallback.
     *
     * @param method         the method to get parameter name for
     * @param parameterIndex the index of the parameter (0-based)
     * @return the parameter name, or "param{index}" as ultimate fallback
     */
    public String getParameterName(Method method, int parameterIndex) {
        List<String> names = resolveParameterNames(method);

        if (parameterIndex >= 0 && parameterIndex < names.size()) {
            return names.get(parameterIndex);
        }

        logger.warn("Parameter index {} out of bounds for method {}, using generic fallback",
                parameterIndex, method != null ? method.getName() : "null");
        return "param" + parameterIndex;
    }

    /**
     * Parses parameter names from the source code using JavaParser.
     * Returns null if parsing fails for any reason.
     */
    private List<String> parseParameterNamesFromSource(Method method) {
        try {

            Class<?> declaringClass = method.getDeclaringClass();
            String sourceFilePath = fileFinder.getTestClassFilePath(declaringClass);

            if (sourceFilePath == null) {
                logger.debug("Could not find source file for class: {}", declaringClass.getName());
                return null;
            }

            CompilationUnit compilationUnit = fileParser.parseFile(sourceFilePath);
            if (compilationUnit == null) {
                logger.debug("Could not parse source file: {}", sourceFilePath);
                return null;
            }

            MethodDeclaration methodDeclaration = findMethodInCompilationUnit(
                    compilationUnit, declaringClass, method);

            if (methodDeclaration == null) {
                logger.debug("Could not find method {} in source file: {}",
                        method.getName(), sourceFilePath);
                return null;
            }

            List<String> parameterNames = new ArrayList<>();
            for (Parameter param : methodDeclaration.getParameters()) {
                parameterNames.add(param.getNameAsString());
            }

            logger.trace("Parsed {} parameter names from source for method: {}",
                    parameterNames.size(), method.getName());
            return parameterNames;

        } catch (Exception e) {
            logger.debug("Failed to parse parameter names from source for method: {} - {}",
                    method.getName(), e.getMessage());
            return null;
        }
    }

    /**
     * Finds a specific method declaration in the compilation unit.
     * Handles nested classes and method overloading by comparing parameter types.
     */
    private MethodDeclaration findMethodInCompilationUnit(CompilationUnit compilationUnit,
                                                          Class<?> declaringClass,
                                                          Method targetMethod) {

        String className = getSimpleClassName(declaringClass);

        return compilationUnit.findAll(ClassOrInterfaceDeclaration.class)
                .stream()
                .filter(classDecl ->
                        className.equals(classDecl.getNameAsString())
                                || declaringClass.getSimpleName()
                                .equals(classDecl.getNameAsString()))
                .flatMap(classDecl -> classDecl.getMethods().stream())
                .filter(methodDecl ->
                        targetMethod.getName().equals(methodDecl.getNameAsString()))
                .filter(methodDecl -> parametersMatch(methodDecl, targetMethod))
                .findFirst()
                .orElse(null);
    }

    /**
     * Checks if a parsed method declaration matches the reflection-based method
     * by comparing parameter types.
     */
    private boolean parametersMatch(MethodDeclaration methodDecl, Method reflectionMethod) {
        if (methodDecl.getParameters().size() != reflectionMethod.getParameterCount()) {
            return false;
        }

        Class<?>[] reflectionParamTypes = reflectionMethod.getParameterTypes();

        for (int i = 0; i < methodDecl.getParameters().size(); i++) {
            Parameter astParam = methodDecl.getParameters().get(i);
            Class<?> reflectionParamType = reflectionParamTypes[i];

            String astTypeName = astParam.getTypeAsString();
            String reflectionTypeName = getSimpleTypeName(reflectionParamType);

            if (!typesMatch(astTypeName, reflectionTypeName)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if two type names represent the same type.
     * Handles common variations like "String" vs "java.lang.String".
     */
    private boolean typesMatch(String astTypeName, String reflectionTypeName) {
        if (astTypeName.equals(reflectionTypeName)) {
            return true;
        }

        String astSimple = getLastComponent(astTypeName);
        String reflectionSimple = getLastComponent(reflectionTypeName);

        return astSimple.equals(reflectionSimple);
    }

    /**
     * Gets reflection-based parameter names as fallback.
     * Uses the existing logic from strategies but centralized here.
     */
    private List<String> getReflectionBasedNames(Method method) {
        List<String> names = new ArrayList<>();
        java.lang.reflect.Parameter[] parameters = method.getParameters();

        for (java.lang.reflect.Parameter param : parameters) {
            names.add(param.getName());
        }

        return names;
    }

    /**
     * Creates a unique cache key for a method including its signature.
     */
    private String createCacheKey(Method method) {
        StringBuilder sb = new StringBuilder();
        sb.append(method.getDeclaringClass().getName())
                .append(".")
                .append(method.getName())
                .append("(");

        Class<?>[] paramTypes = method.getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(paramTypes[i].getName());
        }

        sb.append(")");
        return sb.toString();
    }

    /**
     * Gets the simple class name, handling nested classes.
     */
    private String getSimpleClassName(Class<?> clazz) {
        String fullName = clazz.getName();
        int lastDot = fullName.lastIndexOf('.');
        if (lastDot >= 0) {
            return fullName.substring(lastDot + 1);
        }
        return fullName;
    }

    /**
     * Gets a simple type name for comparison.
     */
    private String getSimpleTypeName(Class<?> type) {
        if (type.isArray()) {
            return getSimpleTypeName(type.getComponentType()) + "[]";
        }
        return type.getSimpleName();
    }

    /**
     * Gets the last component of a dot-separated string.
     */
    private String getLastComponent(String str) {
        int lastDot = str.lastIndexOf('.');
        if (lastDot >= 0) {
            return str.substring(lastDot + 1);
        }
        return str;
    }

    /**
     * Clears the parameter name cache. Useful for testing or memory management.
     */
    public void clearCache() {
        parameterNameCache.clear();
        logger.debug("Parameter name cache cleared");
    }

    /**
     * Gets cache statistics for monitoring and debugging.
     *
     * @return the current number of cached parameter name entries
     */
    public int getCacheSize() {
        return parameterNameCache.size();
    }
}
