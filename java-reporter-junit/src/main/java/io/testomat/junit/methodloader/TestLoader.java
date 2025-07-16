package io.testomat.junit.methodloader;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.extension.ExtensionContext;

public class TestLoader {
    private static final String BASE_API_PATH = "https://app.testomat.io";
    private static final String LOAD_URL_PART = "/api/load?api_key=";
    private final PathFinder pathFinder = new PathFinder();
    private final RequestBodyBuilder requestBodyBuilder = new RequestBodyBuilder();
    private final PropertyProvider provider =
            PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();

    private final CustomHttpClient client = new NativeHttpClient();

    private String apiKey = provider.getProperty("testomatio.api.key");

    public void loadTestBodyIfRequired(final ExtensionContext extensionContext) {
        // Додаємо перевірки і fallback механізми
        if (extensionContext == null || apiKey == null || apiKey.trim().isEmpty()) {
            return; // Тихо пропускаємо якщо немає необхідних даних
        }

        try {
            String filepath = getTestClassFilePath(extensionContext);
            if (filepath == null || filepath.trim().isEmpty()) {
                return; // Не вдалося отримати шлях до файлу
            }

            CompilationUnit cu;
            try {
                // Додаткова перевірка перед парсингом
                Path filePath = Paths.get(filepath);
                if (!filePath.toFile().exists()) {
                    System.err.println("File does not exist: " + filepath);
                    return;
                }

                cu = StaticJavaParser.parse(filePath);
            } catch (Exception parseException) {
                System.err.println("Failed to parse file " + filepath + ": " + parseException.getMessage());
                return;
            }

            // Отримуємо всі тестові методи з файлу
            List<MethodDeclaration> allTestMethods = cu.findAll(MethodDeclaration.class).stream()
                    .filter(this::isTestMethodSafe)
                    .collect(Collectors.toList());

            if (allTestMethods.isEmpty()) {
                return; // Немає тестових методів для відправки
            }

            // Створюємо список TestCase об'єктів
            List<LoaderTestCase> loaderTestCases = new ArrayList<>();
            for (MethodDeclaration method : allTestMethods) {
                try {
                    LoaderTestCase loaderTestCase = createTestCase(method, cu, filepath);
                    loaderTestCases.add(loaderTestCase);
                } catch (Exception e) {
                    // Пропускаємо проблемні методи, але продовжуємо обробку інших
                    System.err.println("Skipping method due to parsing error: " + e.getMessage());
                }
            }

            if (loaderTestCases.isEmpty()) {
                return;
            }

            // Формуємо JSON payload
            String requestBody = requestBodyBuilder.buildRequestBody(loaderTestCases);

            // Відправляємо на API
            String url = BASE_API_PATH + LOAD_URL_PART + apiKey;
            client.post(url, requestBody, null);

        } catch (Exception e) {
            // Логуємо помилку, але не ломаємо тести
            System.err.println("Failed to load test bodies: " + e.getMessage());
        }
    }

    // Безпечний спосіб отримання шляху до файлу без використання проблемних JUnit utilities
    private String getTestClassFilePath(ExtensionContext extensionContext) {
        try {
            // Спробуємо використати наш PathFinder
            String path = pathFinder.getPath(extensionContext);

            // Нормалізуємо шлях для Windows
            if (path != null) {
                path = path.replace('\\', '/');

                // Перевіряємо чи шлях правильний
                if (Paths.get(path).toFile().exists()) {
                    return path;
                }
            }

        } catch (Exception e1) {
            // Ігноруємо помилку і пробуємо fallback
        }

        try {
            // Fallback: використовуємо reflection для отримання класу
            Class<?> testClass = extensionContext.getRequiredTestClass();
            String className = testClass.getName();

            // Конвертуємо package.ClassName в шлях до файлу
            String relativePath = className.replace('.', '/') + ".java";

            // Шукаємо в стандартних місцях
            String[] possiblePaths = {
                    "src/test/java/" + relativePath,
                    "src/main/java/" + relativePath,
                    "test/" + relativePath,
                    relativePath
            };

            for (String path : possiblePaths) {
                try {
                    if (Paths.get(path).toFile().exists()) {
                        return path;
                    }
                } catch (Exception e) {
                    // Ігноруємо помилки і пробуємо наступний шлях
                }
            }

            // Якщо нічого не знайшли, повертаємо конструктивний шлях
            return "src/test/java/" + relativePath;

        } catch (Exception e2) {
            return "src/test/java/UnknownTest.java";
        }
    }

    // Безпечна перевірка чи є метод тестовим (без використання проблемних JUnit utilities)
    private boolean isTestMethodSafe(MethodDeclaration method) {
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
            return false; // У випадку помилки вважаємо що це не тестовий метод
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

    private LoaderTestCase createTestCase(MethodDeclaration method, CompilationUnit cu, String filepath) {
        LoaderTestCase loaderTestCase = new LoaderTestCase();

        try {
            loaderTestCase.setName(getTestName(method));
        } catch (Exception e) {
            loaderTestCase.setName(method.getNameAsString()); // Fallback до назви методу
        }

        try {
            loaderTestCase.setCode(getMethodCode(method));
        } catch (Exception e) {
            loaderTestCase.setCode(method.toString()); // Fallback до базового toString
        }

        try {
            loaderTestCase.setSkipped(isTestSkipped(method));
        } catch (Exception e) {
            loaderTestCase.setSkipped(false); // За замовчуванням не пропущений
        }

        try {
            loaderTestCase.setLabels(extractLabels(method));
        } catch (Exception e) {
            loaderTestCase.setLabels(new ArrayList<>()); // Порожній список лейблів
        }

        try {
            loaderTestCase.setSuites(extractSuites(method, cu));
        } catch (Exception e) {
            // Fallback: використовуємо тільки назву класу
            method.findAncestor(ClassOrInterfaceDeclaration.class)
                    .ifPresent(clazz -> {
                        List<String> fallbackSuites = new ArrayList<>();
                        fallbackSuites.add(clazz.getNameAsString());
                        loaderTestCase.setSuites(fallbackSuites);
                    });
            if (loaderTestCase.getSuites() == null) {
                loaderTestCase.setSuites(new ArrayList<>());
            }
        }

        try {
            loaderTestCase.setFile(extractRelativeFilePath(filepath));
        } catch (Exception e) {
            loaderTestCase.setFile("unknown"); // Fallback значення
        }

        return loaderTestCase;
    }

    private String extractRelativeFilePath(String filepath) {
        try {
            // Нормалізуємо шлях для Windows
            String normalizedPath = filepath.replace('\\', '/');

            // Видаляємо диск Windows (C:, D:, etc.)
            if (normalizedPath.length() > 2 && normalizedPath.charAt(1) == ':') {
                normalizedPath = normalizedPath.substring(2);
            }

            // Видаляємо початковий слеш якщо є
            if (normalizedPath.startsWith("/")) {
                normalizedPath = normalizedPath.substring(1);
            }

            // Беремо частину після src/
            int srcIndex = normalizedPath.indexOf("src/");
            if (srcIndex != -1) {
                return normalizedPath.substring(srcIndex);
            }

            // Fallback: повертаємо нормалізований шлях
            return normalizedPath;

        } catch (Exception e) {
            // Якщо все не вдалося, повертаємо безпечне значення
            return "src/test/java/UnknownFile.java";
        }
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

        // Будуємо повну сигнатуру методу вручну
        // Модифікатори доступу
        method.getModifiers().forEach(modifier -> {
            code.append(modifier.getKeyword().asString()).append(" ");
        });

        // Тип повернення
        code.append(method.getTypeAsString()).append(" ");

        // Ім'я методу
        code.append(method.getNameAsString());

        // Параметри
        code.append("(");
        if (!method.getParameters().isEmpty()) {
            for (int i = 0; i < method.getParameters().size(); i++) {
                if (i > 0) code.append(", ");
                code.append(method.getParameter(i).toString());
            }
        }
        code.append(")");

        // КРИТИЧНО: додаємо throws exceptions
        if (!method.getThrownExceptions().isEmpty()) {
            code.append(" throws ");
            for (int i = 0; i < method.getThrownExceptions().size(); i++) {
                if (i > 0) code.append(", ");
                code.append(method.getThrownException(i).toString());
            }
        }

        // Тіло методу
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

    // Простий валідатор JSON
    private boolean isValidJson(String json) {
        try {
            if (json == null || json.trim().isEmpty()) {
                return false;
            }

            // Перевіряємо базову структуру
            json = json.trim();
            if (!json.startsWith("{") || !json.endsWith("}")) {
                return false;
            }

            // Перевіряємо чи немає неекранованих лапок
            int braceCount = 0;
            boolean inString = false;
            boolean escaped = false;

            for (int i = 0; i < json.length(); i++) {
                char c = json.charAt(i);

                if (escaped) {
                    escaped = false;
                    continue;
                }

                if (c == '\\') {
                    escaped = true;
                    continue;
                }

                if (c == '"') {
                    inString = !inString;
                    continue;
                }

                if (!inString) {
                    if (c == '{') braceCount++;
                    else if (c == '}') braceCount--;
                }
            }

            return braceCount == 0 && !inString;

        } catch (Exception e) {
            return false;
        }
    }
}
