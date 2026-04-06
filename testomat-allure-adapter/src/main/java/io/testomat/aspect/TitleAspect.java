package io.testomat.aspect;

import io.testomat.resolver.TestTitleResolver;
import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class TitleAspect {

    @Around("execution(String io.testomat..*Title*(java.lang.reflect.Method))")
    public Object intercept(ProceedingJoinPoint pjp) {
        Method method = (Method) pjp.getArgs()[0];
        return TestTitleResolver.resolve(method);
    }

}
