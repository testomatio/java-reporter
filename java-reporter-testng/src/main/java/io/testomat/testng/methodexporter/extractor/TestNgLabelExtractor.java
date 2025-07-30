package io.testomat.testng.methodexporter.extractor;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestNgLabelExtractor {
    private static final String COMMENT_LABEL_PATTERN = "@(\\w+)(?::(\\w+))?|#(\\w+)";

    /**
     * Extracts labels from test method annotations, comments and name patterns.
     */
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
                    extractTestAnnotationAttributes(annotation, labels);
                    break;
                case "DataProvider":
                    labels.add("data-provider");
                    break;
                case "Factory":
                    labels.add("factory");
                    break;
                case "BeforeMethod":
                case "AfterMethod":
                case "BeforeClass":
                case "AfterClass":
                case "BeforeSuite":
                case "AfterSuite":
                case "BeforeTest":
                case "AfterTest":
                    labels.add("setup");
                    break;
                case "Parameters":
                    labels.add("parameterized");
                    break;
                case "Ignore":
                    labels.add("disabled");
                    break;
                default:
                    if (annName.endsWith("Test") && !annName.equals("Test")) {
                        labels.add(annName.toLowerCase().replace("test", ""));
                    }
                    break;
            }
        }
    }

    private void extractTestAnnotationAttributes(AnnotationExpr annotation, List<String> labels) {
        if (annotation instanceof NormalAnnotationExpr) {
            NormalAnnotationExpr normalAnnotation = (NormalAnnotationExpr) annotation;
            normalAnnotation.getPairs().forEach(pair -> {
                String name = pair.getNameAsString();
                switch (name) {
                    case "groups":
                        String groupsValue = pair.getValue().toString();
                        extractGroupsFromValue(groupsValue, labels);
                        break;
                    case "priority":
                        labels.add("priority");
                        break;
                    case "enabled":
                        String enabledValue = pair.getValue().toString();
                        if ("false".equals(enabledValue)) {
                            labels.add("disabled");
                        }
                        break;
                    case "dependsOnMethods":
                        labels.add("dependent");
                        break;
                    case "timeOut":
                        labels.add("timeout");
                        break;
                    default:
                        break;
                }
            });
        }
    }

    private void extractGroupsFromValue(String groupsValue, List<String> labels) {
        String cleaned = groupsValue.replaceAll("[\"{}\\s]", "");
        if (!cleaned.isEmpty()) {
            Arrays.stream(cleaned.split(","))
                    .forEach(group -> labels.add("group:" + group));
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
        if (methodName.contains("integration")) {
            labels.add("integration");
        }
        if (methodName.contains("smoke")) {
            labels.add("smoke");
        }
        if (methodName.contains("performance")) {
            labels.add("performance");
        }
        if (methodName.contains("regression")) {
            labels.add("regression");
        }
    }
}
