package io.testomat.testng.methodexporter.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import io.testomat.testng.exception.MethodExporterException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestNgFileParser {
    private static final Logger log = LoggerFactory.getLogger(TestNgFileParser.class);
    private final Object lock = new Object();

    /**
     * Parses Java source file and returns compilation unit.
     */
    public CompilationUnit parseFile(String filepath) {
        log.info("parseFile called with filepath: {}", filepath);

        try {
            if (filepath == null || filepath.isEmpty()) {
                log.warn("Filepath is null or empty");
                return null;
            }

            Path filePath = Paths.get(filepath);
            log.info("Created Path object: {}", filePath);

            boolean exists = filePath.toFile().exists();
            log.info("File exists: {}", exists);

            if (!exists) {
                log.warn("File does not exist: {}", filepath);
                return null;
            }

            log.info("About to parse file with StaticJavaParser");
            synchronized (lock) {
                CompilationUnit result = StaticJavaParser.parse(filePath);
                log.info("StaticJavaParser.parse completed successfully");
                return result;
            }
        } catch (Exception e) {
            log.error("Exception in parseFile for {}: {}", filepath, e.getMessage(), e);
            throw new MethodExporterException("Failed to parse file " + filepath, e);
        }
    }
}
