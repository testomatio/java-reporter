package io.testomat.resolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class AttachmentFileResolverImpl implements AttachmentFileResolver {

    private final String resultsDir;

    public AttachmentFileResolverImpl(String resultsDir) {
        this.resultsDir = resultsDir;
    }

    @Override
    public String find(String uuid) throws IOException {

        try (Stream<Path> paths =
            Files.list(Paths.get(resultsDir))) {

            return paths
                .filter(p ->
                    p.getFileName()
                        .toString()
                        .startsWith(uuid))
                .map(Path::toString)
                .findFirst()
                .orElse(null);
        }
    }
}
