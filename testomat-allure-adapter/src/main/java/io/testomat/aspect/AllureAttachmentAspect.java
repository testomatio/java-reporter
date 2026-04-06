package io.testomat.aspect;

import io.testomat.allure.AllureClient;
import io.testomat.resolver.AttachmentFileResolver;
import io.testomat.testomat.TestomatClient;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
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
    private static final ThreadLocal<Boolean> userAttachment = ThreadLocal.withInitial(() -> false);
    private final AllureClient allure;
    private final TestomatClient testomatio;
    private final AttachmentFileResolver resolver;

    public AllureAttachmentAspect(AllureClient allure,
            TestomatClient testomatio, AttachmentFileResolver resolver) {
        this.allure = allure;
        this.testomatio = testomatio;
        this.resolver = resolver;

    }

    /** Marks user attachments created via Allure API. */
    @Around("execution(* io.qameta.allure.Allure.addAttachment(..))")
    public Object interceptUserAttachment(ProceedingJoinPoint joinPoint) throws Throwable {
        userAttachment.set(true);
        try {
            return joinPoint.proceed();
        } finally {
            userAttachment.remove();
        }
    }

    /** Collects attachment metadata during preparation phase. */
    @Around("execution(* io.qameta.allure.AllureLifecycle.prepareAttachment(..))")
    public Object interceptPrepare(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        String uuid = (String) result;

        if (!userAttachment.get()) {
            return result;
        }

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
        if (meta.level.equals(Nodes.step.name())) {
            testomatio.stepArtifact(meta.path);
        } else if (meta.level.equals(Nodes.test.name())) {
            testomatio.artifact(meta.path);
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
