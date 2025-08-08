package io.testomat.junit.methodexporter.extractors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import io.testomat.junit.exception.ExtractionException;
import io.testomat.junit.exception.MethodExporterException;
import io.testomat.junit.methodexporter.filefinder.FileFinder;
import io.testomat.junit.model.ExporterTestCase;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodCaseExtractor {
    private static final Logger log = LoggerFactory.getLogger(MethodCaseExtractor.class);

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

    /**
     * Extracts ExporterTestCases from compilationUint
     */
    public List<ExporterTestCase> extractTestCases(CompilationUnit cu, String filepath) {
        List<MethodDeclaration> allMethods = cu.findAll(MethodDeclaration.class);
        log.debug("Found {} total methods in file", allMethods.size());

        List<MethodDeclaration> testMethods = allMethods.stream()
                .filter(this::isTestMethod)
                .collect(Collectors.toList());

        log.debug("Found {} test methods after filtering", testMethods.size());

        return convertDeclarationsToLoaderTestCases(testMethods, filepath);
    }

    private List<ExporterTestCase> convertDeclarationsToLoaderTestCases(
            List<MethodDeclaration> declarations, String filepath) {
        List<ExporterTestCase> cases = new ArrayList<>();
        for (MethodDeclaration method : declarations) {
            try {
                log.debug("Converting method: {}", method.getNameAsString());
                ExporterTestCase testCase = createTestCase(method, filepath);
                cases.add(testCase);
                log.debug("Successfully converted method: {}", method.getNameAsString());
            } catch (Exception e) {
                log.error("Failed to convert method {}: {}",
                        method.getNameAsString(), e.getMessage(), e);
                throw new ExtractionException(
                        "Failed to convert List<MethodDeclaration> to List<ExporterTestCase>", e);
            }
        }
        return cases;
    }

    private boolean isTestMethod(MethodDeclaration method) {
        try {
            boolean isTest = method.getAnnotations().stream()
                    .anyMatch(ann -> {
                        String name = ann.getNameAsString();
                        return "Test".equals(name)
                                || "ParameterizedTest".equals(name)
                                || "RepeatedTest".equals(name)
                                || "TestFactory".equals(name);
                    });

            if (isTest) {
                log.debug("Method {} is a test method", method.getNameAsString());
            } else {
                log.debug("Method {} is NOT a test method - skipping export",
                        method.getNameAsString());
            }

            return isTest;
        } catch (Exception e) {
            log.error("Error checking if method {} is test method: {}",
                    method.getNameAsString(), e.getMessage());
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
            testCase.setFile(fileFinder.extractRelativeFilePath(filepath));

            return testCase;
        } catch (Exception e) {
            throw new MethodExporterException("Failed to extract test case from: " + method
                    + "and filepath: " + filepath, e);
        }
    }
}
