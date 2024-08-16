package com.neutroware.ebaysyncserver.ebay.api.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neutroware.ebaysyncserver.ebay.api.getitem.GetItemResponse;
import com.neutroware.ebaysyncserver.ebay.api.getsellerlist.GetSellerListResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ItemSpecificsDeserializer extends JsonDeserializer<GetItemResponse.ItemSpecifics> {

    @Override
    public GetItemResponse.ItemSpecifics deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<GetItemResponse.ItemSpecific> parsedNameValueList = new ArrayList<GetItemResponse.ItemSpecific>();
        ObjectNode node = parser.getCodec().readTree(parser);
        JsonNode nameValueList = node.get("NameValueList");
        if (nameValueList.isArray()) {
            for (JsonNode arrNode : nameValueList) {
                if (arrNode.get("Value").isArray()) {
                    List<String> valueList = new ArrayList<>();
                    for (JsonNode value : arrNode.get("Value")) {
                        valueList.add(value.asText());
                    }
                    GetItemResponse.ItemSpecific itemSpecific = new GetItemResponse.ItemSpecific(arrNode.get("Name").asText(), valueList);
                    parsedNameValueList.add(itemSpecific);
                } else {
                    List<String> valueList = new ArrayList<>();
                    valueList.add(arrNode.get("Value").asText());
                    GetItemResponse.ItemSpecific itemSpecific = new GetItemResponse.ItemSpecific(arrNode.get("Name").asText(), valueList);
                    parsedNameValueList.add(itemSpecific);
                }
            }
        } else {
            if (nameValueList.get("Value").isArray()) {
                List<String> valueList = new ArrayList<>();
                for (JsonNode value : nameValueList.get("Value")) {
                    valueList.add(value.asText());
                }
                GetItemResponse.ItemSpecific itemSpecific = new GetItemResponse.ItemSpecific(nameValueList.get("Name").asText(), valueList);
                parsedNameValueList.add(itemSpecific);
            } else {
                List<String> valueList = new ArrayList<>();
                valueList.add(nameValueList.get("Value").asText());
                GetItemResponse.ItemSpecific itemSpecific = new GetItemResponse.ItemSpecific(nameValueList.get("Name").asText(), valueList);
                parsedNameValueList.add(itemSpecific);
            }
        }
        return new GetItemResponse.ItemSpecifics(parsedNameValueList);
    }
}