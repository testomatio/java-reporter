package io.testomat.api;

/**
 * Created by Lolik on 20.12.2024
 */
public class TestRunResponse {

  public String uid;
  public String url;
  public String publicUrl;
  public String status;

  public String getUid() {
    return uid;
  }

  public String getUrl() {
    return url;
  }

  public String getPublicUrl() {
    return publicUrl;
  }

  public String getStatus() {
    return status;
  }

  @Override
  public String toString() {
    return "TestRunResponse{" +
        "uid='" + uid + '\'' +
        ", url='" + url + '\'' +
        ", publicUrl='" + publicUrl + '\'' +
        ", status='" + status + '\'' +
        '}';
  }
}
