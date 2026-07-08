package io.testomat.testng.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import org.testng.IInvokedMethod;
import org.testng.ISuite;
import org.testng.ITestResult;

public abstract class AbstractHooksContainer {

    private static final List<TestomatHook> HOOKS;

    static {
        List<TestomatHook> hooks = new ArrayList<>();
        ServiceLoader.load(TestomatHook.class)
                .forEach(hooks::add);

        HOOKS = Collections.unmodifiableList(hooks);
    }

    protected void onSuiteStartHookAfterExecution(ISuite suite) {
        for (TestomatHook hook : HOOKS) {
            hook.onSuiteStartHookAfterExecution(suite);
        }
    }

    protected void onSuiteStartHookBeforeExecution(ISuite suite) {
        for (TestomatHook hook : HOOKS) {
            hook.onSuiteStartHookBeforeExecution(suite);
        }
    }

    protected void onSuiteFinishHookAfterExecution(ISuite suite) {
        for (TestomatHook hook : HOOKS) {
            hook.onSuiteFinishHookAfterExecution(suite);
        }
    }

    protected void onSuiteFinishHookBeforeExecution(ISuite suite) {
        for (TestomatHook hook : HOOKS) {
            hook.onSuiteFinishHookBeforeExecution(suite);
        }
    }

    protected void onTestSuccessHookAfterExecution(ITestResult result) {
        for (TestomatHook hook : HOOKS) {
            hook.onTestSuccessHookAfterExecution(result);
        }
    }

    protected void onTestSuccessHookBeforeExecution(ITestResult result) {
        for (TestomatHook hook : HOOKS) {
            hook.onTestSuccessHookBeforeExecution(result);
        }
    }

    protected void onTestFailureHookAfterExecution(ITestResult result) {
        for (TestomatHook hook : HOOKS) {
            hook.onTestFailureHookAfterExecution(result);
        }
    }

    protected void onTestFailureHookBeforeExecution(ITestResult result) {
        for (TestomatHook hook : HOOKS) {
            hook.onTestFailureHookBeforeExecution(result);
        }
    }

    protected void onTestSkippedHookAfterExecution(ITestResult result) {
        for (TestomatHook hook : HOOKS) {
            hook.onTestSkippedHookAfterExecution(result);
        }
    }

    protected void onTestSkippedHookBeforeExecution(ITestResult result) {
        for (TestomatHook hook : HOOKS) {
            hook.onTestSkippedHookBeforeExecution(result);
        }
    }

    protected void onTestStartHookAfterExecution(ITestResult result) {
        for (TestomatHook hook : HOOKS) {
            hook.onTestStartHookAfterExecution(result);
        }
    }

    protected void onTestStartHookBeforeExecution(ITestResult result) {
        for (TestomatHook hook : HOOKS) {
            hook.onTestStartHookBeforeExecution(result);
        }
    }

    protected void beforeInvocationHookAfterExecution(IInvokedMethod method,
                                                      ITestResult testResult) {
        for (TestomatHook hook : HOOKS) {
            hook.beforeInvocationHookAfterExecution(method, testResult);
        }
    }

    protected void beforeInvocationHookBeforeExecution(IInvokedMethod method,
                                                       ITestResult testResult) {
        for (TestomatHook hook : HOOKS) {
            hook.beforeInvocationHookBeforeExecution(method, testResult);
        }
    }

    protected void afterInvocationHookAfterExecution(IInvokedMethod method,
                                                     ITestResult testResult) {
        for (TestomatHook hook : HOOKS) {
            hook.afterInvocationAfter(method, testResult);
        }
    }

    protected void afterInvocationHookBeforeExecution(IInvokedMethod method,
                                                      ITestResult testResult) {
        for (TestomatHook hook : HOOKS) {
            hook.afterInvocationHookBeforeExecution(method, testResult);
        }
    }

    protected void onExecutionStartHookAfterExecution() {
        for (TestomatHook hook : HOOKS) {
            hook.onExecutionStartHookAfterExecution();
        }
    }

    protected void onExecutionStartHookBeforeExecution() {
        for (TestomatHook hook : HOOKS) {
            hook.onExecutionStartHookBeforeExecution();
        }
    }

    protected void onExecutionFinishHookAfterExecution() {
        for (TestomatHook hook : HOOKS) {
            hook.onExecutionFinishHookAfterExecution();
        }
    }

    protected void onExecutionFinishHookBeforeExecution() {
        for (TestomatHook hook : HOOKS) {
            hook.onExecutionFinishHookBeforeExecution();
        }
    }
}
