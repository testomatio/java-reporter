package io.testomat.testng.methodexporter.extractor;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import io.testomat.testng.exception.MethodExporterException;
import io.testomat.testng.methodexporter.model.TestNgExporterTestCase;
import io.testomat.testng.methodexporter.pathfinder.TestNgFileFinder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts TestNG test cases from parsed source code compilation units.
 * Identifies test methods, extracts their code, and builds test case objects.
 */
public class TestNgMethodCaseExtractor {
    private static final Logger log = LoggerFactory.getLogger(TestNgMethodCaseExtractor.class);

    private final TestNgMethodInfoExtractor methodInfoExtractor;
    private final TestNgLabelExtractor labelExtractor;
    private final TestNgFileFinder fileFinder;

    public TestNgMethodCaseExtractor() {
        this.methodInfoExtractor = new TestNgMethodInfoExtractor();
        this.labelExtractor = new TestNgLabelExtractor();
        this.fileFinder = new TestNgFileFinder();
    }

    /**
     * Constructor for testing
     */
    public TestNgMethodCaseExtractor(TestNgMethodInfoExtractor methodInfoExtractor,
                                     TestNgLabelExtractor labelExtractor,
                                     TestNgFileFinder fileFinder) {
        this.methodInfoExtractor = methodInfoExtractor;
        this.labelExtractor = labelExtractor;
        this.fileFinder = fileFinder;
    }

    /**
     * Extracts test cases from compilation unit and file path.
     */
    public List<TestNgExporterTestCase> extractTestCases(CompilationUnit cu, String filepath) {
        log.debug("Extracting test cases from file: {}", filepath);

        List<MethodDeclaration> allMethods = cu.findAll(MethodDeclaration.class);
        log.debug("Found {} total methods in file", allMethods.size());

        List<MethodDeclaration> testMethods = allMethods.stream()
                .filter(this::isTestMethod)
                .collect(Collectors.toList());

        log.debug("Found {} test methods after filtering", testMethods.size());

        List<TestNgExporterTestCase> result = convertDeclarationsToTestCases(testMethods, filepath);
        log.debug("Converted to {} test cases", result.size());

        return result;
    }

    private List<TestNgExporterTestCase> convertDeclarationsToTestCases(
            List<MethodDeclaration> declarations, String filepath) {
        List<TestNgExporterTestCase> cases = new ArrayList<>();
        for (MethodDeclaration method : declarations) {
            try {
                log.debug("Converting method: {}", method.getNameAsString());
                TestNgExporterTestCase testCase = createTestCase(method, filepath);
                cases.add(testCase);
                log.debug("Successfully converted method: {}", method.getNameAsString());
            } catch (Exception e) {
                log.error("Failed to convert method {}: {}",
                        method.getNameAsString(), e.getMessage(), e);
                throw new MethodExporterException(
                        "Failed to convert List<MethodDeclaration> to List<TestNgExporterTestCase>",
                        e);
            }
        }
        return cases;
    }

    private boolean isTestMethod(MethodDeclaration method) {
        try {
            boolean isTest = method.getAnnotations().stream()
                    .anyMatch(ann -> {
                        String name = ann.getNameAsString();
                        return "Test".equals(name);
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

    private TestNgExporterTestCase createTestCase(MethodDeclaration method, String filepath) {
        try {
            TestNgExporterTestCase testCase = new TestNgExporterTestCase();

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

            log.debug("Created test case: name={}, skipped={}, suites={}, labels={}, file={}",
                    testName, isSkipped, suites.size(), labels.size(), relativeFilePath);

            return testCase;
        } catch (Exception e) {
            throw new MethodExporterException("Failed to extract test case from: " + method
                    + " and filepath: " + filepath, e);
        }
    }
}
