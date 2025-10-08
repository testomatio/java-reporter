package io.testomat.core;

import static io.testomat.core.constants.CommonConstants.REPORTER_VERSION;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfoDisplay {
    private static final Logger log = LoggerFactory.getLogger(InfoDisplay.class);

    public static void logVersionAndPrintUrls(Map<String, Object> responseBody) {
        Object publicUrlObject = responseBody.get("public_url");

        log.info("[TESTOMATIO] Testomat.io java core reporter version: [{}]", REPORTER_VERSION);

        if (publicUrlObject != null) {
            log.info("[TESTOMATIO] Public url: {}", publicUrlObject.toString());
        }

        log.info("[TESTOMATIO] See run aggregation at: {}", responseBody.get("url"));
    }
}
