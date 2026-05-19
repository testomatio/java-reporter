package io.testomat.core.constants;

public class ArtifactPropertyNames {
    public static final String BUCKET_PROPERTY_NAME = "s3.bucket";
    public static final String ACCESS_KEY_PROPERTY_NAME = "s3.access-key-id";
    public static final String SECRET_ACCESS_KEY_PROPERTY_NAME = "s3.secret.access-key-id";
    public static final String REGION_PROPERTY_NAME = "s3.region";
    public static final String ENDPOINT_PROPERTY_NAME = "s3.endpoint";

    public static final String FORCE_PATH_PROPERTY_NAME = "s3.force-path-style";

    public static final String PRIVATE_ARTIFACTS_PROPERTY_NAME = "testomatio.artifact.private";
    public static final String ARTIFACT_DISABLE_PROPERTY_NAME = "testomatio.artifact.disable";
    public static final String MAX_SIZE_ARTIFACTS_PROPERTY_NAME = "testomatio.artifact.max-size";

    public static final String STEP_ARTIFACT_ENABLED_PROPERTY_NAME = "testomatio.step.artifacts.enabled";

    public static final String ARTIFACT_EXECUTOR_WORKERS_COUNT = "testomatio.artifacts.executor.workers";
    public static final String ARTIFACT_EXECUTOR_MAX_QUEUE = "testomatio.artifacts.executor.queue";
    public static final String ARTIFACT_EXECUTOR_SHUTDOWN_TIMEOUT = "testomatio.artifacts.executor.shutdown.timeout";
    public static final String ARTIFACT_MAX_CONCURRENCY = "testomatio.artifacts.concurrency";
}
