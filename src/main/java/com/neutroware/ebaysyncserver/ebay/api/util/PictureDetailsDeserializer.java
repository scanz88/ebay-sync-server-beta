package com.neutroware.ebaysyncserver.ebay.api.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neutroware.ebaysyncserver.ebay.api.getitem.GetItemResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PictureDetailsDeserializer extends JsonDeserializer<GetItemResponse.PictureDetails> {

    @Override
    public GetItemResponse.PictureDetails deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        List<String> list = new ArrayList<String>();
        ObjectNode node = parser.getCodec().readTree(parser);
        JsonNode jsonNode = node.get("PictureURL");
        if (jsonNode.isArray()) {
            for (JsonNode arrNode : jsonNode) {
                list.add(arrNode.asText());
            }
        } else {
            list.add(jsonNode.asText());
        }
       return new GetItemResponse.PictureDetails(list);
    }
}
