package com.azarenka.evebuilders.repository.database.casino;

import com.azarenka.evebuilders.domain.casino.CasinoUser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface CasinoUserRepository extends JpaRepository<CasinoUser, String> {

    Optional<CasinoUser> findByCharacterId(Integer characterId);

    @Modifying
    @Query("update CasinoUser u set u.countPoints = :points, u.updateDate = :date where u.characterId = :cid")
    int updatePointsAndDateByCharacterId(@Param("cid") Integer cid,
                                         @Param("points") Integer points,
                                         @Param("date") LocalDate date);
}
