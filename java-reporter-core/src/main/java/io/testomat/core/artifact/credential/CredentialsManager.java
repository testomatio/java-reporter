package io.testomat.core.artifact.credential;

import static io.testomat.core.constants.ArtifactPropertyNames.ACCESS_KEY_PROPERTY_NAME;
import static io.testomat.core.constants.ArtifactPropertyNames.BUCKET_PROPERTY_NAME;
import static io.testomat.core.constants.ArtifactPropertyNames.REGION_PROPERTY_NAME;
import static io.testomat.core.constants.ArtifactPropertyNames.SECRET_ACCESS_KEY_PROPERTY_NAME;
import static io.testomat.core.constants.CredentialConstants.ACCESS_KEY_ID;
import static io.testomat.core.constants.CredentialConstants.BUCKET;
import static io.testomat.core.constants.CredentialConstants.IAM;
import static io.testomat.core.constants.CredentialConstants.PRESIGN;
import static io.testomat.core.constants.CredentialConstants.REGION;
import static io.testomat.core.constants.CredentialConstants.SECRET_ACCESS_KEY;
import static io.testomat.core.constants.CredentialConstants.SHARED;

import io.testomat.core.exception.ArtifactManagementException;
import io.testomat.core.propertyconfig.impl.PropertyProviderFactoryImpl;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import java.lang.reflect.Field;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CredentialsManager {
    private static final Logger log = LoggerFactory.getLogger(CredentialsManager.class);
    private static final S3Credentials credentials = new S3Credentials();

    private static final String SECRET_ACCESS_KEY_FIELD = "secretAccessKey";
    private static final String ACCESS_KEY_ID_FIELD = "accessKeyId";
    private static final String BUCKET_FIELD = "bucket";
    private static final String REGION_FIELD = "region";

    private final PropertyProvider provider =
            PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();

    public static S3Credentials getCredentials() {
        return credentials;
    }

    public void populateCredentials(Map<String, Object> credsFromServer) {
        log.debug("Populating S3 credentials");

        if (credsFromServer == null || credsFromServer.isEmpty()) {
            log.warn("Received null or empty credentials map");
            return;
        }
        credentials.setIam(getBoolean(credsFromServer, IAM, false));


        credentials.setPresign(getBoolean(credsFromServer, PRESIGN, false));
        credentials.setShared(getBoolean(credsFromServer, SHARED, false));

        if (getPropertyFromEnv(SECRET_ACCESS_KEY_PROPERTY_NAME) != null) {
            populateCredentialsFromEnv(getPropertyFromEnv(SECRET_ACCESS_KEY_PROPERTY_NAME), SECRET_ACCESS_KEY_FIELD);
            log.debug("SecretAccessKey from env");
        } else {
            credentials.setSecretAccessKey(getString(credsFromServer, SECRET_ACCESS_KEY));
            log.debug("SecretAccessKey from server");
        }

        if (getPropertyFromEnv(ACCESS_KEY_PROPERTY_NAME) != null) {
            populateCredentialsFromEnv(getPropertyFromEnv(ACCESS_KEY_PROPERTY_NAME), ACCESS_KEY_ID_FIELD);
            log.debug("AccessKey from env");

        } else {
            credentials.setAccessKeyId(getString(credsFromServer, ACCESS_KEY_ID));
            log.debug("AccessKey from server");
        }

        if (getPropertyFromEnv(BUCKET_PROPERTY_NAME) != null) {
            populateCredentialsFromEnv(getPropertyFromEnv(BUCKET_PROPERTY_NAME), BUCKET_FIELD);
        } else {
            credentials.setBucket(getString(credsFromServer, BUCKET));
        }

        if (getPropertyFromEnv(REGION_PROPERTY_NAME) != null) {
            populateCredentialsFromEnv(getPropertyFromEnv(REGION_PROPERTY_NAME), REGION_FIELD);
        } else {
            credentials.setRegion(getString(credsFromServer, REGION));
        }

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

    private boolean getBoolean(Map<String, Object> map, String key, boolean defaultValue) {
        Object value = map.get(key);
        return value != null ? Boolean.parseBoolean(value.toString()) : defaultValue;
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private Object getPropertyFromEnv(String propertyName) {
        try {
            return provider.getProperty(propertyName);
        } catch (Exception e) {
            return null;
        }
    }

    private void populateCredentialsFromEnv(Object envPropertyValue, String fieldName) {
        Field field;
        try {
            field = credentials.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(credentials, envPropertyValue);
        } catch (NoSuchFieldException e) {
            throw new ArtifactManagementException("Failed to set credentials with reflection. "
                    + "The field does not exist: " + fieldName);
        } catch (IllegalAccessException e) {
            throw new ArtifactManagementException("Inaccessible field: " + fieldName);
        }

    }


    private void logCredentialsInitializationResult() {
        log.info("S3 credentials populated: bucket={}, region={}, presign={}, shared={}, iam={}",
                credentials.getBucket(), credentials.getRegion(),
                credentials.isPresign(), credentials.isShared(), credentials.isIam());

        if (!areCredentialsAvailable()) {
            log.error("Credentials population completed but essential fields are missing");
        } else {
            log.debug("All required S3 credentials are available");
        }
    }
}
