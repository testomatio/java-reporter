package io.testomat.junit.methodloader;

import static io.testomat.core.constants.PropertyNameConstants.API_KEY_PROPERTY_NAME;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
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

public class MethodBodyExporter {
    private static final String LOAD_URL = "https://app.testomat.io/api/load?api_key=";
    private static final ConcurrentHashMap<String, Boolean> processedClasses = new ConcurrentHashMap<>();

    private final PropertyProvider provider = PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();
    private final String apiKey = provider.getProperty(API_KEY_PROPERTY_NAME);
    private final Object lock = new Object();
    private final LabelExtractor labelExtractor = new LabelExtractor();

    public void loadTestBodyIfRequired(final ExtensionContext extensionContext) {
        if (!canLoadTests(extensionContext)) {
            return;
        }

        String className = extensionContext.getRequiredTestClass().getName();
        if (processedClasses.putIfAbsent(className, true) != null) {
            return;
        }

        try {
            String filepath = getTestClassFilePath(extensionContext);
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

    private String getTestClassFilePath(ExtensionContext extensionContext) {
        try {
            PathFinder pathFinder = new PathFinder();
            String path = pathFinder.getPath(extensionContext);
            if (path != null) {
                path = path.replace('\\', '/');
                if (Paths.get(path).toFile().exists()) {
                    return path;
                }
            }
        } catch (Exception ignored) {
        }

        return findTestFileByClassName(extensionContext);
    }

    private String findTestFileByClassName(ExtensionContext extensionContext) {
        try {
            Class<?> testClass = extensionContext.getRequiredTestClass();
            String relativePath = testClass.getName().replace('.', '/') + ".java";

            String[] possiblePaths = {
                    "src/test/java/" + relativePath,
                    "test/" + relativePath,
                    relativePath
            };

            for (String path : possiblePaths) {
                if (Paths.get(path).toFile().exists()) {
                    return path;
                }
            }

            return "src/test/java/" + relativePath;
        } catch (Exception e) {
            return "src/test/java/UnknownTest.java";
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
        testCase.setName(getTestName(method));
        testCase.setCode(getMethodCode(method));
        testCase.setSkipped(isTestSkipped(method));
        testCase.setLabels(labelExtractor.extractLabels(method));
        testCase.setSuites(extractSuites(method));
        testCase.setFile(extractRelativeFilePath(filepath));
        return testCase;
    }

    private String getTestName(MethodDeclaration method) {
        try {
            String displayName = method.getAnnotations().stream()
                    .filter(ann -> ann.getNameAsString().equals("DisplayName"))
                    .findFirst()
                    .map(this::getAnnotationValue)
                    .orElse(null);
            return displayName != null ? displayName : method.getNameAsString();
        } catch (Exception e) {
            return method.getNameAsString();
        }
    }

    private String getMethodCode(MethodDeclaration method) {
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
                    if (i > 0) code.append(", ");
                    code.append(method.getParameter(i).toString());
                }
            }
            code.append(")");

            if (!method.getThrownExceptions().isEmpty()) {
                code.append(" throws ");
                for (int i = 0; i < method.getThrownExceptions().size(); i++) {
                    if (i > 0) code.append(", ");
                    code.append(method.getThrownException(i).toString());
                }
            }

            method.getBody().ifPresent(body ->
                    code.append(" ").append(body.toString()));

            return code.toString();
        } catch (Exception e) {
            return method.toString();
        }
    }

    private boolean isTestSkipped(MethodDeclaration method) {
        try {
            return method.getAnnotations().stream()
                    .anyMatch(ann ->
                            ann.getNameAsString().equals("Disabled") ||
                                    ann.getNameAsString().equals("Ignore")) ||
                    method.getNameAsString().startsWith("ignore") ||
                    method.getNameAsString().startsWith("skip");
        } catch (Exception e) {
            return false;
        }
    }

    private List<String> extractSuites(MethodDeclaration testMethod) {
        try {
            List<String> suites = new ArrayList<>();
            List<ClassOrInterfaceDeclaration> classHierarchy = new ArrayList<>();

            ClassOrInterfaceDeclaration currentClass =
                    testMethod.findAncestor(ClassOrInterfaceDeclaration.class).orElse(null);

            while (currentClass != null) {
                classHierarchy.add(0, currentClass);
                currentClass = currentClass.findAncestor(ClassOrInterfaceDeclaration.class).orElse(null);
            }

            for (ClassOrInterfaceDeclaration clazz : classHierarchy) {
                String suiteName = clazz.getAnnotationByName("DisplayName")
                        .map(this::getAnnotationValue)
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

    private String getAnnotationValue(AnnotationExpr annotation) {
        if (annotation instanceof SingleMemberAnnotationExpr) {
            return ((SingleMemberAnnotationExpr) annotation)
                    .getMemberValue()
                    .asStringLiteralExpr()
                    .getValue();
        } else if (annotation instanceof NormalAnnotationExpr) {
            return ((NormalAnnotationExpr) annotation)
                    .getPairs().stream()
                    .filter(pair -> "value".equals(pair.getNameAsString()))
                    .findFirst()
                    .map(pair -> pair.getValue().asStringLiteralExpr().getValue())
                    .orElse(null);
        }
        return null;
    }
}
