package io.testomat.junit.methodexporter;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileParser {
    private final Object lock = new Object();

    public CompilationUnit parseFile(String filepath) {
        try {
            Path filePath = Paths.get(filepath);
            if (!filePath.toFile().exists()) {
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
