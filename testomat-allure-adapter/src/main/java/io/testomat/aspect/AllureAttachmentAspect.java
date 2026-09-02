package io.testomat.aspect;

import io.testomat.allure.AllureClient;
import io.testomat.allure.AllureClientImpl;
import io.testomat.resolver.AttachmentFileResolver;
import io.testomat.resolver.AttachmentFileResolverImpl;
import io.testomat.testomat.TestomatClient;
import io.testomat.testomat.TestomatClientImpl;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.tika.mime.MimeTypes;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aspect for intercepting Allure attachments and forwarding them to Testomat.
 * Captures user-created attachments, collects metadata and sends files
 * to Testomat depending on their level (test or step).
 */
@Aspect
public class AllureAttachmentAspect {
    private static final Logger log = LoggerFactory.getLogger(AllureAttachmentAspect.class);

    private static final Map<String, AttachmentMeta> attachments = new ConcurrentHashMap<>();
    private final AllureClient allure;
    private final TestomatClient testomatio;
    private final AttachmentFileResolver resolver;

    public AllureAttachmentAspect() {
        this(new AllureClientImpl(),
            new TestomatClientImpl(),
            new AttachmentFileResolverImpl());
    }

    public AllureAttachmentAspect(AllureClient allure,
            TestomatClient testomatio, AttachmentFileResolver resolver) {
        this.allure = allure;
        this.testomatio = testomatio;
        this.resolver = resolver;

    }

    /** Collects attachment metadata during preparation phase. */
    @Around("execution(* io.qameta.allure.AllureLifecycle.prepareAttachment(..))")
    public Object interceptPrepare(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        String uuid = (String) result;

        AttachmentMeta meta = attachments.computeIfAbsent(uuid, k -> new AttachmentMeta());

        Object[] args = joinPoint.getArgs();
        meta.name = (String) args[0];
        meta.type = (String) args[1];

        Optional<String> testUuid = allure.getCurrentTest();
        meta.testUuid = testUuid.orElse(null);

        Optional<String> parentUuid = allure.getCurrentTestOrStep();
        meta.parentUuid = parentUuid.orElse(null);

        meta.uuid = uuid;
        meta.level = resolveLevel(meta).name();
        meta.thread = Thread.currentThread().getName();

        return result;
    }

    /** Finalizes metadata after attachment is written. */
    @Around("execution(* io.qameta.allure.AllureLifecycle.writeAttachment(..))")
    public Object interceptWrite(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        String uuid = (String) args[0];
        Object result = joinPoint.proceed();
        AttachmentMeta meta = attachments.get(uuid);

        if (meta == null) {
            return result;
        }

        Object content = args[1];

        if (content instanceof byte[] bytes) {
            meta.size = bytes.length;
        }

        if (content instanceof InputStream) {
            meta.stream = true;
        }

        meta.path = resolver.find(uuid);
        sendToTestomat(meta);
        attachments.remove(uuid);

        return result;
    }

    /** Resolves attachment level (fixture, test, step). */
    private Nodes resolveLevel(AttachmentMeta meta) {
        if (meta.testUuid == null) {
            return Nodes.fixture;
        }

        if (meta.testUuid.equals(meta.parentUuid)) {
            return Nodes.test;
        }

        return Nodes.step;
    }

    /** Sends attachment to Testomat. */
    private void sendToTestomat(AttachmentMeta meta) {
        String filePath = addExtension(meta.path, meta.type);
        if (meta.level.equals(Nodes.step.name())) {
            testomatio.stepArtifact(filePath);
        } else if (meta.level.equals(Nodes.test.name())) {
            testomatio.artifact(filePath);
        }

        log.debug("===== TESTOMAT ATTACHMENT =====");
        log.debug("uuid: {}", meta.uuid);
        log.debug("level: {}", meta.level);
        log.debug("name: {}", meta.name);
        log.debug("type: {}", meta.type);
        log.debug("file: {}", meta.path);
        log.debug("size: {}", meta.size);
        log.debug("stream: {}", meta.stream);
        log.debug("thread: {}", meta.thread);
        log.debug("==============================");
    }

    private String addExtension(String fileName, String mimeType) {
        try {
            String extension = MimeTypes.getDefaultMimeTypes()
                    .forName(mimeType)
                    .getExtension();

            if (fileName.endsWith(extension)) {
                return fileName;
            }

            Path source = Path.of(fileName);
            Path target = Path.of(fileName + extension);

            if (!Files.exists(source)) {
                log.debug("Attachment file not found: {}", source);
                return fileName;
            }

            if (Files.exists(target)) {
                log.debug("Attachment copy already exists: {}", target);
                return target.toString();
            }

            Files.copy(source, target);

            log.debug("Created attachment copy with extension: {} -> {}", source, target);

            return target.toString();

        } catch (Exception e) {
            log.debug("Failed to add extension '{}' to attachment '{}'", mimeType, fileName, e);
            return fileName;
        }
    }

    static class AttachmentMeta {
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

    enum Nodes { step, test, fixture }
}
