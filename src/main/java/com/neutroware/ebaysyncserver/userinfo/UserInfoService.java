package com.neutroware.ebaysyncserver.userinfo;

import com.neutroware.ebaysyncserver.encryption.EncryptionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserInfoService {
    private final UserInfoRepository userInfoRepository;
    private final UserInfoMapper userInfoMapper;
    private final EncryptionService encryptionService;

    public UserInfoResponse findById(String userId) {
        return userInfoRepository.findById(userId)
                .map(userInfoMapper::toUserInfoResponse)
                .orElseThrow(() -> new EntityNotFoundException(("No user info found for user ID: " + userId)));
    }
}
