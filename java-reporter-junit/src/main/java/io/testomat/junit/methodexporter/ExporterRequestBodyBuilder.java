package io.testomat.junit.methodexporter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.testomat.junit.model.ExporterTestCase;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Builds JSON request bodies for test case submissions to the testomat.io service.
 */
public class ExporterRequestBodyBuilder {
    private static final String FRAMEWORK_NAME = "junit";
    private static final String LANGUAGE_NAME = "java";
    private static final Boolean NO_EMPTY_FLAG = true;
    private static final Boolean NO_DETACH_FLAG = true;
    private static final Boolean STRUCTURE_FLAG = true;
    private static final Boolean SYNC_FLAG = true;

    private final ObjectMapper objectMapper;

    public ExporterRequestBodyBuilder() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Builds a JSON request body from a list of test cases.
     *
     * @param exporterTestCases the test cases to include in the request
     * @return JSON string representing the request body
     * @throws RuntimeException if JSON serialization fails
     */
    public String buildRequestBody(List<ExporterTestCase> exporterTestCases) {
        List<ExporterTestCase> processedTestCases = exporterTestCases.stream()
                .map(this::processTestCase)
                .collect(Collectors.toList());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("framework", FRAMEWORK_NAME);
        requestBody.put("language", LANGUAGE_NAME);
        requestBody.put("noempty", NO_EMPTY_FLAG);
        requestBody.put("no-detach", NO_DETACH_FLAG);
        requestBody.put("structure", STRUCTURE_FLAG);
        requestBody.put("sync", SYNC_FLAG);
        requestBody.put("tests", processedTestCases);

        try {
            return objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize request body to JSON", e);
        }
    }

    private ExporterTestCase processTestCase(ExporterTestCase original) {
        ExporterTestCase processed = new ExporterTestCase();
        processed.setName(normalizeString(original.getName()));
        processed.setCode(normalizeString(original.getCode()));
        processed.setFile(normalizeString(original.getFile()));
        processed.setSkipped(original.isSkipped());
        processed.setSuites(original.getSuites());
        processed.setLabels(original.getLabels());
        return processed;
    }

    private String normalizeString(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\r\n", "\n").replace("\r", "\n");
    }
}
