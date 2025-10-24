package io.testomat.cucumber.listener;

import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;

public abstract class AbstractHooksContainer {

    protected void onTestRunStartedHookAfterExecution(TestRunStarted event) {
    }

    protected void onTestRunStartedHookBeforeExecution(TestRunStarted event) {
    }

    protected void onTestRunFinishedHookAfterExecution(TestRunFinished event) {
    }

    protected void onTestRunFinishedHookBeforeExecution(TestRunFinished event) {
    }

    protected void onTestCaseFinishedHookAfterExecution(TestCaseFinished event) {
    }

    protected void onTestCaseFinishedHookBeforeExecution(TestCaseFinished event) {
    }

    protected void onTestCaseFinishedHookFinally(TestCaseFinished event) {
    }

    protected void afterEachHookAfterExecution(TestCaseFinished event) {
    }

    protected void afterEachHookBeforeExecution(TestCaseFinished event) {
    }
}
