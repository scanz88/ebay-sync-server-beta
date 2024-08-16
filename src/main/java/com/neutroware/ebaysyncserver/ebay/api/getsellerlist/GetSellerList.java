package com.neutroware.ebaysyncserver.ebay.api.getsellerlist;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.neutroware.ebaysyncserver.ebay.api.getitem.GetItemResponse;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetSellerList {

    private static final String EBAY_API_URL = "https://api.ebay.com/ws/api.dll";
    private static final String EBAY_API_COMPATIBILITY_LEVEL = "967";
    private static final String EBAY_API_CALL_NAME = "GetSellerList";
    private static final String EBAY_API_SITEID = "0";
    private static final int MAX_ENTRIES_PER_PAGE = 200;
    private static final int MAX_DAYS_INTERVAL = 120;

    private final OkHttpClient httpClient;
    private final XmlMapper xmlMapper = new XmlMapper();

    public List<GetSellerListResponse> getAllActiveListings(String authToken) throws Exception {
        List<GetSellerListResponse> allItems = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date now = new Date();

        String endTimeFrom = dateFormat.format(new Date(now.getTime()));
        String endTimeTo = dateFormat.format(new Date(now.getTime() +(long) MAX_DAYS_INTERVAL * 24 * 60 * 60 * 1000 ));

        int pageNumber = 1;
        while (true) {
            String xmlResponse = getSellerList(authToken, endTimeFrom, endTimeTo, MAX_ENTRIES_PER_PAGE, pageNumber);
            GetSellerListResponse response = xmlMapper.readValue(xmlResponse, GetSellerListResponse.class);
            allItems.add(response);

            boolean hasMoreItems = response.hasMoreItems();

            if (!hasMoreItems) break;

            pageNumber++;
        }

        return allItems;
    }


    private String getSellerList(String authToken, String endTimeFrom, String endTimeTo, int entriesPerPage, int pageNumber) throws Exception {
        String xmlRequest = buildXmlRequest(authToken, endTimeFrom, endTimeTo, entriesPerPage, pageNumber);

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

            String responseBody = response.body().string();

            return responseBody;
        }
    }

    private String buildXmlRequest(String authToken, String endTimeFrom, String endTimeTo, int entriesPerPage, int pageNumber) {
        return String.format(
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                        "<GetSellerListRequest xmlns=\"urn:ebay:apis:eBLBaseComponents\">\n" +
                        "  <RequesterCredentials>\n" +
                        "    <eBayAuthToken>%s</eBayAuthToken>\n" +
                        "  </RequesterCredentials>\n" +
                        "  <EndTimeFrom>%s</EndTimeFrom>\n" +
                        "  <EndTimeTo>%s</EndTimeTo>\n" +
                        "  <DetailLevel>ReturnAll</DetailLevel>\n" +
                        "  <Pagination>\n" +
                        "    <EntriesPerPage>%d</EntriesPerPage>\n" +
                        "    <PageNumber>%d</PageNumber>\n" +
                        "  </Pagination>\n" +
                        // Pagination details
                        "  <OutputSelector>HasMoreItems</OutputSelector>\n" +
                        "  <OutputSelector>PaginationResult.TotalNumberOfPages</OutputSelector>\n" +
                        "  <OutputSelector>PaginationResult.TotalNumberOfEntries</OutputSelector>\n" +
                        "  <OutputSelector>ItemsPerPage</OutputSelector>\n" +
                        "  <OutputSelector>ReturnedItemCountActual</OutputSelector>\n" +
                        "  <OutputSelector>PageNumber</OutputSelector>\n" +
                        // End pagination details
                        "  <OutputSelector>ItemArray.Item.ItemID</OutputSelector>\n" +
                        "  <OutputSelector>ItemArray.Item.Title</OutputSelector>\n" +
                        "  <OutputSelector>ItemArray.Item.Description</OutputSelector>\n" +
                        "  <OutputSelector>ItemArray.Item.PrimaryCategory.CategoryName</OutputSelector>\n" +
                        "  <OutputSelector>ItemArray.Item.SellingStatus.CurrentPrice</OutputSelector>\n" +
                        "  <OutputSelector>ItemArray.Item.SellingStatus.QuantitySold</OutputSelector>\n" +
                        "  <OutputSelector>ItemArray.Item.Quantity</OutputSelector>\n" +
                        "  <OutputSelector>ItemArray.Item.ShippingDetails.CalculatedShippingRate.WeightMajor</OutputSelector>\n" +
                        "  <OutputSelector>ItemArray.Item.ShippingDetails.CalculatedShippingRate.WeightMinor</OutputSelector>\n" +
                        "  <OutputSelector>ItemArray.Item.PictureDetails</OutputSelector>\n" +
                        "</GetSellerListRequest>",
                authToken, endTimeFrom, endTimeTo, entriesPerPage, pageNumber);
    }

}
