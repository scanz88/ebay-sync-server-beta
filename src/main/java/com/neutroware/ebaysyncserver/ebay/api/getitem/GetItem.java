package com.neutroware.ebaysyncserver.ebay.api.getitem;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;

@Service
@RequiredArgsConstructor
public class GetItem {

    private static final String EBAY_API_URL = "https://api.ebay.com/ws/api.dll";
    private static final String EBAY_API_COMPATIBILITY_LEVEL = "967";
    private static final String EBAY_API_CALL_NAME = "GetItem";
    private static final String EBAY_API_SITEID = "0";

    private final OkHttpClient httpClient;
    private final XmlMapper xmlMapper = new XmlMapper();

    public GetItemResponse getItem(String authToken, String itemId) throws Exception {
        String xmlResponse = getItemRequest(authToken, itemId);
        //System.out.println(xmlResponse);

        //ObjectMapper objectMapper = new ObjectMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        //JsonNode arrNode = xmlMapper.readTree(xmlResponse);
        //System.out.println(arrNode.toPrettyString());
        //objectMapper.convertValue(arrNode, GetItemResponse.Item.class);
        //System.out.println(xmlResponse);


        return xmlMapper.readValue(xmlResponse, GetItemResponse.class);
    }

//    public String getItemXml(String authToken, String itemId) throws Exception {
//        String xmlResponse = getItemRequest(authToken, itemId);
//        return xmlResponse;
//    }

    private String getItemRequest(String authToken, String itemId) throws Exception {
        String xmlRequest = buildXmlRequest(authToken, itemId);

        RequestBody body = RequestBody.create(xmlRequest, MediaType.parse("text/xml; charset=utf-8"));
        Request request = new Request.Builder()
                .url(EBAY_API_URL)
                .post(body)
                .addHeader("X-EBAY-API-COMPATIBILITY-LEVEL", EBAY_API_COMPATIBILITY_LEVEL)
                .addHeader("X-EBAY-API-CALL-NAME", EBAY_API_CALL_NAME)
                .addHeader("X-EBAY-API-SITEID", EBAY_API_SITEID)
                .addHeader("Content-Type", "text/xml")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new RuntimeException("Unexpected code " + response);

            return response.body().string();
        }
    }

    private String buildXmlRequest(String authToken, String itemId) {
        return String.format(
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                        "<GetItemRequest xmlns=\"urn:ebay:apis:eBLBaseComponents\">\n" +
                        "  <RequesterCredentials>\n" +
                        "    <eBayAuthToken>%s</eBayAuthToken>\n" +
                        "  </RequesterCredentials>\n" +
                        "  <ItemID>%s</ItemID>\n" +
                        "  <DetailLevel>ReturnAll</DetailLevel>\n" +
                        "  <IncludeItemSpecifics>true</IncludeItemSpecifics>\n" +
                        "  <OutputSelector>Item.ItemID</OutputSelector>\n" +
                        "  <OutputSelector>Item.Title</OutputSelector>\n" +
                        "  <OutputSelector>Item.Description</OutputSelector>\n" +
                        "  <OutputSelector>Item.ItemSpecifics</OutputSelector>\n" +
                        "  <OutputSelector>Item.PrimaryCategory.CategoryName</OutputSelector>\n" +
                        "  <OutputSelector>Item.SellingStatus.CurrentPrice</OutputSelector>\n" +
                        "  <OutputSelector>Item.SellingStatus.QuantitySold</OutputSelector>\n" +
                        "  <OutputSelector>Item.Quantity</OutputSelector>\n" +
                        "  <OutputSelector>Item.ShippingDetails.CalculatedShippingRate.WeightMajor</OutputSelector>\n" +
                        "  <OutputSelector>Item.ShippingDetails.CalculatedShippingRate.WeightMinor</OutputSelector>\n" +
                        "  <OutputSelector>Item.PictureDetails</OutputSelector>\n" +
                        "</GetItemRequest>",
                authToken, itemId);
    }
}
