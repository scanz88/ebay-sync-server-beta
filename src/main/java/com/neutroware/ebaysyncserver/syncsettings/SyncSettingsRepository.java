package com.neutroware.ebaysyncserver.syncsettings;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SyncSettingsRepository extends JpaRepository<SyncSettings, Long> {
   Optional<SyncSettings> findByUserId(String userId);
}
