package com.neutroware.ebaysyncserver.userinfo;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(
        origins = "*"
)
@RequestMapping("userinfo")
@RequiredArgsConstructor
public class UserInfoController {

    private final UserInfoService userInfoService;

    @GetMapping
    public UserInfoResponse get(Authentication currentUser) {
        return userInfoService.findById(currentUser.getName());
    }

}
