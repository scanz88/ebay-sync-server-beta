package com.neutroware.ebaysyncserver.ebay.oauth2;

import org.joda.time.LocalDateTime;

import java.util.Date;

public class RefreshToken {
    private String token;
    private LocalDateTime expiresOn;
    private TokenType tokenType = TokenType.USER;

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

    public TokenType getTokenType() {
        return tokenType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RefreshToken{");
        sb.append("token='").append(token).append('\'');
        sb.append(", expiresOn=").append(expiresOn);
        sb.append(", tokenType=").append(tokenType);
        sb.append('}');
        return sb.toString();
    }
}