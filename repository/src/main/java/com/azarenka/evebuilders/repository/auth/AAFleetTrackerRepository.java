package com.azarenka.evebuilders.repository.auth;

import com.azarenka.evebuilders.domain.authdb.CtaFleetInfo;
import com.azarenka.evebuilders.domain.authdb.UserFlightsInfo;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
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
                           afl.doctrine,
                           afl.fleet_type
                    from afat_fatlink afl
                    where afl.created >= :created
                """)
            .setParameter("created", Timestamp.valueOf(localDateTime))
            .getResultList();

        return rows.stream()
            .map(r -> new CtaFleetInfo(
                (String) r[5],                            // doctrine
                ((Number) r[0]).intValue(),               // id
                ((Timestamp) r[1]).toLocalDateTime(),     // createdAt
                (String) r[2],                            // fleetName
                (String) r[3],                            // hash
                ((Number) r[4]).intValue(),               // creatorId
                (String) r[6]                             // fleetType
            ))
            .collect(Collectors.toList());
    }

    public List<UserFlightsInfo> findUserFlights(List<Integer> ids) {
        List<Object[]> rows = em.createNativeQuery("""
                    select au.username,
                           ee.character_id,
                           count(*) as appearances_count,
                           case when t3t3u.user_id is not null then 1 else 0 end as ts_connected
                    
                    from auth_user au
                    join afat_fat af on au.id = af.character_id
                    join afat_fatlink afl on af.fatlink_id = afl.id
                    left join teamspeak3_teamspeak3user t3t3u on au.id = t3t3u.user_id
                        group by au.username, t3t3u.user_id
                """)
            .getResultList();

        return rows.stream()
            .map(r -> new UserFlightsInfo(
                (String) r[0],
                ((Number) r[1]).longValue(),
                ((Number) r[2]).longValue(),
                ((Number) r[3]).intValue() == 1
            ))
            .collect(Collectors.toList());
    }
}
