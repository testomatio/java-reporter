package io.testomat.junit.methodloader;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LabelExtractor {
    private static final String COMMENT_LABEL_PATTERN = "@(\\w+)(?::(\\w+))?|#(\\w+)";
    
    public List<String> extractLabels(MethodDeclaration testMethod) {
        try {
            List<String> labels = new ArrayList<>();
            extractAnnotationLabels(testMethod, labels);
            extractCommentLabels(testMethod, labels);
            extractNamePatternLabels(testMethod, labels);
            return labels;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void extractAnnotationLabels(MethodDeclaration testMethod, List<String> labels) {
        for (AnnotationExpr annotation : testMethod.getAnnotations()) {
            String annName = annotation.getNameAsString();

            switch (annName) {
                case "Test":
                    labels.add("unit");
                    break;
                case "IntegrationTest":
                case "SpringBootTest":
                    labels.add("integration");
                    break;
                case "ParameterizedTest":
                    labels.add("parameterized");
                    break;
                case "RepeatedTest":
                    labels.add("repeated");
                    break;
                case "TestFactory":
                    labels.add("dynamic");
                    break;
                case "Disabled":
                case "Ignore":
                    labels.add("disabled");
                    break;
                case "Timeout":
                    labels.add("timeout");
                    break;
                case "WebMvcTest":
                    labels.add("web");
                    break;
                case "DataJpaTest":
                    labels.add("jpa");
                    break;
                case "JsonTest":
                    labels.add("json");
                    break;
                case "Tag":
                    String tagValue = getAnnotationValue(annotation);
                    if (tagValue != null) {
                        labels.add(tagValue);
                    }
                    break;
                default:
                    if (annName.endsWith("Test") && !annName.equals("Test")) {
                        labels.add(annName.toLowerCase().replace("test", ""));
                    }
                    break;
            }
        }
    }

    private void extractCommentLabels(MethodDeclaration testMethod, List<String> labels) {
        testMethod.getComment().ifPresent(comment -> {
            String text = comment.getContent();
            Pattern pattern = Pattern.compile(COMMENT_LABEL_PATTERN);
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
    }

    private void extractNamePatternLabels(MethodDeclaration testMethod, List<String> labels) {
        String methodName = testMethod.getNameAsString().toLowerCase();
        if (methodName.contains("integration")) labels.add("integration");
        if (methodName.contains("smoke")) labels.add("smoke");
        if (methodName.contains("performance")) labels.add("performance");
    }

    public String getAnnotationValue(AnnotationExpr annotation) {
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