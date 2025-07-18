package io.testomat.junit.methodloader;

import static io.testomat.core.constants.PropertyNameConstants.API_KEY_PROPERTY_NAME;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import io.testomat.core.client.http.CustomHttpClient;
import io.testomat.core.client.http.NativeHttpClient;
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
    private static final String LOAD_URL = "https://app.testomat.io/api/load?api_key=";
    private static final ConcurrentHashMap<String, Boolean> processedClasses = new ConcurrentHashMap<>();

    private final PropertyProvider provider =
            PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();
    private final String apiKey = provider.getProperty(API_KEY_PROPERTY_NAME);
    private final Object lock = new Object();
    private final LabelExtractor labelExtractor = new LabelExtractor();
    private final PathFinder pathFinder = new PathFinder();
    private final MethodInfoExtractor methodInfoExtractor = new MethodInfoExtractor();

    public void loadTestBodyIfRequired(final ExtensionContext extensionContext) {
        if (!canLoadTests(extensionContext)) {
            return;
        }

        String className = extensionContext.getRequiredTestClass().getName();
        if (processedClasses.putIfAbsent(className, true) != null) {
            return;
        }

        try {
            String filepath = pathFinder.getTestClassFilePath(extensionContext);
            if (filepath == null) {
                return;
            }

            CompilationUnit cu = parseFile(filepath);
            if (cu == null) {
                return;
            }

            List<LoaderTestCase> testCases = extractTestCases(cu, filepath);
            if (testCases.isEmpty()) {
                return;
            }

            submitTestCases(testCases);
        } catch (Exception ignored) {
        }
    }

    private boolean canLoadTests(ExtensionContext extensionContext) {
        return extensionContext != null && apiKey != null && !apiKey.trim().isEmpty();
    }

    private CompilationUnit parseFile(String filepath) {
        try {
            Path filePath = Paths.get(filepath);
            if (!filePath.toFile().exists()) {
                return null;
            }
            synchronized (lock) {
                return StaticJavaParser.parse(filePath);
            }
        } catch (Exception e) {
            return null;
        }
    }

    private List<LoaderTestCase> extractTestCases(CompilationUnit cu, String filepath) {
        List<MethodDeclaration> testMethods = cu.findAll(MethodDeclaration.class).stream()
                .filter(this::isTestMethod)
                .collect(Collectors.toList());

        List<LoaderTestCase> testCases = new ArrayList<>();
        for (MethodDeclaration method : testMethods) {
            try {
                testCases.add(createTestCase(method, filepath));
            } catch (Exception ignored) {
            }
        }
        return testCases;
    }

    private void submitTestCases(List<LoaderTestCase> testCases) {
        RequestBodyBuilder requestBodyBuilder = new RequestBodyBuilder();
        CustomHttpClient client = new NativeHttpClient();
        String requestBody = requestBodyBuilder.buildRequestBody(testCases);
        String url = LOAD_URL + apiKey;

        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                if (attempt > 1) {
                    Thread.sleep(1500);
                }
                client.post(url, requestBody, null);
                return;
            } catch (Exception e) {
                if (!e.getMessage().contains("422") || attempt >= 2) {
                    break;
                }
            }
        }
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
        LoaderTestCase testCase = new LoaderTestCase();
        testCase.setName(methodInfoExtractor.getTestName(method));
        testCase.setCode(methodInfoExtractor.getMethodCode(method));
        testCase.setSkipped(methodInfoExtractor.isTestSkipped(method));
        testCase.setSuites(methodInfoExtractor.extractSuites(method));
        testCase.setLabels(labelExtractor.extractLabels(method));
        testCase.setFile(extractRelativeFilePath(filepath));
        return testCase;
    }

    private String extractRelativeFilePath(String filepath) {
        try {
            String normalizedPath = filepath.replace('\\', '/');

            if (normalizedPath.length() > 2 && normalizedPath.charAt(1) == ':') {
                normalizedPath = normalizedPath.substring(2);
            }

            if (normalizedPath.startsWith("/")) {
                normalizedPath = normalizedPath.substring(1);
            }

            int srcIndex = normalizedPath.indexOf("src/");
            if (srcIndex != -1) {
                return normalizedPath.substring(srcIndex);
            }

            return normalizedPath.isEmpty() ? "src/test/java/UnknownFile.java" : normalizedPath;
        } catch (Exception e) {
            return "unknown";
        }
    }
}
