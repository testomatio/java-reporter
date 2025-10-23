package io.testomat.core.facade.methods.artifact.credential;

/**
 * Data class holding S3 configuration including credentials, bucket, and connection settings.
 * Supports both AWS S3 and S3-compatible storage solutions.
 */
public class S3Credentials {
    private boolean presign;
    private boolean shared;
    private boolean iam;
    private String secretAccessKey;
    private String accessKeyId;
    private String bucket;
    private String region;
    private String customEndpoint;
    private boolean forcePath = false;

    public boolean isForcePath() {
        return forcePath;
    }

    public void setForcePath(boolean forcePath) {
        this.forcePath = forcePath;
    }

    public boolean isPresign() {
        return presign;
    }

    public void setPresign(boolean presign) {
        this.presign = presign;
    }

    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public boolean isIam() {
        return iam;
    }

    public void setIam(boolean iam) {
        this.iam = iam;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCustomEndpoint() {
        return customEndpoint;
    }

    public void setCustomEndpoint(String customEndpoint) {
        this.customEndpoint = customEndpoint;
    }
}