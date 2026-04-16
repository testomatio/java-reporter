package resolver;

import io.testomat.resolver.AttachmentFileResolverImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.assertThat;

class AttachmentFileResolverImplTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldFindAttachmentFile() throws IOException {
        String uuid = "123";
        Path file = Files.createFile(tempDir.resolve(uuid + "-attachment.txt"));
        AttachmentFileResolverImpl resolver = new AttachmentFileResolverImpl(tempDir.toString());
        String result = resolver.find(uuid);
        assertThat(result).isEqualTo(file.toString());
    }

    @Test
    void shouldReturnNullIfFileNotFound() throws IOException {
        AttachmentFileResolverImpl resolver = new AttachmentFileResolverImpl(tempDir.toString());
        String result = resolver.find("unknown");
        assertThat(result).isNull();
    }

    @Test
    void shouldHandleEmptyDirectory() throws IOException {
        AttachmentFileResolverImpl resolver = new AttachmentFileResolverImpl(tempDir.toString());
        String result = resolver.find("123");
        assertThat(result).isNull();
    }

    @Test
    void shouldFindCorrectAttachmentAmongMultipleFiles() throws IOException {
        String uuid = "123";

        Files.createFile(tempDir.resolve(uuid + "-attachment.txt"));
        Files.createFile(tempDir.resolve(uuid + "-other.txt")); // не должен попасть
        Files.createFile(tempDir.resolve("999-attachment.txt")); // другой uuid

        AttachmentFileResolverImpl resolver =
            new AttachmentFileResolverImpl(tempDir.toString());

        String result = resolver.find(uuid);

        assertThat(result).contains(uuid + "-attachment.txt");
    }

    @Test
    void shouldIgnoreFilesWithoutAttachmentSuffix() throws IOException {
        String uuid = "123";

        Files.createFile(tempDir.resolve(uuid + "-log.txt"));

        AttachmentFileResolverImpl resolver =
            new AttachmentFileResolverImpl(tempDir.toString());

        String result = resolver.find(uuid);

        assertThat(result).isNull();
    }

    @Test
    void shouldNotMatchDifferentUuid() throws IOException {
        Files.createFile(tempDir.resolve("999-attachment.txt"));

        AttachmentFileResolverImpl resolver =
            new AttachmentFileResolverImpl(tempDir.toString());

        String result = resolver.find("123");

        assertThat(result).isNull();
    }

    @Test
    void shouldReturnNullForNullUuid() throws IOException {
        AttachmentFileResolverImpl resolver =
            new AttachmentFileResolverImpl(tempDir.toString());

        String result = resolver.find(null);

        assertThat(result).isNull();
    }

    @Test
    void shouldReturnNullForBlankUuid() throws IOException {
        AttachmentFileResolverImpl resolver =
            new AttachmentFileResolverImpl(tempDir.toString());

        String result = resolver.find("   ");

        assertThat(result).isNull();
    }

    @Test
    void shouldReturnNullIfDirectoryDoesNotExist() throws IOException {
        AttachmentFileResolverImpl resolver =
            new AttachmentFileResolverImpl("non-existing-dir");

        String result = resolver.find("123");

        assertThat(result).isNull();
    }

    @Test
    void shouldReturnOneOfMatchingFiles() throws IOException {
        String uuid = "123";

        Path file1 = Files.createFile(tempDir.resolve(uuid + "-attachment1.txt"));
        Path file2 = Files.createFile(tempDir.resolve(uuid + "-attachment2.txt"));

        AttachmentFileResolverImpl resolver =
            new AttachmentFileResolverImpl(tempDir.toString());

        String result = resolver.find(uuid);

        assertThat(result).isIn(file1.toString(), file2.toString());
    }

    @Test
    void shouldUseSystemProperty() {
        System.setProperty("allure.results.directory", "custom-dir");

        AttachmentFileResolverImpl resolver = new AttachmentFileResolverImpl();

        assertThat(resolver)
            .extracting("resultsDir")
            .isEqualTo("custom-dir");

        System.clearProperty("allure.results.directory");
    }

}