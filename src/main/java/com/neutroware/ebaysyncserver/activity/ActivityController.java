package com.neutroware.ebaysyncserver.activity;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(
        origins = "*"
)
@RequestMapping("activity")
@RequiredArgsConstructor
public class ActivityController {
    private final ActivityService activityService;

    @GetMapping
    public List<Activity> activityLog(Authentication currentUser) {
        return activityService.getActivityLog(currentUser.getName());
    }
}
