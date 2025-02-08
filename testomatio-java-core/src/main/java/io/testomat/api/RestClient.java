package io.testomat.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import io.testomat.TestomatioConfig;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Created by Lolik on 22.01.2025
 */
class RestClient {

  private final HttpClient httpClient = HttpClient.newHttpClient();
  private final String apiKey = TestomatioConfig.getApiKey();

  private final ObjectMapper objectMapper;

  public RestClient() {
    objectMapper = new ObjectMapper();
    objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  }

  TestRunResponse createTestRun(TestRun testRun) {
    HttpRequest request = createRequest("reporter")
        .POST(HttpRequest.BodyPublishers.ofString(writeValueAsString(testRun)))
        .build();
    System.out.println(request.uri());
    HttpResponse<String> response = sendRequest(request);
    System.out.println(response.body());
    return readJsonAsObject(response.body(), TestRunResponse.class);
  }

  TestRunResponse updateTestRun(TestRun testRun) {
    HttpRequest request = createRequest("reporter/" + testRun.id)
        .PUT(HttpRequest.BodyPublishers.ofString(writeValueAsString(testRun)))
        .build();
    HttpResponse<String> response = sendRequest(request);
    return readJsonAsObject(response.body(), TestRunResponse.class);
  }

  String addTestResultsToTestRun(TestRun testRun) {
    HttpRequest request = createRequest("reporter/" + testRun.id + "/testrun")
        .POST(HttpRequest.BodyPublishers.ofString(writeValueAsString(testRun)))
        .build();
    HttpResponse<String> response = sendRequest(request);
    return response.body();
  }


  private HttpRequest.Builder createRequest(String path) {
    return HttpRequest.newBuilder()
        .header("Content-Type", "application/json")
        .uri(URI.create(TestomatioConfig.getApiUrl() + path + "?api_key=" + apiKey));
  }

  private HttpResponse<String> sendRequest(HttpRequest request) {
    try {
      return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private String writeValueAsString(Object value) {
    try {
      var json = objectMapper.writeValueAsString(value);
      System.out.println(json);
      return json;
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private <T> T readJsonAsObject(String json, Class<T> type) {
    try {
      System.out.println(json);
      return objectMapper.readValue(json, type);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

}
