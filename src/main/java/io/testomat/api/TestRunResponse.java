package io.testomat.api;

/**
 * Created by Lolik on 20.12.2024
 */
public class TestRunResponse {

  public String uid;
  public String url;
  public String public_url;
  public String status;

  @Override
  public String toString() {
    return "TestRunResponse{" +
        "uid='" + uid + '\'' +
        ", url='" + url + '\'' +
        ", public_url='" + public_url + '\'' +
        ", status='" + status + '\'' +
        '}';
  }
}
