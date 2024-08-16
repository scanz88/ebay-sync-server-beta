package com.neutroware.ebaysyncserver.longjob;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(
        origins = "*"
)
@RequestMapping("long-job")
@RequiredArgsConstructor
public class LongJobController {
    private final LongJobService longJobService;

    @GetMapping("/in-progress")
    public LongJob getCurrentJob(Authentication currentUser) {
        return longJobService.findCurrentJob(currentUser.getName())
                .orElseThrow(() -> new EntityNotFoundException("No jobs currently in progress"));
    }
}
