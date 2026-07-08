package io.testomat.aspect;

import io.testomat.resolver.AllureTmsResolver;
import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class AllureTmsLinkAspect {

    private final AllureTmsResolver resolver;

    public AllureTmsLinkAspect() {
        this(new AllureTmsResolver());
    }

    public AllureTmsLinkAspect(AllureTmsResolver resolver) {
        this.resolver = resolver;
    }

    @Around("execution(String io.testomat.*.extractor..*.*TestId(java.lang.reflect.Method))")
    public Object intercept(ProceedingJoinPoint pjp) throws Throwable {
        String result = (String) pjp.proceed();
        if (result != null && !result.isBlank()) {
            return result;
        }
        Method method = (Method) pjp.getArgs()[0];
        String tmsId = resolver.resolve(method);
        return tmsId != null ? tmsId : result;
    }
}
