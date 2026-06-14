package io.testomat.core.facade.methods.artifact;

import io.testomat.core.facade.Testomatio;
import io.testomat.core.step.StepLifecycle;
import io.testomat.core.step.TestStep;
import java.io.File;
import java.nio.file.Path;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class ArtifactAspect {
    private static final Logger log = LoggerFactory.getLogger(ArtifactAspect.class);

    @AfterReturning(
        pointcut = "@annotation(io.testomat.core.annotation.Artifact)",
        returning = "result"
    )
    public void afterArtifact(Object result) {
        String fileName = resolveFileName(result);
        if (fileName == null) {
            return;
        }

        TestStep testStep = StepLifecycle.current();
        if (testStep == null) {
            testStep = StepLifecycle.lastFinished();
        }
        if (testStep == null || testStep.getId() == null) {
            Testomatio.artifact(fileName);
        } else {
            Testomatio.stepArtifact(fileName);
        }
    }

    private String resolveFileName(Object result) {
        if (result instanceof String) {
            return (String) result;
        }
        if (result instanceof Path) {
            return ((Path) result).toString();
        }
        if (result instanceof File) {
            return ((File) result).getAbsolutePath();
        }

        log.warn(
            "@Artifact ignored: method returned unsupported type '{}'. "
                + "Supported types are String, Path and File.",
            result == null ? "null" : result.getClass().getName()
        );

        return null;
    }
}
