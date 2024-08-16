package com.neutroware.ebaysyncserver.ebay.api.getsellerlist;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.neutroware.ebaysyncserver.ebay.api.getitem.GetItemResponse;
import com.neutroware.ebaysyncserver.ebay.api.util.ItemArrayDeserializer;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GetSellerListResponse(
        @JsonProperty("Timestamp")
        String timestamp,
        @JsonProperty("Ack")
        String ack,
        @JsonProperty("ItemsPerPage")
        Integer itemsPerPage,
        @JsonProperty("PageNumber")
        Integer pageNumber,
        @JsonProperty("ReturnedItemCountActual")
        Integer returnedItemCountActual,
        @JsonProperty("PaginationResult")
        PaginationResult paginationResult,
        @JsonProperty("HasMoreItems")
        Boolean hasMoreItems,
        @JsonProperty("ItemArray")
        @JsonDeserialize(using = ItemArrayDeserializer.class)
        ItemArray itemArray
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PaginationResult(
            @JsonProperty("TotalNumberOfPages")
            Integer totalNumberOfPages,
            @JsonProperty("TotalNumberOfEntries")
            Integer totalNumberOfEntries
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ItemArray(
            @JsonProperty("Item")
            List<GetItemResponse.Item> items
    ) {}
}
