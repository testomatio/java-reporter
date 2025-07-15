package io.testomat.methodloader.junit;

import com.github.javaparser.ast.body.MethodDeclaration;
import java.util.List;

public class RequestBodyBuilder {
    private static final String FRAMEWORK_NAME = "junit";
    public static final String LANGUAGE_NAME = "java";
    public static final Boolean NO_EMPTY_FLAG = true;
    public static final Boolean NO_DETACH_FLAG = false;
    public static final Boolean STRUCTURE_FLAG = true;
    public static final Boolean SYNC_FLAG = false;

    public String buildRequestBody(List<TestCase> testCases) {
        StringBuilder json = new StringBuilder();
        
        json.append("{\n");
        json.append("  \"framework\": \"").append(FRAMEWORK_NAME).append("\",\n");
        json.append("  \"language\": \"").append(LANGUAGE_NAME).append("\",\n");
        json.append("  \"noempty\": ").append(NO_EMPTY_FLAG).append(",\n");
        json.append("  \"no-detach\": ").append(NO_DETACH_FLAG).append(",\n");
        json.append("  \"structure\": ").append(STRUCTURE_FLAG).append(",\n");
        json.append("  \"sync\": ").append(SYNC_FLAG).append(",\n");
        json.append("  \"tests\": [\n");
        
        for (int i = 0; i < testCases.size(); i++) {
            TestCase testCase = testCases.get(i);
            json.append("    {\n");
            json.append("      \"name\": \"").append(escapeJson(testCase.getName())).append("\",\n");
            json.append("      \"suites\": ").append(formatStringArray(testCase.getSuites())).append(",\n");
            json.append("      \"code\": \"").append(escapeJson(testCase.getCode())).append("\",\n");
            json.append("      \"file\": \"").append(escapeJson(testCase.getFile())).append("\",\n");
            json.append("      \"skipped\": ").append(testCase.isSkipped()).append(",\n");
            json.append("      \"labels\": ").append(formatStringArray(testCase.getLabels())).append("\n");
            json.append("    }");
            
            if (i < testCases.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        
        json.append("  ]\n");
        json.append("}");
        
        return json.toString();
    }

    @Deprecated
    public String composeMethodBodiesPayload(List<MethodDeclaration> methodDeclarations) {
        StringBuilder stringBuilder = new StringBuilder();
        for (MethodDeclaration methodDeclaration : methodDeclarations) {
            stringBuilder.append(methodDeclaration.removeComment().toString());
        }
        return stringBuilder.toString();
    }
    
    private String formatStringArray(List<String> strings) {
        if (strings == null || strings.isEmpty()) {
            return "[]";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < strings.size(); i++) {
            sb.append("\"").append(escapeJson(strings.get(i))).append("\"");
            if (i < strings.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
    
    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}
