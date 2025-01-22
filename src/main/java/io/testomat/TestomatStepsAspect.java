package io.testomat;

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


@Aspect
public class TestomatStepsAspect {

  @Pointcut("@annotation(org.testng.annotations.Test) || @annotation(org.junit.Test)")
  public void testMethod() {
  }

  @Pointcut("@annotation(io.testomat.annotation.Step)")
  public void stepMethod() {
  }

  @Before("testMethod()")
  public void beforeTestMethod(JoinPoint joinPoint) {
    TestomatStorage.currentStep.remove();
  }


  @Before("stepMethod()")
  public void beforeStep(JoinPoint joinPoint) {
    TStepResult parentStep = TestomatStorage.currentStep.get();
    TStepResult step = new TStepResult(joinPoint.getSignature().getName(), parentStep);
    step.setParameters(parseParameters(joinPoint));
    step.setArguments(Arrays.asList(joinPoint.getArgs()));
    step.startTime();
    if (parentStep != null) {
      parentStep.addInnerStep(step);
    }
    TestomatStorage.currentStep.set(step);
  }

  @AfterReturning("stepMethod()")
  public void afterStep(JoinPoint joinPoint) {
    finishCurrentStep("passed");
  }

  @AfterThrowing(pointcut = "stepMethod()", throwing = "ex")
  public void afterStepFailure(JoinPoint joinPoint, Throwable ex) {
    finishCurrentStep("failed");
  }

  private void finishCurrentStep(String status) {
    var tStepResult = TestomatStorage.currentStep.get();
    if (tStepResult != null) {
      tStepResult.setStatus(status);
      tStepResult.stopTime();
      TestomatStorage.currentStep.set(tStepResult.getParent());
    }
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
