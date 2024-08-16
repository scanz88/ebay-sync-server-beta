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
import com.neutroware.ebaysyncserver.ebay.api.getsellerlist.GetSellerList;
import com.neutroware.ebaysyncserver.ebay.api.getsellerlist.GetSellerListResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ItemArrayDeserializer extends JsonDeserializer<GetSellerListResponse.ItemArray> {

    @Override
    public GetSellerListResponse.ItemArray deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.coercionConfigFor(GetItemResponse.ShippingDetails.class).setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull);
        List<GetItemResponse.Item> list = new ArrayList<GetItemResponse.Item>();
        ObjectNode node = parser.getCodec().readTree(parser);

        JsonNode arr = node.get("Item");
        if (arr.isArray()) {
            for (JsonNode arrNode : arr) {
                GetItemResponse.Item item = objectMapper.convertValue(arrNode, GetItemResponse.Item.class);
                list.add(item);
            }
        }
        return new GetSellerListResponse.ItemArray(list);
    }
}