package com.azarenka.evebuilders.main.staff.fleet;

import com.azarenka.evebuilders.domain.dto.UserFleetStat;
import com.azarenka.evebuilders.domain.enums.FleetMetric;
import com.vaadin.flow.component.html.Image;

import java.time.LocalDate;
import java.util.Set;

public interface IFleetStatisticController {

    Set<UserFleetStat> fetchLeaderboard(FleetMetric metric, LocalDate from, LocalDate to);

    Image getCharacterPortrait(long characterId);
}
