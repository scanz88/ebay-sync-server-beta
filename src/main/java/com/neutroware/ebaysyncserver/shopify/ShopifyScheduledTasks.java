package com.neutroware.ebaysyncserver.shopify;

import com.neutroware.ebaysyncserver.shopify.api.rest.Orders;
import com.neutroware.ebaysyncserver.shopify.api.util.type.Order;
import com.neutroware.ebaysyncserver.userinfo.UserInfo;
import com.neutroware.ebaysyncserver.userinfo.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ShopifyScheduledTasks {
    private final UserInfoRepository userInfoRepository;
    private final Orders orders;
    private final ShopifyService shopifyService;

    @Scheduled(fixedRate = 1200000) //20 minutes
    @Async("singleThreadExecutor")
    public void pollShopifyOrders() throws Exception {
        List<UserInfo> userInfoList = userInfoRepository.findAll();

        for (UserInfo userInfo : userInfoList) {
            if (userInfo.getInitiated()) {
                Instant now = Instant.now();
                Instant thirtyMinutesAgo = now.minusSeconds(1800); //30 minutes = 1800 seconds
                DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);
                String createdAtMin = formatter.format(thirtyMinutesAgo);

                List<Order> shopifyOrders = orders.getOrders(userInfo.getShopifyStoreName(), shopifyService.getToken(userInfo.getUserId()), createdAtMin);
                for (Order shopifyOrder : shopifyOrders) {
                    shopifyService.processOrder(userInfo.getShopifyStoreName(), shopifyOrder);
                }

            }
        }

    }
}
