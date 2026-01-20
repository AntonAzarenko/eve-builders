package com.azarenka.evebuilders.repository.auth;

import com.azarenka.evebuilders.domain.auth.authdb.CtaFleetInfo;
import com.azarenka.evebuilders.domain.auth.authdb.UserFlightsInfo;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
@Transactional(readOnly = true, transactionManager = "mariaTransactionManager")
public class AAFleetTrackerRepository {

    @PersistenceContext(unitName = "mariadb")
    private EntityManager em;

    public List<CtaFleetInfo> findFleetsByDate(LocalDateTime localDateTime) {
        List<Object[]> rows = em.createNativeQuery("""
                    select afl.id,
                           afl.created,
                           afl.fleet,
                           afl.hash,
                           afl.creator_id,
                           afl.character_id,
                           afl.doctrine,
                           afl.fleet_type
                    from afat_fatlink afl
                    where afl.created >= :created
                """)
            .setParameter("created", Timestamp.valueOf(localDateTime))
            .getResultList();

        return rows.stream()
            .map(r -> new CtaFleetInfo(
                (String) r[6],                            // doctrine
                ((Number) r[0]).intValue(),               // id
                ((Timestamp) r[1]).toLocalDateTime(),     // createdAt
                (String) r[2],                            // fleetName
                (String) r[3],                            // hash
                ((Number) r[4]).intValue(),               // creatorId
                Objects.nonNull((Number) r[5]) ? ((Number) r[5]).intValue() : 0,           // creatorId
                (String) r[7]                             // fleetType
            ))
            .collect(Collectors.toList());
    }

    public List<UserFlightsInfo> findUserFlights(Integer id) {
        List<Object[]> rows = em.createNativeQuery("""
                    select ee.character_name,
                           ee.character_id,
                           1 as appearances_count,
                           case when t3t3u.user_id is not null then 1 else 0 end as ts_connected
                    from afat_fat af
                    left join eveonline_evecharacter ee on  af.character_id = ee.id
                    left join auth_user au on au.id = af.character_id
                    left join teamspeak3_teamspeak3user t3t3u on au.id = t3t3u.user_id
                    where af.fatlink_id = :id
                """)
            .setParameter("id", id)
            .getResultList();

        return rows.stream()
            .map(r -> new UserFlightsInfo(
                (String) r[0],
                Objects.nonNull((Number) r[1]) ? ((Number) r[1]).longValue() : 0,
                ((Number) r[2]).longValue(),
                ((Number) r[3]).intValue() == 1
            ))
            .collect(Collectors.toList());
    }

    public UserFlightsInfo findUserInfoByCreatorId(Integer id) {
        List<Object[]> rows = em.createNativeQuery("""
                     select ee.character_name,
                           ee.character_id,
                           1 as appearances_count,
                           case when t3t3u.user_id is not null then 1 else 0 end as ts_connected
                    from auth_user afl
                    left join eveonline_evecharacter ee on  afl.username = ee.character_name
                    left join teamspeak3_teamspeak3user t3t3u on afl.id = t3t3u.user_id
                    where afl.id = :id
                """)
            .setParameter("id", id)
            .getResultList();

        return rows.stream()
            .map(r -> new UserFlightsInfo(
                (String) r[0],
                Objects.nonNull((Number) r[1]) ? ((Number) r[1]).longValue() : 0,
                ((Number) r[2]).longValue(),
                ((Number) r[3]).intValue() == 1
            ))
            .findFirst().orElse(null);
    }

    public UserFlightsInfo findUserInfoByCharacterId(Integer id) {
        List<Object[]> rows = em.createNativeQuery("""
                   select distinct 
                          ee.character_name,
                          ee.character_id,
                          1 as appearances_count,
                          0 as ts_connected
                   from eveonline_evecharacter ee
                             join afat_fat af on  af.character_id = ee.id
                   where ee.id = :id
                """)
            .setParameter("id", id)
            .getResultList();

        return rows.stream()
            .map(r -> new UserFlightsInfo(
                (String) r[0],
                Objects.nonNull((Number) r[1]) ? ((Number) r[1]).longValue() : 0,
                ((Number) r[2]).longValue(),
                ((Number) r[3]).intValue() == 1
            ))
            .findFirst().orElse(null);
    }
}
