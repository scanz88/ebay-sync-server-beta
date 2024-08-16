package com.neutroware.ebaysyncserver.ebay;

import com.neutroware.ebaysyncserver.ebay.oauth2.OAuthApiClient;
import com.neutroware.ebaysyncserver.ebay.oauth2.OAuthResponse;
import com.neutroware.ebaysyncserver.encryption.EncryptionService;
import com.neutroware.ebaysyncserver.userinfo.UserInfo;
import com.neutroware.ebaysyncserver.userinfo.UserInfoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EbayService {
    @Value("${api.ebay.com.appid}")
    private String appId;
    @Value("${api.ebay.com.certid}")
    private String certId;
    @Value("${api.ebay.com.devid}")
    private String devId;

    private final UserInfoRepository userInfoRepository;
    private final EncryptionService encryptionService;
    private final OAuthApiClient oauthApiClient;

    public static final List<String> SCOPES = Collections.unmodifiableList(List.of(
            "https://api.ebay.com/oauth/api_scope/commerce.identity.readonly",
            "https://api.ebay.com/oauth/api_scope"
    ));

    public void addUserTokens(OAuthResponse response, String userId) {
        UserInfo userInfo = userInfoRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        userInfo.setEbayToken(encryptionService.encrypt(response.getAccessToken().get().getToken()));
        userInfo.setEbayRefreshToken(encryptionService.encrypt(response.getRefreshToken().get().getToken()));
        userInfo.setEbayTokenExp(response.getAccessToken().get().getExpiresOn().toDate());
        userInfo.setEbayRefreshTokenExp(response.getRefreshToken().get().getExpiresOn().toDate());
        userInfoRepository.save(userInfo);
    }

    public String refreshTokenIfExpired(String userId) throws IOException {
        UserInfo userInfo = userInfoRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        Date expiryTime = userInfo.getEbayTokenExp();

        if (expiryTime.before(LocalDateTime.now().toDate())) {
            System.out.println("refreshing token ...");
            String refreshToken = encryptionService.decrypt(userInfo.getEbayRefreshToken());
            OAuthResponse response = oauthApiClient.getAccessToken(refreshToken, SCOPES);
            updateAccessToken(response, userId);
            return response.getAccessToken().get().getToken();
        }
        return encryptionService.decrypt(userInfo.getEbayToken());
    }

    private void updateAccessToken(OAuthResponse response, String userId) {
        UserInfo userInfo = userInfoRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        userInfo.setEbayToken(encryptionService.encrypt(response.getAccessToken().get().getToken()));
        userInfo.setEbayTokenExp(response.getAccessToken().get().getExpiresOn().toDate());
        userInfoRepository.save(userInfo);
    }

    public boolean isNotificationSignatureValid(String notificationSignature, String timestamp) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(timestamp, formatter);
            Instant instant = dateTime.toInstant(ZoneOffset.UTC);

            String sig = timestamp + devId + appId + certId;
            byte[] sigdata = sig.getBytes(StandardCharsets.US_ASCII);

            MessageDigest md5Digest = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md5Digest.digest(sigdata);
            String md5Hash = Base64.getEncoder().encodeToString(hashBytes);
            System.out.println("md5 hashing success ");
            return notificationSignature.equals(md5Hash) && isTimestampWithinRange(instant);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private boolean isTimestampWithinRange(Instant instant) {
        Instant now = Instant.now();
        long minutesDifference = Math.abs(now.toEpochMilli() - instant.toEpochMilli()) / 60000;
        return minutesDifference <= 10;
    }

}
