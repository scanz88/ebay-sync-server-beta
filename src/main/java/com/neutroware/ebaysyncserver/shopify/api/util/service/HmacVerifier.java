package com.neutroware.ebaysyncserver.shopify.api.util.service;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
public class HmacVerifier {

    private final String HMAC_SHA256 = "HmacSHA256";

    public boolean verify(String data, String hmacHeader, String clientSecret) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(clientSecret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKeySpec);

            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            String calculatedHmac = Base64.encodeBase64String(rawHmac);

            return calculatedHmac.equals(hmacHeader);
        } catch (Exception e) {
            throw new RuntimeException("Unable to verify HMAC", e);
        }
    }
}
