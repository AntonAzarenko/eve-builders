package com.azarenka.evebuilders.repository.database.casino;

import com.azarenka.evebuilders.domain.casino.RewardedFleet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RewardedFleetRepository extends JpaRepository<RewardedFleet, String> {

    RewardedFleet findRewardedFleetByFleetId(Integer fleetId);
}
