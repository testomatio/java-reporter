package io.testomat.core.artifact;

/**
 * Builder for creating JSON request bodies containing artifact links for upload to the server.
 * Handles serialization of artifact data and test run information.
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkUploadBodyBuilder {
    private static final Logger log = LoggerFactory.getLogger(LinkUploadBodyBuilder.class);

    public String buildLinkUploadRequestBody(List<ArtifactLinkData> storedLinkData, String apiKey) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        ArrayNode testsArray = mapper.createArrayNode();

        for (ArtifactLinkData data : storedLinkData) {
            ObjectNode testNode = mapper.createObjectNode();
            testNode.put("rid", data.getRid());
            testNode.put("test_id", data.getTestId());
            testNode.put("title", data.getTestName());
            testNode.put("overwrite", "true");
            testNode.set("artifacts", mapper.valueToTree(data.getLinks()));
            testsArray.add(testNode);
        }

        rootNode.put("api_key", apiKey);
        rootNode.set("tests", testsArray);

        String json = null;
        try {
            json = mapper.writeValueAsString(rootNode);
        } catch (JsonProcessingException e) {
            log.warn("Failed to convert convert link storage to json body");
        }
        return json;
    }
}
