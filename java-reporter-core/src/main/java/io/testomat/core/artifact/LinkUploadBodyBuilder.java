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

    public String buildLinkUploadRequestBody(Map<String, List<String>> map) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();

        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            ObjectNode objectNode = mapper.createObjectNode();
            objectNode.put("rid", entry.getKey());
            objectNode.set("artifacts", mapper.valueToTree(entry.getValue()));
            arrayNode.add(objectNode);
        }

        String json = null;
        try {
            json = mapper.writeValueAsString(arrayNode);
        } catch (JsonProcessingException e) {
            log.warn("Failed to convert convert link storage to json body");
        }
        return json;
    }
}
