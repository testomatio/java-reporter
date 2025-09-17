package io.testomat.core.artifact;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkUploadBodyBuilder {
    private static final Logger log = LoggerFactory.getLogger(LinkUploadBodyBuilder.class);

    public String buildLinkUploadRequestBody(Map<String, List<String>> map, String apiKey) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        ArrayNode testsArray = mapper.createArrayNode();

        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            ObjectNode testNode = mapper.createObjectNode();
            testNode.put("rid", entry.getKey());
            testNode.set("artifacts", mapper.valueToTree(entry.getValue()));
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
