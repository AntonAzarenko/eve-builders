package com.azarenka.evebuilders.service.api;

import com.azarenka.evebuilders.domain.dto.UserStat;
import com.azarenka.evebuilders.domain.enums.Metric;

import java.time.LocalDate;
import java.util.List;

public interface IStatisticService {

    List<UserStat> fetchLeaderboard(Metric metric, LocalDate from, LocalDate to, boolean includeInactive);

}
