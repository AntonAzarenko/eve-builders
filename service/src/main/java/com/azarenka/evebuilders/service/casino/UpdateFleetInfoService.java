package com.azarenka.evebuilders.service.casino;

import com.azarenka.evebuilders.domain.authdb.CtaFleetInfo;
import com.azarenka.evebuilders.domain.authdb.UserFlightsInfo;
import com.azarenka.evebuilders.domain.casino.CasinoUser;
import com.azarenka.evebuilders.repository.auth.AAFleetTrackerRepository;
import com.azarenka.evebuilders.service.api.IFlitStatisticService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UpdateFleetInfoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateFleetInfoService.class);

    @Autowired
    private IFlitStatisticService flitStatisticService;
    @Autowired
    private CasinoUserService casinoUserService;
    @Autowired
    private AAFleetTrackerRepository fleetTrackerRepository;

    @Scheduled(cron = "0 0 3 * * *", zone = "Europe/Moscow")
    public void updateUserFleetInfo() {
        LOGGER.info("Updating user fleet info");
        var fleetStats = flitStatisticService.getFleetStats();
        var users = new ArrayList<CasinoUser>();
        fleetStats.forEach(stat -> {
            var casinoUser = new CasinoUser();
            casinoUser.setCharacterId((int) stat.characterId());
            casinoUser.setCharacterName(stat.displayName());
            casinoUser.setCountPoints((int) stat.value());
            users.add(casinoUser);
        });
        casinoUserService.insertUserInfo(users);
        LOGGER.info("Updating user fleet info. Users count: {}", users.size());
    }

    @Scheduled(cron = "0 0 3 * * *", zone = "Europe/Moscow")
    public void updateDailyUserFleetInfo() {
        List<CtaFleetInfo> fleetsByDate = fleetTrackerRepository.findFleetsByDate(LocalDateTime.now().minusHours(1));
        fleetsByDate.forEach(info -> {
            List<UserFlightsInfo> userFlights = fleetTrackerRepository.findUserFlights(List.of(info.getId()));
        });
    }
}
