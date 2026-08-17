package io.testomat.testng.methodexporter.extractor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.javaparser.ast.CompilationUnit;
import io.testomat.testng.methodexporter.model.TestNgExporterTestCase;
import io.testomat.testng.methodexporter.parser.TestNgFileParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TestNgMethodCaseExtractorTest {

    private static final String SOURCE_FILE =
            "package com.example;\n"
            + "\n"
            + "public class SelenideTest {\n"
            + "    @Test\n"
            + "    public void testName() {\n"
            + "        System.out.println(\"hello\");\n"
            + "    }\n"
            + "\n"
            + "    public void helperMethod() {\n"
            + "    }\n"
            + "\n"
            + "    public static class NestedTestClass {\n"
            + "        @Test\n"
            + "        public void testHello() {\n"
            + "            System.out.println(\"world\");\n"
            + "        }\n"
            + "\n"
            + "        public void notATestMethod() {\n"
            + "        }\n"
            + "    }\n"
            + "}\n";

    private TestNgMethodCaseExtractor extractor;
    private TestNgFileParser parser;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        extractor = new TestNgMethodCaseExtractor();
        parser = new TestNgFileParser();
    }

    @Test
    @DisplayName("Should extract test methods from top-level and nested classes")
    void shouldExtractTestMethodsFromTopLevelAndNestedClasses() throws Exception {
        Path sourceFile = tempDir.resolve("SelenideTest.java");
        Files.writeString(sourceFile, SOURCE_FILE);
        CompilationUnit cu = parser.parseFile(sourceFile.toString());

        List<TestNgExporterTestCase> testCases = extractor.extractTestCases(cu,
                sourceFile.toString());

        assertEquals(2, testCases.size());
    }

    @Test
    @DisplayName("Should build suite hierarchy for nested class test method")
    void shouldBuildSuiteHierarchyForNestedClassTestMethod() throws Exception {
        Path sourceFile = tempDir.resolve("SelenideTest.java");
        Files.writeString(sourceFile, SOURCE_FILE);
        CompilationUnit cu = parser.parseFile(sourceFile.toString());

        List<TestNgExporterTestCase> testCases = extractor.extractTestCases(cu,
                sourceFile.toString());

        TestNgExporterTestCase nestedTestCase = findTestCaseByName(testCases, "testHello");
        assertNotNull(nestedTestCase);
        assertEquals(Arrays.asList("SelenideTest", "NestedTestClass"),
                nestedTestCase.getSuites());
    }

    @Test
    @DisplayName("Should build single-level suite for top-level class test method")
    void shouldBuildSingleLevelSuiteForTopLevelClassTestMethod() throws Exception {
        Path sourceFile = tempDir.resolve("SelenideTest.java");
        Files.writeString(sourceFile, SOURCE_FILE);
        CompilationUnit cu = parser.parseFile(sourceFile.toString());

        List<TestNgExporterTestCase> testCases = extractor.extractTestCases(cu,
                sourceFile.toString());

        TestNgExporterTestCase topLevelTestCase = findTestCaseByName(testCases, "testName");
        assertNotNull(topLevelTestCase);
        assertEquals(List.of("SelenideTest"), topLevelTestCase.getSuites());
    }

    @Test
    @DisplayName("Should extract method code and skip only test methods")
    void shouldExtractMethodCodeAndSkipOnlyTestMethods() throws Exception {
        Path sourceFile = tempDir.resolve("SelenideTest.java");
        Files.writeString(sourceFile, SOURCE_FILE);
        CompilationUnit cu = parser.parseFile(sourceFile.toString());

        List<TestNgExporterTestCase> testCases = extractor.extractTestCases(cu,
                sourceFile.toString());

        for (TestNgExporterTestCase testCase : testCases) {
            assertNotNull(testCase.getCode());
            assertTrue(testCase.getCode().contains(testCase.getName()));
            assertNotNull(testCase.getFile());
            assertFalse(testCase.isSkipped());
        }
    }

    private TestNgExporterTestCase findTestCaseByName(List<TestNgExporterTestCase> testCases,
                                                      String name) {
        return testCases.stream()
                .filter(testCase -> testCase.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}