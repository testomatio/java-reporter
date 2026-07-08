package io.testomat.junit.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.launcher.TestPlan;

public abstract class AbstractHookContainer {
    private static final List<TestomatHook> HOOKS;

    static {
        List<TestomatHook> hooks = new ArrayList<>();
        ServiceLoader.load(TestomatHook.class)
                .forEach(hooks::add);

        HOOKS = Collections.unmodifiableList(hooks);
    }

    protected void onSuiteStartHookAfterExecution(ExtensionContext context) {
        for (TestomatHook hook : HOOKS) {
            hook.onSuiteStartHookAfterExecution(context);
        }
    }

    protected void onSuiteStartHookBeforeExecution(ExtensionContext context) {
        for (TestomatHook hook : HOOKS) {
            hook.onSuiteStartHookBeforeExecution(context);
        }
    }

    protected void onSuiteFinishHookAfterExecution(ExtensionContext context) {
        for (TestomatHook hook : HOOKS) {
            hook.onSuiteFinishHookAfterExecution(context);
        }
    }

    protected void onSuiteFinishHookBeforeExecution(ExtensionContext context) {
        for (TestomatHook hook : HOOKS) {
            hook.onSuiteFinishHookBeforeExecution(context);
        }
    }

    protected void beforeEachHookAfterExecution(ExtensionContext context) {
        for (TestomatHook hook : HOOKS) {
            hook.beforeEachHookAfterExecution(context);
        }
    }

    protected void beforeEachHookBeforeExecution(ExtensionContext context) {
        for (TestomatHook hook : HOOKS) {
            hook.beforeEachHookBeforeExecution(context);
        }
    }

    protected void onTestSuccessHookAfterExecution(ExtensionContext context) {
        for (TestomatHook hook : HOOKS) {
            hook.onTestSuccessHookAfterExecution(context);
        }
    }

    protected void onTestSuccessHookBeforeExecution(ExtensionContext context) {
        for (TestomatHook hook : HOOKS) {
            hook.onTestSuccessHookBeforeExecution(context);
        }
    }

    protected void onTestFailureHookAfterExecution(ExtensionContext context, Throwable cause) {
        for (TestomatHook hook : HOOKS) {
            hook.onTestFailureHookAfterExecution(context, cause);
        }
    }

    protected void onTestFailureHookBeforeExecution(ExtensionContext context, Throwable cause) {
        for (TestomatHook hook : HOOKS) {
            hook.onTestFailureHookBeforeExecution(context, cause);
        }
    }

    protected void onTestDisabledHookAfterExecution(ExtensionContext context,
                                                    Optional<String> reason) {
        for (TestomatHook hook : HOOKS) {
            hook.onTestDisabledHookAfterExecution(context, reason);
        }
    }

    protected void onTestDisabledHookBeforeExecution(ExtensionContext context,
                                                     Optional<String> reason) {
        for (TestomatHook hook : HOOKS) {
            hook.onTestDisabledHookBeforeExecution(context, reason);
        }
    }

    protected void onTestAbortedHookAfterExecution(ExtensionContext context, Throwable cause) {
        for (TestomatHook hook : HOOKS) {
            hook.onTestAbortedHookAfterExecution(context, cause);
        }
    }

    protected void onTestAbortedHookBeforeExecution(ExtensionContext context, Throwable cause) {
        for (TestomatHook hook : HOOKS) {
            hook.onTestAbortedHookBeforeExecution(context, cause);
        }
    }

    protected void afterEachHookAfterExecution(ExtensionContext context) {
        for (TestomatHook hook : HOOKS) {
            hook.afterEachHookAfterExecution(context);
        }
    }

    protected void afterEachHookBeforeExecution(ExtensionContext context) {
        for (TestomatHook hook : HOOKS) {
            hook.afterEachHookBeforeExecution(context);
        }
    }

    protected void onExecutionStartHookAfterExecution(TestPlan testPlan) {
        for (TestomatHook hook : HOOKS) {
            hook.onExecutionStartHookAfterExecution(testPlan);
        }
    }

    protected void onExecutionStartHookBeforeExecution(TestPlan testPlan) {
        for (TestomatHook hook : HOOKS) {
            hook.onExecutionStartHookBeforeExecution(testPlan);
        }
    }

    protected void onExecutionFinishHookAfterExecution(TestPlan testPlan) {
        for (TestomatHook hook : HOOKS) {
            hook.onExecutionFinishHookAfterExecution(testPlan);
        }
    }

    protected void onExecutionFinishHookBeforeExecution(TestPlan testPlan) {
        for (TestomatHook hook : HOOKS) {
            hook.onExecutionFinishHookBeforeExecution(testPlan);
        }
    }
}
