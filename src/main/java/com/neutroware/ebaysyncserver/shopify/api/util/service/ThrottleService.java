package com.neutroware.ebaysyncserver.shopify.api.util.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neutroware.ebaysyncserver.shopify.api.util.type.Extensions;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Service
public class ThrottleService {

    public void throttle(Map<Object, Object> extensionsMap) {
        ObjectMapper objectMapper = new ObjectMapper();
        Extensions extensions = objectMapper.convertValue(extensionsMap, Extensions.class);

        int currentlyAvailable = extensions.cost().throttleStatus().currentlyAvailable();
        int actualQueryCost = extensions.cost().actualQueryCost();
        int restoreRate = extensions.cost().throttleStatus().restoreRate();

        System.out.println(extensions);

        if (currentlyAvailable < actualQueryCost) {
            System.out.println("throttling ");
            int deficit = actualQueryCost - currentlyAvailable;
            long waitTime = (long) Math.ceil(deficit/(double) restoreRate);

            try {
                Thread.sleep(Duration.ofSeconds(waitTime).toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }

    }
}
