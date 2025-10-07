package com.azarenka.evebuilders.service.casino;

import com.azarenka.evebuilders.domain.casino.Box;
import com.azarenka.evebuilders.domain.casino.CasinoUser;
import com.azarenka.evebuilders.domain.casino.dto.UserInfo;
import com.azarenka.evebuilders.repository.database.casino.CasinoUserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class CasinoUserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CasinoUserService.class);

    @Autowired
    private CasinoUserRepository repository;
    @Autowired
    private BoxService boxService;
    @Autowired
    private RewardService rewardService;

    @Transactional(readOnly = true)
    public UserInfo getByCharacterId(Integer characterId) {
        LOGGER.info("GetByCharacterId {}", characterId);
        if (characterId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "characterId is required");
        }
        CasinoUser user = repository.findByCharacterId(characterId).orElse((null));
        if (Objects.isNull(user)) {
            return null;
        }
        UserInfo dto = new UserInfo();
        dto.setCharacterId(user.getCharacterId());
        dto.setCharacterName(user.getCharacterName());
        dto.setCountPoints(user.getCountPoints());
        return dto;
    }

    @Transactional(readOnly = true)
    public CasinoUser getCasinoUserByCharacterId(Integer characterId) {
        LOGGER.info("Get CasinoUser by CharacterId={}", characterId);
        var casinoUser = repository.findByCharacterId(characterId).orElse((null));
        LOGGER.info("CasinoUser was found  CharacterId={}, CharacterName={}", characterId, casinoUser.getCharacterName());
        return casinoUser;
    }

    @Transactional
    public UserInfo insertUserInfo(UserInfo payload) {
        LOGGER.info("User insert by payload Payload={}", payload);
        int characterId = payload.getCharacterId();
        int points = payload.getCountPoints();
        repository.updatePointsAndDateByCharacterId(characterId, points, LocalDate.now());
        return payload;
    }

    @Transactional
    public void insertUserInfo(List<CasinoUser> casinoUsers) {
        LOGGER.info("User insert by casino user. User={}", casinoUsers);
        repository.saveAll(casinoUsers);
    }

    public List<Box> getRewardsByCharacterId(Integer characterId) {
        return boxService.getAllByCharacterId(characterId);
    }

    public List<UserInfo> getAvailableRewards() {
        List<UserInfo> userInfos = new ArrayList<>();
        repository.findAll().forEach(casinoUser -> {
            UserInfo userInfo = new UserInfo();
            userInfo.setCharacterId(casinoUser.getCharacterId());
            userInfo.setCharacterName(casinoUser.getCharacterName());
            userInfo.setCountPoints(casinoUser.getCountPoints());
            userInfos.add(userInfo);
        });
        return userInfos;
    }

    public void save(CasinoUser character) {
        LOGGER.info("CasinoUser insert {}", character);
        repository.save(character);
    }
}
