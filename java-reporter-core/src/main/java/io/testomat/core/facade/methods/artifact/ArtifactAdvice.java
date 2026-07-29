package io.testomat.core.facade.methods.artifact;

import io.testomat.core.facade.ServiceRegistryUtil;
import io.testomat.core.facade.methods.artifact.manager.ArtifactManager;
import io.testomat.core.step.StepLifecycle;
import io.testomat.core.step.TestStep;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import net.bytebuddy.asm.Advice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArtifactAdvice {

    public static final Logger log = LoggerFactory.getLogger(ArtifactAdvice.class);

    @Advice.OnMethodExit
    public static void exit(@Advice.Return Object result, @Advice.Origin Method method) {
        String fileName = resolveFileName(result);
        if (fileName == null) return;

        TestStep testStep = StepLifecycle.current();
        if (testStep == null) {
            testStep = StepLifecycle.lastFinished();
        }
        ArtifactManager artifactManager = ServiceRegistryUtil.getService(ArtifactManager.class);
        if (testStep == null || testStep.getId() == null) {
            artifactManager.storeDirectories(fileName);
            log.debug("@Artifact uploaded");
        } else {
            artifactManager.storeStepDirectories(testStep.getId(), fileName);
            log.debug("@Artifact uploaded to step");
        }
    }

    public static String resolveFileName(Object result) {
        if (result instanceof String) {
            return (String) result;
        }
        if (result instanceof Path) {
            return ((Path) result).toString();
        }
        if (result instanceof File) {
            return ((File) result).getAbsolutePath();
        }

        log.debug(
            "@Artifact ignored: method returned unsupported type '{}'. "
                + "Supported types are String, Path and File.",
            result == null ? "null" : result.getClass().getName()
        );

        return null;
    }
}
