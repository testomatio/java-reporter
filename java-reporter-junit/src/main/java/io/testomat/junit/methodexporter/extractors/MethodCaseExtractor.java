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

/**
 * Extracts test case information from parsed Java compilation units.
 * This class identifies test methods within AST nodes and converts them
 * to ExporterTestCase objects containing all necessary export metadata.
 */
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
     *
     * @param methodInfoExtractor the method info extractor
     * @param labelExtractor the label extractor
     * @param fileFinder the file finder
     */
    public MethodCaseExtractor(MethodInfoExtractor methodInfoExtractor,
                               LabelExtractor labelExtractor,
                               FileFinder fileFinder) {
        this.methodInfoExtractor = methodInfoExtractor;
        this.labelExtractor = labelExtractor;
        this.fileFinder = fileFinder;
    }

    /**
     * Extracts test cases from a parsed compilation unit.
     * Identifies test methods and converts them to ExporterTestCase objects
     * with complete metadata including names, labels, code bodies, and suite hierarchy.
     *
     * @param cu the parsed compilation unit to extract from
     * @param filepath the source file path for reference
     * @return list of extracted test cases
     */
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
