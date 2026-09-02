package io.testomat.junit.listener;

import java.util.Optional;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.launcher.TestPlan;

public interface TestomatHook {

    default void onSuiteStartHookAfterExecution(ExtensionContext context) {
    }

    default void onSuiteStartHookBeforeExecution(ExtensionContext context) {
    }

    default void onSuiteFinishHookAfterExecution(ExtensionContext context) {
    }

    default void onSuiteFinishHookBeforeExecution(ExtensionContext context) {
    }

    default void beforeEachHookAfterExecution(ExtensionContext context) {
    }

    default void beforeEachHookBeforeExecution(ExtensionContext context) {
    }

    default void onTestSuccessHookAfterExecution(ExtensionContext context) {
    }

    default void onTestSuccessHookBeforeExecution(ExtensionContext context) {
    }

    default void onTestFailureHookAfterExecution(ExtensionContext context, Throwable cause) {
    }

    default void onTestFailureHookBeforeExecution(ExtensionContext context, Throwable cause) {
    }

    default void onTestDisabledHookAfterExecution(ExtensionContext context,
            Optional<String> reason) {
    }

    default void onTestDisabledHookBeforeExecution(ExtensionContext context,
            Optional<String> reason) {
    }

    default void onTestAbortedHookAfterExecution(ExtensionContext context, Throwable cause) {
    }

    default void onTestAbortedHookBeforeExecution(ExtensionContext context, Throwable cause) {
    }

    default void afterEachHookAfterExecution(ExtensionContext context) {
    }

    default void afterEachHookBeforeExecution(ExtensionContext context) {
    }

    default void onExecutionStartHookAfterExecution(TestPlan testPlan) {
    }

    default void onExecutionStartHookBeforeExecution(TestPlan testPlan) {
    }

    default void onExecutionFinishHookAfterExecution(TestPlan testPlan) {
    }

    default void onExecutionFinishHookBeforeExecution(TestPlan testPlan) {
    }
}
