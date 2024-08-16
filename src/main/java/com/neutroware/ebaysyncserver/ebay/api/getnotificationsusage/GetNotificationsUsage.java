package com.neutroware.ebaysyncserver.ebay.api.getnotificationsusage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetNotificationsUsage {

    private static final String EBAY_API_URL = "https://api.ebay.com/ws/api.dll";
    private static final String EBAY_API_COMPATIBILITY_LEVEL = "967";
    private static final String EBAY_API_CALL_NAME = "GetNotificationsUsage";
    private static final String EBAY_API_SITEID = "0";

    @Value("${api.ebay.com.appid}")
    private String EBAY_API_APP_NAME;
    @Value("${api.ebay.com.devid}")
    private String EBAY_API_DEV_NAME;
    @Value("${api.ebay.com.certid}")
    private String EBAY_API_CERT_NAME;

    private final OkHttpClient httpClient;
    private final XmlMapper xmlMapper = new XmlMapper();

    public JsonNode getNotificationsUsage(String authToken, String itemId) throws Exception {
        String xmlResponse = sendGetNotificationsUsageRequest(authToken, itemId);
        JsonNode jsonResponse = xmlMapper.readTree(xmlResponse);
        return jsonResponse;
    }

    private String sendGetNotificationsUsageRequest(String authToken, String itemId) throws Exception {
        String xmlRequest = buildXmlRequest(authToken, itemId);

        RequestBody body = RequestBody.create(xmlRequest, MediaType.parse("text/xml; charset=utf-8"));
        Request request = new Request.Builder()
                .url(EBAY_API_URL)
                .post(body)
                .addHeader("X-EBAY-API-COMPATIBILITY-LEVEL", EBAY_API_COMPATIBILITY_LEVEL)
                .addHeader("X-EBAY-API-CALL-NAME", EBAY_API_CALL_NAME)
                .addHeader("X-EBAY-API-SITEID", EBAY_API_SITEID)
                .addHeader("X-EBAY-API-APP-NAME", EBAY_API_APP_NAME)
                .addHeader("X-EBAY-API-DEV-NAME", EBAY_API_DEV_NAME)
                .addHeader("X-EBAY-API-CERT-NAME", EBAY_API_CERT_NAME)
                .addHeader("Content-Type", "text/xml")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new RuntimeException("Unexpected code " + response);

            String responseBody = response.body().string();
            return responseBody;
        }
    }

    private String buildXmlRequest(String authToken, String itemId) {
        return String.format(
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                        "<GetNotificationsUsageRequest xmlns=\"urn:ebay:apis:eBLBaseComponents\">\n" +
                        "    <RequesterCredentials>\n" +
                        "        <eBayAuthToken>%s</eBayAuthToken>\n" +
                        "    </RequesterCredentials>\n" +
                        "    <ItemID>%s</ItemID>\n" +
                        "</GetNotificationsUsageRequest>",
                authToken, itemId
        );
    }
}
