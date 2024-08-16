package com.neutroware.ebaysyncserver.longjob;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LongJobService {
    private final LongJobRespository longJobRespository;

    public Optional<LongJob> findCurrentJob(String userId) {
        return longJobRespository.findByUserId(
                userId,
                PageRequest.of(0, 1)
                )
                .stream().findFirst();
    }
}
