package io.testomat.junit.methodloader;

import static io.testomat.core.constants.PropertyNameConstants.API_KEY_PROPERTY_NAME;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import io.testomat.core.propertyconfig.impl.PropertyProviderFactoryImpl;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.junit.jupiter.api.extension.ExtensionContext;

public class MethodExportManager {
    private static final ConcurrentHashMap<String, Boolean> processedClasses = new ConcurrentHashMap<>();

    private final PropertyProvider provider =
            PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();
    private final String apiKey = provider.getProperty(API_KEY_PROPERTY_NAME);
    private final LabelExtractor labelExtractor = new LabelExtractor();
    private final PathFinder pathFinder = new PathFinder();
    private final MethodInfoExtractor methodInfoExtractor = new MethodInfoExtractor();
    private final ExportSender exportSender = new ExportSender();
    private final FileParser fileParser = new FileParser();

    public void loadTestBodyIfRequired(final ExtensionContext extensionContext) {
        if (!canLoadTests(extensionContext)) {
            return;
        }

        String className = extensionContext.getRequiredTestClass().getName();
        if (processedClasses.putIfAbsent(className, true) != null) {
            return;
        }

        String filepath = pathFinder.getTestClassFilePath(extensionContext);
        if (filepath == null) {
            return;
        }

        CompilationUnit cu = fileParser.parseFile(filepath);
        if (cu == null) {
            return;
        }

        List<LoaderTestCase> testCases = extractTestCases(cu, filepath);
        if (testCases.isEmpty()) {
            return;
        }

        exportSender.sendLoaderTestCases(testCases);
    }

    private boolean canLoadTests(ExtensionContext extensionContext) {
        return extensionContext != null && apiKey != null && !apiKey.trim().isEmpty();
    }

    private List<LoaderTestCase> extractTestCases(CompilationUnit cu, String filepath) {
        List<MethodDeclaration> testMethods = cu.findAll(MethodDeclaration.class).stream()
                .filter(this::isTestMethod)
                .collect(Collectors.toList());
        return convertDeclarationsToLoaderTestCases(testMethods, filepath);
    }

    private List<LoaderTestCase> convertDeclarationsToLoaderTestCases(
            List<MethodDeclaration> declarations, String filepath) {
        List<LoaderTestCase> cases = new ArrayList<>();
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
                        return "Test".equals(name) ||
                                "ParameterizedTest".equals(name) ||
                                "RepeatedTest".equals(name) ||
                                "TestFactory".equals(name);
                    });
        } catch (Exception e) {
            return false;
        }
    }

    private LoaderTestCase createTestCase(MethodDeclaration method, String filepath) {

        try {
            LoaderTestCase testCase = new LoaderTestCase();
            testCase.setName(methodInfoExtractor.getTestName(method));
            testCase.setCode(methodInfoExtractor.getMethodCode(method));
            testCase.setSkipped(methodInfoExtractor.isTestSkipped(method));
            testCase.setSuites(methodInfoExtractor.extractSuites(method));
            testCase.setLabels(labelExtractor.extractLabels(method));
            testCase.setFile(pathFinder.extractRelativeFilePath(filepath));
            return testCase;
        } catch (Exception e) {
            throw new MethodLoaderException("Failed to extract test case from: " + method
                    + "and filepath: " + filepath, e);
        }
    }
}
