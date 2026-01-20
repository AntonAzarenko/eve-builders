package com.azarenka.evebuilders.service.casino;

import com.azarenka.evebuilders.domain.casino.Reward;
import com.azarenka.evebuilders.repository.database.casino.BoxRepository;
import com.azarenka.evebuilders.repository.database.casino.RewardRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RewardService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RewardService.class);

    @Autowired
    private RewardRepository repository;
    @Autowired
    private BoxRepository boxRepository;

    public List<Reward> getRewards() {
        LOGGER.info("Get rewards");
        return repository.findAll();
    }

    public Reward save(Reward reward) {
        LOGGER.info("Save reward Reward={}", reward);
        return repository.save(reward);
    }

    public Reward getRewardById(String id) {
        LOGGER.info("Get reward by id={}", id);
        return repository.findRewardByUid(id);
    }

    @Transactional
    public int remove(String rewardId) {
        LOGGER.info("Remove reward Reward={}", rewardId);
        return repository.removeByUid(rewardId);
    }
}
