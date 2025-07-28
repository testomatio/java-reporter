package io.testomat.testng.methodexporter.extractor;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TestNgMethodInfoExtractor {

    private static final String IGNORE_ANNOTATION = "Ignore";
    private static final String IGNORE_METHOD_PREFIX = "ignore";
    private static final String SKIP_METHOD_PREFIX = "skip";

    private final TestNgLabelExtractor labelExtractor;

    public TestNgMethodInfoExtractor() {
        this.labelExtractor = new TestNgLabelExtractor();
    }

    public TestNgMethodInfoExtractor(TestNgLabelExtractor labelExtractor) {
        this.labelExtractor = labelExtractor;
    }

    /**
     * Extracts the test name from the method.
     * For TestNG, just returns the method name as TestNG doesn't have @DisplayName equivalent.
     */
    public String getTestName(MethodDeclaration method) {
        return safeExecute(() -> method.getNameAsString(), method.getNameAsString());
    }

    /**
     * Generates the complete method code including all annotations, signature and body.
     */
    public String getMethodCode(MethodDeclaration method) {
        return safeExecute(() -> buildMethodCode(method), method.toString());
    }

    /**
     * Determines if the test method should be skipped.
     * Checks for @Ignore annotation, method names starting with "ignore" or "skip",
     * and @Test(enabled=false) parameter.
     */
    public boolean isTestSkipped(MethodDeclaration method) {
        return safeExecute(() ->
                hasIgnoreAnnotation(method)
                        || hasSkipMethodName(method)
                        || hasTestEnabledFalse(method), false);
    }

    /**
     * Extracts the suite hierarchy for the test method.
     * Traverses from outermost to innermost class.
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
        //        appendParameters(code, method);
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

    private boolean hasIgnoreAnnotation(MethodDeclaration method) {
        return method.getAnnotations().stream()
                .anyMatch(ann -> ann.getNameAsString().equals(IGNORE_ANNOTATION));
    }

    private boolean hasSkipMethodName(MethodDeclaration method) {
        String methodName = method.getNameAsString();
        return methodName.startsWith(IGNORE_METHOD_PREFIX)
                || methodName.startsWith(SKIP_METHOD_PREFIX);
    }

    private boolean hasTestEnabledFalse(MethodDeclaration method) {
        return method.getAnnotations().stream()
                .filter(ann -> "Test".equals(ann.getNameAsString()))
                .anyMatch(testAnn -> testAnn.toString().contains("enabled = false")
                        || testAnn.toString().contains("enabled=false"));
    }

    private List<String> buildSuiteHierarchy(MethodDeclaration testMethod) {
        List<String> suites = new ArrayList<>();
        List<ClassOrInterfaceDeclaration> classHierarchy = collectClassHierarchy(testMethod);

        for (ClassOrInterfaceDeclaration clazz : classHierarchy) {
            String suiteName = clazz.getNameAsString();
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
