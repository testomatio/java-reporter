package io.testomat.core.client.http.util;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.testomat.core.exception.ResponseJsonParsingException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("JsonResponseMapperUtil Tests")
class JsonResponseMapperUtilTest {

    @Test
    @DisplayName("Should return null for null response type")
    void mapJsonResponse_NullResponseType_ShouldReturnNull() {
        String jsonResponse = "{\"key\":\"value\"}";
        
        Object result = JsonResponseMapperUtil.mapJsonResponse(jsonResponse, null);
        
        assertNull(result);
    }

    @Test
    @DisplayName("Should deserialize to Map successfully")
    void mapJsonResponse_ValidJsonToMap_ShouldDeserializeCorrectly() {
        String jsonResponse = "{\"uid\":\"test-123\",\"status\":\"success\"}";
        
        Map result = JsonResponseMapperUtil.mapJsonResponse(jsonResponse, Map.class);
        
        assertNotNull(result);
        assertEquals("test-123", result.get("uid"));
        assertEquals("success", result.get("status"));
    }

    @Test
    @DisplayName("Should deserialize to custom object successfully")
    void mapJsonResponse_ValidJsonToCustomObject_ShouldDeserializeCorrectly() {
        String jsonResponse = "{\"name\":\"Test\",\"value\":42}";
        
        TestObject result = JsonResponseMapperUtil.mapJsonResponse(jsonResponse, TestObject.class);
        
        assertNotNull(result);
        assertEquals("Test", result.name);
        assertEquals(42, result.value);
    }

    @Test
    @DisplayName("Should handle missing properties with configured behavior")
    void mapJsonResponse_MissingProperties_ShouldIgnoreUnknownProperties() {
        String jsonResponse = "{\"name\":\"Test\",\"unknown\":\"ignored\",\"value\":100}";
        
        TestObject result = JsonResponseMapperUtil.mapJsonResponse(jsonResponse, TestObject.class);
        
        assertNotNull(result);
        assertEquals("Test", result.name);
        assertEquals(100, result.value);
    }

    @Test
    @DisplayName("Should throw exception for invalid JSON")
    void mapJsonResponse_InvalidJson_ShouldThrowParsingException() {
        String invalidJson = "{invalid json";
        
        ResponseJsonParsingException exception = assertThrows(
                ResponseJsonParsingException.class,
                () -> JsonResponseMapperUtil.mapJsonResponse(invalidJson, Map.class)
        );
        
        assertTrue(exception.getMessage().contains("Failed to parse response json"));
        assertTrue(exception.getMessage().contains(invalidJson));
    }

    @Test
    @DisplayName("Should throw exception for null primitive values")
    void mapJsonResponse_NullPrimitiveValue_ShouldThrowParsingException() {
        String jsonWithNullPrimitive = "{\"name\":\"Test\",\"value\":null}";
        
        ResponseJsonParsingException exception = assertThrows(
                ResponseJsonParsingException.class,
                () -> JsonResponseMapperUtil.mapJsonResponse(jsonWithNullPrimitive, TestObject.class)
        );
        
        assertTrue(exception.getMessage().contains("Failed to parse response json"));
    }

    @Test
    @DisplayName("Should handle empty JSON object")
    void mapJsonResponse_EmptyJsonObject_ShouldReturnEmptyMap() {
        String emptyJson = "{}";
        
        Map result = JsonResponseMapperUtil.mapJsonResponse(emptyJson, Map.class);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle JSON array")
    void mapJsonResponse_JsonArray_ShouldDeserializeToList() {
        String jsonArray = "[{\"name\":\"Test1\",\"value\":1},{\"name\":\"Test2\",\"value\":2}]";
        
        List result = JsonResponseMapperUtil.mapJsonResponse(jsonArray, List.class);
        
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should truncate very long response body in error message")
    void mapJsonResponse_VeryLongInvalidJson_ShouldTruncateInErrorMessage() {
        StringBuilder longJson = new StringBuilder("{");
        // Create JSON longer than MAX_RESPONSE_BODY_SIZE (500 chars as per constants)
        for (int i = 0; i < 600; i++) {
            longJson.append("a");
        }
        
        ResponseJsonParsingException exception = assertThrows(
                ResponseJsonParsingException.class,
                () -> JsonResponseMapperUtil.mapJsonResponse(longJson.toString(), Map.class)
        );
        
        assertTrue(exception.getMessage().contains("..."));
        assertTrue(exception.getMessage().length() < longJson.length());
    }

    @Test
    @DisplayName("Should handle special characters in JSON")
    void mapJsonResponse_SpecialCharacters_ShouldDeserializeCorrectly() {
        String jsonWithSpecialChars = "{\"message\":\"Line1\\nLine2\\tTabbed\",\"quote\":\"He said \\\"Hello\\\"\"}";
        
        Map result = JsonResponseMapperUtil.mapJsonResponse(jsonWithSpecialChars, Map.class);
        
        assertNotNull(result);
        assertEquals("Line1\nLine2\tTabbed", result.get("message"));
        assertEquals("He said \"Hello\"", result.get("quote"));
    }

    @Test
    @DisplayName("Should handle null response body gracefully")
    void mapJsonResponse_NullResponseBody_ShouldThrowParsingException() {
        ResponseJsonParsingException exception = assertThrows(
                ResponseJsonParsingException.class,
                () -> JsonResponseMapperUtil.mapJsonResponse(null, Map.class)
        );
        
        assertTrue(exception.getMessage().contains("Failed to parse response json"));
    }

    @Test
    @DisplayName("Should handle empty string response body")
    void mapJsonResponse_EmptyStringResponseBody_ShouldThrowParsingException() {
        ResponseJsonParsingException exception = assertThrows(
                ResponseJsonParsingException.class,
                () -> JsonResponseMapperUtil.mapJsonResponse("", Map.class)
        );
        
        assertTrue(exception.getMessage().contains("Failed to parse response json"));
    }

    @Test
    @DisplayName("Should handle whitespace-only response body")
    void mapJsonResponse_WhitespaceResponseBody_ShouldThrowParsingException() {
        ResponseJsonParsingException exception = assertThrows(
                ResponseJsonParsingException.class,
                () -> JsonResponseMapperUtil.mapJsonResponse("   ", Map.class)
        );
        
        assertTrue(exception.getMessage().contains("Failed to parse response json"));
    }

    // Test helper class
    public static class TestObject {
        @JsonProperty("name")
        public String name;
        
        @JsonProperty("value")
        public int value;
        
        public TestObject() {} // Required for Jackson
        
        public TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }
}