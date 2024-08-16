package com.neutroware.ebaysyncserver.userinfo;

import org.springframework.data.jpa.repository.JpaRepository;


public interface UserInfoRepository extends JpaRepository<UserInfo, String> {
    UserInfo findByShopifyStoreName(String shopifyStoreName);
}
