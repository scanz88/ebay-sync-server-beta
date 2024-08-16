package com.neutroware.ebaysyncserver.ebay;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.neutroware.ebaysyncserver.ebay.api.additem.AddItem;
import com.neutroware.ebaysyncserver.ebay.api.enditem.EndItem;
import com.neutroware.ebaysyncserver.ebay.api.getitem.GetItem;
import com.neutroware.ebaysyncserver.ebay.api.getitem.GetItemResponse;
import com.neutroware.ebaysyncserver.ebay.api.getnotificationsusage.GetNotificationsUsage;
import com.neutroware.ebaysyncserver.ebay.api.getsellerevents.GetSellerEvents;
import com.neutroware.ebaysyncserver.ebay.api.getsellerlist.GetSellerList;
import com.neutroware.ebaysyncserver.ebay.api.getsellerlist.GetSellerListResponse;
import com.neutroware.ebaysyncserver.ebay.api.getsellertransactions.GetSellerTransactions;
import com.neutroware.ebaysyncserver.ebay.api.reviseinventorystatus.ReviseInventoryStatus;
import com.neutroware.ebaysyncserver.ebay.api.setnotificationpreferences.SetNotificationPreferences;
import com.neutroware.ebaysyncserver.ebay.oauth2.OAuthApiClient;
import com.neutroware.ebaysyncserver.ebay.oauth2.OAuthResponse;
import com.neutroware.ebaysyncserver.product.ImportUtils;
import com.neutroware.ebaysyncserver.userinfo.UserInfo;
import com.neutroware.ebaysyncserver.userinfo.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@RestController
@CrossOrigin(
        origins = "*"
)
@RequestMapping("ebay")
@RequiredArgsConstructor
public class EbayController {

    private final OAuthApiClient oauthApiClient;
    private  final EbayService ebayService;

    private final UserInfoRepository userInfoRepository;
    private final XmlMapper xmlMapper = new XmlMapper();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final GetSellerList getSellerList;
    private final ReviseInventoryStatus reviseInventoryStatus;
    private final GetItem getItem;
    private final AddItem addItem;
    private final OkHttpClient client;
    private final EndItem endItem;
    private final SetNotificationPreferences setNotificationPreferences;
    private final ImportUtils importUtils;
    private final GetNotificationsUsage getNotificationsUsage;
    private final GetSellerEvents getSellerEvents;
    private final GetSellerTransactions getSellerTransactions;

    @GetMapping("/getUserAuthorizationUrl")
    public String getUserAuthorizationUrl() {
//        scopes.add("https://api.ebay.com/oauth/api_scope");
//        scopes.add("https://api.ebay.com/oauth/api_scope/sell.marketing.readonly");
//        scopes.add("https://api.ebay.com/oauth/api_scope/sell.marketing");
//        scopes.add("https://api.ebay.com/oauth/api_scope/sell.inventory.readonly");
//        scopes.add("https://api.ebay.com/oauth/api_scope/sell.inventory");
//        scopes.add("https://api.ebay.com/oauth/api_scope/sell.account.readonly");
//        scopes.add("https://api.ebay.com/oauth/api_scope/sell.account");
//        scopes.add("https://api.ebay.com/oauth/api_scope/sell.fulfillment.readonly");
//        scopes.add("https://api.ebay.com/oauth/api_scope/sell.fulfillment");
//        scopes.add("https://api.ebay.com/oauth/api_scope/sell.analytics.readonly");
//        scopes.add("https://api.ebay.com/oauth/api_scope/sell.finances");
//        scopes.add("https://api.ebay.com/oauth/api_scope/sell.payment.dispute");
//        scopes.add("https://api.ebay.com/oauth/api_scope/commerce.notification.subscription");
//        scopes.add("https://api.ebay.com/oauth/api_scope/commerce.notification.subscription.readonly");
       // scopes.add("https://api.ebay.com/oauth/api_scope/commerce.identity.readonly");

        return oauthApiClient.generateUserAuthorizationUrl(EbayService.SCOPES);
    }

    @PostMapping("/exchangeCodeForAccessToken")
    public void exchangeCodeForAccessToken(@RequestBody final String code, Authentication currentUser) throws Exception {
        OAuthResponse response = oauthApiClient.exchangeCodeForAccessToken(code);
        ebayService.addUserTokens(response, currentUser.getName());
    }

    @GetMapping("/setupNotifications")
    public void setupNotifications(Authentication currentUser) throws Exception {
        String token = ebayService.refreshTokenIfExpired(currentUser.getName());
        JsonNode response = setNotificationPreferences.setNotificationPreferences(token);
        System.out.println(response.toString());

    }

    //This endpoint is called by ebay to send notifications
    @PostMapping("/notification")
    public void receiveNotification(@RequestBody String soapResponse) throws Exception {
        System.out.println("calling receive notifiation........");


        JsonNode jsonResponse = xmlMapper.readTree(soapResponse);
        String notificationSignature = jsonResponse.at("/Header/RequesterCredentials/NotificationSignature").asText();
        String timestampItemListed = jsonResponse.at("/Body/GetItemResponse/Timestamp").asText(null);
        String timestampFixedPriceTransaction = jsonResponse.at("/Body/GetItemTransactionsResponse/Timestamp").asText(null);

        if (timestampItemListed != null) {
            boolean isValid = ebayService.isNotificationSignatureValid(notificationSignature, timestampItemListed);
            //process itemlisted event
            System.out.println("item listed event response");
            System.out.println(jsonResponse);
        }

        if (timestampFixedPriceTransaction != null) {
            boolean isValid = ebayService.isNotificationSignatureValid(notificationSignature, timestampFixedPriceTransaction);
            //process fixedpricetransaction event
        }

        System.out.println(jsonResponse.toString());
    }


}
