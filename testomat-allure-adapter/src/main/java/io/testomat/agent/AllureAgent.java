package io.testomat.agent;

import io.testomat.advice.AttachmentAdvice;
import io.testomat.advice.TitleAdvice;
import io.testomat.advice.TmsLinkAdvice;
import java.lang.instrument.Instrumentation;
import java.util.concurrent.atomic.AtomicBoolean;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Installs the runtime transformations required by the Allure adapter. */
public final class AllureAgent {

    private static final Logger log = LoggerFactory.getLogger(AllureAgent.class);
    private static final AtomicBoolean installed = new AtomicBoolean();

    private AllureAgent() {
    }

    /** Installs transformations once and retransforms already loaded target classes. */
    public static void install() {
        if (!installed.compareAndSet(false, true)) {
            return;
        }

        try {
            Instrumentation instrumentation = ByteBuddyAgent.install();
            AgentBuilder builder = new AgentBuilder.Default()
                    .disableClassFormatChanges()
                    .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION);

            builder.type(ElementMatchers.named("io.qameta.allure.AllureLifecycle"))
                    .transform((type, description, loader, module, domain) -> type
                            .visit(Advice.to(AttachmentAdvice.Prepare.class)
                                    .on(ElementMatchers.named("prepareAttachment")))
                            .visit(Advice.to(AttachmentAdvice.Write.class)
                                    .on(ElementMatchers.named("writeAttachment"))))
                    .installOn(instrumentation);

            builder.type(ElementMatchers.nameStartsWith("io.testomat.")
                    .and(ElementMatchers.nameContains(".extractor.")))
                    .transform((type, description, loader, module, domain) -> type
                            .visit(Advice.to(TmsLinkAdvice.class)
                                    .on(ElementMatchers.nameContains("TestId")
                                            .and(ElementMatchers.returns(String.class))
                                            .and(ElementMatchers.takesArgument(0,
                                                    java.lang.reflect.Method.class)))))
                    .installOn(instrumentation);

            builder.type(ElementMatchers.nameStartsWith("io.testomat."))
                    .transform((type, description, loader, module, domain) -> type
                            .visit(Advice.to(TitleAdvice.class)
                                    .on(ElementMatchers.nameContains("Title")
                                            .and(ElementMatchers.returns(String.class))
                                            .and(ElementMatchers.takesArgument(0,
                                                    java.lang.reflect.Method.class)))))
                    .installOn(instrumentation);

            log.debug("Installed Allure Byte Buddy transformations");
        } catch (Throwable e) {
            installed.set(false);
            log.debug("Failed to install Allure Byte Buddy transformations", e);
        }
    }
}
