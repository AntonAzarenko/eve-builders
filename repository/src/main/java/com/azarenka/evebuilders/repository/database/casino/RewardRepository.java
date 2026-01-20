package com.azarenka.evebuilders.repository.database.casino;

import com.azarenka.evebuilders.domain.casino.Reward;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RewardRepository extends JpaRepository<Reward, String> {

    Reward findRewardByUid(String id);

    int removeByUid(String rewardId);
}
