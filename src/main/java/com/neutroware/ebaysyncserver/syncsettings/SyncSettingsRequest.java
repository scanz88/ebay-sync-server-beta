package com.neutroware.ebaysyncserver.syncsettings;

public record SyncSettingsRequest(
        boolean sync,
        boolean applyMarkup,
        Float markupPercent,
        boolean applyMarkdown,
        Float markdownPercent
) {}