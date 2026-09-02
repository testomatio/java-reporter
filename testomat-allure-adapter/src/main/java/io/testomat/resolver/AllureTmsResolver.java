package io.testomat.resolver;

import io.qameta.allure.TmsLink;
import java.lang.reflect.Method;

public class AllureTmsResolver implements TestMetadataResolver {

    @Override
    public String resolve(Method method) {
        TmsLink tmsLink = method.getAnnotation(TmsLink.class);
        if (tmsLink != null) {
            return tmsLink.value();
        }
        return null;
    }
}
