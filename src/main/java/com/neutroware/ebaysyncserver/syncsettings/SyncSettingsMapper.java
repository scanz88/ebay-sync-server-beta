package com.neutroware.ebaysyncserver.syncsettings;

import org.springframework.stereotype.Service;

@Service
public class SyncSettingsMapper {

    public SyncSettings toSyncSettings(SyncSettingsRequest request) {
        return SyncSettings.builder()
                .sync(request.sync())
                .applyMarkup(request.applyMarkup())
                .markupPercent(request.markupPercent())
                .applyMarkdown(request.applyMarkdown())
                .markdownPercent(request.markdownPercent())
                .build();
    }

    public SyncSettingsResponse toSyncSettingsResponse(SyncSettings settings) {
        return SyncSettingsResponse.builder()
                .id(settings.getSyncSettingsId())
                .sync(settings.getSync())
                .applyMarkup(settings.getApplyMarkup())
                .markupPercent(settings.getMarkupPercent())
                .applyMarkdown(settings.getApplyMarkdown())
                .markdownPercent(settings.getMarkdownPercent())
                .userId(settings.getUserId())
                .build();
    }
}
