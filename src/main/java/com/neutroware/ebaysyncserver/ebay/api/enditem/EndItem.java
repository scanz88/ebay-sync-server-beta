package com.neutroware.ebaysyncserver.ebay.api.enditem;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EndItem {

    private static final String EBAY_API_URL = "https://api.ebay.com/ws/api.dll";
    private static final String EBAY_API_COMPATIBILITY_LEVEL = "967";
    private static final String EBAY_API_CALL_NAME = "EndItem";
    private static final String EBAY_API_SITEID = "0";

    private final OkHttpClient httpClient;
    private final XmlMapper xmlMapper = new XmlMapper();

    public JsonNode endListing(String authToken, String itemId) throws Exception {
        String xmlResponse = endItemRequest(authToken, itemId);
        return xmlMapper.readTree(xmlResponse);
    }

    private String endItemRequest(String authToken, String itemId) throws Exception {
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
                        "<EndItemRequest xmlns=\"urn:ebay:apis:eBLBaseComponents\">\n" +
                        "  <RequesterCredentials>\n" +
                        "    <eBayAuthToken>%s</eBayAuthToken>\n" +
                        "  </RequesterCredentials>\n" +
                        "  <ItemID>%s</ItemID>\n" +
                        "  <EndingReason>NotAvailable</EndingReason>\n" +
                        "</EndItemRequest>",
                authToken, itemId);
    }
}
