package com.azarenka.evebuilders.repository.database.casino;

import com.azarenka.evebuilders.domain.casino.Box;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BoxRepository extends JpaRepository<Box, String> {

    List<Box> findBoxesByCharacterId(Integer characterId);

    Box findBoxByUid(String boxId);

    List<Box> findAllByClaimedFalse();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update Box b
              set b.claimed = :status,
                  b.updateDate = :date
            where b.uid = :uid
           """)
    int updateClaimedAndUpdateDateByUid(@Param("uid") String uid,
                                        @Param("status") boolean status,
                                        @Param("date") LocalDate date);
}
