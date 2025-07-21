package io.testomat.junit.methodexporter;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.testomat.junit.model.ExporterTestCase;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ExporterRequestBodyBuilderTest {

    private final ExporterRequestBodyBuilder builder = new ExporterRequestBodyBuilder();

    @Test
    public void testBuildRequestBodyWithWindowsPaths() {
        ExporterTestCase testCase = new ExporterTestCase();
        testCase.setName("testMethod");
        testCase.setCode("@Test public void testMethod() {}");
        testCase.setFile("src\\test\\java\\com\\example\\TestClass.java");
        testCase.setSkipped(false);
        testCase.setSuites(Arrays.asList("TestClass"));
        testCase.setLabels(Arrays.asList("unit"));

        List<ExporterTestCase> testCases = Collections.singletonList(testCase);
        String result = builder.buildRequestBody(testCases);

        assertNotNull(result);
        assertTrue(result.contains(
                "\"file\": \"src\\\\test\\\\java\\\\com\\\\example\\\\TestClass.java\""));
        assertTrue(result.contains("\"framework\": \"junit\""));
        assertTrue(result.contains("\"language\": \"java\""));
    }

    @Test
    public void testBuildRequestBodyWithUnixPaths() {
        ExporterTestCase testCase = new ExporterTestCase();
        testCase.setName("testMethod");
        testCase.setCode("@Test public void testMethod() {}");
        testCase.setFile("src/test/java/com/example/TestClass.java");
        testCase.setSkipped(false);
        testCase.setSuites(Arrays.asList("TestClass"));
        testCase.setLabels(Arrays.asList("unit"));

        List<ExporterTestCase> testCases = Collections.singletonList(testCase);
        String result = builder.buildRequestBody(testCases);

        assertNotNull(result);
        assertTrue(result.contains("\"file\": \"src/test/java/com/example/TestClass.java\""));
        assertTrue(result.contains("\"framework\": \"junit\""));
        assertTrue(result.contains("\"language\": \"java\""));
    }

    @Test
    public void testBuildRequestBodyWithMixedSlashesInPath() {
        ExporterTestCase testCase = new ExporterTestCase();
        testCase.setName("testMethod");
        testCase.setCode("@Test public void testMethod() {}");
        testCase.setFile("src\\test/java\\com/example\\TestClass.java");
        testCase.setSkipped(false);
        testCase.setSuites(Arrays.asList("TestClass"));
        testCase.setLabels(Arrays.asList("unit"));

        List<ExporterTestCase> testCases = Collections.singletonList(testCase);
        String result = builder.buildRequestBody(testCases);

        assertNotNull(result);
        assertTrue(result.contains(
                "\"file\": \"src\\\\test/java\\\\com/example\\\\TestClass.java\""));
    }

    @Test
    public void testBuildRequestBodyWithSpecialCharactersInPath() {
        ExporterTestCase testCase = new ExporterTestCase();
        testCase.setName("testMethod");
        testCase.setCode("@Test public void testMethod() {}");
        testCase.setFile("src/test/java/com/example/Test\"Class.java");
        testCase.setSkipped(false);
        testCase.setSuites(Arrays.asList("TestClass"));
        testCase.setLabels(Arrays.asList("unit"));

        List<ExporterTestCase> testCases = Collections.singletonList(testCase);
        String result = builder.buildRequestBody(testCases);

        assertNotNull(result);
        assertTrue(result.contains("\"file\": \"src/test/java/com/example/Test\\\"Class.java\""));
    }

    @Test
    public void testBuildRequestBodyWithNewlinesInCode() {
        ExporterTestCase testCase = new ExporterTestCase();
        testCase.setName("testMethod");
        testCase.setCode("@Test\npublic void testMethod() {\n    // test code\n}");
        testCase.setFile("src/test/java/TestClass.java");
        testCase.setSkipped(false);
        testCase.setSuites(Arrays.asList("TestClass"));
        testCase.setLabels(Arrays.asList("unit"));

        List<ExporterTestCase> testCases = Collections.singletonList(testCase);
        String result = builder.buildRequestBody(testCases);

        assertNotNull(result);
        assertTrue(result.contains(
                "\"code\": \"@Test\\npublic void testMethod() {\\n    // test code\\n}\""));
    }

    @Test
    public void testBuildRequestBodyWithWindowsNewlines() {
        ExporterTestCase testCase = new ExporterTestCase();
        testCase.setName("testMethod");
        testCase.setCode("@Test\r\npublic void testMethod() {\r\n    // test code\r\n}");
        testCase.setFile("src/test/java/TestClass.java");
        testCase.setSkipped(false);
        testCase.setSuites(Arrays.asList("TestClass"));
        testCase.setLabels(Arrays.asList("unit"));

        List<ExporterTestCase> testCases = Collections.singletonList(testCase);
        String result = builder.buildRequestBody(testCases);

        assertNotNull(result);
        assertTrue(result.contains(
                "\"code\": \"@Test\\npublic void testMethod() {\\n    // test code\\n}\""));
    }

    @Test
    public void testBuildRequestBodyWithAbsoluteWindowsPath() {
        ExporterTestCase testCase = new ExporterTestCase();
        testCase.setName("testMethod");
        testCase.setCode("@Test public void testMethod() {}");
        testCase.setFile("C:\\Users\\developer\\project\\src\\test\\java\\TestClass.java");
        testCase.setSkipped(false);
        testCase.setSuites(Arrays.asList("TestClass"));
        testCase.setLabels(Arrays.asList("unit"));

        List<ExporterTestCase> testCases = Collections.singletonList(testCase);
        String result = builder.buildRequestBody(testCases);

        assertNotNull(result);
        assertTrue(result.contains(
                "\"file\": \"C:\\\\Users\\\\developer\\\\"
                        + "project\\\\src\\\\test\\\\java\\\\TestClass.java\""));
    }

    @Test
    public void testBuildRequestBodyWithAbsoluteUnixPath() {
        ExporterTestCase testCase = new ExporterTestCase();
        testCase.setName("testMethod");
        testCase.setCode("@Test public void testMethod() {}");
        testCase.setFile("/home/user/project/src/test/java/TestClass.java");
        testCase.setSkipped(false);
        testCase.setSuites(Arrays.asList("TestClass"));
        testCase.setLabels(Arrays.asList("unit"));

        List<ExporterTestCase> testCases = Collections.singletonList(testCase);
        String result = builder.buildRequestBody(testCases);

        assertNotNull(result);
        assertTrue(result.contains(
                "\"file\": \"/home/user/project/src/test/java/TestClass.java\""));
    }

    @Test
    public void testBuildRequestBodyWithNullFilePath() {
        ExporterTestCase testCase = new ExporterTestCase();
        testCase.setName("testMethod");
        testCase.setCode("@Test public void testMethod() {}");
        testCase.setFile(null);
        testCase.setSkipped(false);
        testCase.setSuites(Arrays.asList("TestClass"));
        testCase.setLabels(Arrays.asList("unit"));

        List<ExporterTestCase> testCases = Collections.singletonList(testCase);
        String result = builder.buildRequestBody(testCases);

        assertNotNull(result);
        assertTrue(result.contains("\"file\": \"\""));
    }

    @Test
    public void testBuildRequestBodyWithMultipleTestCasesFromDifferentPaths() {
        ExporterTestCase windowsTest = new ExporterTestCase();
        windowsTest.setName("windowsTest");
        windowsTest.setCode("@Test public void windowsTest() {}");
        windowsTest.setFile("src\\test\\java\\WindowsTest.java");
        windowsTest.setSkipped(false);
        windowsTest.setSuites(Arrays.asList("WindowsTest"));
        windowsTest.setLabels(Arrays.asList("unit"));

        ExporterTestCase unixTest = new ExporterTestCase();
        unixTest.setName("unixTest");
        unixTest.setCode("@Test public void unixTest() {}");
        unixTest.setFile("src/test/java/UnixTest.java");
        unixTest.setSkipped(false);
        unixTest.setSuites(Arrays.asList("UnixTest"));
        unixTest.setLabels(Arrays.asList("unit"));

        List<ExporterTestCase> testCases = Arrays.asList(windowsTest, unixTest);
        String result = builder.buildRequestBody(testCases);

        assertNotNull(result);
        assertTrue(result.contains("\"file\": \"src\\\\test\\\\java\\\\WindowsTest.java\""));
        assertTrue(result.contains("\"file\": \"src/test/java/UnixTest.java\""));
        assertTrue(result.contains("\"tests\": ["));
        assertTrue(result.contains(",\n"));
    }
}