package com.neutroware.ebaysyncserver.ebay.oauth2;

import com.nimbusds.jose.shaded.gson.Gson;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;



@Service
@RequiredArgsConstructor
public class OAuthApiClient {

    @Value("${api.ebay.com.appid}")
    private String appId;
    @Value("${api.ebay.com.certid}")
    private String certId;
    @Value("${api.ebay.com.devid}")
    private String devId;
    @Value("${api.ebay.com.redirecturi}")
    private String redirectUri;

    private final OkHttpClient client;

    private static final String CRED_SEPERATOR = ":";
    private static final String WEB_ENDPOINT = "https://auth.ebay.com/oauth2/authorize";
    private static final String API_ENDPOINT = "https://api.ebay.com/identity/v1/oauth2/token";

    private String buildAuthorization() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.appId).append(CRED_SEPERATOR).append(this.certId);
        byte[] encodedBytes = Base64.getEncoder().encode(sb.toString().getBytes());
        return "Basic " + new String(encodedBytes);
    }

    private Optional<String> buildScopeForRequest(List<String> scopes) {
        String scopeList = null;
        if (null != scopes && !scopes.isEmpty()) {
            scopeList = String.join("+", scopes);
        }
        return Optional.of(scopeList);
    }

    private LocalDateTime generateExpiration(int expiresIn) {
        return LocalDateTime.now().plusSeconds(expiresIn);
    }

    private OAuthResponse parseUserToken(String s) {
        Gson gson = new Gson();
        TokenResponse tokenResponse = gson.fromJson(s, TokenResponse.class);
        AccessToken accessToken = new AccessToken();
        accessToken.setTokenType(TokenType.USER);
        accessToken.setToken(tokenResponse.getAccessToken());
        accessToken.setExpiresOn(generateExpiration(tokenResponse.getExpiresIn()));

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(tokenResponse.getRefreshToken());
        refreshToken.setExpiresOn(generateExpiration(tokenResponse.getRefreshTokenExpiresIn()));

        return new OAuthResponse(Optional.of(accessToken), Optional.of(refreshToken));
    }

    private OAuthResponse handleError(Response response) throws IOException {
        String errorMessage = response.body().string();
        response.close();
        return new OAuthResponse(errorMessage);
    }

    public String generateUserAuthorizationUrl(List<String> scopes) {
        StringBuilder sb = new StringBuilder();
        String scope = this.buildScopeForRequest(scopes).orElse("");

        sb.append(WEB_ENDPOINT).append("?");
        sb.append("client_id=").append(this.appId).append("&");
        sb.append("response_type=code").append("&");
        sb.append("redirect_uri=").append(this.redirectUri).append("&");
        sb.append("scope=").append(scope).append("&");
        return sb.toString();
    }

    public OAuthResponse exchangeCodeForAccessToken(String code) throws IOException {

        StringBuilder requestData = new StringBuilder();
        requestData.append("grant_type=authorization_code").append("&");
        requestData.append(String.format("redirect_uri=%s", this.redirectUri)).append("&");
        requestData.append(String.format("code=%s", code));
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), requestData.toString());

        Request request = new Request.Builder().url(API_ENDPOINT)
                .header("Authorization", buildAuthorization())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            String responseBody = response.body().string();
            response.close();
            return parseUserToken(responseBody);
        } else {
            return handleError(response);
        }
    }

    public OAuthResponse getAccessToken(String refreshToken, List<String> scopes) throws IOException {
        String scope = buildScopeForRequest(scopes).orElse("");

        StringBuilder requestData = new StringBuilder();
        requestData.append("grant_type=refresh_token").append("&");
        requestData.append(String.format("refresh_token=%s", refreshToken));
        requestData.append(String.format("scope=%s", scope)).append("&");
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), requestData.toString());

        Request request = new Request.Builder().url(API_ENDPOINT)
                .header("Authorization", buildAuthorization())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            String responseBody = response.body().string();
            response.close();
            return parseUserToken(responseBody);
        } else {
            return handleError(response);
        }
    }
}
