package io.testomat.testng.listener;

import org.testng.IInvokedMethod;
import org.testng.ISuite;
import org.testng.ITestResult;

public abstract class AbstractHooksContainer {

    protected void onSuiteStartHookAfterExecution(ISuite suite) {
    }

    protected void onSuiteStartHookBeforeExecution(ISuite suite) {
    }

    protected void onSuiteFinishHookAfterExecution(ISuite suite) {
    }

    protected void onSuiteFinishHookBeforeExecution(ISuite suite) {
    }

    protected void onTestSuccessHookAfterExecution(ITestResult result) {
    }

    protected void onTestSuccessHookBeforeExecution(ITestResult result) {
    }

    protected void onTestFailureHookAfterExecution(ITestResult result) {
    }

    protected void onTestFailureHookBeforeExecution(ITestResult result) {
    }

    protected void onTestSkippedHookAfterExecution(ITestResult result) {
    }

    protected void onTestSkippedHookBeforeExecution(ITestResult result) {
    }

    protected void onTestStartHookAfterExecution(ITestResult result) {
    }

    protected void onTestStartHookBeforeExecution(ITestResult result) {
    }

    protected void beforeInvocationHookAfterExecution(IInvokedMethod method,
                                                      ITestResult testResult) {
    }

    protected void beforeInvocationHookBeforeExecution(IInvokedMethod method,
                                                       ITestResult testResult) {
    }

    protected void afterInvocationHookAfterExecution(IInvokedMethod method,
                                                     ITestResult testResult) {
    }

    protected void afterInvocationHookBeforeExecution(IInvokedMethod method,
                                                      ITestResult testResult) {
    }

    protected void onExecutionStartHookAfterExecution() {
    }

    protected void onExecutionStartHookBeforeExecution() {
    }

    protected void onExecutionFinishHookAfterExecution() {
    }

    protected void onExecutionFinishHookBeforeExecution() {
    }
}
