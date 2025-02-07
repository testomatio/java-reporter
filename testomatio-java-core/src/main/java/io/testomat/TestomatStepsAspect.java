package io.testomat;

import io.testomat.model.TStepResult;
import io.testomat.utils.StringFormatterUtils;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Aspect
public class TestomatStepsAspect {

  private static final Logger logger = LoggerFactory.getLogger(TestomatStepsAspect.class);

  @Pointcut("@annotation(io.testomat.annotation.Step) || @annotation(io.qameta.allure.Step)")
  public void stepMethod() {
  }

  @Pointcut("execution(* *(..))")
  public void anyMethod() {
  }

  @Before("anyMethod() && stepMethod()")
  public void beforeStep(JoinPoint joinPoint) {
    logger.info("Step started: " + joinPoint.getSignature().getName());
    TStepResult parentStep = Testomat.getCurrentTestResult().getCurrentStep();
    TStepResult step = new TStepResult(StringFormatterUtils.capitalizeAndSplit(joinPoint.getSignature().getName()),
        parentStep);
    step.setParameters(parseParameters(joinPoint));
    step.setArguments(Arrays.asList(joinPoint.getArgs()));
    step.startTime();
    Testomat.getCurrentTestResult().setCurrentStep(step);
    if (parentStep == null) {
      Testomat.getCurrentTestResult().getSteps().add(step);
    } else {
      parentStep.addInnerStep(step);
    }
  }

  @AfterReturning("anyMethod() && stepMethod()")
  public void afterStep(JoinPoint joinPoint) {
    var tStepResult = Testomat.getCurrentTestResult().getCurrentStep();
    tStepResult.setStatus("passed");
    tStepResult.stopTime();
    Testomat.getCurrentTestResult().setCurrentStep(tStepResult.getParent());
  }

  @AfterThrowing(pointcut = "anyMethod() && stepMethod()", throwing = "ex")
  public void afterStepFailure(JoinPoint joinPoint, Throwable ex) {
    var tStepResult = Testomat.getCurrentTestResult().getCurrentStep();
    tStepResult.setStatus("failed");
    tStepResult.stopTime();
    tStepResult.setError(ex.getMessage());
    Testomat.getCurrentTestResult().setCurrentStep(tStepResult.getParent());
  }

  private Map<String, Object> parseParameters(JoinPoint joinPoint) {
    Map<String, Object> parameters = new LinkedHashMap<>();
    String[] parameterNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
    Object[] args = joinPoint.getArgs();
    for (int i = 0; i < args.length; i++) {
      parameters.put(parameterNames[i], args[i]);
    }
    return parameters;
  }

}
