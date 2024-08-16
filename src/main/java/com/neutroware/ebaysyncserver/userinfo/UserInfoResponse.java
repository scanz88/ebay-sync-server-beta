package com.neutroware.ebaysyncserver.userinfo;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfoResponse {
    private String userId;
    private Boolean initiated;
    private Boolean ebayAccountLinked;
}
