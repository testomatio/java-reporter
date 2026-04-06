package io.testomat.aspect;

import io.qameta.allure.Allure;
import io.testomat.allure.AllureClient;
import io.testomat.allure.AllureClientImpl;
import io.testomat.resolver.AttachmentFileResolver;
import io.testomat.resolver.AttachmentFileResolverImpl;
import io.testomat.testomat.TestomatClient;
import io.testomat.testomat.TestomatClientImpl;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
public class AllureAttachmentAspect {

    private static final Map<String, AttachmentMeta> attachments = new ConcurrentHashMap<>();
    private static final ThreadLocal<Boolean> userAttachment = ThreadLocal.withInitial(() -> false);
    private final AllureClient allure;
    private final TestomatClient testomatio;
    private final AttachmentFileResolver resolver;

    public AllureAttachmentAspect(AllureClient allure, TestomatClient testomatio, AttachmentFileResolver resolver) {
        this.allure = allure;
        this.testomatio = testomatio;
        this.resolver = resolver;

    }

    @Around("execution(* io.qameta.allure.Allure.addAttachment(..))")
    public Object interceptUserAttachment(ProceedingJoinPoint joinPoint) throws Throwable {
        userAttachment.set(true);
        try {
            return joinPoint.proceed();
        }
        finally {
            userAttachment.remove();
        }
    }

    @Around("execution(* io.qameta.allure.AllureLifecycle.prepareAttachment(..))")
    public Object interceptPrepare(ProceedingJoinPoint joinPoint) throws Throwable {
        Optional<String> testUuid = allure.getCurrentTest();
        Optional<String> parentUuid = allure.getCurrentTestOrStep();
        Object[] args = joinPoint.getArgs();
        Object result = joinPoint.proceed();
        String uuid = (String) result;

        if (!userAttachment.get()) {
            return result;
        }

        AttachmentMeta meta =
            attachments.computeIfAbsent(
                uuid,
                k -> new AttachmentMeta()
            );

        meta.uuid = uuid;
        meta.testUuid = testUuid.orElse(null);
        meta.parentUuid = parentUuid.orElse(null);
        meta.name = (String) args[0];
        meta.type = (String) args[1];
        meta.level = resolveLevel(meta).name();
        meta.thread = Thread.currentThread().getName();

        return result;
    }

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

    private Nodes resolveLevel(AttachmentMeta meta) {
        if (meta.testUuid == null) {
            return Nodes.fixture;
        }

        if (meta.testUuid.equals(meta.parentUuid)) {
            return Nodes.test;
        }

        return Nodes.step;
    }

    private void sendToTestomat(AttachmentMeta meta) {
        if (meta.level.equals(Nodes.step.name())) {
            testomatio.stepArtifact(meta.path);
        } else if (meta.level.equals(Nodes.test.name())) {
            testomatio.artifact(meta.path);
        }

        System.out.println("===== TESTOMAT ATTACHMENT =====");
        System.out.println("level: " + meta.level);
        System.out.println("name: " + meta.name);
        System.out.println("file: " + meta.path);
        System.out.println("thread: " + meta.thread);
        System.out.println("==============================");
    }

    static class AttachmentMeta {
        String uuid;
        String testUuid;
        String parentUuid;
        String name;
        String type;
        String path;
        int size;
        boolean stream;
        String level;
        String thread;

    }

    enum Nodes {step, test, fixture}
}