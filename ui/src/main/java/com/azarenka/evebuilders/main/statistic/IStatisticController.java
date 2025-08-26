package com.azarenka.evebuilders.main.statistic;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.dto.UserInfo;
import com.azarenka.evebuilders.domain.dto.UserStat;
import com.azarenka.evebuilders.domain.enums.Metric;
import com.vaadin.flow.component.html.Image;

import java.time.LocalDate;
import java.util.List;

public interface IStatisticController {

    List<UserStat> fetchLeaderboard(Metric metric, LocalDate from, LocalDate to, boolean includeInactive);

    List<DistributedOrder> findDistributedOrders(LocalDate from, LocalDate to, boolean includeInactive);

    List<Order> findOrders(LocalDate from, LocalDate to, boolean includeInactive);

    List<UserInfo> findUsersMeta();

    Image getCharacterPortrait(String userName);
}
