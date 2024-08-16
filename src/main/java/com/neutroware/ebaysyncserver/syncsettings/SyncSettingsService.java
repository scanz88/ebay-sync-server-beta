package com.neutroware.ebaysyncserver.syncsettings;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SyncSettingsService {

    private final SyncSettingsRepository syncSettingsRepository;
    private final SyncSettingsMapper syncSettingsMapper;

    public Long create(SyncSettingsRequest request, Authentication currentUser) {
        SyncSettings settings = syncSettingsMapper.toSyncSettings(request);
        settings.setUserId(currentUser.getName());
        return syncSettingsRepository.saveAndFlush(settings).getSyncSettingsId();
    }

    public SyncSettingsResponse findById(Long id) {
        return syncSettingsRepository.findById(id)
                .map(syncSettingsMapper::toSyncSettingsResponse)
                .orElseThrow(() -> new EntityNotFoundException(("No sync settings found for ID: " + id)));
    }

    public SyncSettingsResponse findByUserId(String userId) {
        return syncSettingsRepository.findByUserId(userId)
                .map(syncSettingsMapper::toSyncSettingsResponse)
                .orElseThrow(() -> new EntityNotFoundException(("No sync settings found for user ID: " + userId)));
    }

    public Long update(SyncSettingsRequest request, String userId) {
        SyncSettings existingSyncSettings = syncSettingsRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("No sync settings found for user ID: " + userId));
        SyncSettings settings = syncSettingsMapper.toSyncSettings(request);
        settings.setSyncSettingsId(existingSyncSettings.getSyncSettingsId());
        settings.setUserId(existingSyncSettings.getUserId());
        return syncSettingsRepository.saveAndFlush(settings).getSyncSettingsId();
    }
}
