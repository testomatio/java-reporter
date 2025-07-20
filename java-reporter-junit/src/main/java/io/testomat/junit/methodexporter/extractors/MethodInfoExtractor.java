package io.testomat.junit.methodexporter.extractors;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import java.util.ArrayList;
import java.util.List;

public class MethodInfoExtractor {
    private final LabelExtractor labelExtractor;

    public MethodInfoExtractor() {
        this.labelExtractor = new LabelExtractor();
    }

    /**
     * Constructor for testing
     */
    public MethodInfoExtractor(LabelExtractor labelExtractor) {
        this.labelExtractor = labelExtractor;
    }

    public String getTestName(MethodDeclaration method) {
        try {
            String displayName = method.getAnnotations().stream()
                    .filter(ann -> ann.getNameAsString().equals("DisplayName"))
                    .findFirst()
                    .map(labelExtractor::getAnnotationValue)
                    .orElse(null);
            return displayName != null ? displayName : method.getNameAsString();
        } catch (Exception e) {
            return method.getNameAsString();
        }
    }

    public String getMethodCode(MethodDeclaration method) {
        try {
            StringBuilder code = new StringBuilder();

            method.getAnnotations().forEach(annotation -> {
                String annotationStr = annotation.toString();
                if (!annotationStr.contains("@TestId") && !annotationStr.contains("@Execution")) {
                    code.append(annotationStr).append("\n");
                }
            });

            method.getModifiers().forEach(modifier ->
                    code.append(modifier.getKeyword().asString()).append(" "));

            code.append(method.getTypeAsString()).append(" ")
                    .append(method.getNameAsString())
                    .append("(");

            if (!method.getParameters().isEmpty()) {
                for (int i = 0; i < method.getParameters().size(); i++) {
                    if (i > 0) {
                        code.append(", ");
                    }
                    code.append(method.getParameter(i).toString());
                }
            }
            code.append(")");

            if (!method.getThrownExceptions().isEmpty()) {
                code.append(" throws ");
                for (int i = 0; i < method.getThrownExceptions().size(); i++) {
                    if (i > 0) {
                        code.append(", ");
                    }
                    code.append(method.getThrownException(i).toString());
                }
            }

            method.getBody().ifPresent(body ->
                    code.append(" ").append(body));

            return code.toString();
        } catch (Exception e) {
            return method.toString();
        }
    }

    public boolean isTestSkipped(MethodDeclaration method) {
        try {
            return method.getAnnotations().stream()
                    .anyMatch(ann ->
                            ann.getNameAsString().equals("Disabled")
                                    || ann.getNameAsString().equals("Ignore"))
                    || method.getNameAsString().startsWith("ignore")
                    || method.getNameAsString().startsWith("skip");
        } catch (Exception e) {
            return false;
        }
    }

    public List<String> extractSuites(MethodDeclaration testMethod) {
        try {
            List<String> suites = new ArrayList<>();
            List<ClassOrInterfaceDeclaration> classHierarchy = new ArrayList<>();

            ClassOrInterfaceDeclaration currentClass =
                    testMethod.findAncestor(ClassOrInterfaceDeclaration.class).orElse(null);

            while (currentClass != null) {
                classHierarchy.add(0, currentClass);
                currentClass =
                        currentClass.findAncestor(ClassOrInterfaceDeclaration.class).orElse(null);
            }

            for (ClassOrInterfaceDeclaration clazz : classHierarchy) {
                String suiteName = clazz.getAnnotationByName("DisplayName")
                        .map(labelExtractor::getAnnotationValue)
                        .orElse(clazz.getNameAsString());
                suites.add(suiteName);
            }

            return suites;
        } catch (Exception e) {
            List<String> suites = new ArrayList<>();
            testMethod.findAncestor(ClassOrInterfaceDeclaration.class)
                    .ifPresent(clazz -> suites.add(clazz.getNameAsString()));
            return suites;
        }
    }
}
