package io.testomat.core.facade.methods.artifact.credential;

import static io.testomat.core.constants.ArtifactPropertyNames.ACCESS_KEY_PROPERTY_NAME;
import static io.testomat.core.constants.ArtifactPropertyNames.BUCKET_PROPERTY_NAME;
import static io.testomat.core.constants.ArtifactPropertyNames.ENDPOINT_PROPERTY_NAME;
import static io.testomat.core.constants.ArtifactPropertyNames.FORCE_PATH_PROPERTY_NAME;
import static io.testomat.core.constants.ArtifactPropertyNames.PRIVATE_ARTIFACTS_PROPERTY_NAME;
import static io.testomat.core.constants.ArtifactPropertyNames.REGION_PROPERTY_NAME;
import static io.testomat.core.constants.ArtifactPropertyNames.SECRET_ACCESS_KEY_PROPERTY_NAME;
import static io.testomat.core.constants.CredentialConstants.ACCESS_KEY_ID;
import static io.testomat.core.constants.CredentialConstants.BUCKET;
import static io.testomat.core.constants.CredentialConstants.ENDPOINT;
import static io.testomat.core.constants.CredentialConstants.FORCE_PATH;
import static io.testomat.core.constants.CredentialConstants.IAM;
import static io.testomat.core.constants.CredentialConstants.PRESIGN;
import static io.testomat.core.constants.CredentialConstants.REGION;
import static io.testomat.core.constants.CredentialConstants.SECRET_ACCESS_KEY;
import static io.testomat.core.constants.CredentialConstants.SHARED;

import io.testomat.core.propertyconfig.impl.PropertyProviderFactoryImpl;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages S3 credentials by combining environment variables and server-provided configuration.
 * Provides centralized access to S3 credentials with priority given to environment variables.
 */
public class CredentialsManager {
    private static final Logger log = LoggerFactory.getLogger(CredentialsManager.class);
    private static final S3Credentials credentials = new S3Credentials();


    private final PropertyProvider provider =
            PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();

    /**
     * Returns the singleton S3Credentials instance.
     *
     * @return current S3 credentials
     */
    public static S3Credentials getCredentials() {
        return credentials;
    }

    /**
     * Populates S3 credentials from server response, with environment variables taking precedence.
     *
     * @param credsFromServer credentials map received from server
     */
    public void populateCredentials(Map<String, Object> credsFromServer) {
        log.debug("Populating S3 credentials");

        if (credsFromServer == null || credsFromServer.isEmpty()) {
            log.warn("Received null or empty credentials map");
            return;
        }
        populateCredentialField(FORCE_PATH_PROPERTY_NAME, FORCE_PATH, credsFromServer, "ForcePath",
                value -> credentials.setForcePath(getBooleanValue(value)));

        populateCredentialField(ENDPOINT_PROPERTY_NAME, ENDPOINT, credsFromServer, "Endpoint",
                value -> credentials.setCustomEndpoint(getStringValue(value)));

        populateCredentialField(PRIVATE_ARTIFACTS_PROPERTY_NAME, PRESIGN, credsFromServer, "Presign",
                value -> credentials.setPresign(getBooleanValue(value)));

        populateCredentialField(SECRET_ACCESS_KEY_PROPERTY_NAME, SECRET_ACCESS_KEY, credsFromServer, "SecretAccessKey",
                value -> credentials.setSecretAccessKey(getStringValue(value)));

        populateCredentialField(ACCESS_KEY_PROPERTY_NAME, ACCESS_KEY_ID, credsFromServer, "AccessKey",
                value -> credentials.setAccessKeyId(getStringValue(value)));

        populateCredentialField(BUCKET_PROPERTY_NAME, BUCKET, credsFromServer, "Bucket",
                value -> credentials.setBucket(getStringValue(value)));

        populateCredentialField(REGION_PROPERTY_NAME, REGION, credsFromServer, "Region",
                value -> credentials.setRegion(getStringValue(value)));

        credentials.setIam(getBooleanValue(credsFromServer.get(IAM)));
        credentials.setShared(getBooleanValue(credsFromServer.get(SHARED)));

        logCredentialsInitializationResult();
    }

    private boolean areCredentialsAvailable() {
        boolean accessKeyAvailable = credentials.getAccessKeyId() != null;
        boolean secretKeyAvailable = credentials.getSecretAccessKey() != null;
        boolean bucketAvailable = credentials.getBucket() != null;
        boolean regionAvailable = credentials.getRegion() != null;

        boolean allAvailable = accessKeyAvailable
                && secretKeyAvailable
                && bucketAvailable
                && regionAvailable;

        if (!allAvailable) {
            log.warn("Missing S3 credentials - accessKey: {}, secretKey: {}, bucket: {}, region: {}",
                    accessKeyAvailable, secretKeyAvailable, bucketAvailable, regionAvailable);
        }

        return allAvailable;
    }

    private Object getPropertyFromEnv(String propertyName) {
        try {
            return provider.getProperty(propertyName);
        } catch (Exception e) {
            return null;
        }
    }

    private void populateCredentialField(String envPropertyName, String serverKey,
                                         Map<String, Object> credsFromServer, String fieldDisplayName,
                                         java.util.function.Consumer<Object> setter) {
        Object envValue = getPropertyFromEnv(envPropertyName);
        if (envValue != null) {
            setter.accept(envValue);
            log.debug("{} from env", fieldDisplayName);
        } else {
            Object serverValue = credsFromServer.get(serverKey);
            if (serverValue != null) {
                setter.accept(serverValue);
            }
            log.debug("{} from server", fieldDisplayName);
        }
    }

    private String getStringValue(Object value) {
        return value != null ? value.toString() : null;
    }

    private boolean getBooleanValue(Object value) {
        return value != null && Boolean.parseBoolean(value.toString());
    }

    private void logCredentialsInitializationResult() {
        log.debug("S3 credentials populated: bucket={}, region={}, presign={}, shared={}, iam={}",
                credentials.getBucket(), credentials.getRegion(),
                credentials.isPresign(), credentials.isShared(), credentials.isIam());

        if (!areCredentialsAvailable()) {
            log.error("Credentials population completed but essential fields are missing");
        } else {
            log.debug("All required S3 credentials are available");
        }
    }
}
