package com.neutroware.ebaysyncserver.activity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ActivityService {
    private final ActivityRepository activityRepository;

    public List<Activity> getActivityLog(String userId) {
        return activityRepository.findByUserIdOrderByCreatedDateDesc(userId);
    }

    public void recordActivity(
            ActivityFlow flow,
            ActivityStatus status,
            String userId,
            String description
    ) {
        Activity activity = new Activity();
        activity.setFlow(flow);
        activity.setStatus(status);
        activity.setDescription(description);
        activity.setUserId(userId);
        activityRepository.save(activity);
    }
}
