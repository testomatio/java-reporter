package io.testomat.junit.methodexporter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.testomat.junit.model.ExporterTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExporterRequestBodyBuilderTest {

    private ExporterRequestBodyBuilder builder;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        builder = new ExporterRequestBodyBuilder();
        mapper = new ObjectMapper();
    }

    @Test
    void buildRequestBody_withSingleTestCase_returnsValidJsonAndNormalizesFields() throws Exception {
        ExporterTestCase testCase = new ExporterTestCase();
        testCase.setName("My\r\nTest");
        testCase.setCode("System.out.println(\"Hello\");");
        testCase.setFile("File\rPath.java");
        testCase.setSkipped(true);
        testCase.setSuites(Arrays.asList("Suite1", "Suite2"));
        testCase.setLabels(Collections.singletonList("smoke"));

        String json = builder.buildRequestBody(List.of(testCase));

        JsonNode root = mapper.readTree(json);
        assertEquals("junit", root.get("framework").asText());
        assertEquals("java", root.get("language").asText());
        assertTrue(root.get("noempty").asBoolean());
        assertTrue(root.get("no-detach").asBoolean());
        assertTrue(root.get("structure").asBoolean());
        assertTrue(root.get("sync").asBoolean());

        JsonNode tests = root.get("tests");
        assertEquals(1, tests.size());

        JsonNode exported = tests.get(0);
        assertEquals("My\nTest", exported.get("name").asText()); // normalized
        assertEquals("File\nPath.java", exported.get("file").asText()); // normalized
        assertTrue(exported.get("skipped").asBoolean());
        assertEquals(2, exported.get("suites").size());
        assertEquals("smoke", exported.get("labels").get(0).asText());
    }

    @Test
    void buildRequestBody_withEmptyList_producesEmptyTestsArray() throws Exception {
        String json = builder.buildRequestBody(Collections.emptyList());

        JsonNode root = mapper.readTree(json);
        assertTrue(root.get("tests").isArray());
        assertEquals(0, root.get("tests").size());
    }

    @Test
    void buildRequestBody_withNullStrings_normalizesToEmpty() throws Exception {
        ExporterTestCase testCase = new ExporterTestCase();
        testCase.setName(null);
        testCase.setCode(null);
        testCase.setFile(null);
        testCase.setSkipped(false);
        testCase.setSuites(Collections.emptyList());
        testCase.setLabels(Collections.emptyList());

        String json = builder.buildRequestBody(List.of(testCase));

        JsonNode exported = mapper.readTree(json).get("tests").get(0);
        assertEquals("", exported.get("name").asText());
        assertEquals("", exported.get("code").asText());
        assertEquals("", exported.get("file").asText());
    }

    @Test
    void buildRequestBody_withMultipleCases_allCasesIncluded() throws Exception {
        ExporterTestCase case1 = new ExporterTestCase();
        case1.setName("Case1");
        case1.setCode("Code1");
        case1.setFile("File1");
        case1.setSkipped(false);

        ExporterTestCase case2 = new ExporterTestCase();
        case2.setName("Case2");
        case2.setCode("Code2");
        case2.setFile("File2");
        case2.setSkipped(true);

        String json = builder.buildRequestBody(List.of(case1, case2));

        JsonNode tests = mapper.readTree(json).get("tests");
        assertEquals(2, tests.size());
        assertEquals("Case1", tests.get(0).get("name").asText());
        assertEquals("Case2", tests.get(1).get("name").asText());
        assertTrue(tests.get(1).get("skipped").asBoolean());
    }
}
