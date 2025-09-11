package io.testomat.core.util;

import io.testomat.core.exception.ArtifactManagementException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RidGeneratorUtil {
    private static final Logger log = LoggerFactory.getLogger(RidGeneratorUtil.class);
    private static final String MD5_ALGORITHM = "MD5";

    public static String generate(Method method) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance(MD5_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            log.debug("Failed to implement MessageDigest algorithm: " + MD5_ALGORITHM);
            throw new ArtifactManagementException("No such algorithm for MessageDigest: "
                    + MD5_ALGORITHM);
        }
        String identifier = method.getDeclaringClass().getCanonicalName() + "." + method.getName();
        byte[] hash = messageDigest.digest(identifier.getBytes(StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }

        return method.getName() + ":" + sb;
    }
}
