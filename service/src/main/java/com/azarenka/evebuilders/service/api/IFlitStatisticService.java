package com.azarenka.evebuilders.service.api;

import com.azarenka.evebuilders.domain.dto.UserFleetStat;
import com.azarenka.evebuilders.domain.enums.FleetMetric;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface IFlitStatisticService {

    Set<UserFleetStat> buildLeaderboard(FleetMetric metric, LocalDate from, LocalDate to);

    Set<UserFleetStat> getFleetStats();
}
