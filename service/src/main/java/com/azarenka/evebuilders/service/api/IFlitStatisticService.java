package com.azarenka.evebuilders.service.api;

import com.azarenka.evebuilders.domain.dto.UserFleetStat;
import com.azarenka.evebuilders.domain.enums.FleetMetric;

import java.time.LocalDate;
import java.util.List;

public interface IFlitStatisticService {

    List<UserFleetStat> buildLeaderboard(FleetMetric metric, LocalDate from, LocalDate to);

    List<UserFleetStat> getFleetStats();
}
