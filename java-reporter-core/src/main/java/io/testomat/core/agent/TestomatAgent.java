package io.testomat.core.agent;

import io.testomat.core.facade.methods.artifact.ArtifactAdvice;
import io.testomat.core.step.StepAdvice;
import java.lang.instrument.Instrumentation;
import io.testomat.core.annotation.Artifact;
import io.testomat.core.annotation.Step;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestomatAgent {

    private static final Logger log = LoggerFactory.getLogger(TestomatAgent.class);
    private static volatile boolean installed = false;

    public static void install() {
        if (installed) return;
        installed = true;

        try {
            var instrumentation = ByteBuddyAgent.install();

            new AgentBuilder.Default()
                .type(ElementMatchers.declaresMethod(ElementMatchers.isAnnotatedWith(Step.class)))
                .transform((builder, type, classLoader, module, protectionDomain) ->
                    builder.visit(Advice.to(StepAdvice.class)
                        .on(ElementMatchers.isAnnotatedWith(Step.class)))
                )
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .installOn(instrumentation);

            new AgentBuilder.Default()
                .type(ElementMatchers.declaresMethod(ElementMatchers.isAnnotatedWith(Artifact.class)))
                .transform((builder, type, classLoader, module, protectionDomain) ->
                    builder.visit(Advice.to(ArtifactAdvice.class)
                        .on(ElementMatchers.isAnnotatedWith(Artifact.class)))
                )
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .installOn(instrumentation);

            retransformLoadedClasses(instrumentation);

            log.info("Testomat.io Byte Buddy agent installed successfully");
        } catch (Throwable t) {
            log.error("FATAL: Failed to install Byte Buddy agent, @Step and @Artifact annotations will not be intercepted", t);
        }
    }

    private static void retransformLoadedClasses(Instrumentation instrumentation) {
        Class<?>[] loadedClasses = instrumentation.getAllLoadedClasses();
        int count = 0;
        for (Class<?> clazz : loadedClasses) {
            try {
                if (isApplicable(clazz)) {
                    instrumentation.retransformClasses(clazz);
                    count++;
                }
            } catch (Throwable e) {
                log.debug("Could not retransform class {}", clazz.getName(), e);
            }
        }
        if (count > 0) {
            log.debug("Retransformed {} already-loaded classes for @Step/@Artifact", count);
        }
    }

    private static boolean isApplicable(Class<?> clazz) {
        if (clazz.isInterface() || clazz.isAnnotation() || clazz.isEnum() || clazz.isPrimitive()) return false;
        for (var method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Step.class) || method.isAnnotationPresent(Artifact.class)) {
                return true;
            }
        }
        return false;
    }
}
