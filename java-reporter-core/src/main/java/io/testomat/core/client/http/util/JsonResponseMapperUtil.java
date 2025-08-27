package io.testomat.core.client.http.util;

import static io.testomat.core.constants.CommonConstants.MAX_RESPONSE_BODY_SIZE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.testomat.core.exception.ResponseJsonParsingException;

/**
 * Utility class for JSON response deserialization.
 * Provides centralized JSON processing with pre-configured ObjectMapper
 * for consistent response handling across all HTTP operations.
 */
public class JsonResponseMapperUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);

    /**
     * Deserializes JSON response body to specified type.
     * Handles null response types gracefully and provides meaningful error messages
     * with truncated response bodies for debugging.
     *
     * @param responseBody JSON response string from HTTP request
     * @param responseType target class for deserialization, null if no response expected
     * @param <T> response object type
     * @return deserialized response object, or null if responseType is null
     * @throws ResponseJsonParsingException if JSON parsing fails
     */
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
