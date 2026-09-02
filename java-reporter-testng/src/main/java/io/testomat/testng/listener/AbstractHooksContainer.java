package io.testomat.testng.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IInvokedMethod;
import org.testng.ISuite;
import org.testng.ITestResult;

public abstract class AbstractHooksContainer {

    private static final Logger log = LoggerFactory.getLogger(AbstractHooksContainer.class);

    private static final List<TestomatHook> HOOKS;

    static {
        List<TestomatHook> hooks = new ArrayList<>();
        ServiceLoader.load(TestomatHook.class)
                .forEach(hooks::add);

        HOOKS = Collections.unmodifiableList(hooks);
    }

    protected boolean onSuiteStartHookAfterExecution(ISuite suite) {
        return runHooks("onSuiteStartHookAfterExecution",
                hook -> hook.onSuiteStartHookAfterExecution(suite));
    }

    protected boolean onSuiteStartHookBeforeExecution(ISuite suite) {
        return runHooks("onSuiteStartHookBeforeExecution",
                hook -> hook.onSuiteStartHookBeforeExecution(suite));
    }

    protected void onSuiteFinishHookAfterExecution(ISuite suite) {
        runHooks("onSuiteFinishHookAfterExecution",
                hook -> hook.onSuiteFinishHookAfterExecution(suite));
    }

    protected void onSuiteFinishHookBeforeExecution(ISuite suite) {
        runHooks("onSuiteFinishHookBeforeExecution",
                hook -> hook.onSuiteFinishHookBeforeExecution(suite));
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

    private boolean runHooks(String hookName, Consumer<TestomatHook> action) {
        boolean failed = false;
        for (TestomatHook hook : HOOKS) {
            try {
                action.accept(hook);
            } catch (Exception e) {
                log.error("Hook '{}' failed: {}", hookName, e.getMessage(), e);
                failed = true;
            }
        }
        return failed;
    }
}
