package com.azarenka.evebuilders.service.casino;

import com.azarenka.evebuilders.domain.casino.Box;
import com.azarenka.evebuilders.domain.casino.BoxTypeEnum;
import com.azarenka.evebuilders.repository.database.casino.BoxRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class BoxService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BoxService.class);

    @Autowired
    private BoxRepository boxRepository;

    @Transactional
    public Box save(Box box) {
        return boxRepository.save(box);
    }

    @Transactional
    public List<Box> saveAll(List<Box> boxes) {
        LOGGER.info("Saving " + boxes.size() + " boxes");
        return boxRepository.saveAll(boxes);
    }

    public List<Box> getAllByCharacterId(Integer characterId) {
        return boxRepository.findBoxesByCharacterId(characterId);
    }

    public BoxTypeEnum getTypeById(String uid) {
        return boxRepository.findBoxByUid(uid).getBoxType();
    }

    public List<Box> getAllNotClaimedBoxes() {
        return boxRepository.findAllByClaimedFalse();
    }

    @Transactional
    public void update(String boxId, boolean status) {
        LOGGER.info("Updating. BoxId={}. Status={}", boxId, status);
        boxRepository.updateClaimedAndUpdateDateByUid(boxId, status, LocalDate.now());
    }
}
