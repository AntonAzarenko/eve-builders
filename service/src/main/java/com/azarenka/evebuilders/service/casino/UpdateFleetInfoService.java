package com.azarenka.evebuilders.service.casino;

import com.azarenka.evebuilders.domain.authdb.CtaFleetInfo;
import com.azarenka.evebuilders.domain.authdb.UserFlightsInfo;
import com.azarenka.evebuilders.domain.casino.CasinoUser;
import com.azarenka.evebuilders.domain.casino.RewardedFleet;
import com.azarenka.evebuilders.repository.auth.AAFleetTrackerRepository;
import com.azarenka.evebuilders.repository.database.casino.RewardedFleetRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class UpdateFleetInfoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateFleetInfoService.class);

    @Autowired
    private CasinoUserService casinoUserService;
    @Autowired
    private AAFleetTrackerRepository fleetTrackerRepository;
    @Autowired
    private RewardedFleetRepository rewardedFleetRepository;
    @Value("${casino.start_fleet_info_date}")
    private LocalDateTime localDateTime;

    //@Scheduled(cron = "0 0 3 * * *", zone = "Europe/Moscow")
    @Deprecated
    public void updateUserFleetInfo() {
        LOGGER.info("Updating user fleet info");
        updateUsersFleetInfo(fleetTrackerRepository.findFleetsByDate(LocalDateTime.of(2020, 10, 1, 4, 0)));
    }

    @Scheduled(cron = "0 30 * * * *")
    public void updateDailyUserFleetInfoJob() {
        List<CtaFleetInfo> fleetsByDate = fleetTrackerRepository.findFleetsByDate(LocalDateTime.now().minusHours(24));
        updateUsersFleetInfo(fleetsByDate);
    }

    public void updateDailyUserFleetInfo() {
        List<CtaFleetInfo> fleetsByDate = fleetTrackerRepository.findFleetsByDate(localDateTime);
        updateUsersFleetInfo(fleetsByDate);
    }

    private void updateUsersFleetInfo(List<CtaFleetInfo> fleetsByDate) {
        fleetsByDate.forEach(fleetInfo -> {
            List<UserFlightsInfo> userFlights = fleetTrackerRepository.findUserFlights(fleetInfo.getId());
            LOGGER.debug("Find users fleet. FleetId={}, UserFlights count: {}", fleetInfo.getId(),
                userFlights.size());
            var existed = rewardedFleetRepository.findRewardedFleetByFleetId(fleetInfo.getId());
            if (Objects.isNull(existed)) {
                RewardedFleet rewardedFleet = createFleet(fleetInfo);
                rewardedFleet.setCountRewarded(userFlights.size());
                LOGGER.info("Save fleet. FleetId={}, UsersCount: {}", rewardedFleet.getFleetId(), userFlights.size());
                rewardedFleetRepository.save(rewardedFleet);
                userFlights.forEach(user -> {
                    var character =
                        casinoUserService.getCasinoUserByCharacterId(Math.toIntExact(user.getCharacterId()));
                    if (Objects.nonNull(character)) {
                        int value = character.getCountPoints();
                        character.setCountPoints(value + 1);
                    } else {
                        character = createUser(user);
                        character.setCountPoints(1);
                    }
                    casinoUserService.save(character);

                });
                UserFlightsInfo fc;
                if (Objects.nonNull(fleetInfo.getCharacterId()) && fleetInfo.getCharacterId() != 0) {
                    fc = fleetTrackerRepository.findUserInfoByCharacterId(fleetInfo.getCharacterId());
                } else {
                    fc = fleetTrackerRepository.findUserInfoByCreatorId(fleetInfo.getCreatorId());
                }
                LOGGER.info("Updated FC. FleetId={}, FcName: {}", fleetInfo.getId(), fc.getUsername());
                var casinoUserByCharacterId =
                    casinoUserService.getCasinoUserByCharacterId(Math.toIntExact(fc.getCharacterId()));
                casinoUserService.save(casinoUserByCharacterId);
                LOGGER.info("Saved FC. FleetId={}, FcName: {}", fleetInfo.getId(),
                    casinoUserByCharacterId.getCharacterName());
            } else {
                LOGGER.debug("Fleet already existed. FleetId={}, UserFlights count: {}", fleetInfo.getId(),
                    userFlights.size());
            }
        });
    }

    private RewardedFleet createFleet(CtaFleetInfo ctaFleetInfo) {
        RewardedFleet rewardedFleet = new RewardedFleet();
        rewardedFleet.setUid(UUID.randomUUID().toString());
        rewardedFleet.setFleetId(ctaFleetInfo.getId());
        rewardedFleet.setRewarded(true);
        rewardedFleet.setTitle(ctaFleetInfo.getFleetName());
        rewardedFleet.setHash(ctaFleetInfo.getHash());
        return rewardedFleet;
    }

    private CasinoUser createUser(UserFlightsInfo userFlightsInfo) {
        CasinoUser casinoUser = new CasinoUser();
        casinoUser.setCreateDate(LocalDate.now());
        casinoUser.setUpdateDate(LocalDate.now());
        casinoUser.setCharacterId(Integer.valueOf(Math.toIntExact(userFlightsInfo.getCharacterId())));
        casinoUser.setCharacterName(userFlightsInfo.getUsername());
        return casinoUser;
    }
}
