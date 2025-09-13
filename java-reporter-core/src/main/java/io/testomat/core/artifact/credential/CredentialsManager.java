package io.testomat.core.artifact.credential;

import static io.testomat.core.constants.CredentialConstants.ACCESS_KEY_PROPERTY_NAME;
import static io.testomat.core.constants.CredentialConstants.BUCKET_PROPERTY_NAME;
import static io.testomat.core.constants.CredentialConstants.IAM_PROPERTY_NAME;
import static io.testomat.core.constants.CredentialConstants.PRESIGN_PROPERTY_NAME;
import static io.testomat.core.constants.CredentialConstants.REGION_PROPERTY_NAME;
import static io.testomat.core.constants.CredentialConstants.SECRET_KEY_PROPERTY_NAME;
import static io.testomat.core.constants.CredentialConstants.SHARED_PROPERTY_NAME;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CredentialsManager {
    private static final Logger log = LoggerFactory.getLogger(CredentialsManager.class);
    private static final S3Credentials credentials = new S3Credentials();

    public static S3Credentials getCredentials() {
        return credentials;
    }

    public void populateCredentialsFromServerResponse(Map<String, Object> credsFromServer) {
        log.debug("Populating S3 credentials from configuration map");

        if (credsFromServer == null || credsFromServer.isEmpty()) {
            log.warn("Received null or empty credentials map");
            return;
        }

        credentials.setPresign(getBoolean(credsFromServer, PRESIGN_PROPERTY_NAME, false));
        credentials.setShared(getBoolean(credsFromServer, SHARED_PROPERTY_NAME, false));
        credentials.setIam(getBoolean(credsFromServer, IAM_PROPERTY_NAME, false));
        credentials.setSecretAccessKey(getString(credsFromServer, SECRET_KEY_PROPERTY_NAME));
        credentials.setAccessKeyId(getString(credsFromServer, ACCESS_KEY_PROPERTY_NAME));
        credentials.setBucket(getString(credsFromServer, BUCKET_PROPERTY_NAME));
        credentials.setRegion(getString(credsFromServer, REGION_PROPERTY_NAME));

        log.info("S3 credentials populated: bucket={}, region={}, presign={}, shared={}, iam={}",
                credentials.getBucket(), credentials.getRegion(),
                credentials.isPresign(), credentials.isShared(), credentials.isIam());

        if (!areCredentialsAvailable()) {
            log.error("Credentials population completed but essential fields are missing");
        } else {
            log.debug("All required S3 credentials are available");
        }
    }

    public void populateCredentialsFromEnvironment() {

    }

    private boolean areCredentialsAvailable() {
        boolean accessKeyAvailable = credentials.getAccessKeyId() != null;
        boolean secretKeyAvailable = credentials.getSecretAccessKey() != null;
        boolean bucketAvailable = credentials.getBucket() != null;
        boolean regionAvailable = credentials.getRegion() != null;

        boolean allAvailable = accessKeyAvailable && secretKeyAvailable && bucketAvailable && regionAvailable;

        if (!allAvailable) {
            log.warn("Missing S3 credentials - accessKey: {}, secretKey: {}, bucket: {}, region: {}",
                    accessKeyAvailable, secretKeyAvailable, bucketAvailable, regionAvailable);
        }

        return allAvailable;
    }

    private boolean getBoolean(Map<String, Object> map, String key, boolean defaultValue) {
        Object value = map.get(key);
        return value != null ? Boolean.parseBoolean(value.toString()) : defaultValue;
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
}
