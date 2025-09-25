package com.azarenka.evebuilders.main.staff.fleet;

import com.azarenka.evebuilders.domain.db.User;
import com.azarenka.evebuilders.domain.dto.UserFleetStat;
import com.azarenka.evebuilders.domain.enums.FleetMetric;
import com.azarenka.evebuilders.service.api.IFlitStatisticService;
import com.azarenka.evebuilders.service.api.IUserService;
import com.azarenka.evebuilders.service.converter.VaadinImageConverter;
import com.azarenka.evebuilders.service.impl.intergarion.EvePortraitService;
import com.vaadin.flow.component.html.Image;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
public class FleetStatisticController implements IFleetStatisticController {

    @Autowired
    private EvePortraitService evePortraitService;
    @Autowired
    private IUserService userService;
    @Autowired
    private IFlitStatisticService service;

    @Override
    public List<UserFleetStat> fetchLeaderboard(FleetMetric metric, LocalDate from, LocalDate to) {
        return service.buildLeaderboard(metric, from, to);
    }

    @Override
    public Image getCharacterPortrait(long characterId) {
        if (Objects.nonNull(characterId)) {
            if (Objects.nonNull(characterId)) {
                byte[] portrait = evePortraitService.getPortrait(Long.valueOf(characterId), 32);
                return VaadinImageConverter.createImageFromBytes(portrait);
            }
        }
        return new Image();
    }
}
