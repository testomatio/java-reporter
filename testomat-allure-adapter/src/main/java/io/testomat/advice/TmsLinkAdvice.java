package io.testomat.advice;

import io.testomat.resolver.AllureTmsResolver;
import java.lang.reflect.Method;
import net.bytebuddy.asm.Advice;

/** Supplies a Testomat TMS id when an Allure extractor has none. */
public final class TmsLinkAdvice {

    public static final AllureTmsResolver resolver = new AllureTmsResolver();

    private TmsLinkAdvice() {
    }

    @Advice.OnMethodExit
    public static void onExit(@Advice.Argument(0) Method method,
            @Advice.Return(readOnly = false) String returned) {
        returned = resolve(method, returned, resolver);
    }

    public static String resolve(Method method, String returned, AllureTmsResolver tmsResolver) {
        if (returned != null && !returned.isBlank()) {
            return returned;
        }

        String tmsId = tmsResolver.resolve(method);
        return tmsId != null ? tmsId : returned;
    }
}
