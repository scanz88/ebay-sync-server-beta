package com.neutroware.ebaysyncserver.ebay.oauth2;

import org.joda.time.LocalDateTime;

import java.util.Date;

public class AccessToken {
    private String token;
    private LocalDateTime expiresOn;
    private TokenType tokenType;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getExpiresOn() {
        return expiresOn;
    }

    public void setExpiresOn(LocalDateTime expiresOn) {
        this.expiresOn = expiresOn;
    }

    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AccessToken{");
        sb.append(", token='").append(token).append('\'');
        sb.append(", expiresOn=").append(expiresOn);
        sb.append('}');
        return sb.toString();
    }
}