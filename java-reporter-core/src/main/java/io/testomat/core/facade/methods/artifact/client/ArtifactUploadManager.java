package io.testomat.core.facade.methods.artifact.client;

import static io.testomat.core.constants.ArtifactPropertyNames.ARTIFACT_EXECUTOR_MAX_QUEUE;
import static io.testomat.core.constants.ArtifactPropertyNames.ARTIFACT_EXECUTOR_SHUTDOWN_TIMEOUT;
import static io.testomat.core.constants.ArtifactPropertyNames.ARTIFACT_EXECUTOR_WORKERS_COUNT;

import io.testomat.core.facade.methods.artifact.credential.CredentialsManager;
import io.testomat.core.facade.methods.artifact.credential.S3Credentials;
import io.testomat.core.facade.methods.artifact.util.ArtifactKeyGenerator;
import io.testomat.core.propertyconfig.impl.PropertyProviderFactoryImpl;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import io.testomat.core.propertyconfig.interf.PropertyProviderFactory;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Asynchronous S3 artifact upload manager.
 *
 * <p>Test threads publish upload tasks into a bounded queue,
 * worker threads consume tasks and start non-blocking S3 uploads.
 *
 * <p>Supports parallel uploads, backpressure protection
 * and graceful shutdown.
 */
public class ArtifactUploadManager {
    private static final Logger log = LoggerFactory.getLogger(ArtifactUploadManager.class);

    private final PropertyProviderFactory factory =
        PropertyProviderFactoryImpl.getPropertyProviderFactory();
    private final PropertyProvider provider = factory.getPropertyProvider();

    /**
     * Workers only consume queue and start async uploads.
     */
    private final int WORKERS_COUNT =
        Integer.parseInt(provider.getProperty(ARTIFACT_EXECUTOR_WORKERS_COUNT));

    /**
     * Protection from OOM.
     */
    private final int MAX_QUEUE_SIZE =
        Integer.parseInt(provider.getProperty(ARTIFACT_EXECUTOR_MAX_QUEUE));

    /**
     * Maximum graceful shutdown wait time.
     */
    private final int SHUTDOWN_TIMEOUT_SECONDS =
        Integer.parseInt(provider.getProperty(ARTIFACT_EXECUTOR_SHUTDOWN_TIMEOUT));

    /**
     * Upload task queue.
     *
     * Test threads publish tasks into the queue,
     * worker threads consume them and start async uploads.
     */
    private final BlockingQueue<UploadTask> queue =
        new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);

    /**
     * Registry of active asynchronous uploads.
     *
     * Used during graceful shutdown to wait for completion
     * of all in-flight uploads.
     */
    private final ConcurrentLinkedQueue<CompletableFuture<?>> uploads =
        new ConcurrentLinkedQueue<>();

    private final AtomicInteger activeUploads =
        new AtomicInteger();

    private final ArtifactKeyGenerator keyGenerator;

    private final ExecutorService workers;

    private final S3AsyncClient s3AsyncClient;

    private volatile boolean running = true;

    /**
     * Creates and starts the artifact upload manager.
     *
     * <p>Initializes the S3 async client, worker thread pool
     * and starts upload worker threads.
     */
    public ArtifactUploadManager() {
        this.keyGenerator = new ArtifactKeyGenerator();
        this.s3AsyncClient =
            new S3AsyncClientFactory()
                .createS3AsyncClient();
        this.workers =
            Executors.newFixedThreadPool(
                WORKERS_COUNT,
                r -> {
                    Thread t =
                        new Thread(r, "ArtifactUploadWorker");
                    t.setDaemon(true);
                    return t;
                }
            );
        for (int i = 0; i < WORKERS_COUNT; i++) {
            workers.submit(this::workerLoop);
        }
        log.info("ArtifactUploadManager started. Workers={}", WORKERS_COUNT);
    }

    /**
     * Worker loop that consumes upload tasks from the queue
     * and starts asynchronous S3 uploads.
     */
    private void workerLoop() {
        while (running || !queue.isEmpty()) {
            try {
                UploadTask task = queue.poll(1, TimeUnit.SECONDS);
                if (task == null) {
                    continue;
                }

                int startedUploads = activeUploads.incrementAndGet();

                log.debug(
                    "UPLOAD STARTED | activeUploads={} | thread={}",
                    startedUploads,
                    Thread.currentThread().getName()
                );

                CompletableFuture<?> future =
                    s3AsyncClient.putObject(
                        task.getRequest(),
                        AsyncRequestBody.fromFile(task.getDir())
                    );

                uploads.add(future);
                future.whenComplete((response, throwable) -> {
                    int remainingUploads = activeUploads.decrementAndGet();
                    log.debug(
                        "UPLOAD FINISHED | activeUploads={} | thread={}",
                        remainingUploads,
                        Thread.currentThread().getName()
                    );

                    /**
                     * Prevent memory leak
                     */
                    uploads.remove(future);
                    if (throwable != null) {
                        log.error("Artifact upload failed: {}", task.getDir(), throwable);
                    } else {
                        log.debug("Artifact uploaded successfully: {}", task.getDir());
                    }
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                log.error("Worker error", e);
            }
        }
    }

    /**
     * Enqueues an artifact upload task.
     *
     * <p>Test threads only publish tasks into the queue.
     * Uploads are performed asynchronously by worker threads.
     */
    public void publish(UUID stepId, Path path) {
        try {
            if (path == null) {
                return;
            }
            S3Credentials credentials = CredentialsManager.getCredentials();
            String key =
                keyGenerator.generateKey(
                    path.toString(),
                    stepId.toString(),
                    "step"
                );
            String acl = credentials.isPresign() ? "private" : "public-read";

            PutObjectRequest request =
                PutObjectRequest.builder()
                    .bucket(credentials.getBucket())
                    .key(key)
                    .acl(acl)
                    .build();

            UploadTask task = new UploadTask(request, path);

            boolean offered = queue.offer(task);

            if (!offered) {
                log.warn("Upload queue is full. Artifact skipped: {}", path);
            }

        } catch (Exception e) {
            log.error("Failed to enqueue artifact upload: {}", path, e);
        }
    }

    /**
     * Gracefully shuts down the upload manager
     * and waits for active uploads to complete.
     */
    public void shutdown() {
        log.info("Shutting down ArtifactUploadManager");
        running = false;
        workers.shutdown();
        try {
            boolean terminated = workers.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!terminated) {
                log.warn("Workers did not terminate within timeout");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        CompletableFuture<?>[] futures = uploads.toArray(new CompletableFuture[0]);
        try {
            CompletableFuture.allOf(futures).get(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            log.info("All artifact uploads completed");
        } catch (TimeoutException e) {
            log.warn(
                "Artifact upload timeout reached after {} seconds. " +
                    "Some uploads may not be completed.",
                SHUTDOWN_TIMEOUT_SECONDS
            );
        } catch (Exception e) {
            log.error("Failed while waiting uploads completion", e);
        }
        s3AsyncClient.close();
        log.info("ArtifactUploadManager stopped");
    }
}
