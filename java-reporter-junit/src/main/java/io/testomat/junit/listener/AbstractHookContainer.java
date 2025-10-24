package io.testomat.junit.listener;

import java.util.Optional;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.launcher.TestPlan;

public abstract class AbstractHookContainer {
    protected void onSuiteStartHookAfterExecution(ExtensionContext context) {
    }

    protected void onSuiteStartHookBeforeExecution(ExtensionContext context) {
    }

    protected void onSuiteFinishHookAfterExecution(ExtensionContext context) {
    }

    protected void onSuiteFinishHookBeforeExecution(ExtensionContext context) {
    }

    protected void beforeEachHookAfterExecution(ExtensionContext context) {
    }

    protected void beforeEachHookBeforeExecution(ExtensionContext context) {
    }

    protected void onTestSuccessHookAfterExecution(ExtensionContext context) {
    }

    protected void onTestSuccessHookBeforeExecution(ExtensionContext context) {
    }

    protected void onTestFailureHookAfterExecution(ExtensionContext context, Throwable cause) {
    }

    protected void onTestFailureHookBeforeExecution(ExtensionContext context, Throwable cause) {
    }

    protected void onTestDisabledHookAfterExecution(ExtensionContext context,
                                                    Optional<String> reason) {
    }

    protected void onTestDisabledHookBeforeExecution(ExtensionContext context,
                                                     Optional<String> reason) {
    }

    protected void onTestAbortedHookAfterExecution(ExtensionContext context, Throwable cause) {
    }

    protected void onTestAbortedHookBeforeExecution(ExtensionContext context, Throwable cause) {
    }

    protected void afterEachHookAfterExecution(ExtensionContext context) {
    }

    protected void afterEachHookBeforeExecution(ExtensionContext context) {
    }

    protected void onExecutionStartHookAfterExecution(TestPlan testPlan) {
    }

    protected void onExecutionStartHookBeforeExecution(TestPlan testPlan) {
    }

    protected void onExecutionFinishHookAfterExecution(TestPlan testPlan) {
    }

    protected void onExecutionFinishHookBeforeExecution(TestPlan testPlan) {
    }
}
