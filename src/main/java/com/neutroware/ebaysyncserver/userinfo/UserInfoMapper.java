package com.neutroware.ebaysyncserver.userinfo;

import org.springframework.stereotype.Service;

@Service
public class UserInfoMapper {

    public UserInfoResponse toUserInfoResponse(UserInfo userInfo) {
        return UserInfoResponse.builder()
                .userId(userInfo.getUserId())
                .initiated(userInfo.getInitiated())
                .ebayAccountLinked(userInfo.getEbayToken() != null && userInfo.getEbayRefreshToken() != null)
                .build();
    }
}
