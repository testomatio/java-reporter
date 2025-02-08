package io.testomat;

import io.testomat.model.TStepResult;

/**
 * Created by Lolik on 29.01.2025
 */
class Stepper {

  static void startStep(String title, String status){
    TStepResult parentStep = Testomatio.getCurrentTestResult().getCurrentStep();
    TStepResult step = new TStepResult(title, parentStep);
    step.setStatus(status);
    step.startTime();
    Testomatio.getCurrentTestResult().setCurrentStep(step);
    if (parentStep == null) {
      Testomatio.getCurrentTestResult().getSteps().add(step);
    } else {
      parentStep.addInnerStep(step);
    }
  }

  static void stopStep(){
    var tStepResult = Testomatio.getCurrentTestResult().getCurrentStep();
    tStepResult.setStatus("passed");
    tStepResult.stopTime();
    Testomatio.getCurrentTestResult().setCurrentStep(tStepResult.getParent());
  }

}
