package com.neutroware.ebaysyncserver.syncsettings;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SyncSettingsResponse {
    private Long id;
    private Boolean sync;
    private Boolean applyMarkup;
    private Float markupPercent;
    private Boolean applyMarkdown;
    private Float markdownPercent;
    private String userId;
}
