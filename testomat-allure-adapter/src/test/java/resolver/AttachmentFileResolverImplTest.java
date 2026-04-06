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

}