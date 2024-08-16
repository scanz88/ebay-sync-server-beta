package com.neutroware.ebaysyncserver.ebay.api.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neutroware.ebaysyncserver.ebay.api.getitem.GetItemResponse;

import java.io.IOException;

public class WeightDeserializer extends JsonDeserializer<GetItemResponse.Weight> {

    @Override
    public GetItemResponse.Weight deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        ObjectNode node = parser.getCodec().readTree(parser);
        String unit = node.get("unit").asText();
        Float value = Float.parseFloat(node.get("").asText());
        return new GetItemResponse.Weight(unit, value);
    }
}
