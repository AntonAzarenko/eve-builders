package com.azarenka.evebuilders.service.casino;

import com.azarenka.evebuilders.domain.auth.authdb.CtaFleetInfo;
import com.azarenka.evebuilders.domain.auth.authdb.UserFlightsInfo;
import com.azarenka.evebuilders.domain.casino.CasinoUser;
import com.azarenka.evebuilders.domain.casino.HistoryCta;
import com.azarenka.evebuilders.domain.casino.RewardedFleet;
import com.azarenka.evebuilders.repository.auth.AAFleetTrackerRepository;
import com.azarenka.evebuilders.repository.database.casino.HistoryCtaRepository;
import com.azarenka.evebuilders.repository.database.casino.RewardedFleetRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Autowired
    private HistoryCtaRepository historyCtaRepository;

    @Value("${casino.start_fleet_info_date}")
    private LocalDateTime localDateTime;

    //@Scheduled(cron = "0 0 3 * * *", zone = "Europe/Moscow")
    @Deprecated
    public void updateUserFleetInfo() {
        LOGGER.info("Updating user fleet info");
        updateUsersFleetInfo(fleetTrackerRepository.findFleetsByDate(LocalDateTime.of(2020, 10, 1, 4, 0)));
    }

    @Scheduled(cron = "0 0 0/2 * * *")
    @Transactional
    public void updateDailyUserFleetInfoJob() {
        List<CtaFleetInfo> fleetsByDate = fleetTrackerRepository.findFleetsByDate(LocalDateTime.now().minusHours(24));
        updateUsersFleetInfo(fleetsByDate);
    }

    public void updateHistoryCtas() {
        List<CtaFleetInfo> fleetsByDate = fleetTrackerRepository.findFleetsByDate(localDateTime);
        updateHistoryCtas(fleetsByDate);
    }

    public void updateDailyUserFleetInfo() {
        List<CtaFleetInfo> fleetsByDate = fleetTrackerRepository.findFleetsByDate(localDateTime);
        updateUsersFleetInfo(fleetsByDate);
        updateHistoryCtas();
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
                    var historyCtaByCharacterIdAndEventDate =
                        historyCtaRepository.findHistoryCtaByCharacterIdAndEventDate(character.getCharacterId(),
                            fleetInfo.getCreatedAt());
                    if (Objects.isNull(historyCtaByCharacterIdAndEventDate)) {
                        var historyCta = createHistoryCta(character.getCharacterId(),
                            character.getCharacterName(), fleetInfo.getCreatedAt(), fleetInfo.getFleetName());
                        historyCtaRepository.save(historyCta);
                    }
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
                if (Objects.nonNull(casinoUserByCharacterId)) {
                    casinoUserService.save(casinoUserByCharacterId);
                    var historyCtaByCharacterIdAndEventDate =
                        historyCtaRepository.findHistoryCtaByCharacterIdAndEventDate(
                            casinoUserByCharacterId.getCharacterId(),
                            fleetInfo.getCreatedAt());
                    if (Objects.isNull(historyCtaByCharacterIdAndEventDate)) {
                        var historyCta = createHistoryCta(casinoUserByCharacterId.getCharacterId(),
                            casinoUserByCharacterId.getCharacterName(), fleetInfo.getCreatedAt(),
                            fleetInfo.getFleetName());
                        historyCtaRepository.save(historyCta);
                    }
                    LOGGER.info("Saved FC. FleetId={}, FcName: {}", fleetInfo.getId(),
                        casinoUserByCharacterId.getCharacterName());
                }
            } else {
                LOGGER.debug("Fleet already existed. FleetId={}, UserFlights count: {}", fleetInfo.getId(),
                    userFlights.size());
            }
        });
    }

    private void updateHistoryCtas(List<CtaFleetInfo> fleetsByDate) {
        fleetsByDate.forEach(fleetInfo -> {
            List<UserFlightsInfo> userFlights = fleetTrackerRepository.findUserFlights(fleetInfo.getId());
            LOGGER.info("Find users fleet for History. FleetId={}, UserFlights count: {}", fleetInfo.getId(),
                userFlights.size());
            var existed = rewardedFleetRepository.findRewardedFleetByFleetId(fleetInfo.getId());
            if (Objects.nonNull(existed)) {
                userFlights.forEach(user -> {
                    var character =
                        casinoUserService.getCasinoUserByCharacterId(Math.toIntExact(user.getCharacterId()));
                    if (Objects.isNull(character)) {
                        character = createUser(user);
                    }
                    var historyCtaByCharacterIdAndEventDate =
                        historyCtaRepository.findHistoryCtaByCharacterIdAndEventDate(character.getCharacterId(),
                            fleetInfo.getCreatedAt());
                    if (Objects.isNull(historyCtaByCharacterIdAndEventDate)) {
                        var historyCta = createHistoryCta(character.getCharacterId(),
                            character.getCharacterName(), fleetInfo.getCreatedAt(), fleetInfo.getFleetName());
                        historyCtaRepository.save(historyCta);
                        LOGGER.info("Saved History. FleetId={}, UserName: {}", fleetInfo.getId(),
                            character.getCharacterName());
                    }
                });
                UserFlightsInfo fc;
                if (Objects.nonNull(fleetInfo.getCharacterId()) && fleetInfo.getCharacterId() != 0) {
                    fc = fleetTrackerRepository.findUserInfoByCharacterId(fleetInfo.getCharacterId());
                } else {
                    fc = fleetTrackerRepository.findUserInfoByCreatorId(fleetInfo.getCreatorId());
                }
                var casinoUserByCharacterId =
                    casinoUserService.getCasinoUserByCharacterId(Math.toIntExact(fc.getCharacterId()));
                if (Objects.nonNull(casinoUserByCharacterId)) {
                    var historyCtaByCharacterIdAndEventDate =
                        historyCtaRepository.findHistoryCtaByCharacterIdAndEventDate(
                            casinoUserByCharacterId.getCharacterId(),
                            fleetInfo.getCreatedAt());
                    if (Objects.isNull(historyCtaByCharacterIdAndEventDate)) {
                        var historyCta = createHistoryCta(casinoUserByCharacterId.getCharacterId(),
                            casinoUserByCharacterId.getCharacterName(), fleetInfo.getCreatedAt(),
                            fleetInfo.getFleetName());
                        historyCtaRepository.save(historyCta);
                        LOGGER.info("Saved History. FleetId={}, FcName: {}", fleetInfo.getId(),
                            casinoUserByCharacterId.getCharacterName());
                    }
                }
            } else {
                LOGGER.debug("History already existed. FleetId={}, UserFlights count: {}", fleetInfo.getId(),
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

    private HistoryCta createHistoryCta(Integer characterId, String characterName, LocalDateTime eventDate,
                                        String fleetName) {
        HistoryCta historyCta = new HistoryCta();
        historyCta.setId(UUID.randomUUID().toString());
        historyCta.setFleetName(fleetName);
        historyCta.setCharacterName(characterName);
        historyCta.setCharacterId(characterId);
        historyCta.setPoints(1);
        historyCta.setEventDate(eventDate);
        return historyCta;
    }
}
