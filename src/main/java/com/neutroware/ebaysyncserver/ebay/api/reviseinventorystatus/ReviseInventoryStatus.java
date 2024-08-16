package com.neutroware.ebaysyncserver.ebay.api.reviseinventorystatus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviseInventoryStatus{

    private static final String EBAY_API_URL = "https://api.ebay.com/ws/api.dll";
    private static final String EBAY_API_COMPATIBILITY_LEVEL = "967";
    private static final String EBAY_API_CALL_NAME = "ReviseInventoryStatus";
    private static final String EBAY_API_SITEID = "0";

    private final OkHttpClient httpClient;
    private final XmlMapper xmlMapper = new XmlMapper();

    public JsonNode updateQuantity(String authToken, String itemId, int quantity) throws Exception {
        String xmlResponse = reviseInventoryStatus(authToken, itemId, quantity);
        return xmlMapper.readTree(xmlResponse);
    }

    private String reviseInventoryStatus(String authToken, String itemId, int quantity) throws Exception {
        String xmlRequest = buildXmlRequest(authToken, itemId, quantity);

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

    private String buildXmlRequest(String authToken, String itemId, int quantity) {
        return String.format(
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                        "<ReviseInventoryStatusRequest xmlns=\"urn:ebay:apis:eBLBaseComponents\">\n" +
                        "  <RequesterCredentials>\n" +
                        "    <eBayAuthToken>%s</eBayAuthToken>\n" +
                        "  </RequesterCredentials>\n" +
                        "  <InventoryStatus>\n" +
                        "    <ItemID>%s</ItemID>\n" +
                        "    <Quantity>%d</Quantity>\n" +
                        "  </InventoryStatus>\n" +
                        "</ReviseInventoryStatusRequest>",
                authToken, itemId, quantity);
    }
}
