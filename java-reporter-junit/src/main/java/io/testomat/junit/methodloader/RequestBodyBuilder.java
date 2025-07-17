package io.testomat.junit.methodloader;

import java.util.List;

/**
 * Builds JSON request bodies for test case submissions to the testomat.io service.
 */
public class RequestBodyBuilder {
    private static final String FRAMEWORK_NAME = "junit";
    public static final String LANGUAGE_NAME = "java";
    public static final Boolean NO_EMPTY_FLAG = true;
    public static final Boolean NO_DETACH_FLAG = true;
    public static final Boolean STRUCTURE_FLAG = true;
    public static final Boolean SYNC_FLAG = true;

    /**
     * Builds a JSON request body from a list of test cases.
     *
     * @param loaderTestCases the test cases to include in the request
     * @return JSON string representing the request body
     */
    public String buildRequestBody(List<LoaderTestCase> loaderTestCases) {
        StringBuilder json = new StringBuilder();

        json.append("{\n")
                .append("  \"framework\": \"").append(FRAMEWORK_NAME).append("\",\n")
                .append("  \"language\": \"").append(LANGUAGE_NAME).append("\",\n")
                .append("  \"noempty\": ").append(NO_EMPTY_FLAG).append(",\n")
                .append("  \"no-detach\": ").append(NO_DETACH_FLAG).append(",\n")
                .append("  \"structure\": ").append(STRUCTURE_FLAG).append(",\n")
                .append("  \"sync\": ").append(SYNC_FLAG).append(",\n")
                .append("  \"tests\": [\n");

        for (int i = 0; i < loaderTestCases.size(); i++) {
            LoaderTestCase testCase = loaderTestCases.get(i);
            appendTestCase(json, testCase);

            if (i < loaderTestCases.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("  ]\n").append("}");
        return json.toString();
    }



    private void appendTestCase(StringBuilder json, LoaderTestCase testCase) {
        json.append("    {\n")
                .append("      \"name\": \"").append(escapeJson(testCase.getName())).append("\",\n")
                .append("      \"suites\": ").append(formatStringArray(testCase.getSuites())).append(",\n")
                .append("      \"code\": \"").append(escapeJson(testCase.getCode())).append("\",\n")
                .append("      \"file\": \"").append(escapeJson(testCase.getFile())).append("\",\n")
                .append("      \"skipped\": ").append(testCase.isSkipped()).append(",\n")
                .append("      \"labels\": ").append(formatStringArray(testCase.getLabels())).append("\n")
                .append("    }");
    }

    private String formatStringArray(List<String> strings) {
        if (strings == null || strings.isEmpty()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < strings.size(); i++) {
            sb.append("\"").append(escapeJson(strings.get(i))).append("\"");
            if (i < strings.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.append("]").toString();
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        String normalized = value.replace("\r\n", "\n").replace("\r", "\n");

        return normalized.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\u0000", "\\u0000")
                .replace("\u0001", "\\u0001")
                .replace("\u0002", "\\u0002")
                .replace("\u0003", "\\u0003")
                .replace("\u0004", "\\u0004")
                .replace("\u0005", "\\u0005")
                .replace("\u0006", "\\u0006")
                .replace("\u0007", "\\u0007");
    }
}
