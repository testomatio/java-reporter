package io.testomat.testng.methodexporter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.testomat.testng.methodexporter.model.TestNgExporterTestCase;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TestNgExporterRequestBodyBuilderTest {

    private final TestNgExporterRequestBodyBuilder builder =
            new TestNgExporterRequestBodyBuilder();

    @Test
    @DisplayName("Should build request body with metadata and test cases")
    void shouldBuildRequestBodyWithMetadataAndTestCases() {
        TestNgExporterTestCase testCase = new TestNgExporterTestCase();
        testCase.setName("testName");
        testCase.setSuites(List.of("SelenideTest"));
        testCase.setCode("public void testName() { }");
        testCase.setFile("com/example/SelenideTest.java");
        testCase.setSkipped(false);
        testCase.setLabels(List.of("smoke", "regression"));

        String body = builder.buildRequestBody(List.of(testCase));

        assertTrue(body.contains("\"framework\": \"testng\""));
        assertTrue(body.contains("\"language\": \"java\""));
        assertTrue(body.contains("\"noempty\": true"));
        assertTrue(body.contains("\"structure\": true"));
        assertTrue(body.contains("\"sync\": true"));
        assertTrue(body.contains("\"name\": \"testName\""));
        assertTrue(body.contains("\"suites\": [\"SelenideTest\"]"));
        assertTrue(body.contains("\"code\": \"public void testName() { }\""));

        String expectedFile =
                "\"file\": \"com/example/SelenideTest.java\"";
        assertTrue(body.contains(expectedFile));
        assertTrue(body.contains("\"skipped\": false"));
        assertTrue(body.contains("\"labels\": [\"smoke\", \"regression\"]"));
    }

    @Test
    @DisplayName("Should build request body for multiple test cases")
    void shouldBuildRequestBodyForMultipleTestCases() {
        TestNgExporterTestCase firstCase = new TestNgExporterTestCase();
        firstCase.setName("testOne");
        firstCase.setSuites(List.of("SuiteA"));

        TestNgExporterTestCase secondCase = new TestNgExporterTestCase();
        secondCase.setName("testTwo");
        secondCase.setSuites(Arrays.asList("SuiteA", "SuiteB"));

        String body = builder.buildRequestBody(Arrays.asList(firstCase, secondCase));

        assertTrue(body.contains("\"name\": \"testOne\""));
        assertTrue(body.contains("\"name\": \"testTwo\""));
        assertTrue(body.contains("\"suites\": [\"SuiteA\", \"SuiteB\"]"));
        assertTrue(body.indexOf("testOne") < body.indexOf("testTwo"));
    }

    @Test
    @DisplayName("Should escape quotes, backslashes and newlines in code")
    void shouldEscapeQuotesBackslashesAndNewlinesInCode() {
        TestNgExporterTestCase testCase = new TestNgExporterTestCase();
        testCase.setName("testName");
        testCase.setSuites(List.of("SuiteA"));
        testCase.setCode("System.out.println(\"hello\");\nString path = \"C:\\\\temp\";");

        String body = builder.buildRequestBody(List.of(testCase));

        assertTrue(body.contains("System.out.println(\\\"hello\\\");\\n"));
        assertTrue(body.contains("C:\\\\\\\\temp"));
        assertFalse(body.contains("println(\"hello\");\n"));
    }

    @Test
    @DisplayName("Should build empty tests array for empty input")
    void shouldBuildEmptyTestsArrayForEmptyInput() {
        String body = builder.buildRequestBody(Collections.emptyList());

        assertTrue(body.contains("\"tests\": [\n  ]"));
    }

    @Test
    @DisplayName("Should handle null and empty values")
    void shouldHandleNullAndEmptyValues() {
        TestNgExporterTestCase testCase = new TestNgExporterTestCase();
        testCase.setName("testName");
        testCase.setSuites(Collections.emptyList());
        testCase.setLabels(null);
        testCase.setCode(null);

        String body = builder.buildRequestBody(List.of(testCase));

        assertTrue(body.contains("\"suites\": []"));
        assertTrue(body.contains("\"labels\": []"));
        assertTrue(body.contains("\"code\": \"\""));
    }
}