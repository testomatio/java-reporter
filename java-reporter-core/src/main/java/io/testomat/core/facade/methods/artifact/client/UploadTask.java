package io.testomat.core.facade.methods.artifact.client;

import java.nio.file.Path;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class UploadTask {

    PutObjectRequest request;
    Path dir;

    public UploadTask(PutObjectRequest request, Path path) {
        this.request = request;
        this.dir = path;
    }

    public Path getDir() {
        return dir;
    }

    public void setDir(Path dir) {
        this.dir = dir;
    }

    public PutObjectRequest getRequest() {
        return request;
    }

    public void setRequest(PutObjectRequest request) {
        this.request = request;
    }
}
