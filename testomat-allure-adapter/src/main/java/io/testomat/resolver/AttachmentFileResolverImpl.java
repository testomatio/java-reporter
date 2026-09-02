package io.testomat.resolver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves attachment file path in Allure results directory by UUID.
 */
public class AttachmentFileResolverImpl implements AttachmentFileResolver {
    private static final Logger log = LoggerFactory.getLogger(AttachmentFileResolverImpl.class);

    private final String resultsDir;

    public AttachmentFileResolverImpl() {
        this.resultsDir = resolveAllurePath();
    }

    public AttachmentFileResolverImpl(String resultsDir) {
        this.resultsDir = resultsDir;
    }

    /**
     * Finds attachment file by UUID.
     *
     * @param uuid attachment UUID
     * @return file path or null if not found
     */
    @Override
    public String find(String uuid) throws IOException {
        if (uuid == null || uuid.isBlank()) {
            return null;
        }

        Path dir = Paths.get(resultsDir);

        if (!Files.exists(dir)) {
            return null;
        }

        try (Stream<Path> paths = Files.list(dir)) {
            return paths
                .filter(p ->
                    p.getFileName().toString().startsWith(uuid)
                        && p.getFileName().toString().contains("-attachment"))
                .map(Path::toString)
                .findFirst()
                .orElse(null);
        }
    }

    private String resolveAllurePath() {
        String systemProperty = System.getProperty("allure.results.directory");
        if (systemProperty != null && !systemProperty.isBlank()) {
            return systemProperty;
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream("allure.properties")) {
            if (inputStream != null) {
                Properties props = new Properties();
                props.load(inputStream);

                String result = props.getProperty("allure.results.directory");
                if (result != null && !result.isBlank()) {
                    return result;
                }
            }
        } catch (IOException e) {
            log.trace("Failed to read allure.properties", e);
        }

        return "allure-results";
    }
}
