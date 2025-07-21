package io.testomat.junit.methodexporter.extractors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import io.testomat.junit.exception.ExtractionException;
import io.testomat.junit.exception.MethodExporterException;
import io.testomat.junit.methodexporter.patfinder.FileFinder;
import io.testomat.junit.model.ExporterTestCase;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MethodCaseExtractor {

    private final MethodInfoExtractor methodInfoExtractor;
    private final LabelExtractor labelExtractor;
    private final FileFinder fileFinder;

    public MethodCaseExtractor() {
        this.methodInfoExtractor = new MethodInfoExtractor();
        this.labelExtractor = new LabelExtractor();
        this.fileFinder = new FileFinder();
    }

    /**
     * Constructor for testing
     */
    public MethodCaseExtractor(MethodInfoExtractor methodInfoExtractor,
                               LabelExtractor labelExtractor,
                               FileFinder fileFinder) {
        this.methodInfoExtractor = methodInfoExtractor;
        this.labelExtractor = labelExtractor;
        this.fileFinder = fileFinder;
    }

    public List<ExporterTestCase> extractTestCases(CompilationUnit cu, String filepath) {
        List<MethodDeclaration> allMethods = cu.findAll(MethodDeclaration.class);

        List<MethodDeclaration> testMethods = allMethods.stream()
                .filter(this::isTestMethod)
                .collect(Collectors.toList());

        return convertDeclarationsToLoaderTestCases(testMethods, filepath);
    }

    private List<ExporterTestCase> convertDeclarationsToLoaderTestCases(
            List<MethodDeclaration> declarations, String filepath) {
        List<ExporterTestCase> cases = new ArrayList<>();
        for (MethodDeclaration method : declarations) {
            try {
                ExporterTestCase testCase = createTestCase(method, filepath);
                cases.add(testCase);
            } catch (Exception e) {
                throw new ExtractionException(
                        "Failed to convert List<MethodDeclaration> to List<ExporterTestCase>", e);
            }
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

            String relativeFilePath = fileFinder.extractRelativeFilePath(filepath);
            testCase.setFile(relativeFilePath);

            return testCase;
        } catch (Exception e) {
            throw new MethodExporterException("Failed to extract test case from: " + method
                    + "and filepath: " + filepath, e);
        }
    }
}
