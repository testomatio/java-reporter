package io.testomat.junit.methodexporter;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MethodCaseExtractor {

    private final LabelExtractor labelExtractor = new LabelExtractor();
    private final PathFinder pathFinder = new PathFinder();
    private final MethodInfoExtractor methodInfoExtractor = new MethodInfoExtractor();

    public List<ExporterTestCase> extractTestCases(CompilationUnit cu, String filepath) {
        List<MethodDeclaration> testMethods = cu.findAll(MethodDeclaration.class).stream()
                .filter(this::isTestMethod)
                .collect(Collectors.toList());
        return convertDeclarationsToLoaderTestCases(testMethods, filepath);
    }

    private List<ExporterTestCase> convertDeclarationsToLoaderTestCases(
            List<MethodDeclaration> declarations, String filepath) {
        List<ExporterTestCase> cases = new ArrayList<>();
        for (MethodDeclaration method : declarations) {
            cases.add(createTestCase(method, filepath));
        }
        return cases;
    }

    private boolean isTestMethod(MethodDeclaration method) {
        try {
            return method.getAnnotations().stream()
                    .anyMatch(ann -> {
                        String name = ann.getNameAsString();
                        return "Test".equals(name)
                                || "ParameterizedTest".equals(name)
                                || "RepeatedTest".equals(name)
                                || "TestFactory".equals(name);
                    });
        } catch (Exception e) {
            return false;
        }
    }

    private ExporterTestCase createTestCase(MethodDeclaration method, String filepath) {

        try {
            ExporterTestCase testCase = new ExporterTestCase();
            testCase.setName(methodInfoExtractor.getTestName(method));
            testCase.setCode(methodInfoExtractor.getMethodCode(method));
            testCase.setSkipped(methodInfoExtractor.isTestSkipped(method));
            testCase.setSuites(methodInfoExtractor.extractSuites(method));
            testCase.setLabels(labelExtractor.extractLabels(method));
            testCase.setFile(pathFinder.extractRelativeFilePath(filepath));
            return testCase;
        } catch (Exception e) {
            throw new MethodExporterException("Failed to extract test case from: " + method
                    + "and filepath: " + filepath, e);
        }
    }
}
