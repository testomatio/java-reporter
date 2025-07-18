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
        List<MethodDeclaration> allMethods = cu.findAll(MethodDeclaration.class);
        
        List<MethodDeclaration> testMethods = allMethods.stream()
                .filter(this::isTestMethod)
                .collect(Collectors.toList());
        
        List<ExporterTestCase> result = convertDeclarationsToLoaderTestCases(testMethods, filepath);
        return result;
    }

    private List<ExporterTestCase> convertDeclarationsToLoaderTestCases(
            List<MethodDeclaration> declarations, String filepath) {
        List<ExporterTestCase> cases = new ArrayList<>();
        for (int i = 0; i < declarations.size(); i++) {
            MethodDeclaration method = declarations.get(i);
            try {
                ExporterTestCase testCase = createTestCase(method, filepath);
                cases.add(testCase);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return cases;
    }

    private boolean isTestMethod(MethodDeclaration method) {
        try {
            boolean result = method.getAnnotations().stream()
                    .anyMatch(ann -> {
                        String name = ann.getNameAsString();
                        return "Test".equals(name)
                                || "ParameterizedTest".equals(name)
                                || "RepeatedTest".equals(name)
                                || "TestFactory".equals(name);
                    });
            return result;
        } catch (Exception e) {
            return false;
        }
    }

    private ExporterTestCase createTestCase(MethodDeclaration method, String filepath) {
        try {
            ExporterTestCase testCase = new ExporterTestCase();
            
            String testName = methodInfoExtractor.getTestName(method);
            testCase.setName(testName);
            
            String methodCode = methodInfoExtractor.getMethodCode(method);
            testCase.setCode(methodCode);
            
            boolean isSkipped = methodInfoExtractor.isTestSkipped(method);
            testCase.setSkipped(isSkipped);
            
            List<String> suites = methodInfoExtractor.extractSuites(method);
            testCase.setSuites(suites);
            
            List<String> labels = labelExtractor.extractLabels(method);
            testCase.setLabels(labels);
            
            String relativeFilePath = pathFinder.extractRelativeFilePath(filepath);
            testCase.setFile(relativeFilePath);
            
            return testCase;
        } catch (Exception e) {
            e.printStackTrace();
            throw new MethodExporterException("Failed to extract test case from: " + method
                    + "and filepath: " + filepath, e);
        }
    }
}
