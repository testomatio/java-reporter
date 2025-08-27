package io.testomat.junit.methodexporter.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import io.testomat.junit.exception.MethodExporterException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Parses Java source files into compilation units using JavaParser.
 * This class provides thread-safe parsing capabilities for extracting
 * AST representations from Java source files.
 */
public class FileParser {
    private final Object lock = new Object();

    /**
     * Parses a Java source file into a compilation unit.
     * Uses synchronized parsing to ensure thread safety when accessing JavaParser.
     *
     * @param filepath the path to the Java source file to parse
     * @return the parsed compilation unit, or null if file does not exist
     * @throws MethodExporterException if parsing fails
     */
    public CompilationUnit parseFile(String filepath) {
        try {
            Path filePath = Paths.get(filepath);

            boolean exists = filePath.toFile().exists();

            if (!exists) {
                return null;
            }

            synchronized (lock) {
                return StaticJavaParser.parse(filePath);
            }
        } catch (Exception e) {
            throw new MethodExporterException("Failed to parse file " + filepath, e);
        }
    }
}
