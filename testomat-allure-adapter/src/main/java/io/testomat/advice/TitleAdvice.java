package io.testomat.advice;

import io.testomat.resolver.TestTitleResolver;
import java.lang.reflect.Method;
import net.bytebuddy.asm.Advice;

/** Replaces framework-specific titles with the resolved Testomat title. */
public final class TitleAdvice {

    private TitleAdvice() {
    }

    @Advice.OnMethodExit
    public static void onExit(@Advice.Argument(0) Method method,
            @Advice.Return(readOnly = false) String returned) {
        returned = resolve(method);
    }

    public static String resolve(Method method) {
        return TestTitleResolver.resolve(method);
    }
}
