package io.testomat;

import io.testomat.model.TStepResult;

/**
 * Created by Lolik on 29.01.2025
 */
class Stepper {

  static void startStep(String title, String status){
    TStepResult parentStep = Testomat.getCurrentTestResult().getCurrentStep();
    TStepResult step = new TStepResult(title, parentStep);
    step.setStatus(status);
    step.startTime();
    Testomat.getCurrentTestResult().setCurrentStep(step);
    if (parentStep == null) {
      Testomat.getCurrentTestResult().getSteps().add(step);
    } else {
      parentStep.addInnerStep(step);
    }
  }

  static void stopStep(){
    var tStepResult = Testomat.getCurrentTestResult().getCurrentStep();
    tStepResult.setStatus("passed");
    tStepResult.stopTime();
    Testomat.getCurrentTestResult().setCurrentStep(tStepResult.getParent());
  }

}
