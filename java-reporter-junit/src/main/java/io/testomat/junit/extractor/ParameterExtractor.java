package io.testomat.junit.extractor;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParameterExtractor {

    private static final Logger logger = LoggerFactory.getLogger(ParameterExtractor.class);
    private static final ParameterParser parser = new ParameterParser();

    public static Object extractTestParameters(ExtensionContext context) {
        if (!isParameterizedTest(context)) {
            return null;
        }

        try {
            Method method = context.getTestMethod().orElse(null);
            if (method == null) {
                return null;
            }

            Object[] parameters = tryExtractParameters(context);
            return parameters != null ? formatParameters(parameters, method) : null;

        } catch (Exception e) {
            logger.debug("Parameter extraction failed", e);
            return null;
        }
    }

    public static boolean isParameterizedTest(ExtensionContext context) {
        try {
            Method method = context.getTestMethod().orElse(null);
            return method != null && method.isAnnotationPresent(ParameterizedTest.class);
        } catch (Exception e) {
            return false;
        }
    }

    private static Object[] tryExtractParameters(ExtensionContext context) {
        Object[] parameters = InvocationInterceptor.getCapturedParameters(context);
        if (parameters != null && parameters.length > 0) {
            return parameters;
        }

        parameters = ParameterCapture.getCapturedParameters(context);
        if (parameters != null && parameters.length > 0) {
            return parameters;
        }

        return parser.parseParametersFromDisplayName(context.getDisplayName());
    }

    private static Object formatParameters(Object[] parameters, Method method) {
        if (parameters == null || parameters.length == 0) {
            return null;
        }

        if (parameters.length == 1) {
            return parameters[0];
        }

        return buildParameterMap(parameters, method);
    }

    private static Map<String, Object> buildParameterMap(Object[] parameters, Method method) {
        Map<String, Object> paramMap = new HashMap<>();
        Parameter[] methodParams = method.getParameters();
        
        for (int i = 0; i < parameters.length; i++) {
            String paramName = getParameterName(methodParams, i);
            paramMap.put(paramName, parameters[i]);
        }

        return paramMap;
    }

    private static String getParameterName(Parameter[] methodParams, int index) {
        if (index < methodParams.length && methodParams[index].isNamePresent()) {
            String name = methodParams[index].getName();
            return name.matches("arg\\d+") ? "param" + index : name;
        }
        return "param" + index;
    }
}