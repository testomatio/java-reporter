package io.testomat.advice;

import io.testomat.allure.AllureClient;
import io.testomat.allure.AllureClientImpl;
import io.testomat.resolver.AttachmentFileResolver;
import io.testomat.resolver.AttachmentFileResolverImpl;
import io.testomat.testomat.TestomatClient;
import io.testomat.testomat.TestomatClientImpl;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.bytebuddy.asm.Advice;
import org.apache.tika.mime.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Captures Allure attachments and forwards test and step files to Testomat. */
public final class AttachmentAdvice {

    public static final AttachmentHandler handler = new AttachmentHandler(
            new AllureClientImpl(), new TestomatClientImpl(), new AttachmentFileResolverImpl());

    private AttachmentAdvice() {
    }

    /** Records attachment metadata after Allure creates its identifier. */
    public static class Prepare {
        @Advice.OnMethodExit
        public static void onExit(@Advice.Return String uuid, @Advice.Argument(0) String name,
                @Advice.Argument(1) String type) {
            handler.prepare(uuid, name, type);
        }
    }

    /** Sends a recorded attachment after Allure writes its content. */
    public static class Write {
        @Advice.OnMethodExit
        public static void onExit(@Advice.Argument(0) String uuid,
                @Advice.Argument(1) Object content) throws IOException {
            handler.write(uuid, content);
        }
    }

    public static final class AttachmentHandler {
        private static final Logger log = LoggerFactory.getLogger(AttachmentHandler.class);

        private final Map<String, AttachmentMeta> attachments = new ConcurrentHashMap<>();
        private final AllureClient allure;
        private final TestomatClient testomatio;
        private final AttachmentFileResolver resolver;

        public AttachmentHandler(AllureClient allure, TestomatClient testomatio,
                AttachmentFileResolver resolver) {
            this.allure = allure;
            this.testomatio = testomatio;
            this.resolver = resolver;
        }

        public void prepare(String uuid, String name, String type) {
            if (uuid == null) {
                return;
            }

            AttachmentMeta meta = attachments.computeIfAbsent(uuid,
                    ignored -> new AttachmentMeta());
            meta.name = name;
            meta.type = type;

            Optional<String> testUuid = allure.getCurrentTest();
            meta.testUuid = testUuid.orElse(null);

            Optional<String> parentUuid = allure.getCurrentTestOrStep();
            meta.parentUuid = parentUuid.orElse(null);
            meta.uuid = uuid;
            meta.level = resolveLevel(meta);
            meta.thread = Thread.currentThread().getName();
        }

        public void write(String uuid, Object content) throws IOException {
            AttachmentMeta meta = attachments.get(uuid);
            if (meta == null) {
                return;
            }

            if (content instanceof byte[] bytes) {
                meta.size = bytes.length;
            }
            if (content instanceof InputStream) {
                meta.stream = true;
            }

            meta.path = resolver.find(uuid);
            sendToTestomat(meta);
            attachments.remove(uuid);
        }

        private static String resolveLevel(AttachmentMeta meta) {
            if (meta.testUuid == null) {
                return "fixture";
            }
            return meta.testUuid.equals(meta.parentUuid) ? "test" : "step";
        }

        private void sendToTestomat(AttachmentMeta meta) {
            String filePath = addExtension(meta.path, meta.type);
            if ("step".equals(meta.level)) {
                testomatio.stepArtifact(filePath);
            } else if ("test".equals(meta.level)) {
                testomatio.artifact(filePath);
            }

            log.debug("Sent Allure attachment uuid={} level={} file={}",
                    meta.uuid, meta.level, meta.path);
        }

        private static String addExtension(String fileName, String mimeType) {
            try {
                String extension = MimeTypes.getDefaultMimeTypes().forName(mimeType).getExtension();
                if (fileName.endsWith(extension)) {
                    return fileName;
                }

                Path source = Path.of(fileName);
                Path target = Path.of(fileName + extension);
                if (!Files.exists(source)) {
                    return fileName;
                }
                if (Files.exists(target)) {
                    return target.toString();
                }

                Files.copy(source, target);
                return target.toString();
            } catch (Exception e) {
                log.debug("Failed to add extension '{}' to attachment '{}'", mimeType, fileName, e);
                return fileName;
            }
        }
    }

    private static final class AttachmentMeta {
        private String uuid;
        private String testUuid;
        private String parentUuid;
        private String name;
        private String type;
        private String path;
        private int size;
        private boolean stream;
        private String level;
        private String thread;
    }
}
