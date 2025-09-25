package com.azarenka.evebuilders.main.statistic;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.db.User;
import com.azarenka.evebuilders.domain.dto.UserInfo;
import com.azarenka.evebuilders.domain.dto.UserStat;
import com.azarenka.evebuilders.domain.enums.Metric;
import com.azarenka.evebuilders.service.api.IStatisticService;
import com.azarenka.evebuilders.service.api.IUserService;
import com.azarenka.evebuilders.service.converter.VaadinImageConverter;
import com.azarenka.evebuilders.service.impl.intergarion.EvePortraitService;
import com.vaadin.flow.component.html.Image;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class StatisticController implements IStatisticController {

    @Autowired
    private EvePortraitService evePortraitService;
    @Autowired
    private IUserService userService;
    @Autowired
    private IStatisticService service;

    @Override
    public List<UserStat> fetchLeaderboard(Metric metric, LocalDate from, LocalDate to, boolean includeInactive) {
        return service.fetchLeaderboard(metric, from, to, includeInactive);
    }

    @Override
    public List<DistributedOrder> findDistributedOrders(LocalDate from, LocalDate to, boolean includeInactive) {
        return List.of();
    }

    @Override
    public List<Order> findOrders(LocalDate from, LocalDate to, boolean includeInactive) {
        return List.of();
    }

    @Override
    public List<UserInfo> findUsersMeta() {
        return List.of();
    }

    @Override
    public Image getCharacterPortrait(String userName) {
        Optional<User> optionalUser = userService.getByUsername(userName);
        if (optionalUser.isPresent()) {
            String characterId = optionalUser.get().getCharacterId();
            if (Objects.nonNull(characterId)) {
                byte[] portrait = evePortraitService.getPortrait(Long.valueOf(characterId), 32);
                return VaadinImageConverter.createImageFromBytes(portrait);
            }
        }
        return new Image();
    }
}
