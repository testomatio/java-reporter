package io.testomat.methodloader.junit;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import io.testomat.core.client.http.CustomHttpClient;
import io.testomat.core.client.http.NativeHttpClient;
import io.testomat.core.propertyconfig.impl.PropertyProviderFactoryImpl;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.extension.ExtensionContext;

public class TestBodyLoader {
    private static final String BASE_API_PATH = "https://api.testomat.io";
    private static final String LOAD_URL_PART = "/api/load?api_key=";
    private final PathFinder pathFinder = new PathFinder();
    private final MethodBodyParser methodBodyParser = new MethodBodyParser();
    private final RequestBodyBuilder requestBodyBuilder = new RequestBodyBuilder();
    private final PropertyProvider provider =
            PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();

    private final CustomHttpClient client = new NativeHttpClient();

    private String apiKey = provider.getProperty("testomatio.api.key");

    public void loadTestBodyIfRequired(final ExtensionContext extensionContext) {
        String filepath = pathFinder.getPath(extensionContext);
        CompilationUnit cu;
        try {
            cu = StaticJavaParser.parse(Paths.get(filepath));
        } catch (IOException e) {
            throw new ParsingException("Could not parse file " + filepath, e);
        }

        // Отримуємо всі тестові методи з файлу (включно з @Nested класами)
        List<MethodDeclaration> allTestMethods = cu.findAll(MethodDeclaration.class).stream()
                .filter(this::isTestMethod)
                .collect(Collectors.toList());

        if (allTestMethods.isEmpty()) {
            return; // Немає тестових методів для відправки
        }

        // Створюємо список TestCase об'єктів
        List<TestCase> testCases = new ArrayList<>();
        for (MethodDeclaration method : allTestMethods) {
            TestCase testCase = createTestCase(method, cu, filepath);
            testCases.add(testCase);
        }

        // Формуємо JSON payload
        String requestBody = requestBodyBuilder.buildRequestBody(testCases);

        // Відправляємо на API
        String url = BASE_API_PATH + LOAD_URL_PART + apiKey;
        try {
            client.post(url, requestBody, null);
        } catch (IOException e) {
            throw new RuntimeException("failed to load methods", e);
        }
    }

    private boolean isTestMethod(MethodDeclaration method) {
        return method.getAnnotations().stream()
                .anyMatch(ann -> {
                    String name = ann.getNameAsString();
                    return "Test".equals(name) ||
                            "ParameterizedTest".equals(name) ||
                            "RepeatedTest".equals(name) ||
                            "TestFactory".equals(name);
                });
    }

    private TestCase createTestCase(MethodDeclaration method, CompilationUnit cu, String filepath) {
        TestCase testCase = new TestCase();

        testCase.setName(getTestName(method));
        testCase.setCode(getMethodCode(method));
        testCase.setSkipped(isTestSkipped(method));
        testCase.setLabels(extractLabels(method));
        testCase.setSuites(extractSuites(method, cu));
        testCase.setFile(extractRelativeFilePath(filepath));

        return testCase;
    }

    private String extractRelativeFilePath(String filepath) {
        // Спрощено: беремо частину після src/
        int srcIndex = filepath.indexOf("src/");
        if (srcIndex != -1) {
            return filepath.substring(srcIndex).replace('\\', '/');
        }
        return filepath.replace('\\', '/');
    }

    private String getTestName(MethodDeclaration methodDeclaration) {
        String displayName = methodDeclaration.getAnnotations().stream()
                .filter(ann -> ann.getNameAsString().equals("DisplayName"))
                .findFirst()
                .map(this::getAnnotationValue)
                .orElse(null);

        return displayName != null ? displayName : methodDeclaration.getNameAsString();
    }

    private String getMethodCode(MethodDeclaration method) {
        StringBuilder code = new StringBuilder();

        // Додаємо анотації
        method.getAnnotations().forEach(annotation -> {
            code.append(annotation.toString()).append("\n");
        });

        // Додаємо сигнатуру та тіло методу
        code.append(method.getDeclarationAsString(false, false, true));
        method.getBody().ifPresent(body -> {
            code.append(" ").append(body.toString());
        });

        return code.toString();
    }

    public String getMethodCode(List<MethodDeclaration> methods, String methodName) {
        return methods.stream()
                .filter(methodDeclaration
                        -> methodDeclaration.getNameAsString().equalsIgnoreCase(methodName))
                .map(this::getMethodCode)
                .findFirst()
                .orElseThrow(() -> new ParsingException(
                        "Failed to get method body for method name: " + methodName));
    }

    public List<String> extractSuites(MethodDeclaration testMethod, CompilationUnit cu) {
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
    }

    public boolean isTestSkipped(MethodDeclaration method) {
        return method.getAnnotations().stream()
                .anyMatch(ann ->
                        ann.getNameAsString().equals("Disabled") ||
                                ann.getNameAsString().equals("Ignore") ||
                                method.getNameAsString().startsWith("ignore") ||
                                method.getNameAsString().startsWith("skip")
                );
    }

    public List<String> extractLabels(MethodDeclaration testMethod) {
        List<String> labels = new ArrayList<>();

        for (AnnotationExpr annotation : testMethod.getAnnotations()) {
            String annName = annotation.getNameAsString();

            switch (annName) {
                case "Test":
                    labels.add("unit");
                    break;
                case "IntegrationTest":
                    labels.add("integration");
                    break;
                case "ParameterizedTest":
                    labels.add("parameterized");
                    break;
                case "Disabled":
                    labels.add("disabled");
                    break;
                case "Tag":
                    String tagValue = getAnnotationValue(annotation);
                    if (tagValue != null) {
                        labels.add(tagValue);
                    }
                    break;
            }
        }

        testMethod.getComment().ifPresent(comment -> {
            String text = comment.getContent();

            Pattern pattern = Pattern.compile("@(\\w+)(?::(\\w+))?|#(\\w+)");
            Matcher matcher = pattern.matcher(text);

            while (matcher.find()) {
                if (matcher.group(3) != null) {
                    labels.add(matcher.group(3));
                } else {
                    String tag = matcher.group(1);
                    String value = matcher.group(2);
                    labels.add(value != null ? tag + ":" + value : tag);
                }
            }
        });

        String methodName = testMethod.getNameAsString().toLowerCase();
        if (methodName.contains("integration")) labels.add("integration");
        if (methodName.contains("smoke")) labels.add("smoke");
        if (methodName.contains("performance")) labels.add("performance");

        return labels;
    }

    private String getAnnotationValue(AnnotationExpr annotation) {
        if (annotation instanceof SingleMemberAnnotationExpr) {
            return ((SingleMemberAnnotationExpr) annotation)
                    .getMemberValue()
                    .asStringLiteralExpr()
                    .getValue();
        }
        else if (annotation instanceof NormalAnnotationExpr) {
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