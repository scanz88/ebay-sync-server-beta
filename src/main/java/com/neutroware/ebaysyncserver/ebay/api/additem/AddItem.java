package com.neutroware.ebaysyncserver.ebay.api.additem;

import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AddItem {

    private static final String EBAY_API_URL = "https://api.ebay.com/ws/api.dll";
    private static final String EBAY_API_COMPATIBILITY_LEVEL = "967";
    private static final String EBAY_API_CALL_NAME = "AddFixedPriceItem";
    private static final String EBAY_API_SITEID = "0";

    private final OkHttpClient httpClient;

    public String addItem(String authToken, String itemXml) throws Exception {
       String xmlRequest = buildXmlRequest(authToken, itemXml);

       // String xmlRequest = this.sampleXml;

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

    public String buildXmlRequest(String authToken, String itemXml) {
        // Extract the <Item> section from the response XML
        String itemSection = itemXml.substring(itemXml.indexOf("<Item>"), itemXml.indexOf("</Item>") + 7);

        return String.format(
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                        "<AddFixedPriceItemRequest xmlns=\"urn:ebay:apis:eBLBaseComponents\">\n" +
                        "  <RequesterCredentials>\n" +
                        "    <eBayAuthToken>%s</eBayAuthToken>\n" +
                        "  </RequesterCredentials>\n" +
                        "  %s\n" +
                        "</AddFixedPriceItemRequest>",
                authToken, itemSection);
    }

    // add starting price, token, remove pictures, change title
    /*
    <eBayAuthToken>v^1.1#i^1#p^3#I^3#f^0#r^0#t^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
                                        </eBayAuthToken>
    * <*/
    // <StartPrice currencyID="USD">29.99</StartPrice>
    String sampleXml = """
            <?xml version="1.0" encoding="utf-8"?>
            <AddFixedPriceItemRequest
                xmlns="urn:ebay:apis:eBLBaseComponents">
                <RequesterCredentials>
                     <eBayAuthToken>v^1.1#i^1#p^3#I^3#f^0#r^0#t^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
                                                            </eBayAuthToken>
                </RequesterCredentials>
                <Item>
                    <AutoPay>false</AutoPay>
                    <Country>US</Country>
                    <Currency>USD</Currency>
                    <Description>&lt;p&gt;Vintage Adult Erotica Paperback… &lt;/p&gt;&lt;p&gt;&lt;b&gt;VELVET TONGUE &lt;/b&gt;&lt;/p&gt;&lt;p&gt;&lt;b&gt;by Travis Van Dellen &lt;/b&gt;&lt;/p&gt;&lt;p&gt;&lt;b&gt;1968 &lt;/b&gt;&lt;/p&gt;&lt;p&gt;&lt;b&gt;CONTINENTAL CLASSICS CC-202 &lt;/b&gt;&lt;/p&gt;&lt;br /&gt;&lt;p&gt;160 pages.&lt;/p&gt;&lt;p&gt;Measures 7” x 4 1/4”.&lt;/p&gt;&lt;br /&gt;&lt;p&gt;&lt;b&gt;CONDITION&lt;/b&gt;:&lt;/p&gt;&lt;p&gt;Good, vintage pre-owned condition.  Creasing / wear to cover &amp;amp; back cover.  Pages clean inside.  See photos for details. &lt;/p&gt;&lt;br /&gt;&lt;p&gt;Please message for any questions, thanks.&lt;/p&gt;</Description>
                    <ListingDetails/>
                    <ListingDuration>GTC</ListingDuration>
                    <ListingType>FixedPriceItem</ListingType>
                    <Location>Philadelphia, Pennsylvania</Location>
                    <PrimaryCategory>
                        <CategoryID>29223</CategoryID>
                    </PrimaryCategory>
                    <PrivateListing>false</PrivateListing>
                    <Quantity>1</Quantity>
                    <StartPrice currencyID="USD">29.99</StartPrice>
                    <ShippingDetails>
                        <ApplyShippingDiscount>false</ApplyShippingDiscount>
                        <CalculatedShippingRate>
                            <PackageDepth measurementSystem="English" unit="inches">4</PackageDepth>
                            <PackageLength measurementSystem="English" unit="inches">10</PackageLength>
                            <PackageWidth measurementSystem="English" unit="inches">8</PackageWidth>
                            <PackagingHandlingCosts currencyID="USD">0.0</PackagingHandlingCosts>
                            <ShippingIrregular>false</ShippingIrregular>
                            <ShippingPackage>PackageThickEnvelope</ShippingPackage>
                            <WeightMajor measurementSystem="English" unit="lbs">1</WeightMajor>
                            <WeightMinor measurementSystem="English" unit="oz">0</WeightMinor>
                            <InternationalPackagingHandlingCosts currencyID="USD">0.0</InternationalPackagingHandlingCosts>
                        </CalculatedShippingRate>
                        <SalesTax>
                            <SalesTaxPercent>0.0</SalesTaxPercent>
                            <ShippingIncludedInTax>false</ShippingIncludedInTax>
                        </SalesTax>
                        <ShippingServiceOptions>
                            <ShippingService>USPSParcel</ShippingService>
                            <ShippingServicePriority>1</ShippingServicePriority>
                            <ExpeditedService>false</ExpeditedService>
                            <ShippingTimeMin>2</ShippingTimeMin>
                            <ShippingTimeMax>5</ShippingTimeMax>
                        </ShippingServiceOptions>
                        <InternationalShippingServiceOption>
                            <ShippingService>USPSFirstClassMailInternationalParcel</ShippingService>
                            <ShippingServicePriority>1</ShippingServicePriority>
                            <ShipToLocation>Worldwide</ShipToLocation>
                        </InternationalShippingServiceOption>
                        <ShippingType>Calculated</ShippingType>
                        <ThirdPartyCheckout>false</ThirdPartyCheckout>
                        <ShippingDiscountProfileID>0</ShippingDiscountProfileID>
                        <InternationalShippingDiscountProfileID>0</InternationalShippingDiscountProfileID>
                        <SellerExcludeShipToLocationsPreference>true</SellerExcludeShipToLocationsPreference>
                    </ShippingDetails>
                    <ShipToLocations>Worldwide</ShipToLocations>
                    <Site>US</Site>
                    <TimeLeft>P7DT14H53M4S</TimeLeft>
                    <Title>VELVET TONGUE Travis Van Dellen 1968 CONTINENTAL CLASSICS CC-202 Sleaze Erotica</Title>
                    <BestOfferDetails>
                        <BestOfferEnabled>true</BestOfferEnabled>
                    </BestOfferDetails>
                    <PostalCode>19122</PostalCode>
                    <PictureDetails>
                        <GalleryType>Gallery</GalleryType>
                        <PictureURL>https://i.ebayimg.com/00/s/MTQyNVgxNDA3/z/ntkAAOSwIjJmTLDC/$_12.JPG?set_id=880000500F</PictureURL>
                        <PictureURL>https://i.ebayimg.com/00/s/MTI2MFg5NDI=/z/ih0AAOSwUatmTLDA/$_12.JPG?set_id=880000500F</PictureURL>
                        <PictureURL>https://i.ebayimg.com/00/s/ODg3WDE0OTQ=/z/U2wAAOSwPFpmTLDD/$_12.JPG?set_id=880000500F</PictureURL>
                        <PictureURL>https://i.ebayimg.com/00/s/MTYwMFgxMjAw/z/mZMAAOSwhDpmTLDF/$_12.JPG?set_id=880000500F</PictureURL>
                        <PictureURL>https://i.ebayimg.com/00/s/OTM3WDEyNTY=/z/ivAAAOSwP41mTLDG/$_12.JPG?set_id=880000500F</PictureURL>
                        <PictureURL>https://i.ebayimg.com/00/s/MTYwMFgxMjAw/z/T78AAOSwf2tmTLDK/$_12.JPG?set_id=880000500F</PictureURL>
                        <PictureURL>https://i.ebayimg.com/00/s/MTYwMFgxMjAw/z/oZ4AAOSwF0ZmTLDN/$_12.JPG?set_id=880000500F</PictureURL>
                        <PictureURL>https://i.ebayimg.com/00/s/MTYwMFgxMjAw/z/sbQAAOSwAFlmTLDQ/$_12.JPG?set_id=880000500F</PictureURL>
                        <PictureURL>https://i.ebayimg.com/00/s/MTYwMFgxMjAw/z/-o8AAOSwMCBmTLDZ/$_12.JPG?set_id=880000500F</PictureURL>
                        <PictureURL>https://i.ebayimg.com/00/s/MTYwMFgxMjAw/z/DjoAAOSwwwhmTLDh/$_12.JPG?set_id=880000500F</PictureURL>
                        <PictureURL>https://i.ebayimg.com/00/s/MTYwMFgxMjAw/z/tZgAAOSwxyNmTLDm/$_12.JPG?set_id=880000500F</PictureURL>
                        <PictureURL>https://i.ebayimg.com/00/s/ODQ3WDEyNTY=/z/OpQAAOSwaGxmTLDr/$_12.JPG?set_id=880000500F</PictureURL>
                        <PictureSource>EPS</PictureSource>
                    </PictureDetails>
                    <DispatchTimeMax>1</DispatchTimeMax>
                    <ItemSpecifics>
                        <NameValueList>
                            <Name>Binding</Name>
                            <Value>Softcover, Wraps</Value>
                            <Source>ItemSpecific</Source>
                        </NameValueList>
                        <NameValueList>
                            <Name>Language</Name>
                            <Value>English</Value>
                            <Source>ItemSpecific</Source>
                        </NameValueList>
                        <NameValueList>
                            <Name>Special Attributes</Name>
                            <Value>1st Edition</Value>
                            <Value>Vintage Paperback</Value>
                            <Source>ItemSpecific</Source>
                        </NameValueList>
                        <NameValueList>
                            <Name>Region</Name>
                            <Value>North America</Value>
                            <Source>ItemSpecific</Source>
                        </NameValueList>
                        <NameValueList>
                            <Name>Author</Name>
                            <Value>Travis Van Dellen</Value>
                            <Source>ItemSpecific</Source>
                        </NameValueList>
                        <NameValueList>
                            <Name>Publisher</Name>
                            <Value>Continental Classics</Value>
                            <Source>ItemSpecific</Source>
                        </NameValueList>
                        <NameValueList>
                            <Name>Topic</Name>
                            <Value>Erotica</Value>
                            <Source>ItemSpecific</Source>
                        </NameValueList>
                        <NameValueList>
                            <Name>Country/Region of Manufacture</Name>
                            <Value>United States</Value>
                            <Source>ItemSpecific</Source>
                        </NameValueList>
                        <NameValueList>
                            <Name>Subject</Name>
                            <Value>Vintage Paperbacks</Value>
                            <Source>ItemSpecific</Source>
                        </NameValueList>
                        <NameValueList>
                            <Name>Original/Facsimile</Name>
                            <Value>Original</Value>
                            <Source>ItemSpecific</Source>
                        </NameValueList>
                        <NameValueList>
                            <Name>Year Printed</Name>
                            <Value>1968</Value>
                            <Source>ItemSpecific</Source>
                        </NameValueList>
                    </ItemSpecifics>
                    <ReturnPolicy>
                        <RefundOption>MoneyBack</RefundOption>
                        <ReturnsWithinOption>Days_30</ReturnsWithinOption>
                        <ReturnsAcceptedOption>ReturnsAccepted</ReturnsAcceptedOption>
                        <ShippingCostPaidByOption>Buyer</ShippingCostPaidByOption>
                    </ReturnPolicy>
                    <SellerProfiles>
                        <SellerShippingProfile>
                            <ShippingProfileID>236553114010</ShippingProfileID>
                            <ShippingProfileName>Calculated:GROUND ADV .&lt;15.99oz Copy (2)</ShippingProfileName>
                        </SellerShippingProfile>
                        <SellerReturnProfile>
                            <ReturnProfileID>234932428010</ReturnProfileID>
                            <ReturnProfileName>30 days money back 4dd4f7f7400e000</ReturnProfileName>
                        </SellerReturnProfile>
                        <SellerPaymentProfile>
                            <PaymentProfileID>189316236010</PaymentProfileID>
                            <PaymentProfileName>eBay Payments</PaymentProfileName>
                        </SellerPaymentProfile>
                    </SellerProfiles>
                    <eBayPlus>false</eBayPlus>
                </Item>
            </AddFixedPriceItemRequest>
            """;
}

