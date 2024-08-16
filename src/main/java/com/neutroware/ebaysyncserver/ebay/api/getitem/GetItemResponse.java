package com.neutroware.ebaysyncserver.ebay.api.getitem;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.neutroware.ebaysyncserver.ebay.api.util.*;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public record GetItemResponse(
        @JsonProperty("Timestamp")
        String timestamp,
        @JsonProperty("Ack")
        String ack,
        @JsonProperty("Item")
        Item item
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(
            @JsonProperty("ItemID")
            String itemId,
            @JsonDeserialize(using = TrimStringDeserializer.class)
            @JsonProperty("Title")
            String title,
            @JsonProperty("Description")
            String description,
            @JsonDeserialize(using = ItemSpecificsDeserializer.class)
            @JsonProperty("ItemSpecifics")
            ItemSpecifics itemSpecifics,
            @JsonProperty("PrimaryCategory")
            PrimaryCategory primaryCategory,
            @JsonProperty("Quantity")
            Integer quantity,
            @JsonProperty("SellingStatus")
            SellingStatus sellingStatus,
            @JsonProperty("ShippingDetails")
            ShippingDetails shippingDetails,
            @JsonProperty("PictureDetails")
            @JsonDeserialize(using = PictureDetailsDeserializer.class)
            PictureDetails pictureDetails
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ItemSpecifics(
            @JsonProperty("NameValueList")
           List<ItemSpecific> nameValueList
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ItemSpecific(
            @JsonProperty("Name")
            String name,
            @JsonProperty("Value")
            List<String> value
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PrimaryCategory(
            @JsonProperty("CategoryName")
            String categoryName
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SellingStatus(
            @JsonDeserialize(using = CurrentPriceDeserializer.class)
            @JsonProperty("CurrentPrice")
            CurrentPrice currentPrice,
            @JsonProperty("QuantitySold")
            Integer quantitySold
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CurrentPrice(
            String currencyID,
            Float value
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ShippingDetails(
            @JsonProperty("CalculatedShippingRate")
            CalculatedShippingRate calculatedShippingRate
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CalculatedShippingRate(
            @JsonDeserialize(using = WeightDeserializer.class)
            @JsonProperty("WeightMajor")
            Weight weightMajor,
            @JsonDeserialize(using = WeightDeserializer.class)
            @JsonProperty("WeightMinor")
            Weight weightMinor
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Weight(
            String unit,
            Float value
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PictureDetails(
            @JsonProperty("PictureURL")
           List<String> pictureURL
    ) {}
}
