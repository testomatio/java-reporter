package io.testomat.testng.methodexporter.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.javaparser.ast.CompilationUnit;
import io.testomat.testng.exception.MethodExporterException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TestNgFileParserTest {

    private static final String VALID_JAVA_CLASS =
            "package com.example;\n"
            + "\n"
            + "public class SampleTest {\n"
            + "    @org.testng.annotations.Test\n"
            + "    public void testMethod() {\n"
            + "    }\n"
            + "}\n";

    private final TestNgFileParser parser = new TestNgFileParser();

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Should parse valid Java source file")
    void shouldParseValidJavaSourceFile() throws Exception {
        Path sourceFile = tempDir.resolve("SampleTest.java");
        Files.writeString(sourceFile, VALID_JAVA_CLASS);

        CompilationUnit result = parser.parseFile(sourceFile.toString());

        assertNotNull(result);
        assertEquals("SampleTest", result.getType(0).getNameAsString());
    }

    @Test
    @DisplayName("Should return null for null filepath")
    void shouldReturnNullForNullFilepath() {
        assertNull(parser.parseFile(null));
    }

    @Test
    @DisplayName("Should return null for empty filepath")
    void shouldReturnNullForEmptyFilepath() {
        assertNull(parser.parseFile(""));
    }

    @Test
    @DisplayName("Should return null for blank filepath")
    void shouldReturnNullForBlankFilepath() {
        assertNull(parser.parseFile("   "));
    }

    @Test
    @DisplayName("Should return null for non-existing file")
    void shouldReturnNullForNonExistingFile() {
        String filepath = tempDir.resolve("MissingTest.java").toString();

        assertNull(parser.parseFile(filepath));
    }

    @Test
    @DisplayName("Should throw MethodExporterException for invalid Java content")
    void shouldThrowMethodExporterExceptionForInvalidContent() throws Exception {
        Path sourceFile = tempDir.resolve("InvalidTest.java");
        Files.writeString(sourceFile, "this is not valid java {{{");

        assertThrows(MethodExporterException.class,
                () -> parser.parseFile(sourceFile.toString()));
    }
}