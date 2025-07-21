package io.testomat.junit.methodexporter.extractors;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MethodInfoExtractor {

    private static final String DISPLAY_NAME_ANNOTATION = "DisplayName";
    private static final String DISABLED_ANNOTATION = "Disabled";
    private static final String IGNORE_ANNOTATION = "Ignore";
    private static final String IGNORE_METHOD_PREFIX = "ignore";
    private static final String SKIP_METHOD_PREFIX = "skip";

    private final LabelExtractor labelExtractor;

    public MethodInfoExtractor() {
        this.labelExtractor = new LabelExtractor();
    }

    public MethodInfoExtractor(LabelExtractor labelExtractor) {
        this.labelExtractor = labelExtractor;
    }

    /**
     * Extracts the test name from the method.
     * Returns the value from @DisplayName annotation if present, otherwise returns the method name.
     *
     * @param method the method declaration to extract name from
     * @return the test name or method name as fallback
     */
    public String getTestName(MethodDeclaration method) {
        return safeExecute(() -> {
            String displayName = extractDisplayName(method);
            return displayName != null ? displayName : method.getNameAsString();
        }, method.getNameAsString());
    }

    /**
     * Generates the complete method code including all annotations, signature and body.
     *
     * @param method the method declaration to extract code from
     * @return the complete method code as string
     */
    public String getMethodCode(MethodDeclaration method) {
        return safeExecute(() -> buildMethodCode(method), method.toString());
    }

    /**
     * Determines if the test method should be skipped.
     * Checks for @Disabled/@Ignore annotations and method names starting with "ignore" or "skip".
     *
     * @param method the method declaration to check
     * @return true if the test should be skipped, false otherwise
     */
    public boolean isTestSkipped(MethodDeclaration method) {
        return safeExecute(() ->
                hasSkipAnnotations(method) || hasSkipMethodName(method), false);
    }

    /**
     * Extracts the suite hierarchy for the test method.
     * Traverses from outermost to innermost class, using @DisplayName values when available.
     *
     * @param testMethod the method declaration to extract suites for
     * @return list of suite names in hierarchical order
     */
    public List<String> extractSuites(MethodDeclaration testMethod) {
        return safeExecute(() ->
                buildSuiteHierarchy(testMethod), createFallbackSuites(testMethod));
    }

    private <T> T safeExecute(SafeOperation<T> operation, T fallbackValue) {
        try {
            return operation.execute();
        } catch (Exception e) {
            return fallbackValue;
        }
    }

    private String extractDisplayName(MethodDeclaration method) {
        return method.getAnnotations().stream()
                .filter(ann -> ann.getNameAsString().equals(DISPLAY_NAME_ANNOTATION))
                .findFirst()
                .map(labelExtractor::getAnnotationValue)
                .orElse(null);
    }

    private String buildMethodCode(MethodDeclaration method) {
        StringBuilder code = new StringBuilder();

        appendAllAnnotations(code, method);
        appendMethodSignature(code, method);
        appendMethodBody(code, method);

        return code.toString();
    }

    private void appendAllAnnotations(StringBuilder code, MethodDeclaration method) {
        method.getAnnotations().forEach(annotation -> {
            code.append(annotation.toString()).append("\n");
        });
    }

    private void appendMethodSignature(StringBuilder code, MethodDeclaration method) {
        appendModifiers(code, method);
        appendReturnTypeAndName(code, method);
        appendParameters(code, method);
        appendThrownExceptions(code, method);
    }

    private void appendModifiers(StringBuilder code, MethodDeclaration method) {
        method.getModifiers().forEach(modifier ->
                code.append(modifier.getKeyword().asString()).append(" "));
    }

    private void appendReturnTypeAndName(StringBuilder code, MethodDeclaration method) {
        code.append(method.getTypeAsString())
                .append(" ")
                .append(method.getNameAsString())
                .append("(");
    }

    private void appendParameters(StringBuilder code, MethodDeclaration method) {
        if (!method.getParameters().isEmpty()) {
            for (int i = 0; i < method.getParameters().size(); i++) {
                if (i > 0) {
                    code.append(", ");
                }
                code.append(method.getParameter(i).toString());
            }
        }
        code.append(")");
    }

    private void appendThrownExceptions(StringBuilder code, MethodDeclaration method) {
        if (!method.getThrownExceptions().isEmpty()) {
            code.append(" throws ");
            for (int i = 0; i < method.getThrownExceptions().size(); i++) {
                if (i > 0) {
                    code.append(", ");
                }
                code.append(method.getThrownException(i).toString());
            }
        }
    }

    private void appendMethodBody(StringBuilder code, MethodDeclaration method) {
        Optional<BlockStmt> body = method.getBody();
        body.ifPresent(blockStmt -> code.append(" ").append(blockStmt));
    }

    private boolean hasSkipAnnotations(MethodDeclaration method) {
        return method.getAnnotations().stream()
                .anyMatch(ann ->
                        ann.getNameAsString().equals(DISABLED_ANNOTATION)
                                || ann.getNameAsString().equals(IGNORE_ANNOTATION));
    }

    private boolean hasSkipMethodName(MethodDeclaration method) {
        String methodName = method.getNameAsString();
        return methodName.startsWith(IGNORE_METHOD_PREFIX)
                || methodName.startsWith(SKIP_METHOD_PREFIX);
    }

    private List<String> buildSuiteHierarchy(MethodDeclaration testMethod) {
        List<String> suites = new ArrayList<>();
        List<ClassOrInterfaceDeclaration> classHierarchy = collectClassHierarchy(testMethod);

        for (ClassOrInterfaceDeclaration clazz : classHierarchy) {
            String suiteName = extractSuiteName(clazz);
            suites.add(suiteName);
        }

        return suites;
    }

    private List<ClassOrInterfaceDeclaration> collectClassHierarchy(MethodDeclaration testMethod) {
        List<ClassOrInterfaceDeclaration> classHierarchy = new ArrayList<>();
        ClassOrInterfaceDeclaration currentClass =
                testMethod.findAncestor(ClassOrInterfaceDeclaration.class).orElse(null);

        while (currentClass != null) {
            classHierarchy.add(0, currentClass);
            currentClass =
                    currentClass.findAncestor(ClassOrInterfaceDeclaration.class).orElse(null);
        }

        return classHierarchy;
    }

    private String extractSuiteName(ClassOrInterfaceDeclaration clazz) {
        return clazz.getAnnotationByName(DISPLAY_NAME_ANNOTATION)
                .map(labelExtractor::getAnnotationValue)
                .orElse(clazz.getNameAsString());
    }

    private List<String> createFallbackSuites(MethodDeclaration testMethod) {
        List<String> suites = new ArrayList<>();
        testMethod.findAncestor(ClassOrInterfaceDeclaration.class)
                .ifPresent(clazz -> suites.add(clazz.getNameAsString()));
        return suites;
    }

    @FunctionalInterface
    private interface SafeOperation<T> {
        T execute() throws Exception;
    }
}
