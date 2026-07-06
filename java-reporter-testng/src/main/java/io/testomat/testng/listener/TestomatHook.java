package io.testomat.testng.listener;

import org.testng.IInvokedMethod;
import org.testng.ISuite;
import org.testng.ITestResult;

public interface TestomatHook {

    default void onSuiteStartHookBeforeExecution(ISuite suite) {

    }

    default void onSuiteStartHookAfterExecution(ISuite suite) {

    }

    default void onSuiteFinishHookBeforeExecution(ISuite suite) {

    }

    default void onSuiteFinishHookAfterExecution(ISuite suite) {

    }

    default void onTestStartHookBeforeExecution(ITestResult result) {

    }

    default void onTestStartHookAfterExecution(ITestResult result) {

    }

    default void onTestSuccessHookBeforeExecution(ITestResult result) {

    }

    default void onTestSuccessHookAfterExecution(ITestResult result) {

    }

    default void onTestFailureHookBeforeExecution(ITestResult result) {

    }

    default void onTestFailureHookAfterExecution(ITestResult result) {

    }

    default void onTestSkippedHookBeforeExecution(ITestResult result) {

    }

    default void onTestSkippedHookAfterExecution(ITestResult result) {

    }

    default void beforeInvocationHookBeforeExecution(IInvokedMethod method, ITestResult result) {

    }

    default void beforeInvocationHookAfterExecution(IInvokedMethod method, ITestResult result) {

    }

    default void afterInvocationHookBeforeExecution(IInvokedMethod method, ITestResult result) {

    }

    default void afterInvocationAfter(IInvokedMethod method, ITestResult result) {

    }

    default void onExecutionStartHookBeforeExecution() {

    }

    default void onExecutionStartHookAfterExecution() {

    }

    default void onExecutionFinishHookBeforeExecution() {

    }

    default void onExecutionFinishHookAfterExecution() {

    }
}
