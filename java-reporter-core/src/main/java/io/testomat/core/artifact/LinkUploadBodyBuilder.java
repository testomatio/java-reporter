package io.testomat.core.artifact;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.testomat.core.ArtifactLinkData;
import java.util.List;
import java.util.Map;
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
