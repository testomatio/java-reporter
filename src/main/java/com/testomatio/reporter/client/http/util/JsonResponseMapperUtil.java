package com.testomatio.reporter.client.http.util;

import static com.testomatio.reporter.constants.CommonConstants.MAX_RESPONSE_BODY_SIZE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testomatio.reporter.exception.ResponseJsonParsingException;

public class JsonResponseMapperUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);

    public static <T> T mapJsonResponse(String responseBody, Class<T> responseType) {
        if (responseType == null) {
            return null;
        }
        try {
            return objectMapper.readValue(responseBody, responseType);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            String truncatedBody = responseBody != null
                    && responseBody.length() > MAX_RESPONSE_BODY_SIZE
                    ? responseBody.substring(0, MAX_RESPONSE_BODY_SIZE) + "..."
                    : responseBody;
            throw new ResponseJsonParsingException(
                    "Failed to parse response json: " + truncatedBody);
        }
    }
}
