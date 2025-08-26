package com.azarenka.evebuilders.domain.dto;

import com.azarenka.evebuilders.domain.enums.Metric;

public record UserStat(int rank, String username, String displayName, int value, Metric metricLabel, boolean active) {
}
