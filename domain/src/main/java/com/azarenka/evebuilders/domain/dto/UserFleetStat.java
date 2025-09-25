package com.azarenka.evebuilders.domain.dto;

import com.azarenka.evebuilders.domain.enums.FleetMetric;

public record UserFleetStat(
    String displayName,
    long characterId,
    int rank,
    long value,
    FleetMetric metricLabel,
    boolean active
) {}
